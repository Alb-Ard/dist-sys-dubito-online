package org.albard.utils;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

public final class Logger {
    private enum Level {
        TRACE("VRB"), DEBUG("DBG"), INFO("INF"), ERROR("ERR");

        private final String text;

        private Level(final String text) {
            this.text = text;
        }
    };

    private static final Level MINIMUM_LEVEL = Level.TRACE;
    private static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    private Logger() {
    }

    public static void logTrace(final String message) {
        printToStream(Level.TRACE, System.out, message);
    }

    public static void logDebug(final String message) {
        printToStream(Level.DEBUG, System.out, message);
    }

    public static void logInfo(final String message) {
        printToStream(Level.INFO, System.out, message);
    }

    public static void logError(final String message) {
        printToStream(Level.ERROR, System.err, message);
    }

    private static void printToStream(final Level level, final PrintStream stream, final String message) {
        if (level.ordinal() >= MINIMUM_LEVEL.ordinal()) {
            stream.println(new StringBuilder("[").append(DATE_FORMATTER.format(new Date())).append("] [")
                    .append(level.text).append("] ").append(message));
        }
    }
}