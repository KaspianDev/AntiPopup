package com.github.kaspiandev.antipopup.spigot;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;

// Temp
@SuppressWarnings("deprecation")
public class ViaHook {

    public ViaHook() {
        var viaProtocolManager = Via.getManager().getProtocolManager();
        Protocol<?, ?, ?, ?> protocol = viaProtocolManager.getProtocol(
                ProtocolVersion.v1_19_1, ProtocolVersion.v1_19);

        if (protocol == null) return;

        // 0x3F - 1.19 SERVER_DATA packet.
        // 0x42 - 1.19.1+ SERVER_DATA packet.
        protocol.registerClientbound(State.PLAY, 0x3F, 0x42,
                new ServerDataRemapper(), true);
    }

    private static class ServerDataRemapper extends PacketRemapper {

        @Override
        public void registerMap() {
            map(Type.OPTIONAL_COMPONENT);
            map(Type.OPTIONAL_STRING);
            map(Type.BOOLEAN);
            create(Type.BOOLEAN, true);
        }

    }

}
