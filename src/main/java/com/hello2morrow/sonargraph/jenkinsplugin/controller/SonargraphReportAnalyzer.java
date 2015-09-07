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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.RecorderLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

/**
 * Processes a previously generated Sonargraph report.
 * 
 * @author Ingmar
 */
public class SonargraphReportAnalyzer extends AbstractSonargraphRecorder
{

    private final String reportName;

    @DataBoundConstructor
    public SonargraphReportAnalyzer(String reportDirectory, String reportName, String architectureViolationsAction, String unassignedTypesAction,
            String cyclicElementsAction, String thresholdViolationsAction, String architectureWarningsAction, String workspaceWarningsAction,
            String workItemsAction, String emptyWorkspaceAction, String replaceDefaultMetrics, List<ChartForMetric> additionalMetricsToDisplay)
    {
        super(reportDirectory, architectureViolationsAction, unassignedTypesAction, cyclicElementsAction, thresholdViolationsAction,
                architectureWarningsAction, workspaceWarningsAction, workItemsAction, emptyWorkspaceAction, replaceDefaultMetrics,
                additionalMetricsToDisplay);

        assert (reportName != null) && (reportName.length() > 0) : "Parameter 'sonargraphReportName' of method 'SonargraphReportAnalyzer' must not be empty";
        this.reportName = reportName;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
        assert listener != null : "Parameter 'listener' of method 'perform' must not be null";
        logExecutionStart(build, listener, SonargraphReportAnalyzer.class);

        if (!super.processMetricsForCharts(build))
        {
            RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE,
                    "There was an error trying to save the configuration of metrics to be displayed in charts");
            return false;
        }

        FilePath absoluteReportFolder = new FilePath(build.getWorkspace(), getReportDirectory());
        if (super.processSonargraphReport(build, absoluteReportFolder, reportName, listener.getLogger()))
        {
            addActions(build);
        }
        return true;
    }

    public String getReportName()
    {
        return reportName;
    }

    @Override
    public DescriptorImpl getDescriptor()
    {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends AbstractBuildStepDescriptor
    {
        public DescriptorImpl()
        {
            super();
            load();
        }

        @Override
        public String getDisplayName()
        {
            return ConfigParameters.REPORT_ANALYZER_DISPLAY_NAME.getValue();
        }

        public FormValidation doCheckReportName(@QueryParameter String value)
        {
            return StringUtility.validateNotNullAndRegexp(value, "[\\/\\\\a-zA-Z0-9_.-]+") ? FormValidation.ok() : FormValidation
                    .error("Please enter a valid name for the report");
        }
    }
}
