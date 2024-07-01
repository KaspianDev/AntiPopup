package com.github.kaspiandev.antipopup.listener;

import com.github.kaspiandev.antipopup.platform.Platform;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;

public class PacketEventsListener extends PacketListenerAbstract {

    private final Platform platform;

    public PacketEventsListener(Platform platform) {
        super(PacketListenerPriority.LOW);
        this.platform = platform;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        ClientVersion clientVersion = event.getUser().getClientVersion();
        if (packetType == PacketType.Status.Server.RESPONSE
                && clientVersion.isNewerThan(ClientVersion.V_1_18_2)) {
            WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
            JsonObject newObj = wrapper.getComponent();
            newObj.addProperty("preventsChatReports", true);
            wrapper.setComponent(newObj);
        } else if (packetType == PacketType.Play.Server.SERVER_DATA
                && clientVersion.isOlderThan(ClientVersion.V_1_20_5)
                && !platform.getApConfig().isShowPopup()) {
            WrapperPlayServerServerData wrapper = new WrapperPlayServerServerData(event);
            wrapper.setEnforceSecureChat(true);
        } else if (packetType == PacketType.Play.Server.JOIN_GAME
                && clientVersion.isNewerThan(ClientVersion.V_1_20_3)
                && !platform.getApConfig().isShowPopup()) {
            WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);
            wrapper.setEnforcesSecureChat(true);
        } else if (packetType == PacketType.Play.Server.PLAYER_CHAT_HEADER
                && !platform.getApConfig().isSendHeader()) {
            event.setCancelled(true);
        } else if (platform.getApConfig().isExperimentalMode() && packetType == PacketType.Play.Server.CHAT_MESSAGE) {
            WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
            ChatMessage chatMessage = wrapper.getMessage();
            if (chatMessage instanceof ChatMessage_v1_19_1 message_v1_19_1) {
                event.setCancelled(true);
                ChatType.Bound formatting = message_v1_19_1.getChatFormatting();
                Component component = message_v1_19_1.getType().getChatDecoration().decorate(message_v1_19_1.getChatContent(), formatting);
                event.getUser().sendPacket(new WrapperPlayServerSystemChatMessage(false, component));
            } else if (chatMessage instanceof ChatMessage_v1_19_3 message_v1_19_3) {
                event.setCancelled(true);
                ChatType.Bound formatting = message_v1_19_3.getChatFormatting();
                Component component = message_v1_19_3.getType().getChatDecoration().decorate(message_v1_19_3.getChatContent(), formatting);
                event.getUser().sendPacket(new WrapperPlayServerSystemChatMessage(false, component));
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CHAT_SESSION_UPDATE) {
            event.setCancelled(true);
        }
    }

}
