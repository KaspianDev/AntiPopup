package com.github.kaspiandev.antipopup.spigot.hook.viaversion;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.Protocol1_19To1_19_1;

public class Via_1_19_to_1_19_1 extends ViaProtocolModifier<Protocol1_19To1_19_1> {

    public Via_1_19_to_1_19_1() {
        super(VersionComparison.EQUALS, ServerVersion.V_1_19);
    }

    @Override
    public void modify() {
        protocol.appendClientbound(ClientboundPackets1_19.SERVER_DATA, (wrapper) -> {
            wrapper.set(Types.BOOLEAN, 1, true);
        });
    }

    @Override
    public Class<Protocol1_19To1_19_1> getProtocolClass() {
        return Protocol1_19To1_19_1.class;
    }

}
