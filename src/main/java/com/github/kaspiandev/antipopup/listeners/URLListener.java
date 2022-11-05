package com.github.kaspiandev.antipopup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class URLListener implements Listener {

    // Must be monitor
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);

        String message = event.getMessage();
        Player sender = event.getPlayer();

        for (Player player : event.getRecipients()) {
            player.sendMessage(sender.getUniqueId(),
                    String.format(event.getFormat(), sender.getName(), message));
        }
    }
}