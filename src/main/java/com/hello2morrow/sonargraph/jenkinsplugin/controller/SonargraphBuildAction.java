package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.model.AbstractBuild;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

import de.schlichtherle.truezip.file.TFile;

public class SonargraphBuildAction extends AbstractHTMLAction
{
    private AbstractBuild<?, ?> build;

    /**
     * Recorder seems to be needed to provide the link between the recorder and action.
     * TODO: Verify this assumption
     */
    private AbstractSonargraphRecorder recorder;

    public SonargraphBuildAction(AbstractBuild<?, ?> build, AbstractSonargraphRecorder recorder)
    {
        this.build = build;
        this.recorder = recorder;
    }

    @Override
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        String reportHistoryDir = new TFile(build.getProject().getRootDir(), ConfigParameters.REPORT_HISTORY_FOLDER.getValue()).getAbsolutePath();
        enableDirectoryBrowserSupport(req, rsp, new TFile(reportHistoryDir, "sonargraph-report-build-" + build.getNumber()).getAbsolutePath());
    }

    public AbstractBuild<?, ?> getBuild()
    {
        return build;
    }

    public String getIconFileName()
    {
        return ConfigParameters.SONARGRAPH_ICON.getValue();
    }

    public String getDisplayName()
    {
        return ConfigParameters.ACTION_DISPLAY_NAME.getValue();
    }

    public String getUrlName()
    {
        return "html-report";
    }

    @Override
    public String getHTMLReport() throws IOException
    {
        String projectPath = new TFile(build.getProject().getRootDir()).getAbsolutePath();
        String reportHistoryFolderAbsolutePath = new TFile(projectPath, ConfigParameters.REPORT_HISTORY_FOLDER.getValue()).getAbsolutePath();
        String buildReportFolderAbsolutePath = new TFile(reportHistoryFolderAbsolutePath, "sonargraph-report-build-" + build.getNumber())
                .getAbsolutePath();
        String reportFileName = recorder instanceof SonargraphReportAnalyzer ? ((SonargraphReportAnalyzer) recorder).getReportName()
                : ConfigParameters.SONARGRAPH_HTML_REPORT_FILE_NAME.getValue();
        TFile htmlFile = new TFile(buildReportFolderAbsolutePath, StringUtility.replaceXMLWithHTMLExtension(reportFileName));
        return readHTMLReport(htmlFile.getAbsolutePath());
    }
}
