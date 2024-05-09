package com.github.kaspiandev.antipopup.spigot.hook.viaversion;

import com.github.kaspiandev.antipopup.spigot.AntiPopup;
import com.github.kaspiandev.antipopup.spigot.hook.Hook;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ViaVersionHook implements Hook {

    private final List<Class<? extends ViaProtocolModifier<?>>> modifiers;
    private final List<ViaProtocolModifier<?>> registeredModifiers;

    public ViaVersionHook() {
        this.modifiers = new ArrayList<>();
        this.registeredModifiers = new ArrayList<>();
    }

    public void addModifier(Class<? extends ViaProtocolModifier<?>> modifier) {
        modifiers.add(modifier);
    }

    @Override
    public String getPluginName() {
        return "ViaVersion";
    }

    @Override
    public void register() {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        for (Class<? extends ViaProtocolModifier<?>> modifier : modifiers) {
            try {
                ViaProtocolModifier<?> modifierInstance = modifier.getDeclaredConstructor().newInstance();

                if (serverVersion.is(modifierInstance.getComparison(), modifierInstance.getVersion())) {
                    modifierInstance.modify();
                    registeredModifiers.add(modifierInstance);
                    modifiers.remove(modifier);
                    AntiPopup.getInstance().getLogger().info(() -> getRegisteredMessage(modifierInstance));
                }
            } catch (IllegalAccessException | InvocationTargetException
                     | NoSuchMethodException | InstantiationException ignored) {}
        }
    }

    private String getRegisteredMessage(ViaProtocolModifier<?> modifier) {
        String comparison = switch (modifier.getComparison()) {
            case EQUALS -> "equal to";
            case OLDER_THAN -> "older than";
            case NEWER_THAN -> "newer than";
            case NEWER_THAN_OR_EQUALS -> "newer than or equal to";
            case OLDER_THAN_OR_EQUALS -> "older than or equal to";
        };
        return "Registered a modifier targeted for versions "
                + comparison
                + " "
                + modifier.getVersion().getReleaseName()
                + ".";
    }

    public List<ViaProtocolModifier<?>> getRegisteredModifiers() {
        return registeredModifiers;
    }

}
