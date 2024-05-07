package com.github.kaspiandev.antipopup.velocity.platform;

import com.github.kaspiandev.antipopup.config.APConfig;
import com.github.kaspiandev.antipopup.platform.Platform;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.UUID;

public class VelocityPlatform extends Platform {

    private final ProxyServer server;

    public VelocityPlatform(ProxyServer server, APConfig apConfig) {
        super(apConfig);
        this.server = server;
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return server.getPlayer(playerUUID).orElseThrow().getUsername();
    }

}
