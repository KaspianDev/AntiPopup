package com.github.kaspiandev.antipopup.nms.v1_19_2;

import com.github.kaspiandev.antipopup.spigot.nms.PacketInjector;
import io.netty.channel.*;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

@SuppressWarnings("unused")
public class PlayerInjector_v1_19_2 implements PacketInjector {

    private static final String HANDLER_NAME = "antipopup_handler";

    public void inject(Player player) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
                    Component content = chatPacket.message().unsignedContent()
                                                  .orElse(chatPacket.message().serverContent());
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
        Channel channel = listener.getConnection().channel;
        ChannelPipeline pipeline = channel.pipeline();

        if (pipeline.get(HANDLER_NAME) != null) {
            pipeline.remove(HANDLER_NAME);
        }

        channel.eventLoop().submit(() -> {
            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, duplexHandler);
        });
    }

    public void uninject(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.getConnection().channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(HANDLER_NAME);
            return null;
        });
    }

}
