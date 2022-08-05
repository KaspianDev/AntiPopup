package screw.microsoft.antipopup;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class AntiPopup extends JavaPlugin {

    static YamlDocument config;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().debug(false).bStats(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
        getLogger().info("Loaded PacketEvents");
    }

    @Override
    public void onEnable() {
        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .build());
            getLogger().info("Config enabled.");
        } catch (IOException ex) {
            getLogger().warning("Config file could not be initialized");
            throw new RuntimeException(ex);
        }
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
        PacketEvents.getAPI().init();
        getLogger().info("Initiated PacketEvents");
        if(PacketEvents.getAPI().getServerManager().getVersion().is(VersionComparison.EQUALS, ServerVersion.V_1_19)
            && !config.getBoolean("no-warning")) {
            getLogger().warning("!!!              WARNING              !!!");
            getLogger().warning("There is a known problem with using");
            getLogger().warning("AntiPopup with ViaVersion on 1.19.");
            getLogger().warning("Players will still get the popup, either");
            getLogger().warning("wait for a fix or download a patched ViaVersion.");
            getLogger().warning("You can also update your server to 1.19.1+.");
            getLogger().warning("");
            getLogger().warning("Patch: https://github.com/KaspianDev/ViaVersion-patched/actions");
            getLogger().warning("Note: It is not ideal, I recommend just updating your server.");
            getLogger().warning("Remove warning by setting no-warning to true in config.");
        }

        // To be added soon
        /*
        if(config.getBoolean("first-run").equals(true)) {
            try {
                FileInputStream in=new FileInputStream("server.properties");
                Properties props = new Properties();
                props.load(in);
                props.setProperty("enforce-secure-profile", String.valueOf(false));
                in.close();
                FileOutputStream out=new FileOutputStream("server.properties");
                props.store(out, "");
                out.close();
                config.set("first-run", false);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        */
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().info("Disabled PacketEvents");
    }
}
