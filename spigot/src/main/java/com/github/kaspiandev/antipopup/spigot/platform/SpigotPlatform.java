package com.github.kaspiandev.antipopup.spigot.platform;

import com.github.kaspiandev.antipopup.config.APConfig;
import com.github.kaspiandev.antipopup.platform.Platform;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class SpigotPlatform extends Platform {

    public SpigotPlatform(APConfig apConfig) {
        super(apConfig);
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getDisplayName();
    }

}
