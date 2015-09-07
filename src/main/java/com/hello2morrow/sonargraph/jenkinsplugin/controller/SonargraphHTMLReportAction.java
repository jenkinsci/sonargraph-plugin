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
