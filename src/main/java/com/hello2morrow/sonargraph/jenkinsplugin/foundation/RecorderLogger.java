package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import java.io.PrintStream;
import java.util.logging.Level;

public final class RecorderLogger
{
    private RecorderLogger()
    {
        super();
    }

    public static void logToConsoleOutput(PrintStream logger, Level level, String message)
    {
        assert logger != null : "Parameter 'logger' of method 'logToConsoleOutput' must not be null";
        logger.println("[" + level.toString() + "] <SONARGRAPH> " + message);
        SonargraphLogger.INSTANCE.log(level, message);
    }
}