package com.github.kaspiandev.antipopup.listeners;

import com.github.kaspiandev.antipopup.AntiPopup;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PacketEventsListener extends PacketListenerAbstract {

    private final YamlDocument yamlDoc = AntiPopup.getYamlDoc();

    public PacketEventsListener() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Status.Server.RESPONSE) {
            if (event.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_19_1)) return;
            WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
            JsonObject newObj = wrapper.getComponent();
            newObj.addProperty("preventsChatReports", true);
            wrapper.setComponent(newObj);
        }
        if (event.getPacketType() == PacketType.Play.Server.SERVER_DATA) {
            WrapperPlayServerServerData serverData = new WrapperPlayServerServerData(event);
            serverData.setEnforceSecureChat(true);
        }
        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE
                    && yamlDoc.getBoolean("strip-signature")
                    && yamlDoc.getString("mode").equals("PACKET")) {
            WrapperPlayServerChatMessage chatMessage = new WrapperPlayServerChatMessage(event);
            ChatMessage message = chatMessage.getMessage();
            if (message instanceof ChatMessage_v1_19_1 v1_19_1) {
                v1_19_1.setSignature(new byte[0]);
                v1_19_1.setSalt(0);
                v1_19_1.setSenderUUID(new UUID(0L, 0L));
                v1_19_1.setPreviousSignature(null);
            }
        }
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_CHAT_HEADER
                    && yamlDoc.getBoolean("dont-send-header")) {
            event.setCancelled(true);
        }
    }
}