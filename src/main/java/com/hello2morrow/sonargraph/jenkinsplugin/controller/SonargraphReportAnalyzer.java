package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

import de.schlichtherle.truezip.file.TFile;

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
            String workItemsAction, String emptyWorkspaceAction, List<ChartForMetric> metricsToDisplay)
    {
        super(reportDirectory, architectureViolationsAction, unassignedTypesAction, cyclicElementsAction, thresholdViolationsAction,
                architectureWarningsAction, workspaceWarningsAction, workItemsAction, emptyWorkspaceAction, metricsToDisplay);

        assert (reportName != null) && (reportName.length() > 0) : "Parameter 'sonargraphReportName' of method 'SonargraphReportAnalyzer' must not be empty";
        this.reportName = reportName;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
        assert listener != null : "Parameter 'listener' of method 'perform' must not be null";
        logExecutionStart(build, listener, SonargraphReportAnalyzer.class);
        String absoluteReportFolder = new TFile(build.getWorkspace().getRemote(), getReportDirectory()).getNormalizedAbsolutePath();
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
