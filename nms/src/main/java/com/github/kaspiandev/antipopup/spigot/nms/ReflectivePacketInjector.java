package com.github.kaspiandev.antipopup.spigot.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A single implementation for servers retaining the modern mapped chat contract. */
public final class ReflectivePacketInjector implements PacketInjector, AutoCloseable {
    private static final String HANDLER_NAME = "antipopup_handler";
    private final ConnectionAccess connections;
    private final ChatPacketAccess chat;
    private final Logger logger;
    private final Map<UUID, Channel> channels = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public ReflectivePacketInjector(Class<?> craftPlayer, ClassLoader loader, Logger logger)
            throws ReflectiveOperationException {
        this(new ConnectionAccess(craftPlayer, loader), ChatPacketAccess.discover(loader), logger);
    }

    ReflectivePacketInjector(ConnectionAccess connections, ChatPacketAccess chat, Logger logger) {
        this.connections = connections;
        this.chat = chat;
        this.logger = logger;
    }

    @Override
    public void inject(Player player) {
        try {
            injectChannel(player.getUniqueId(), connections.channel(player));
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logger.log(Level.SEVERE, "AntiPopup could not protect chat for " + player.getUniqueId(), ex);
            throw new IllegalStateException("AntiPopup player connection discovery failed", ex);
        }
    }

    void injectChannel(UUID id, Channel channel) {
        if (channel == null) throw new IllegalStateException("Player network channel is null");
        if (closed.get()) throw new IllegalStateException("Injector is closed");
        Channel previous = channels.put(id, channel);
        if (previous != null && previous != channel) removeHandler(previous);
        // All pipeline changes and packet replacement happen on this connection's event loop.
        channel.eventLoop().execute(() -> {
            if (closed.get() || channels.get(id) != channel || !channel.isOpen()) {
                channels.remove(id, channel);
                return;
            }
            if (channel.pipeline().get(HANDLER_NAME) != null) channel.pipeline().remove(HANDLER_NAME);
            if (channel.pipeline().get("packet_handler") == null) {
                logger.severe("AntiPopup cannot protect " + id + ": packet_handler is missing; closing connection.");
                channels.remove(id, channel);
                channel.close();
                return;
            }
            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChatHandler(id));
        });
    }

    @Override
    public void uninject(Player player) {
        remove(player.getUniqueId());
    }

    void remove(UUID id) {
        Channel channel = channels.remove(id);
        if (channel != null) removeHandler(channel);
    }

    private static void removeHandler(Channel channel) {
        if (!channel.isRegistered()) return;
        Runnable remove = () -> {
            if (channel.pipeline().get(HANDLER_NAME) != null) channel.pipeline().remove(HANDLER_NAME);
        };
        if (channel.eventLoop().inEventLoop()) remove.run();
        else {
            try {
                channel.eventLoop().execute(remove);
            } catch (RejectedExecutionException ignored) {
                // A terminated event loop is already destroying this channel's pipeline.
            }
        }
    }

    @Override
    public void close() {
        closed.set(true);
        channels.values().forEach(ReflectivePacketInjector::removeHandler);
        channels.clear();
    }

    private final class ChatHandler extends ChannelDuplexHandler {
        private final UUID id;
        private boolean failed;

        ChatHandler(UUID id) { this.id = id; }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            channels.remove(id, ctx.channel());
            ctx.fireChannelInactive();
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            if (!chat.playerChat.isInstance(packet)) {
                ctx.write(packet, promise);
                return;
            }
            final Object replacement;
            try {
                replacement = chat.convert(packet);
            } catch (ReflectiveOperationException | RuntimeException ex) {
                if (!failed) logger.log(Level.SEVERE,
                        "AntiPopup chat conversion failed for " + id + "; closing connection to avoid sending unconverted player chat.", ex);
                failed = true;
                promise.tryFailure(ex);
                ctx.close();
                return;
            }
            // Preserve ordering, completion and flush semantics of the original write.
            ctx.write(replacement, promise);
        }
    }
}
