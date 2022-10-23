package com.github.kaspiandev.antipopup;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import static com.github.kaspiandev.antipopup.AntiPopup.config;

public class LogFilter implements Filter {

    @Override
    public Filter.Result filter(LogEvent event) {

        // We wanna eliminate possibilities of false catches.
        if (config.getBoolean("filter-not-secure", true)
                    && event.getMessage().getFormattedMessage().contains("[Not Secure] ")
                    && event.getLoggerName().equals("net.minecraft.server.MinecraftServer")
                    && event.getLevel() == Level.INFO) {
            LogManager.getLogger(event.getLoggerName()).log(event.getLevel(),
                    event.getMessage().getFormattedMessage()
                            .replace("[Not Secure] ", ""));
            return Filter.Result.DENY;
        }
        if (config.getBoolean("sync-time-suppress", false)
                    && event.getMessage().getFormattedMessage().contains("sent out-of-order chat:")) {
            return Filter.Result.DENY;

        }
        String packetEventError = "PacketEvents caught an unhandled "
                                          + "exception while calling your listener."
                                          + " java.lang.IndexOutOfBoundsException: index: 3,"
                                          + " length: 47 (expected: range(0, 3))";
        if (config.getBoolean("suppress-pe-warnings", false)
                    && event.getMessage().getFormattedMessage().contains(packetEventError)) {
            return Filter.Result.DENY;
        }
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Result getOnMismatch() {
        return null;
    }

    @Override
    public Result getOnMatch() {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return null;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

}