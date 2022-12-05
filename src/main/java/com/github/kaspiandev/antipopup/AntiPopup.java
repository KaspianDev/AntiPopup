package com.github.kaspiandev.antipopup;

import com.github.kaspiandev.antipopup.api.Api;
import com.github.kaspiandev.antipopup.listeners.KickListener;
import com.github.kaspiandev.antipopup.listeners.PacketEventsListener;
import com.github.kaspiandev.antipopup.listeners.URLListener;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static org.bukkit.Bukkit.getPluginManager;

public final class AntiPopup extends JavaPlugin {

    public static YamlDocument config;
    static Plugin instance;
    static Metrics metrics;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().debug(false).bStats(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
        getLogger().info("Loaded PacketEvents.");
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .build());
            getLogger().fine("Config enabled.");
        } catch (IOException ex) {
            getLogger().warning("Config file could not be initialized.");
            throw new RuntimeException(ex);
        }

        if (config.getBoolean("bstats", false)) {
            metrics = new Metrics(this, 16308);
            metrics.addCustomChart(new SimplePie("runs_viaversion",
                    () -> Bukkit.getPluginManager().isPluginEnabled("ViaVersion") ? "Yes" : "No"));
            getLogger().info("Loaded optional metrics.");
        }

        if (getPluginManager().getPlugin("ViaVersion") != null
                    && PacketEvents.getAPI().getServerManager().getVersion().equals(ServerVersion.V_1_19)) {
            try {
                var hookClass = ViaHook.class;
                hookClass.getConstructor().newInstance();
                getLogger().info("Enabled 1.19 ViaVersion Hook.");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
        PacketEvents.getAPI().init();
        getLogger().fine("Initiated PacketEvents.");

        getServer().getPluginManager().registerEvents(new KickListener(), this);
        if (config.getBoolean("enable-urls")) {
            getServer().getPluginManager().registerEvents(new URLListener(), this);
        }
        getLogger().fine("Listeners registered.");

        Objects.requireNonNull(this.getCommand("antipopup")).setExecutor(new CommandRegister());
        getLogger().fine("Commands registered.");

        if (config.getBoolean("filter-not-secure", true)
                    || config.getBoolean("sync-time-suppress", false)) {
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
            getLogger().info("Logger filter enabled.");
        } else {
            getLogger().fine("Logger filter has not been enabled.");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (config.getBoolean("auto-setup", false)) new Api(instance).setupAntiPopup(80);
            if (config.getBoolean("first-run")) {
                try {
                    FileInputStream in = new FileInputStream("server.properties");
                    Properties props = new Properties();
                    props.load(in);
                    if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                        getLogger().warning("------------------[ READ ME PLEASE ]------------------");
                        getLogger().warning("This is your first startup with AntiPopup.");
                        getLogger().warning("Run command 'antipopup setup' to disable");
                        getLogger().warning("enforce-secure-profile for better experience.");
                        getLogger().warning("This will not force players to sign their messages.");
                        getLogger().warning("Thanks for using AntiPopup!");
                        getLogger().warning("------------------------------------------------------");
                    }
                    in.close();
                    config.set("first-run", false);
                    config.save();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
            if (config.getBoolean("ask-bstats")) {
                try {
                    getLogger().warning("--------------------[ READ ME PLEASE ]--------------------");
                    getLogger().warning("This is your first startup with AntiPopup.");
                    getLogger().warning("I would like to kindly ask you to enable bstats");
                    getLogger().warning("configuration value to help me improve AntiPopup.");
                    getLogger().warning("Because I respect your freedom it's disabled by default.");
                    getLogger().warning("Thanks for using AntiPopup! (you will not see this again)");
                    getLogger().warning("----------------------------------------------------------");
                    config.set("ask-bstats", false);
                    config.save();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }, 5);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().info("Disabled PacketEvents.");
    }
}
