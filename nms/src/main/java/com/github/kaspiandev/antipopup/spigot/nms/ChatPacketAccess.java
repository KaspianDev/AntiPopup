package com.github.kaspiandev.antipopup.spigot.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/** Validated, cached access to the modern Mojang-mapped chat packet contract. */
final class ChatPacketAccess {
    final Class<?> playerChat;
    private final Method unsignedContent;
    private final Method body;
    private final Method text;
    private final Method literal;
    private final Method chatType;
    private final Method decorate;
    private final Constructor<?> systemChat;
    private final boolean optionalContent;

    static ChatPacketAccess discover(ClassLoader loader) throws ReflectiveOperationException {
        return new ChatPacketAccess(
                Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerChatPacket", false, loader),
                Class.forName("net.minecraft.network.protocol.game.ClientboundSystemChatPacket", false, loader),
                Class.forName("net.minecraft.network.chat.Component", false, loader),
                Class.forName("net.minecraft.network.chat.ChatType$Bound", false, loader));
    }

    // Accepting classes separately also lets tests exercise the real resolver without a server.
    ChatPacketAccess(Class<?> playerChat, Class<?> systemChat, Class<?> component, Class<?> bound)
            throws ReflectiveOperationException {
        this.playerChat = playerChat;
        this.unsignedContent = playerChat.getMethod("unsignedContent");
        this.optionalContent = unsignedContent.getReturnType() == Optional.class;
        if (optionalContent) {
            require(unsignedContent.getGenericReturnType() instanceof ParameterizedType,
                    "unsignedContent Optional must declare its component type");
            ParameterizedType type = (ParameterizedType) unsignedContent.getGenericReturnType();
            require(type.getActualTypeArguments()[0] instanceof Class<?>
                            && component.isAssignableFrom((Class<?>) type.getActualTypeArguments()[0]),
                    "unsignedContent must return Optional<Component>");
        } else {
            require(component.isAssignableFrom(unsignedContent.getReturnType()), "unsignedContent must return Component");
        }
        this.body = playerChat.getMethod("body");
        this.text = body.getReturnType().getMethod("content");
        require(text.getReturnType() == String.class, "body.content must return String");
        this.literal = component.getMethod("literal", String.class);
        require(Modifier.isStatic(literal.getModifiers()) && component.isAssignableFrom(literal.getReturnType()),
                "Component.literal must be a static Component factory");
        this.chatType = playerChat.getMethod("chatType");
        require(bound.isAssignableFrom(chatType.getReturnType()), "chatType must return resolved ChatType.Bound");
        this.decorate = bound.getMethod("decorate", component);
        require(component.isAssignableFrom(decorate.getReturnType()), "decorate must return Component");
        this.systemChat = systemChat.getConstructor(component, boolean.class);
    }

    Object convert(Object packet) throws ReflectiveOperationException {
        Object content = unsignedContent.invoke(packet);
        if (optionalContent) content = ((Optional<?>) content).orElse(null);
        if (content == null) content = literal.invoke(null, text.invoke(body.invoke(packet)));
        Object decorated = decorate.invoke(chatType.invoke(packet), content);
        if (decorated == null) throw new IllegalStateException("Chat decoration returned null");
        return systemChat.newInstance(decorated, false);
    }

    private static void require(boolean condition, String message) throws NoSuchMethodException {
        if (!condition) throw new NoSuchMethodException(message);
    }
}
