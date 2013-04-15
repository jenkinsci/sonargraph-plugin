package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;

public class RecorderLoggerTest
{
    private static final String dummyLogFileName = "src/test/resources/dummy.log";
    private TFile dummyLogFile = new TFile(dummyLogFileName);

    @Before
    public void before() throws IOException
    {
        removeFiles();
        if (!dummyLogFile.exists())
        {
            dummyLogFile.createNewFile();

        }
    }

    @After
    public void tearDown() throws IOException
    {
        removeFiles();
    }

    private void removeFiles() throws IOException
    {
        if ((dummyLogFile != null) & dummyLogFile.exists())
        {
            dummyLogFile.rm();
        }
    }

    @Test
    public void testLogToConsoleOutput() throws IOException
    {
        PrintStream logger = new PrintStream(dummyLogFileName);
        String testText = "test Text";
        RecorderLogger.logToConsoleOutput(logger, Level.INFO, testText);
        RecorderLogger.logToConsoleOutput(logger, Level.WARNING, testText);
        RecorderLogger.logToConsoleOutput(logger, Level.SEVERE, testText);
        logger.close();

        TFileReader reader = new TFileReader(dummyLogFile);
        BufferedReader buffReader = new BufferedReader(reader);

        String line;
        line = buffReader.readLine();
        assertTrue(line.contains("[INFO]"));
        assertTrue(line.contains("<SONARGRAPH>"));
        assertTrue(line.contains(testText));
        line = buffReader.readLine();
        assertTrue(line.contains("[WARNING]"));
        assertTrue(line.contains("<SONARGRAPH>"));
        assertTrue(line.contains(testText));
        line = buffReader.readLine();
        assertTrue(line.contains("[SEVERE]"));
        assertTrue(line.contains("<SONARGRAPH>"));
        assertTrue(line.contains(testText));

        buffReader.close();
    }

}
