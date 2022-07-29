package screw.microsoft.antipopup;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerChatHeader;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.util.UUID;

public class PacketEventsListener extends PacketListenerAbstract {

    public PacketEventsListener() {
        super(PacketListenerPriority.HIGH);
    }

    YamlDocument config = AntiPopup.config;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SERVER_DATA) {
            WrapperPlayServerServerData serverData = new WrapperPlayServerServerData(event);
            serverData.setEnforceSecureChat(true);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_CHAT_HEADER
                && config.getBoolean("strip-signature").equals(true)) {
            WrapperPlayServerPlayerChatHeader chatMessage = new WrapperPlayServerPlayerChatHeader(event);
            chatMessage.setSignature(null);
            chatMessage.setPreviousSignature(null);
            chatMessage.setHash(null);
            chatMessage.setPlayerUUID(new UUID(0L, 0L));
        }
    }
}

