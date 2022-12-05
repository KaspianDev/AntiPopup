package com.github.kaspiandev.antipopup.api;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.Plugin;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.bukkit.Bukkit.*;

public class Api {

    private final Plugin instance;

    public Api(Plugin instance) {
        this.instance = instance;
    }

    /**
     * This "sets up" the plugin aka changes
     * enforce-secure-profile property in server.properties
     * to false.
     *
     * @param time Delay of the setup.
     */
    public void setupAntiPopup(int time) {
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
                }, time);
            } else {
                getLogger().fine("AntiPopup has been already set up.");
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}