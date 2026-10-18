package com.github.kaspiandev.antipopup.spigot.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ReflectivePacketInjectorTest {
    public interface Component {
        static Component literal(String text) { return new Text(text); }
    }
    public record Text(String value) implements Component { }
    public record Rich(String value, String click, String hover) implements Component { }
    public record Decorated(Component message, String sender) implements Component { }
    public record Body(String content) { }
    public static class Bound {
        public Component decorate(Component content) { return new Decorated(content, "Custom sender"); }
    }
    public record PlayerChat(Component unsignedContent, Body body, Bound chatType) { }
    public record SystemChat(Component content, boolean overlay) { }
    public record OldPlayerChat(Component unsignedContent, Body body, Object chatType) { }
    public record OptionalPlayerChat(Optional<Component> unsignedContent, Body body, Bound chatType) { }
    public record InvalidOptionalPlayerChat(Optional<String> unsignedContent, Body body, Bound chatType) { }
    public static class BrokenBound extends Bound {
        @Override public Component decorate(Component content) { throw new IllegalStateException("Broken decoration"); }
    }
    public static class Network {
        private final Channel channel;
        Network(Channel channel) { this.channel = channel; }
    }
    public static class CommonListener {
        private final Network renamedNetworkField;
        CommonListener(Network network) { renamedNetworkField = network; }
    }
    public static class GameListener extends CommonListener {
        GameListener(Network network) { super(network); }
    }
    public static class Handle {
        private final GameListener renamedPlayerField;
        Handle(GameListener listener) { renamedPlayerField = listener; }
    }
    public static class CraftPlayer {
        private final Handle handle;
        CraftPlayer(Handle handle) { this.handle = handle; }
        public Handle getHandle() { return handle; }
    }
    public static class Ambiguous {
        private Network first;
        private Network second;
    }

    private ChatPacketAccess access() throws ReflectiveOperationException {
        return new ChatPacketAccess(PlayerChat.class, SystemChat.class, Component.class, Bound.class);
    }

    private EmbeddedChannel channel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("packet_handler", new ChannelDuplexHandler());
        return channel;
    }

    private ReflectivePacketInjector injector(Logger logger) throws ReflectiveOperationException {
        return new ReflectivePacketInjector(null, access(), logger);
    }

    @Test void discoversRenamedInheritedConnectionFieldsByType() throws Exception {
        EmbeddedChannel channel = channel();
        try {
            ConnectionAccess connections = new ConnectionAccess(CraftPlayer.class, GameListener.class, Network.class);
            CraftPlayer player = new CraftPlayer(new Handle(new GameListener(new Network(channel))));
            assertSame(channel, connections.channelOf(player));
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test void rejectsAmbiguousConnectionFields() {
        assertThrows(NoSuchFieldException.class, () -> ConnectionAccess.uniqueField(Ambiguous.class, Network.class));
    }

    @Test void rejectsOlderUnresolvedChatTypeInsteadOfPretendingToSupportIt() {
        assertThrows(NoSuchMethodException.class,
                () -> new ChatPacketAccess(OldPlayerChat.class, SystemChat.class, Component.class, Bound.class));
    }

    @Test void preservesRichContentAndCustomDecoration() throws Exception {
        Rich original = new Rich("Hello", "https://example.org", "Hover text");
        SystemChat result = (SystemChat) access().convert(new PlayerChat(original, new Body("signed"), new Bound()));
        assertSame(original, ((Decorated) result.content()).message());
        assertEquals("Custom sender", ((Decorated) result.content()).sender());
        assertFalse(result.overlay());
    }

    @Test void usesLiteralBodyWhenUnsignedContentIsAbsent() throws Exception {
        SystemChat result = (SystemChat) access().convert(new PlayerChat(null, new Body("Plain message"), new Bound()));
        assertEquals(new Text("Plain message"), ((Decorated) result.content()).message());
    }

    @Test void supportsOptionalContentIntroducedIn26_3() throws Exception {
        ChatPacketAccess access = new ChatPacketAccess(OptionalPlayerChat.class, SystemChat.class, Component.class, Bound.class);
        Rich original = new Rich("Hello", "https://example.org", "Hover");
        SystemChat rich = (SystemChat) access.convert(new OptionalPlayerChat(Optional.of(original), new Body("signed"), new Bound()));
        assertSame(original, ((Decorated) rich.content()).message());
        SystemChat plain = (SystemChat) access.convert(new OptionalPlayerChat(Optional.empty(), new Body("plain"), new Bound()));
        assertEquals(new Text("plain"), ((Decorated) plain.content()).message());
    }

    @Test void rejectsOptionalWithWrongContentType() {
        assertThrows(NoSuchMethodException.class,
                () -> new ChatPacketAccess(InvalidOptionalPlayerChat.class, SystemChat.class, Component.class, Bound.class));
    }

    @Test void preservesPacketOrderAndOriginalPromiseWithoutResendingThroughPlayer() throws Exception {
        EmbeddedChannel channel = channel();
        ReflectivePacketInjector injector = injector(Logger.getAnonymousLogger());
        try {
            injector.injectChannel(UUID.randomUUID(), channel);
            channel.runPendingTasks();
            Object before = new Object();
            Object after = new Object();
            channel.writeOutbound(before);
            ChannelPromise completion = channel.newPromise();
            channel.writeAndFlush(new PlayerChat(null, new Body("hello"), new Bound()), completion);
            channel.writeOutbound(after);
            assertTrue(completion.isSuccess());
            assertSame(before, channel.readOutbound());
            assertInstanceOf(SystemChat.class, channel.readOutbound());
            assertSame(after, channel.readOutbound());
            assertNull(channel.readOutbound());
        } finally { injector.close(); channel.runPendingTasks(); channel.finishAndReleaseAll(); }
    }

    @Test void conversionFailureIsLoggedFailsPromiseAndNeverForwardsOriginal() throws Exception {
        EmbeddedChannel channel = channel();
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        int[] errors = {0};
        logger.addHandler(new Handler() {
            public void publish(LogRecord record) { errors[0]++; }
            public void flush() { }
            public void close() { }
        });
        ReflectivePacketInjector injector = injector(logger);
        try {
            injector.injectChannel(UUID.randomUUID(), channel);
            channel.runPendingTasks();
            ChannelPromise completion = channel.newPromise();
            channel.writeAndFlush(new PlayerChat(null, new Body("private"), new BrokenBound()), completion);
            assertTrue(completion.isDone());
            assertFalse(completion.isSuccess());
            assertNotNull(completion.cause());
            assertEquals(1, errors[0]);
            assertNull(channel.readOutbound());
            assertFalse(channel.isOpen());
        } finally { injector.close(); channel.runPendingTasks(); channel.finishAndReleaseAll(); }
    }

    @Test void repeatedAttachAndQuitCleanUpHandlers() throws Exception {
        EmbeddedChannel channel = channel();
        ReflectivePacketInjector injector = injector(Logger.getAnonymousLogger());
        UUID id = UUID.randomUUID();
        try {
            injector.injectChannel(id, channel);
            injector.injectChannel(id, channel);
            channel.runPendingTasks();
            assertNotNull(channel.pipeline().get("antipopup_handler"));
            injector.remove(id);
            channel.runPendingTasks();
            assertNull(channel.pipeline().get("antipopup_handler"));
            injector.injectChannel(id, channel);
            channel.runPendingTasks();
            injector.close();
            channel.runPendingTasks();
            assertNull(channel.pipeline().get("antipopup_handler"));
            assertNotNull(channel.pipeline().get("packet_handler"));
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test void shutdownBeforeQueuedAttachDoesNotLeaveAHandler() throws Exception {
        EmbeddedChannel channel = channel();
        ReflectivePacketInjector injector = injector(Logger.getAnonymousLogger());
        try {
            injector.injectChannel(UUID.randomUUID(), channel);
            injector.close();
            channel.runPendingTasks();
            assertNull(channel.pipeline().get("antipopup_handler"));
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test void missingPacketHandlerClosesConnectionAndDoesNotInstallAnUnusableInterceptor() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        ReflectivePacketInjector injector = injector(logger);
        try {
            injector.injectChannel(UUID.randomUUID(), channel);
            channel.runPendingTasks();
            assertFalse(channel.isOpen());
            assertNull(channel.pipeline().get("antipopup_handler"));
        } finally { injector.close(); channel.finishAndReleaseAll(); }
    }
}
