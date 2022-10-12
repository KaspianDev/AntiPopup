package com.github.kaspiandev.antipopup;

import com.github.kaspiandev.antipopup.api.Api;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.github.kaspiandev.antipopup.AntiPopup.config;
import static com.github.kaspiandev.antipopup.AntiPopup.instance;
import static org.bukkit.Bukkit.getLogger;

public class CommandRegister implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             String[] args) {

        Api api = new Api(instance);

        if (args.length == 1
                && sender instanceof ConsoleCommandSender) {
            if ("setup".equals(args[0])) {
                api.setupAntiPopup(100);
            } else if ("reload".equals(args[0])) {
                try {
                    config.reload();
                    getLogger().info("Config has been reloaded.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        return false;
    }
}