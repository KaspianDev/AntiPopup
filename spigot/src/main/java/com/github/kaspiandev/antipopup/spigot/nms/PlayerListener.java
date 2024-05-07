package com.github.kaspiandev.antipopup.spigot.nms;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final PacketInjector injector;

    public PlayerListener(PacketInjector injector) {
        this.injector = injector;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        injector.inject(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        injector.uninject(event.getPlayer());
    }

}
