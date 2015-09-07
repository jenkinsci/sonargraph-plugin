package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hudson.FilePath;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;


public class SonargraphBuildAnalyzerTest
{
    private static final String REPORT_FILE_NAME = "./src/test/resources/sonargraph-sonar-report.xml";
    private static final String DUMMY_LOG_FILE_NAME = "./src/test/resources/dummy.log";
    private File m_dummyLogFile;
    private PrintStream m_logger;

    @Before
    public void setUp() throws IOException
    {
        m_dummyLogFile = new File(DUMMY_LOG_FILE_NAME);
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
            m_dummyLogFile.delete();
        }
    }

    @Test
    public void testChangeBuildResultIfViolationTresholdsExceeded() throws IOException, InterruptedException
    {
        Result result = null;
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new FilePath((VirtualChannel)null, REPORT_FILE_NAME), m_logger);
        //Number of violations is 3780
        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3781, 3783);
        assertNull(result);

        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3779, 3781);
        assertEquals(Result.UNSTABLE, result);

        result = analyzer.changeBuildResultIfViolationThresholdsExceeded(3777, 3779);
        assertEquals(Result.FAILURE, result);
    }

    @Test
    public void testChangeBuildResultIfMetricValueNotZero() throws IOException, InterruptedException
    {
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new FilePath((VirtualChannel)null, REPORT_FILE_NAME), m_logger);
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
    public void testChangeBuildResultIfMetricValueIsZero() throws IOException, InterruptedException
    {
        SonargraphBuildAnalyzer analyzer = new SonargraphBuildAnalyzer(new FilePath((VirtualChannel)null, REPORT_FILE_NAME), m_logger);

        analyzer.changeBuildResultIfMetricValueIsZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, BuildActionsEnum.FAILED.getActionCode());
        assertNull(analyzer.getOverallBuildResult());

        analyzer.changeBuildResultIfMetricValueIsZero(SonargraphMetrics.NUMBER_OF_TARGET_FILES, BuildActionsEnum.FAILED.getActionCode());
        assertNull(analyzer.getOverallBuildResult());
    }
}
