package com.github.kaspiandev.antipopup;

import com.github.kaspiandev.antipopup.api.Api;
import com.github.kaspiandev.antipopup.listeners.PacketEventsListener;
import com.github.kaspiandev.antipopup.listeners.URLListener;
import com.github.kaspiandev.antipopup.nms.PlayerListener;
import com.github.kaspiandev.antipopup.nms.v1_19_3.PlayerInjector;
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

    private static YamlDocument yamlDoc;
    private static Plugin instance;

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
            yamlDoc = YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .build());
            getLogger().info("Config enabled.");
        } catch (IOException ex) {
            getLogger().warning("Config file could not be initialized.");
            throw new RuntimeException(ex);
        }

        if (yamlDoc.getBoolean("bstats")) {
            Metrics metrics = new Metrics(this, 16308);
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
        getLogger().info("Initiated PacketEvents.");

        if (yamlDoc.getBoolean("setup-mode")
                    && PacketEvents.getAPI().getServerManager().getVersion().equals(ServerVersion.V_1_19_2)) {
            yamlDoc.set("mode", "NMS");
            yamlDoc.set("setup-mode", false);
            try {
                yamlDoc.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (yamlDoc.getString("mode").equals("NMS")) {
            if (PacketEvents.getAPI().getServerManager().getVersion().equals(ServerVersion.V_1_19_3)) {
                getServer().getPluginManager().registerEvents(new PlayerListener(new PlayerInjector()), this);
            }
        }

        if (yamlDoc.getString("mode").equals("BUKKIT")) {
            getServer().getPluginManager().registerEvents(new URLListener(), this);
            getLogger().info("Listeners registered.");
        }

        Objects.requireNonNull(this.getCommand("antipopup")).setExecutor(new CommandRegister());
        getLogger().info("Commands registered.");

        if (yamlDoc.getBoolean("filter-not-secure")
                    || yamlDoc.getBoolean("sync-time-suppress")) {
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
            getLogger().info("Logger filter enabled.");
        } else {
            getLogger().info("Logger filter has not been enabled.");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (yamlDoc.getBoolean("auto-setup")) new Api(instance).setupAntiPopup(80);
            if (yamlDoc.getBoolean("first-run")) {
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
                    yamlDoc.save();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
            if (yamlDoc.getBoolean("ask-bstats")) {
                try {
                    getLogger().warning("--------------------[ READ ME PLEASE ]--------------------");
                    getLogger().warning("This is your first startup with AntiPopup.");
                    getLogger().warning("I would like to kindly ask you to enable bstats");
                    getLogger().warning("configuration value to help me improve AntiPopup.");
                    getLogger().warning("Because I respect your freedom it's disabled by default.");
                    getLogger().warning("Thanks for using AntiPopup! (you will not see this again)");
                    getLogger().warning("----------------------------------------------------------");
                    yamlDoc.set("ask-bstats", false);
                    yamlDoc.save();
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

    public static Plugin getInstance() {
        return instance;
    }

    public static YamlDocument getYamlDoc() {
        return yamlDoc;
    }
}