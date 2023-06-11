package com.github.kaspiandev.antipopup;

import com.github.kaspiandev.antipopup.api.Api;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.bukkit.Bukkit.getLogger;

public class CommandRegister implements CommandExecutor {

    private final Plugin instance = AntiPopup.getInstance();
    private final YamlDocument yamlDoc = AntiPopup.getYamlDoc();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 1
                && sender instanceof ConsoleCommandSender) {
            if ("setup".equals(args[0])) {
                Api api = new Api(instance);
                api.setupAntiPopup(100, false);
            } else if ("reload".equals(args[0])) {
                try {
                    yamlDoc.reload();
                    getLogger().info("Config has been reloaded.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        return false;
    }

}
