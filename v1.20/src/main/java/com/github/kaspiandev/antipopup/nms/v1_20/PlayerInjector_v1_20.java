package com.github.kaspiandev.antipopup.nms.v1_20;

import com.github.kaspiandev.antipopup.nms.AbstractInjector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Optional;

@SuppressWarnings("unused")
public class PlayerInjector_v1_20 implements AbstractInjector {

    private static Field connectionField;

    static {
        try {
            for (Field field : ServerGamePacketListenerImpl.class.getDeclaredFields()) {
                if (field.getType().equals(Connection.class)) {
                    field.setAccessible(true);
                    connectionField = field;
                    break;
                }
            }
        } catch (SecurityException | InaccessibleObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public void inject(Player player) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
                    Component content = chatPacket.unsignedContent();
                    if (content == null) {
                        content = Component.literal(chatPacket.body().content());
                    }
                    Optional<ChatType.Bound> chatType = chatPacket.chatType().resolve(
                            ((CraftServer) Bukkit.getServer()).getServer().registryAccess());

                    ((CraftPlayer) player).getHandle().connection.send(
                            new ClientboundSystemChatPacket(chatType.orElseThrow().decorate(content), false));
                    return;
                }
                super.write(ctx, packet, promise);
            }
        };

        ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
        Channel channel = getConnection(listener).channel;
        channel.pipeline().addBefore("packet_handler", "antipopup_handler", duplexHandler);
    }

    public void uninject(Player player) {
        ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
        Channel channel = getConnection(listener).channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("antipopup_handler");
            return null;
        });
    }

    private Connection getConnection(ServerGamePacketListenerImpl listener) {
        try {
            return (Connection) connectionField.get(listener);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
