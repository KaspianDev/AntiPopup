package com.github.kaspiandev.antipopup.spigot.hook.viaversion;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.Protocol1_19_1To1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;

public class Via_1_19_to_1_19_1 extends ViaProtocolModifier<Protocol1_19_1To1_19> {

    public Via_1_19_to_1_19_1() {
        super(VersionComparison.EQUALS, ServerVersion.V_1_19);
    }

    @Override
    public void modify() {
        protocol.appendClientbound(ClientboundPackets1_19.SERVER_DATA, (wrapper) -> {
            wrapper.set(Type.BOOLEAN, 1, true);
        });
    }

    @Override
    public Class<Protocol1_19_1To1_19> getProtocolClass() {
        return Protocol1_19_1To1_19.class;
    }

}
