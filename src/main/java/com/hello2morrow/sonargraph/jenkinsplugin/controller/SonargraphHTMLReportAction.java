package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.FilePath;
import hudson.model.AbstractProject;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

public class SonargraphHTMLReportAction extends InvisibleFromSidebarAction
{
    /** Project or build that is calling this action. */
    private final AbstractProject<?, ?> project;

    /** Object that defines the post-build step associated with this action. */
    private final AbstractSonargraphRecorder builder;

    public SonargraphHTMLReportAction(AbstractProject<?, ?> project, AbstractSonargraphRecorder builder)
    {
        this.project = project;
        this.builder = builder;
    }

    public AbstractProject<?, ?> getProject()
    {
        return project;
    }

    public String getUrlName()
    {
        return ConfigParameters.HTML_REPORT_ACTION_URL.getValue();
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        enableDirectoryBrowserSupport(req, rsp, new FilePath(project.getSomeWorkspace(), builder.getReportDirectory()));
    }

    public String getHTMLReport() throws IOException, InterruptedException
    {
        String reportRelativePath = builder.getReportDirectory();
        FilePath reportFolderAbsouletPath = new FilePath(project.getSomeWorkspace(), reportRelativePath);
        String reportFileName = builder instanceof SonargraphReportAnalyzer ? ((SonargraphReportAnalyzer) builder).getReportName()
                : ConfigParameters.SONARGRAPH_HTML_REPORT_FILE_NAME.getValue();
        FilePath htmlFile = new FilePath(reportFolderAbsouletPath, StringUtility.replaceXMLWithHTMLExtension(reportFileName));

        return readHTMLReport(htmlFile);
    }
}
