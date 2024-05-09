package com.github.kaspiandev.antipopup.spigot.hook.viaversion;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.Protocol;

public abstract class ViaProtocolModifier<T extends Protocol<?, ?, ?, ?>> {

    protected final T protocol;
    protected final VersionComparison comparison;
    protected final ServerVersion version;

    protected ViaProtocolModifier(VersionComparison comparison, ServerVersion version) throws NullPointerException {
        T protocol = Via.getManager().getProtocolManager().getProtocol(getProtocolClass());
        if (protocol == null) throw new NullPointerException();
        this.protocol = protocol;
        this.comparison = comparison;
        this.version = version;
    }

    protected abstract void modify();

    protected abstract Class<T> getProtocolClass();

    public VersionComparison getComparison() {
        return comparison;
    }

    public ServerVersion getVersion() {
        return version;
    }

}
