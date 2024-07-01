package com.github.kaspiandev.antipopup.listener;

import com.github.kaspiandev.antipopup.platform.Platform;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;

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
