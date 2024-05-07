package com.github.kaspiandev.antipopup.spigot;

import com.github.kaspiandev.antipopup.config.APConfig;
import com.github.kaspiandev.antipopup.listener.PacketEventsListener;
import com.github.kaspiandev.antipopup.log.LogFilter;
import com.github.kaspiandev.antipopup.message.ConsoleMessages;
import com.github.kaspiandev.antipopup.nms.v1_19_2.PlayerInjector_v1_19_2;
import com.github.kaspiandev.antipopup.nms.v1_19_3.PlayerInjector_v1_19_3;
import com.github.kaspiandev.antipopup.nms.v1_19_4.PlayerInjector_v1_19_4;
import com.github.kaspiandev.antipopup.nms.v1_20_1.PlayerInjector_v1_20_1;
import com.github.kaspiandev.antipopup.nms.v1_20_2.PlayerInjector_v1_20_2;
import com.github.kaspiandev.antipopup.nms.v1_20_4.PlayerInjector_v1_20_4;
import com.github.kaspiandev.antipopup.nms.v1_20_6.PlayerInjector_v1_20_6;
import com.github.kaspiandev.antipopup.spigot.api.Api;
import com.github.kaspiandev.antipopup.spigot.listeners.ChatListener;
import com.github.kaspiandev.antipopup.spigot.nms.PlayerListener;
import com.github.kaspiandev.antipopup.spigot.platform.SpigotPlatform;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.tcoded.folialib.FoliaLib;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.apache.logging.log4j.LogManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
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

    private static File propertiesFile;
    private static APConfig config;
    private static FoliaLib foliaLib;

    public static FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public static File getPropertiesFile() {
        return propertiesFile;
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().debug(false).bStats(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
        getLogger().info("Loaded PacketEvents.");
    }

    @Override
    public void onEnable() {
        foliaLib = new FoliaLib(this);
        try {
            config = new APConfig(getDataFolder(), this.getClassLoader());
            getLogger().info("Config enabled.");
        } catch (IOException ex) {
            getLogger().warning("Config file could not be initialized.");
            throw new RuntimeException(ex);
        }
        SpigotPlatform spigotPlatform = new SpigotPlatform(config);
        propertiesFile = new File(Bukkit.getWorldContainer().getParentFile(), config.getPropertiesLocation());

        if (config.isBstats()) {
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

        if (config.isClickableUrls()) {
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
            getLogger().info("Enabled URL support.");
        }

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener(spigotPlatform));
        PacketEvents.getAPI().init();
        getLogger().info("Initiated PacketEvents.");

        PluginManager pluginManager = getServer().getPluginManager();
        ServerManager serverManager = PacketEvents.getAPI().getServerManager();

        if (config.isBlockChatReports()
                && serverManager.getVersion().isOlderThan(ServerVersion.V_1_19_1)) {
            config.setBlockChatReports(false);
            ConsoleMessages.log(ConsoleMessages.BLOCKING_REPORTS_UNSUPPORTED, getLogger()::severe);
            throw new IllegalStateException("Blocking chat reports was enabled but your server version isn't supported!");
        }

        if (config.isBlockChatReports()) {
            PlayerListener playerListener = switch (serverManager.getVersion()) {
                case V_1_20_5, V_1_20_6 -> new PlayerListener(new PlayerInjector_v1_20_6());
                case V_1_20_3, V_1_20_4 -> new PlayerListener(new PlayerInjector_v1_20_4());
                case V_1_20_2 -> new PlayerListener(new PlayerInjector_v1_20_2());
                case V_1_20, V_1_20_1 -> new PlayerListener(new PlayerInjector_v1_20_1());
                case V_1_19_4 -> new PlayerListener(new PlayerInjector_v1_19_4());
                case V_1_19_3 -> new PlayerListener(new PlayerInjector_v1_19_3());
                case V_1_19_1, V_1_19_2 -> new PlayerListener(new PlayerInjector_v1_19_2());
                default -> throw new IllegalStateException("No valid injector found for the server version!");
            };

            pluginManager.registerEvents(playerListener, this);
            getLogger().info("Hooked on " + serverManager.getVersion().getReleaseName());

            Objects.requireNonNull(this.getCommand("antipopup")).setExecutor(new CommandRegister(config));
            getLogger().info("Commands registered.");

            if (config.isFilterNotSecure()) {
                ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
                getLogger().info("Logger filter enabled.");
            }
        }

        foliaLib.getImpl().runLater(() -> {
            if (config.isAutoSetup()) Api.setupAntiPopup(80, true);
            if (config.isFirstRun()) {
                try {
                    FileInputStream in = new FileInputStream(propertiesFile);
                    Properties props = new Properties();
                    props.load(in);
                    if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                        ConsoleMessages.log(ConsoleMessages.ASK_SETUP, getLogger()::warning);
                    }
                    in.close();
                    config.setFirstRun(false);
                    config.save();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (config.isAskBstats()) {
                ConsoleMessages.log(ConsoleMessages.ASK_BSTATS, getLogger()::warning);
                config.setAskBstats(false);
                config.save();
            }
        }, 5 * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().info("Disabled PacketEvents.");
    }

}
