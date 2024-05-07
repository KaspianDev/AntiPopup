package com.github.kaspiandev.antipopup.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class APConfig {

    private final YamlDocument document;

    public APConfig(File folder, ClassLoader loader) throws IOException {
        this.document = YamlDocument.create(new File(folder, "config.yml"),
                Objects.requireNonNull(loader.getResourceAsStream("config.yml")),
                GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                               .build());
    }

    public boolean isBstats() {
        return document.getBoolean("bstats");
    }

    public boolean isFilterNotSecure() {
        return document.getBoolean("filter-not-secure");
    }

    public boolean isSendHeader() {
        return document.getBoolean("send-header");
    }

    public boolean isAutoSetup() {
        return document.getBoolean("auto-setup");
    }

    public boolean isBlockChatReports() {
        return document.getBoolean("block-chat-reports");
    }

    public void setBlockChatReports(boolean value) {
        document.set("block-chat-reports", value);
    }

    public boolean isClickableUrls() {
        return document.getBoolean("clickable-urls");
    }

    public String getPropertiesLocation() {
        return document.getString("properties-location");
    }

    public boolean isFirstRun() {
        return document.getBoolean("first-run");
    }

    public void setFirstRun(boolean value) {
        document.set("first-run", value);
    }

    public boolean isAskBstats() {
        return document.getBoolean("ask-bstats");
    }

    public void setAskBstats(boolean value) {
        document.set("ask-bstats", value);
    }

    public boolean isSetupMode() {
        return document.getBoolean("setup-mode");
    }

    public void setSetupMode(boolean value) {
        document.set("setup-mode", value);
    }

    public boolean isShowPopup() {
        return document.getBoolean("show-popup");
    }

    public void save() {
        try {
            document.save();
        } catch (IOException ex) {
            throw new RuntimeException("Could not save the config.", ex);
        }
    }

    public void reload() {
        try {
            document.reload();
        } catch (IOException ex) {
            throw new RuntimeException("Could not reload the config.", ex);
        }
    }

}
