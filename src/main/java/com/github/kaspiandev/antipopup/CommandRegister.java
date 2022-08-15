package com.github.kaspiandev.antipopup;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.bukkit.Bukkit.*;
import static com.github.kaspiandev.antipopup.AntiPopup.instance;

public class CommandRegister implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             String[] args) {
        if (args.length == 1
                && sender instanceof ConsoleCommandSender) {
            if ("setup".equals(args[0])) {
                try {
                    FileInputStream in = new FileInputStream("server.properties");
                    Properties props = new Properties();
                    props.load(in);
                    if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                        props.setProperty("enforce-secure-profile", String.valueOf(false));
                        in.close();
                        FileOutputStream out = new FileOutputStream("server.properties");
                        props.store(out, "Minecraft server properties");
                        out.close();
                        getLogger().warning("-----------------[ READ ME ]-----------------");
                        getLogger().warning("Plugin is set up fully now. We changed value");
                        getLogger().warning("of enforce-secure-chat in server.properties.");
                        getLogger().warning("");
                        getLogger().warning("Server will restart in five seconds.");
                        getLogger().warning("---------------------------------------------");
                        getScheduler().runTaskLater(instance, () -> {
                            PacketEvents.getAPI().terminate();
                            getServer().spigot().restart();
                        }, 100);
                    } else {
                        getLogger().info("AntiPopup has been already set up.");
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

        }
        return false;
    }
}