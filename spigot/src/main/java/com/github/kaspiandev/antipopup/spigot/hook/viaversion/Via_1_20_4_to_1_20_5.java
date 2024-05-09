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
            wrapper.passthrough(Type.INT); // Entity ID
            wrapper.passthrough(Type.BOOLEAN); // Hardcore
            wrapper.passthrough(Type.STRING_ARRAY); // World List
            wrapper.passthrough(Type.VAR_INT); // Max players
            wrapper.passthrough(Type.VAR_INT); // View distance
            wrapper.passthrough(Type.VAR_INT); // Simulation distance
            wrapper.passthrough(Type.BOOLEAN); // Reduced debug info
            wrapper.passthrough(Type.BOOLEAN); // Show death screen
            wrapper.passthrough(Type.BOOLEAN); // Limited crafting
            wrapper.passthrough(Type.VAR_INT); // Dimension
            wrapper.passthrough(Type.STRING); // World
            wrapper.passthrough(Type.LONG); // Seed
            wrapper.passthrough(Type.BYTE); // Gamemode
            wrapper.passthrough(Type.BYTE); // Previous gamemode
            wrapper.passthrough(Type.BOOLEAN); // Debug
            wrapper.passthrough(Type.BOOLEAN); // Flat
            wrapper.passthrough(Type.OPTIONAL_GLOBAL_POSITION); // Last death location
            wrapper.passthrough(Type.VAR_INT); // Portal cooldown
            wrapper.write(Type.BOOLEAN, true); // Enforces secure chat
        });
    }

    @Override
    public Class<Protocol1_20_5To1_20_3> getProtocolClass() {
        return Protocol1_20_5To1_20_3.class;
    }

}
