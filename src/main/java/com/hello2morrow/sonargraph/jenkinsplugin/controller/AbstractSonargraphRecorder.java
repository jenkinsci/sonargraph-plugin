/*******************************************************************************
 * Jenkins Sonargraph Plugin
 * Copyright (C) 2009-2015 hello2morrow GmbH
 * mailto: info AT hello2morrow DOT com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *******************************************************************************/
package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.FilePath;
import hudson.maven.MavenModuleSet;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.RecorderLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.CSVChartsForMetricsHandler;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.PluginVersionReader;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.ReportHistoryFileManager;

@SuppressWarnings("unchecked")
public abstract class AbstractSonargraphRecorder extends Recorder
{
    private final String reportDirectory;
    private final String architectureViolationsAction;
    private final String unassignedTypesAction;
    private final String cyclicElementsAction;
    private final String thresholdViolationsAction;
    private final String architectureWarningsAction;
    private final String workspaceWarningsAction;
    private final String workItemsAction;
    private final String emptyWorkspaceAction;

    private final String replaceDefaultMetrics;
    private final List<ChartForMetric> additionalMetricsToDisplay;

    public AbstractSonargraphRecorder(String reportDirectory, String architectureViolationsAction, String unassignedTypesAction,
            String cyclicElementsAction, String thresholdViolationsAction, String architectureWarningsAction, String workspaceWarningsAction,
            String workItemsAction, String emptyWorkspaceAction, String replaceDefaultMetrics, List<ChartForMetric> additionalMetricsToDisplay)
    {
        this.reportDirectory = reportDirectory;
        this.architectureViolationsAction = architectureViolationsAction;
        this.unassignedTypesAction = unassignedTypesAction;
        this.cyclicElementsAction = cyclicElementsAction;
        this.thresholdViolationsAction = thresholdViolationsAction;
        this.architectureWarningsAction = architectureWarningsAction;
        this.workspaceWarningsAction = workspaceWarningsAction;
        this.workItemsAction = workItemsAction;
        this.emptyWorkspaceAction = emptyWorkspaceAction;
        this.replaceDefaultMetrics = replaceDefaultMetrics;
        if (additionalMetricsToDisplay == null)
        {
            this.additionalMetricsToDisplay = Collections.<ChartForMetric> emptyList();
        }
        else
        {
            this.additionalMetricsToDisplay = new ArrayList<ChartForMetric>(additionalMetricsToDisplay);
            for (Iterator<ChartForMetric> iter = this.additionalMetricsToDisplay.iterator(); iter.hasNext();)
            {
                ChartForMetric next = iter.next();
                if (SonargraphMetrics.EMPTY.getStandardName().equals(next.getMetricName()))
                {
                    iter.remove();
                }
            }
        }
    }

    private static List<ChartForMetric> getDefaultMetrics()
    {
        List<ChartForMetric> chartMetrics = new ArrayList<ChartForMetric>();
        for (SonargraphMetrics metric : SonargraphMetrics.getDefaultMetrics())
        {
            chartMetrics.add(new ChartForMetric(metric.getStandardName()));
        }

        return chartMetrics;
    }

    /**
     * We override the getProjectAction method to define our custom action
     * that will show the charts for sonargraph's metrics.
     */
    @Override
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project)
    {
        Collection<Action> actions = new ArrayList<Action>();
        if (project instanceof Project || project instanceof MavenModuleSet)
        {
            actions.add(new SonargraphChartAction(project, this));
            actions.add(new SonargraphHTMLReportAction(project, this));
        }
        return actions;
    }

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }

    protected boolean processSonargraphReport(AbstractBuild<?, ?> build, FilePath sonargraphReportDirectory, String reportFileName, PrintStream logger)
            throws IOException, InterruptedException
    {
        assert build != null : "Parameter 'build' of method 'processSonargraphReport' must not be null";
        assert sonargraphReportDirectory != null : "Parameter 'sonargraphReportFile' of method 'processSonargraphReport' must not be null";

        FilePath projectRootDir = new FilePath(build.getProject().getRootDir());
        ReportHistoryFileManager reportHistoryManager = new ReportHistoryFileManager(projectRootDir,
                ConfigParameters.REPORT_HISTORY_FOLDER.getValue(), logger);
        try
        {
            reportHistoryManager.storeGeneratedReportDirectory(sonargraphReportDirectory, build.getNumber(), logger);
        }
        catch (IOException ex)
        {
            RecorderLogger.logToConsoleOutput(logger, Level.SEVERE, "Failed to process the generated Sonargraph report");
            return false;
        }

        String reportFileNameWithExtension = StringUtility.addXmlExtensionIfNotPreset(reportFileName);
        FilePath reportFile = new FilePath(sonargraphReportDirectory, reportFileNameWithExtension);
        if (!reportFile.exists())
        {
            RecorderLogger.logToConsoleOutput(logger, Level.SEVERE, "Sonargraph analysis cannot be executed as Sonargraph report does not exist.");
            build.setResult(Result.ABORTED);
            return false;
        }

        SonargraphBuildAnalyzer sonargraphBuildAnalyzer = new SonargraphBuildAnalyzer(reportFile, logger);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_VIOLATIONS, architectureViolationsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES, unassignedTypesAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, cyclicElementsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS, thresholdViolationsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS, architectureWarningsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS, workspaceWarningsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueNotZero(SonargraphMetrics.NUMBER_OF_TASKS, workItemsAction);
        sonargraphBuildAnalyzer.changeBuildResultIfMetricValueIsZero(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES, emptyWorkspaceAction);
        Result buildResult = sonargraphBuildAnalyzer.getOverallBuildResult();

        File metricHistoryFile = new File(build.getProject().getRootDir(), ConfigParameters.METRIC_HISTORY_CSV_FILE_PATH.getValue());
        try
        {
            sonargraphBuildAnalyzer.saveMetricsToCSV(metricHistoryFile, build.getTimestamp().getTimeInMillis(), build.getNumber());
        }
        catch (IOException ex)
        {
            RecorderLogger.logToConsoleOutput(logger, Level.SEVERE, "Failed to save Sonargraph metrics to CSV data file");
            return false;
        }
        if (buildResult != null)
        {
            RecorderLogger.logToConsoleOutput(logger, Level.INFO, "Sonargraph analysis has set the final build result to '" + buildResult.toString()
                    + "'");
            build.setResult(buildResult);
        }
        return true;
    }

    protected final boolean processMetricsForCharts(AbstractBuild<?, ?> build)
    {
        assert build != null : "Parameter 'build' of method 'processMetricsForCharts' must not be null";

        File chartsForMetricsFile = new File(build.getProject().getRootDir(), ConfigParameters.CHARTS_FOR_METRICS_CSV_FILE_PATH.getValue());
        try
        {
            CSVChartsForMetricsHandler chartsForMetricsHandler = new CSVChartsForMetricsHandler();
            List<String> metricsAsStrings = new ArrayList<String>();

            //Add default metrics
            if (getReplaceDefaultMetrics() == null || !getReplaceDefaultMetrics().trim().equals(Boolean.toString(true)))
            {
                for (ChartForMetric next : getDefaultMetrics())
                {
                    metricsAsStrings.add(next.getMetricName());
                }
            }

            //Always add additional metrics (if there are any)
            for (ChartForMetric chartForMetric : getAdditionalMetricsToDisplay())
            {
                metricsAsStrings.add(chartForMetric.getMetricName());
            }
            chartsForMetricsHandler.writeChartsForMetrics(chartsForMetricsFile, metricsAsStrings);
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    protected void logExecutionStart(AbstractBuild<?, ?> build, BuildListener listener, Class<? extends AbstractSonargraphRecorder> recorderClazz)
    {
        RecorderLogger.logToConsoleOutput(
                listener.getLogger(),
                Level.INFO,
                "Sonargraph Jenkins Plugin, Version '" + PluginVersionReader.INSTANCE.getVersion() + "', post-build step '" + recorderClazz.getName()
                        + "'\n" + "Start structural analysis on project '" + build.getProject().getDisplayName() + "', build number '"
                        + build.getNumber() + "'");
    }

    protected void addActions(AbstractBuild<?, ?> build)
    {
        build.addAction(new SonargraphBadgeAction());
        build.addAction(new SonargraphBuildAction(build, this));
    }

    public String getReportDirectory()
    {
        return reportDirectory;
    }

    public String getArchitectureViolationsAction()
    {
        return architectureViolationsAction;
    }

    public String getUnassignedTypesAction()
    {
        return unassignedTypesAction;
    }

    public String getCyclicElementsAction()
    {
        return cyclicElementsAction;
    }

    public String getThresholdViolationsAction()
    {
        return thresholdViolationsAction;
    }

    public String getArchitectureWarningsAction()
    {
        return architectureWarningsAction;
    }

    public String getWorkspaceWarningsAction()
    {
        return workspaceWarningsAction;
    }

    public String getWorkItemsAction()
    {
        return workItemsAction;
    }

    public String getEmptyWorkspaceAction()
    {
        return emptyWorkspaceAction;
    }

    public String getReplaceDefaultMetrics()
    {
        if (replaceDefaultMetrics == null)
        {
            return Boolean.toString(false);
        }
        return replaceDefaultMetrics;
    }

    public List<ChartForMetric> getAdditionalMetricsToDisplay()
    {
        if (additionalMetricsToDisplay == null)
        {
            return Collections.emptyList();
        }
        return additionalMetricsToDisplay;
    }

    public String getDefaultMetricsAsString()
    {
        StringBuilder defaultMetricsAsString = new StringBuilder();

        List<SonargraphMetrics> defaultMetrics = SonargraphMetrics.getDefaultMetrics();
        int numberOfDefaultMetrics = defaultMetrics.size();

        for (int i = 0; i < numberOfDefaultMetrics; i++)
        {
            if (i == numberOfDefaultMetrics - 1)
            {
                defaultMetricsAsString.append(" and ");
            }
            defaultMetricsAsString.append(defaultMetrics.get(i).getDescription());
            defaultMetricsAsString.append(", ");
        }
        defaultMetricsAsString.replace(defaultMetricsAsString.length() - 2, defaultMetricsAsString.length(), StringUtility.EMPTY_STRING);

        return defaultMetricsAsString.toString();
    }
}
