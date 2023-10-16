package com.github.kaspiandev.antipopup.util;

import org.bukkit.Bukkit;

public class VersionUtil {

    private VersionUtil() {}

    public static String getMinecraftVersion() {
        String fullVersion = Bukkit.getServer().getClass().getPackage().getName();
        return fullVersion.substring(fullVersion.lastIndexOf('.') + 2)
                          .replace('_', '.')
                          .replace("R", "");
    }

}
