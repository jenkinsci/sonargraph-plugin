package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.model.Project;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

import de.schlichtherle.truezip.file.TFile;

public class SonargraphHTMLReportAction extends InvisibleFromSidebarAction
{
    /** Project or build that is calling this action. */
    private final Project<?, ?> project;

    /** Object that defines the post-buld step asociated with this action. */
    private final AbstractSonargraphRecorder builder;

    public SonargraphHTMLReportAction(Project<?, ?> project, AbstractSonargraphRecorder builder)
    {
        this.project = project;
        this.builder = builder;
    }

    public Project<?, ?> getProject()
    {
        return project;
    }

    public String getUrlName()
    {
        return ConfigParameters.HTML_REPORT_ACTION_URL.getValue();
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        enableDirectoryBrowserSupport(req, rsp, new TFile(project.getWorkspace().getRemote(), builder.getReportDirectory()).getAbsolutePath());
    }

    public String getHTMLReport() throws IOException
    {
        String reportRelativePath = builder.getReportDirectory();
        String reportFolderAbsouletPath = new TFile(project.getWorkspace().getRemote(), reportRelativePath).getNormalizedAbsolutePath();
        String reportFileName = builder instanceof SonargraphReportAnalyzer ? ((SonargraphReportAnalyzer) builder).getReportName()
                : ConfigParameters.SONARGRAPH_HTML_REPORT_FILE_NAME.getValue();
        TFile htmlFile = new TFile(reportFolderAbsouletPath, StringUtility.replaceXMLWithHTMLExtension(reportFileName));

        return readHTMLReport(htmlFile.getAbsolutePath());
    }
}
