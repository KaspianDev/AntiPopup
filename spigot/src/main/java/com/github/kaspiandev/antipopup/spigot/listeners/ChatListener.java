package com.github.kaspiandev.antipopup.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class ChatListener implements Listener {

    // Must be monitor
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (event.getRecipients().isEmpty()) return;
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String message = String.format(event.getFormat(), sender.getDisplayName(), event.getMessage());

        Bukkit.getConsoleSender().sendMessage(message);
        for (Player player : event.getRecipients()) {
            player.sendMessage(sender.getUniqueId(), message);
        }
        Bukkit.getServer().getPluginManager().callEvent(new AsyncPlayerChatEvent(
                true, sender, event.getMessage(), Set.of()));
    }

}
