package com.github.kaspiandev.antipopup.spigot;

import com.github.kaspiandev.antipopup.config.APConfig;
import com.github.kaspiandev.antipopup.spigot.api.Api;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.getLogger;

public class CommandRegister implements CommandExecutor {

    private final APConfig config;

    public CommandRegister(APConfig config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 1
                && sender instanceof ConsoleCommandSender) {
            if ("setup".equals(args[0])) {
                Api.setupAntiPopup(100, false);
            } else if ("reload".equals(args[0])) {
                config.reload();
                getLogger().info("Config has been reloaded.");
            }

        }
        return false;
    }

}
