package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hudson.model.Result;

import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

import de.schlichtherle.truezip.file.TFile;

public class SonargraphBuildAnalyzerTest
{
    private static final String REPORT_FILE_NAME = "./src/test/resources/sonargraph-sonar-report.xml";
    private static final String DUMMY_LOG_FILE_NAME = "./src/test/resources/dummy.log";
    private TFile m_dummyLogFile;
    private PrintStream m_logger;

    @Before
    public void setUp() throws IOException
    {
        m_dummyLogFile = new TFile(DUMMY_LOG_FILE_NAME);
        if (!m_dummyLogFile.exists())
        {
            m_dummyLogFile.createNewFile();
        }
        m_logger = new PrintStream(DUMMY_LOG_FILE_NAME);
    }

    @After
    public void tearDown() throws IOException
    {
        m_logger.close();
        if ((m_dummyLogFile != null) & m_dummyLogFile.exists())
        {
            m_dummyLogFile.rm();
        }
    }

    @Test
    public void testChangeBuildResultIfViolationTresholdsExceeded() throws IOException
    {
        Result result = null;
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new TFile(REPORT_FILE_NAME), m_logger);
        //Number of violations is 3780
        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3781, 3783);
        assertNull(result);

        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3779, 3781);
        assertEquals(Result.UNSTABLE, result);

        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3777, 3779);
        assertEquals(Result.FAILURE, result);
    }

    @Test
    public void testChangeBuildResultIfMetricValueNotZero() throws IOException
    {
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new TFile(REPORT_FILE_NAME), m_logger);
        analyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.NOTHING.getActionCode());
        assertNull(analyzer.getOverallBuildResult());

        analyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.UNSTABLE.getActionCode());
        assertEquals(Result.UNSTABLE, analyzer.getOverallBuildResult());

        analyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.FAILED.getActionCode());
        assertEquals(Result.FAILURE, analyzer.getOverallBuildResult());

        analyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.UNSTABLE.getActionCode());
        assertEquals(Result.FAILURE, analyzer.getOverallBuildResult());
    }

    @Test
    public void testChangeBuildResultIfMetricValueIsZero() throws IOException
    {
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new TFile(REPORT_FILE_NAME), m_logger);

        analyzer.changeBuildResultIfMetricValueIsZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.FAILED.getActionCode());
        assertNull(analyzer.getOverallBuildResult());

        analyzer.changeBuildResultIfMetricValueIsZero(SonargraphMetrics.NUMBER_OF_TARGET_FILES, BuildActionsEnum.FAILED.getActionCode());
        assertNull(analyzer.getOverallBuildResult());
    }
}
