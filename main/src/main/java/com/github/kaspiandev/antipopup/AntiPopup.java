package com.github.kaspiandev.antipopup;

import com.github.kaspiandev.antipopup.api.Api;
import com.github.kaspiandev.antipopup.listeners.ChatListener;
import com.github.kaspiandev.antipopup.listeners.PacketEventsListener;
import com.github.kaspiandev.antipopup.message.ConsoleMessages;
import com.github.kaspiandev.antipopup.nms.PlayerListener;
import com.github.kaspiandev.antipopup.nms.v1_19_2.PlayerInjector_v1_19_2;
import com.github.kaspiandev.antipopup.nms.v1_19_3.PlayerInjector_v1_19_3;
import com.github.kaspiandev.antipopup.nms.v1_19_4.PlayerInjector_v1_19_4;
import com.github.kaspiandev.antipopup.nms.v1_20.PlayerInjector_v1_20;
import com.github.kaspiandev.antipopup.nms.v1_20_2.PlayerInjector_v1_20_2;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.tcoded.folialib.FoliaLib;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getPluginManager;

public final class AntiPopup extends JavaPlugin {

    private static YamlDocument yamlDoc;
    private static Plugin instance;
    private static FoliaLib foliaLib;

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
        foliaLib = new FoliaLib(this);
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
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (yamlDoc.getBoolean("clickable-urls")) {
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
            getLogger().info("Enabled URL support.");
        }

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
        PacketEvents.getAPI().init();
        getLogger().info("Initiated PacketEvents.");

        PluginManager pluginManager = getServer().getPluginManager();
        ServerManager serverManager = PacketEvents.getAPI().getServerManager();

        if (yamlDoc.getBoolean("block-chat-reports")
                && serverManager.getVersion().isOlderThan(ServerVersion.V_1_19_1)) {
            yamlDoc.set("block-chat-reports", false);
            ConsoleMessages.log(ConsoleMessages.BLOCKING_REPORTS_UNSUPPORTED, getLogger()::severe);
            throw new IllegalStateException("Blocking chat reports was enabled but your server version isn't supported!");
        }

        if (yamlDoc.getBoolean("block-chat-reports")) {
            PlayerListener playerListener = switch (serverManager.getVersion()) {
                case V_1_20_2 -> {
                    ConsoleMessages.log(ConsoleMessages.EXPERIMENTAL_SUPPORT, getLogger()::warning);
                    yield new PlayerListener(new PlayerInjector_v1_20_2());
                }
                case V_1_20, V_1_20_1 -> new PlayerListener(new PlayerInjector_v1_20());
                case V_1_19_4 -> new PlayerListener(new PlayerInjector_v1_19_4());
                case V_1_19_3 -> new PlayerListener(new PlayerInjector_v1_19_3());
                case V_1_19_1, V_1_19_2 -> new PlayerListener(new PlayerInjector_v1_19_2());
                default -> throw new IllegalStateException("No valid injector found for the server version!");
            };

            pluginManager.registerEvents(playerListener, this);
            getLogger().info("Hooked on " + serverManager.getVersion().getReleaseName());

            Objects.requireNonNull(this.getCommand("antipopup")).setExecutor(new CommandRegister());
            getLogger().info("Commands registered.");

            if (yamlDoc.getBoolean("filter-not-secure")) {
                ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
                getLogger().info("Logger filter enabled.");
            } else {
                getLogger().info("Logger filter has not been enabled.");
            }
        }

        foliaLib.getImpl().runLater(() -> {
            if (yamlDoc.getBoolean("auto-setup")) Api.setupAntiPopup(80, true);
            if (yamlDoc.getBoolean("first-run")) {
                try {
                    FileInputStream in = new FileInputStream("server.properties");
                    Properties props = new Properties();
                    props.load(in);
                    if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                        ConsoleMessages.log(ConsoleMessages.ASK_SETUP, getLogger()::warning);
                    }
                    in.close();
                    yamlDoc.save();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (yamlDoc.getBoolean("ask-bstats")) {
                try {
                    ConsoleMessages.log(ConsoleMessages.ASK_BSTATS, getLogger()::warning);
                    yamlDoc.set("ask-bstats", false);
                    yamlDoc.save();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, 5 * 50L, TimeUnit.MILLISECONDS);
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

    public static FoliaLib getFoliaLib() {
        return foliaLib;
    }

}
