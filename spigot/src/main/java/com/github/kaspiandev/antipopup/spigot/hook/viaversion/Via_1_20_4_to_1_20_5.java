package com.github.kaspiandev.antipopup.spigot.hook.viaversion;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;

public class Via_1_20_4_to_1_20_5 extends ViaProtocolModifier<Protocol1_20_5To1_20_3> {

    public Via_1_20_4_to_1_20_5() {
        super(VersionComparison.OLDER_THAN, ServerVersion.V_1_20_5);
    }

    @Override
    public void modify() {
        protocol.appendClientbound(ClientboundPackets1_20_3.JOIN_GAME, (wrapper) -> {
            wrapper.set(Type.BOOLEAN, 6, true); // Enforces secure chat
        });
    }

    @Override
    public Class<Protocol1_20_5To1_20_3> getProtocolClass() {
        return Protocol1_20_5To1_20_3.class;
    }

}
