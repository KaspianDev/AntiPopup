package com.github.kaspiandev.antipopup.spigot.nms;

import org.bukkit.entity.Player;

public interface PacketInjector {

    void inject(Player player);

    void uninject(Player player);

}
