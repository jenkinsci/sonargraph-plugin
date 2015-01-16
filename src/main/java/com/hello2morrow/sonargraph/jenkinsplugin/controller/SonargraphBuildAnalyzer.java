package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.model.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.RecorderLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphNumberFormat;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IReportReader;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphReport;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.CSVFileHandler;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.ReportFileReader;

import de.schlichtherle.truezip.file.TFile;

/**
 * Class that analyzes the values found for the metrics and takes action
 * depending of what the user selected to do.
 * 
 * @author esteban
 */
class SonargraphBuildAnalyzer
{
    /**
     * HashMap containing a summary of results for the metrics. e.g. key:
     * NumberOfViolations, value: 3
     */
    private SonargraphReport m_report;

    /**
     * HashMap containing a code for the build result and a Result object for
     * each code.
     */
    private final HashMap<String, Result> m_buildResults = new HashMap<String, Result>();

    /** Final result of the build process after being affected by the metrics analysis.s */
    private Result m_overallBuildResult;

    private OutputStream m_logger = null;

    /**
     * Constructor.
     * @param architectReportPath Absolute path to the Sonargraph architect report.
     */
    public SonargraphBuildAnalyzer(TFile architectReportPath, OutputStream logger)
    {
        assert architectReportPath != null : "The path for the Sonargraph architect report must not be null";
        assert logger != null : "Parameter 'logger' of method 'SonargraphBuildAnalyzer' must not be null";
        m_logger = logger;
        IReportReader reportReader = new ReportFileReader();
        m_report = reportReader.readSonargraphReport(architectReportPath);

        m_buildResults.put(BuildActionsEnum.UNSTABLE.getActionCode(), Result.UNSTABLE);
        m_buildResults.put(BuildActionsEnum.FAILED.getActionCode(), Result.FAILURE);

        m_overallBuildResult = null;
    }

    /**
     * Analyzes architecture specific metrics.
     */
    public Result changeBuildResultIfViolationThresholdsExceeded(Integer numberOfViolationsUnstable, Integer numberOfViolationsFailed)
    {
        Result result = null;

        Integer numberOfViolations = SonargraphNumberFormat.parse(m_report.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_VIOLATIONS)).intValue();
        if (numberOfViolations > 0)
        {
            if (numberOfViolations >= numberOfViolationsFailed)
            {
                result = m_buildResults.get(BuildActionsEnum.FAILED.getActionCode());
            }
            else if ((numberOfViolations >= numberOfViolationsUnstable) && (numberOfViolations < numberOfViolationsFailed))
            {
                result = m_buildResults.get(BuildActionsEnum.UNSTABLE.getActionCode());
            }
        }
        return result;
    }

    public void changeBuildResultIfMetricValueNotZero(SonargraphMetrics metric, String userDefinedAction)
    {
        if (m_report.getSystemMetricValue(metric) == null)
        {
            RecorderLogger.logToConsoleOutput((PrintStream) m_logger, Level.WARNING, "Metric '" + metric.getStandardName()
                    + "' not present in analysis");
            return;
        }
        Integer metricValue = SonargraphNumberFormat.parse(m_report.getSystemMetricValue(metric)).intValue();
        if (metricValue > 0)
        {
            if (userDefinedAction.equals(BuildActionsEnum.FAILED.getActionCode()))
            {
                m_overallBuildResult = m_buildResults.get(BuildActionsEnum.FAILED.getActionCode());
                RecorderLogger.logToConsoleOutput((PrintStream) m_logger, Level.INFO, "Changing build result to " + m_overallBuildResult.toString()
                        + " because value for " + metric.getDescription() + " is " + metricValue);
            }
            else if (userDefinedAction.equals(BuildActionsEnum.UNSTABLE.getActionCode()))
            {
                if ((m_overallBuildResult == null) || !m_overallBuildResult.equals(Result.FAILURE))
                {
                    m_overallBuildResult = m_buildResults.get(BuildActionsEnum.UNSTABLE.getActionCode());
                    RecorderLogger.logToConsoleOutput((PrintStream) m_logger, Level.INFO,
                            "Changing build result to " + m_overallBuildResult.toString() + " because value for " + metric.getDescription() + " is "
                                    + metricValue);
                }
            }

        }
    }

    public void changeBuildResultIfMetricValueIsZero(SonargraphMetrics metric, String userDefinedAction)
    {
        Integer metricValue = SonargraphNumberFormat.parse(m_report.getSystemMetricValue(metric)).intValue();
        if (metricValue.equals(0))
        {
            if (userDefinedAction.equals(BuildActionsEnum.FAILED.getActionCode()))
            {
                m_overallBuildResult = m_buildResults.get(BuildActionsEnum.FAILED.getActionCode());
                RecorderLogger.logToConsoleOutput((PrintStream) m_logger, Level.INFO, "Changing build result to " + m_overallBuildResult.toString()
                        + " because value for " + metric.getDescription() + " is " + metricValue);

            }
            else if (userDefinedAction.equals(BuildActionsEnum.UNSTABLE.getActionCode()))
            {
                if ((m_overallBuildResult == null) || !m_overallBuildResult.equals(Result.FAILURE))
                {
                    m_overallBuildResult = m_buildResults.get(BuildActionsEnum.UNSTABLE.getActionCode());
                    RecorderLogger.logToConsoleOutput((PrintStream) m_logger, Level.INFO,
                            "Changing build result to " + m_overallBuildResult.toString() + " because value for " + metric.getDescription() + " is "
                                    + metricValue);
                }
            }

        }
    }

    /**
     * Appends all gathered metrics to the sonargraph CSV file.
     */
    public void saveMetricsToCSV(TFile metricHistoryFile, long timeOfBuild, Integer buildNumber) throws IOException
    {
        IMetricHistoryProvider fileHandler = new CSVFileHandler(metricHistoryFile);
        HashMap<SonargraphMetrics, String> buildMetricValues = new HashMap<SonargraphMetrics, String>();

        for (SonargraphMetrics metric : SonargraphMetrics.values())
        {
            if (metric == SonargraphMetrics.EMPTY)
            {
                continue;
            }
            buildMetricValues.put(metric, m_report.getSystemMetricValue(metric));
        }

        fileHandler.writeMetricValues(buildNumber, timeOfBuild, buildMetricValues);
    }

    public Result getOverallBuildResult()
    {
        return m_overallBuildResult;
    }
}
