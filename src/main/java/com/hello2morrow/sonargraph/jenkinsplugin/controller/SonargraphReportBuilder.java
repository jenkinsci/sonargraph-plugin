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
import hudson.Launcher.ProcStarter;
import hudson.Proc;
import hudson.maven.MavenModuleSet;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Maven.MavenInstallation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.RecorderLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphProductType;

/**
 * This class contains all the functionality of the build step.
 * 
 * @author esteban
 * 
 */
public class SonargraphReportBuilder extends AbstractSonargraphRecorder
{
    private static final String PROPERTY_PREFIX = " -Dsonargraph.";
    private static final String SONARGRAPH_REPORT_FILE_NAME = "sonargraph-report";

    private static final String MAVEN3_EXECUTABLE_NAME = "mvn";

    private static final String GROUP_ID = "com.hello2morrow.sonargraph";
    private static final String ARTIFACT_ID = "maven-sonargraph-plugin";
    private static final String ARCHITECT_GOAL = "architect-report";
    private static final String QUALITY_GOAL = "quality-report-direct-parsing-mode";

    private final String mavenInstallation;
    private final String systemFile;
    private final String useSonargraphWorkspace;
    private final String prepareForSonar;

    /**
     * Constructor. Fields in the config.jelly must match the parameters in this
     * constructor.
     */
    @DataBoundConstructor
    public SonargraphReportBuilder(String mavenInstallation, String systemFile, String reportDirectory, String useSonargraphWorkspace,
            String prepareForSonar, String architectureViolationsAction, String unassignedTypesAction, String cyclicElementsAction,
            String thresholdViolationsAction, String architectureWarningsAction, String workspaceWarningsAction, String workItemsAction,
            String emptyWorkspaceAction, String replaceDefaultMetrics, List<ChartForMetric> additionalMetricsToDisplay)
    {
        super(reportDirectory, architectureViolationsAction, unassignedTypesAction, cyclicElementsAction, thresholdViolationsAction,
                architectureWarningsAction, workspaceWarningsAction, workItemsAction, emptyWorkspaceAction, replaceDefaultMetrics,
                additionalMetricsToDisplay);

        this.mavenInstallation = mavenInstallation;
        this.systemFile = systemFile;
        this.useSonargraphWorkspace = useSonargraphWorkspace;
        this.prepareForSonar = prepareForSonar;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
        super.logExecutionStart(build, listener, SonargraphReportBuilder.class);

        AbstractProject<? extends AbstractProject<?, ?>, ? extends AbstractBuild<?, ?>> project = build.getProject();
        String pathToPom = null;
        if (project instanceof Project<?, ?>)
        {
            List<Builder> builders = ((Project<?, ?>) project).getBuilders();
            Maven mavenBuilder = null;
            for (Builder builder : builders)
            {
                if (builder instanceof Maven)
                {
                    mavenBuilder = (Maven) builder;
                    break;
                }
            }
            if (mavenBuilder != null)
            {
                pathToPom = mavenBuilder.pom;
            }
            else
            {
                RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE, "Sonargraph was not able to find a maven based build step.");
                return false;
            }
        }
        else if (project instanceof MavenModuleSet)
        {
            pathToPom = ((MavenModuleSet) project).getRootPOM(null);
        }

        FilePath absoluteReportDir = new FilePath(build.getWorkspace(), getReportDirectory());
        String mvnCommand = createMvnCommand(launcher, build, pathToPom, getDescriptor(), listener);

        ProcStarter procStarter = launcher.new ProcStarter();
        procStarter.cmdAsSingleString(mvnCommand);
        procStarter.stdout(listener.getLogger());
        procStarter = procStarter.pwd(build.getWorkspace());
        Proc proc = launcher.launch(procStarter);
        int processExitCode = proc.join();

        if (processExitCode != 0)
        {
            RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE,
                    "There was an error when executing Sonargraph's Maven goal. Check the global configuration"
                            + " parameters and the relative paths to make sure that everything is in place.");
            return false;
        }

        if (!super.processMetricsForCharts(build))
        {
            RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE,
                    "There was an error trying to save the configuration of metrics to be displayed in charts");
            return false;
        }

        FilePath sonargraphReportDirectory = absoluteReportDir;
        if (super.processSonargraphReport(build, sonargraphReportDirectory, SONARGRAPH_REPORT_FILE_NAME, listener.getLogger()))
        {
            //only add the actions after the processing has been successful
            addActions(build);
        }

        /*
         * Must return true for jenkins to mark the build as SUCCESS. Only then,
         * it can be downgraded to the result that was set but it can never be
         * upgraded.
         */
        return true;
    }

    private String createMvnCommand(Launcher launcher, AbstractBuild<?, ?> build, String pomPath, DescriptorImpl descriptor, BuildListener listener)
            throws IOException, InterruptedException
    {
        String pathToMvn = getMavenExecutable(launcher, build, listener);

        FilePath workspacePath = build.getWorkspace();
        FilePath absoluteReportDir = new FilePath(workspacePath, getReportDirectory());

        StringBuilder mvnCommand = new StringBuilder(pathToMvn);

        if (pomPath != null && !pomPath.isEmpty())
        {
            FilePath pomFile = new FilePath(workspacePath, pomPath);
            if (!pomFile.exists())
            {
                pomFile = new FilePath(workspacePath, pomPath);
            }
            mvnCommand.append(" -f ");
            mvnCommand.append(escapePath(pomFile.getRemote()));
        }

        // FIXME: Why are some modules not found if goal is run on multi-module
        // projects? "package" solves this at the cost of extra time needed.
        mvnCommand.append(" package -Dmaven.test.skip=true");

        mvnCommand.append(" ").append(GROUP_ID).append(":").append(ARTIFACT_ID).append(":").append(descriptor.getVersion()).append(":");
        if (descriptor.getProductType().equals(SonargraphProductType.ARCHITECT.getId()))
        {
            mvnCommand.append(ARCHITECT_GOAL);
        }
        else
        {
            mvnCommand.append(QUALITY_GOAL);
        }

        if ((systemFile != null) && (systemFile.length() > 0))
        {
            FilePath sonargraphFile = new FilePath(workspacePath, systemFile);
            if (!sonargraphFile.exists())
            {
                sonargraphFile = new FilePath(workspacePath, systemFile);
            }
            if (!sonargraphFile.exists())
            {
                RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE,
                        "Specified Sonargraph system file '" + sonargraphFile.getRemote() + "' does not exist!");
            }
            mvnCommand.append(PROPERTY_PREFIX).append("file=").append(escapePath(sonargraphFile.getRemote()));
        }

        if ((descriptor.getLicense() != null) && (descriptor.getLicense().length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("license=").append(escapePath(descriptor.getLicense()));
        }
        else if ((descriptor.getActivationCode() != null) && (descriptor.getActivationCode().length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("activationCode=").append(descriptor.getActivationCode());
        }
        else
        {
            RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE, "You have to either specify a license file or activation code!");
        }
        mvnCommand.append(PROPERTY_PREFIX).append("prepareForJenkins=true");
        mvnCommand.append(PROPERTY_PREFIX).append("reportDirectory=").append(escapePath(absoluteReportDir.getRemote()));
        mvnCommand.append(PROPERTY_PREFIX).append("reportName=").append(SONARGRAPH_REPORT_FILE_NAME);
        mvnCommand.append(PROPERTY_PREFIX).append("reportType=HTML");
        if ((systemFile != null) && (systemFile.length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("useSonargraphWorkspace=").append(useSonargraphWorkspace);
        }
        mvnCommand.append(PROPERTY_PREFIX).append("prepareForSonar=").append(prepareForSonar);
        return mvnCommand.toString();
    }

    private String getMavenExecutable(Launcher launcher, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException
    {
        String pathToMvn = null;

        MavenInstallation[] installations = Jenkins.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations();

        SonargraphLogger.INSTANCE.log(Level.FINE, "There are " + installations.length + " Maven installations on "
                + Computer.currentComputer().getDisplayName());
        for (MavenInstallation installation : installations)
        {
            MavenInstallation translatedInstallation = installation.forNode(Computer.currentComputer().getNode(), listener);
            translatedInstallation = translatedInstallation.forEnvironment(build.getEnvironment(listener));
            SonargraphLogger.INSTANCE.log(Level.FINE, "Maven installation " + translatedInstallation.getName() + " with home "
                    + translatedInstallation.getHome());
            if (installation.getName().equals(mavenInstallation))
            {
                pathToMvn = translatedInstallation.getExecutable(launcher);
                SonargraphLogger.INSTANCE.log(Level.FINE, "Using Maven installation " + translatedInstallation.getName() + " with home "
                        + translatedInstallation.getHome());
                break;
            }
        }

        if (pathToMvn == null)
        {
            pathToMvn = MAVEN3_EXECUTABLE_NAME;
            SonargraphLogger.INSTANCE.log(Level.WARNING, "Can't get path to maven installation '" + mavenInstallation
                    + "', using command 'mvn' without path.");
        }
        return pathToMvn;
    }

    private String escapePath(String path)
    {
        return "\"" + path + "\"";
    }

    public String getMavenInstallation()
    {
        return mavenInstallation;
    }

    public String getSystemFile()
    {
        return systemFile;
    }

    public String getUseSonargraphWorkspace()
    {
        return useSonargraphWorkspace;
    }

    public String getPrepareForSonar()
    {
        return prepareForSonar;
    }

    @Override
    public DescriptorImpl getDescriptor()
    {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends AbstractBuildStepDescriptor
    {
        /** Absolute path to the Sonargraph license file. */
        private String license;

        private String activationCode;

        /** Installed version of Sonargraph. */
        private String version;

        /** Either SonargraphArchitect or SonargraphQuality **/
        private String productType;

        public DescriptorImpl()
        {
            super();
            load();
        }

        @Override
        public String getDisplayName()
        {
            return ConfigParameters.REPORT_BUILDER_DISPLAY_NAME.getValue();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException
        {
            productType = formData.getString("productType").trim();
            version = formData.getString("version").trim();
            license = formData.getString("license").trim();
            activationCode = formData.getString("activationCode").trim();

            save();
            return super.configure(req, formData);
        }

        public String getProductType()
        {
            return productType;
        }

        public String getVersion()
        {
            return version;
        }

        public String getLicense()
        {
            return license;
        }

        public String getActivationCode()
        {
            return activationCode;
        }

        public String getUserHome()
        {
            return System.getProperty("user.home");
        }

        public FormValidation doCheckVersion(@QueryParameter String value)
        {
            return StringUtility.validateNotNullAndRegexp(value.trim(), "^7.(\\d+\\.)\\d+$") ? FormValidation.ok() : FormValidation
                    .error("Please enter a valid version. Format '7.x.y', starting from '7.1.9'");
        }

        public FormValidation doCheckLicense(@QueryParameter String value)
        {
            boolean hasLicenseCorrectExtension = StringUtility.validateNotNullAndRegexp(value, "([a-zA-Z]:\\\\)?([\\/\\\\a-zA-Z0-9_.-]+)+.license$");
            if (!hasLicenseCorrectExtension)
            {
                return FormValidation.error("Please enter a valid path for the license");
            }

            File licenseFile = new File(value);
            if (!licenseFile.exists())
            {
                return FormValidation.error("Please specify a path of an existing license file");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSystemFile(@QueryParameter String value)
        {
            if ((value == null) || (value.length() == 0))
            {
                return FormValidation.ok();
            }
            return StringUtility.validateNotNullAndRegexp(value, "([a-zA-Z]:\\\\)?[\\/\\\\a-zA-Z0-9_.-]+.sonargraph$") ? FormValidation.ok()
                    : FormValidation.error("Please enter a valid system file");

        }

        public ListBoxModel doFillMavenInstallationItems()
        {
            ListBoxModel items = new ListBoxModel();
            MavenInstallation[] installations = Jenkins.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations();
            for (MavenInstallation installation : installations)
            {
                // use name as value instead of home because home is node dependent and must be resolved later
                items.add(installation.getName(), installation.getName());
            }
            return items;
        }

        public ListBoxModel doFillProductTypeItems()
        {
            ListBoxModel items = new ListBoxModel();
            for (SonargraphProductType productType : SonargraphProductType.values())
            {
                items.add(productType.getDisplayName(), productType.getId());
            }
            return items;
        }
    }
}
