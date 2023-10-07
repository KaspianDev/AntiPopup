package com.github.kaspiandev.antipopup.api;

import com.github.kaspiandev.antipopup.AntiPopup;
import com.github.kaspiandev.antipopup.message.ConsoleMessages;
import com.github.retrooper.packetevents.PacketEvents;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class Api {

    private Api() {}

    // TODO: Refactor eventually
    /**
     * This "sets up" the plugin aka changes
     * enforce-secure-profile property in server.properties
     * to false.
     *
     * @param time Delay of the setup.
     */
    public static void setupAntiPopup(int time, boolean silent) {
        try {
            FileInputStream in = null;
            try {
                in = new FileInputStream("server.properties");
                Properties props = new Properties();
                props.load(in);
                if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                    props.setProperty("enforce-secure-profile", String.valueOf(false));
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream("server.properties");
                        props.store(out, "Minecraft server properties");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        if (out != null) out.close();
                    }
                    ConsoleMessages.log(ConsoleMessages.SETUP_SUCCESS, getLogger()::warning);
                    AntiPopup.getFoliaLib().getImpl().runLater(() -> {
                        PacketEvents.getAPI().terminate();
                        getServer().spigot().restart();
                    }, time * 50L, TimeUnit.MILLISECONDS);
                } else if (!silent) {
                    getLogger().info("AntiPopup has been already set up.");
                }
            } finally {
                if (in != null) in.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
