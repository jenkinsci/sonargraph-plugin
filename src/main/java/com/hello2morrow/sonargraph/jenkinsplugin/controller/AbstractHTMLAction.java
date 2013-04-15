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

import de.schlichtherle.truezip.file.TFile;

public abstract class AbstractHTMLAction implements Action
{

    protected void enableDirectoryBrowserSupport(StaplerRequest req, StaplerResponse rsp, String directoryToServe) throws IOException,
            ServletException
    {
        DirectoryBrowserSupport directoryBrowser = new DirectoryBrowserSupport(this, new FilePath(new TFile(directoryToServe)), this.getDisplayName()
                + "html2", "graph.gif", false);
        directoryBrowser.generateResponse(req, rsp, this);
    }

    protected String readHTMLReport(String pathToReport) throws IOException
    {
        SonargraphLogger.INSTANCE.log(Level.INFO, "Reading Sonargraph HTML Report from '" + pathToReport + "'");
        TextFileReader fileReader = new TextFileReader();
        String htmlReport = null;
        TFile htmlFile = new TFile(pathToReport);

        if (htmlFile.exists())
        {
            htmlReport = fileReader.readLargeTextFile(htmlFile.getAbsolutePath());
        }
        else
        {
            htmlReport = "Unable to read Sonargraph HTML report.";
        }

        return htmlReport;
    }

    public abstract void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException;

    public abstract String getHTMLReport() throws IOException;
}
