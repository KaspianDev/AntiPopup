package com.github.kaspiandev.antipopup.listeners;

import org.bukkit.Bukkit;
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

        Player sender = event.getPlayer();
        String message = String.format(event.getFormat(), sender.getName(), event.getMessage());

        Bukkit.getConsoleSender().sendMessage(message);

        for (Player player : event.getRecipients()) {
            player.sendMessage(sender.getUniqueId(), message);
        }
    }
}