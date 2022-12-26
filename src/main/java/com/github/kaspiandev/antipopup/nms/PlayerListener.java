package com.github.kaspiandev.antipopup.nms;

import com.github.kaspiandev.antipopup.nms.AbstractInjector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final AbstractInjector injector;

    public PlayerListener(AbstractInjector injector) {
        this.injector = injector;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        injector.inject(event.getPlayer());
        System.out.println("injected");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        injector.uninject(event.getPlayer());
    }

}