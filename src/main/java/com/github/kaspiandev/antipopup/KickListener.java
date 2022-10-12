package com.github.kaspiandev.antipopup;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(PlayerKickEvent event) {
        // Couldnt test
        if (AntiPopup.config.getBoolean("prevent-time-sync-kick", false)) {
            if (event.getReason().equals("Out-of-order chat packet received. Did your system time change?"))
                event.setCancelled(true);
        }
    }
}