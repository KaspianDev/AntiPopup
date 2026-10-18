package com.github.kaspiandev.antipopup.spigot.nms;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Finds unique typed fields, including inherited fields; never guesses by field order. */
final class ConnectionAccess {
    private final Method getHandle;
    private final Field playerListener;
    private final Field networkConnection;
    private final Field channel;

    ConnectionAccess(Class<?> craftPlayer, ClassLoader loader) throws ReflectiveOperationException {
        this(craftPlayer,
                Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl", false, loader),
                Class.forName("net.minecraft.network.Connection", false, loader));
    }

    ConnectionAccess(Class<?> craftPlayer, Class<?> listenerType, Class<?> connectionType)
            throws ReflectiveOperationException {
        getHandle = craftPlayer.getMethod("getHandle");
        playerListener = uniqueField(getHandle.getReturnType(), listenerType);
        networkConnection = uniqueField(listenerType, connectionType);
        channel = uniqueField(connectionType, Channel.class);
    }

    Channel channel(Player player) throws ReflectiveOperationException {
        return channelOf(player);
    }

    Channel channelOf(Object player) throws ReflectiveOperationException {
        Object handle = getHandle.invoke(player);
        Object listener = playerListener.get(handle);
        Object connection = networkConnection.get(listener);
        return (Channel) channel.get(connection);
    }

    static Field uniqueField(Class<?> owner, Class<?> fieldType) throws NoSuchFieldException {
        Field result = null;
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !fieldType.isAssignableFrom(field.getType())) continue;
                if (result != null) throw new NoSuchFieldException("Ambiguous " + fieldType.getName() + " fields in " + owner.getName());
                result = field;
            }
        }
        if (result == null) throw new NoSuchFieldException("No " + fieldType.getName() + " field in " + owner.getName());
        if (!result.trySetAccessible()) throw new NoSuchFieldException("Cannot access " + result);
        return result;
    }
}
