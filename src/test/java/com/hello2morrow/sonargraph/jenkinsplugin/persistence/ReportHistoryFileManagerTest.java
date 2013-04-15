package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schlichtherle.truezip.file.TFile;

public class ReportHistoryFileManagerTest
{
    private static final String reportFileName = "src/test/resources/sonargraph-sonar-report.xml";
    private static final String archReportHistoryPath = "src/test/resources/temp";
    private static final String buildReportDirectoryPath = "src/test/resources/report";
    private TFile sonargraphReportFile;
    private static final String dummyLogFileName = "src/test/resources/dummy.log";
    private TFile dummyLogFile = new TFile(dummyLogFileName);
    private PrintStream m_logger;;

    @Before
    public void before() throws IOException
    {
        removeFiles();
        if (!dummyLogFile.exists())
        {
            dummyLogFile.createNewFile();
        }
        m_logger = new PrintStream(dummyLogFileName);
    }

    @After
    public void tearDown() throws IOException
    {
        if (m_logger != null)
        {
            m_logger.close();
        }
        removeFiles();
    }

    private void removeFiles() throws IOException
    {
        if ((sonargraphReportFile != null) && sonargraphReportFile.exists())
        {
            sonargraphReportFile.rm();
        }
        TFile historyDir = new TFile(archReportHistoryPath);
        if (historyDir.exists())
        {
            historyDir.rm_r();
        }
        TFile buildReportDir = new TFile(buildReportDirectoryPath);
        if (buildReportDir.exists())
        {
            buildReportDir.rm_r();
        }
        if ((dummyLogFile != null) && dummyLogFile.exists())
        {
            dummyLogFile.rm();
        }
    }

    @Test
    public void testStoreGeneratedReport() throws IOException
    {
        ReportHistoryFileManager rhfm = new ReportHistoryFileManager(new TFile(archReportHistoryPath), "sonargraphReportHistory", m_logger);
        Integer buildNumber = 1;
        sonargraphReportFile = new TFile(rhfm.getReportHistoryDirectory(), ReportHistoryFileManager.SONARGRAPH_JENKINS_REPORT_FILE_NAME_PREFIX
                + buildNumber + ".xml");
        assertFalse(sonargraphReportFile.exists());

        rhfm.storeGeneratedReport(new TFile(reportFileName), buildNumber, m_logger);
        assertTrue(sonargraphReportFile.exists());
    }

    @Test
    public void testStoreGeneratedReportDirectory() throws IOException
    {
        ReportHistoryFileManager rhfm = new ReportHistoryFileManager(new TFile(archReportHistoryPath), "sonargraphReportHistory", m_logger);
        TFile buildReportDirectory = new TFile(buildReportDirectoryPath);
        if (!buildReportDirectory.exists())
        {
            buildReportDirectory.mkdir();
        }
        TFile testFile = new TFile(buildReportDirectory.getPath(), "testFile.xml");
        if (!testFile.exists())
        {
            testFile.createNewFile();
        }
        rhfm.storeGeneratedReportDirectory(buildReportDirectory, 1, m_logger);
        String buildReportDirInHistory = "/sonargraph-report-build-1";
        assertTrue(new TFile(rhfm.getReportHistoryDirectory(), buildReportDirInHistory).exists());
        assertTrue(new TFile(rhfm.getReportHistoryDirectory() + buildReportDirInHistory, "/testFile.xml").exists());
    }
}
