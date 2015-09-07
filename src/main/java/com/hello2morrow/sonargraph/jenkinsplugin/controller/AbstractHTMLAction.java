package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.TextFileReader;

public abstract class AbstractHTMLAction implements Action
{

    protected void enableDirectoryBrowserSupport(StaplerRequest req, StaplerResponse rsp, FilePath directoryToServe) throws IOException,
            ServletException
    {
        DirectoryBrowserSupport directoryBrowser = new DirectoryBrowserSupport(this, directoryToServe, this.getDisplayName() + "html2", "graph.gif",
                false);
        directoryBrowser.generateResponse(req, rsp, this);
    }

    protected String readHTMLReport(FilePath pathToReport) throws IOException, InterruptedException
    {
        SonargraphLogger.INSTANCE.log(Level.INFO, "Reading Sonargraph HTML Report from '" + pathToReport + "'");
        TextFileReader fileReader = new TextFileReader();
        String htmlReport = null;

        if (pathToReport.exists())
        {
            htmlReport = fileReader.readLargeTextFile(pathToReport);
        }
        else
        {
            SonargraphLogger.INSTANCE.log(Level.INFO, "Unable to read Sonargraph HTML report from '" + pathToReport + "'");
            htmlReport = "Unable to read Sonargraph HTML report.";
        }

        return htmlReport;
    }

    public abstract void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException;

    public abstract String getHTMLReport() throws IOException, InterruptedException;
}
