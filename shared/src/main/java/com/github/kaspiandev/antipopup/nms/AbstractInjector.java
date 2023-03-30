package com.github.kaspiandev.antipopup.nms;

import org.bukkit.entity.Player;

public interface AbstractInjector {

    void inject(Player player);

    void uninject(Player player);

}