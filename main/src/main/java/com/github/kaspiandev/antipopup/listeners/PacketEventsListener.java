package com.github.kaspiandev.antipopup.listeners;

import com.github.kaspiandev.antipopup.AntiPopup;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import dev.dejvokep.boostedyaml.YamlDocument;

public class PacketEventsListener extends PacketListenerAbstract {

    private final YamlDocument yamlDoc = AntiPopup.getYamlDoc();

    public PacketEventsListener() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Status.Server.RESPONSE) {
            if (event.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_19)) return;
            WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
            JsonObject newObj = wrapper.getComponent();
            newObj.addProperty("preventsChatReports", true);
            wrapper.setComponent(newObj);
        }
        if (event.getPacketType() == PacketType.Play.Server.SERVER_DATA) {
            WrapperPlayServerServerData serverData = new WrapperPlayServerServerData(event);
            serverData.setEnforceSecureChat(true);
        }
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_CHAT_HEADER
                && yamlDoc.getBoolean("dont-send-header")) {
            event.setCancelled(true);
        }
    }

}
