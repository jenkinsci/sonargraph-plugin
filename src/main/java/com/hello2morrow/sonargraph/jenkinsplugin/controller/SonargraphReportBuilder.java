package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.Extension;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Proc;
import hudson.maven.MavenModuleSet;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Maven.MavenInstallation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.RecorderLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.ProductVersion;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphProductType;

import de.schlichtherle.truezip.file.TFile;

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

    private final String mavenInstallation;
    private final String systemFile;
    private final String useSonargraphWorkspace;
    private final String prepareForSonar;

    private static final String GROUP_ID = "com.hello2morrow.sonargraph";
    private static final String ARTIFACT_ID = "maven-sonargraph-plugin";
    private static final String ARCHITECT_GOAL = "architect-report";
    private static final String QUALITY_GOAL = "quality-report-direct-parsing-mode";

    private final String pathToExecutable = "/bin";
    private static final String M2_HOME = "M2_HOME";

    /**
     * Constructor. Fields in the config.jelly must match the parameters in this
     * constructor.
     */
    @DataBoundConstructor
    public SonargraphReportBuilder(String mavenInstallation, String systemFile, String reportDirectory, String useSonargraphWorkspace,
            String prepareForSonar, String architectureViolationsAction, String unassignedTypesAction, String cyclicElementsAction,
            String thresholdViolationsAction, String architectureWarningsAction, String workspaceWarningsAction, String workItemsAction,
            String emptyWorkspaceAction)
    {
        super(reportDirectory, architectureViolationsAction, unassignedTypesAction, cyclicElementsAction, thresholdViolationsAction,
                architectureWarningsAction, workspaceWarningsAction, workItemsAction, emptyWorkspaceAction);

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

        String absoluteReportDir = new TFile(build.getWorkspace().getRemote(), getReportDirectory()).getNormalizedAbsolutePath();
        String mvnCommand = createMvnCommand(build.getWorkspace().getRemote(), pathToPom, System.getProperty("os.name", "unknown").trim()
                .toLowerCase(), getDescriptor(), listener.getLogger());

        ProcStarter procStarter = launcher.new ProcStarter();
        HashMap<String, String> envVars = new HashMap<String, String>();
        procStarter.cmdAsSingleString(mvnCommand);
        procStarter.stdout(listener.getLogger());
        procStarter = procStarter.pwd(build.getWorkspace()).envs(build.getEnvironment(listener));
        envVars.put(M2_HOME, mavenInstallation);
        procStarter.envs(envVars);
        Proc proc = launcher.launch(procStarter);
        int processExitCode = proc.join();

        if (processExitCode != 0)
        {
            RecorderLogger.logToConsoleOutput(listener.getLogger(), Level.SEVERE,
                    "There was an error when executing Sonargraph's Maven goal. Check the global configuration"
                            + " parameters and the relative paths to make sure that everything is in place.");
            return false;
        }

        String sonargraphReportDirectory = new TFile(absoluteReportDir).getAbsolutePath();
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

    private String createMvnCommand(String workspacePath, String pomPath, String operatingSystem, DescriptorImpl descriptor, PrintStream logger)
    {
        String absoluteReportDir = new TFile(workspacePath, getReportDirectory()).getNormalizedAbsolutePath();
        String pathToMvn = null;

        if ((mavenInstallation != null) && !mavenInstallation.equals("\"null\""))
        {
            pathToMvn = new TFile(mavenInstallation + pathToExecutable, "mvn").getAbsolutePath();
        }
        else
        {
            if (mavenInstallation.equals("\"null\""))
            {
                SonargraphLogger.INSTANCE.log(Level.WARNING, "Invalid path to maven installation '" + mavenInstallation
                        + "' configured, using command 'mvn' without path.");
            }
            pathToMvn = "mvn";
        }

        StringBuilder mvnCommand = new StringBuilder(pathToMvn);
        if (operatingSystem.startsWith("windows"))
        {
            mvnCommand.append(".bat");
        }

        if (pomPath != null && !pomPath.isEmpty())
        {
            String absolutePathToPom = new TFile(workspacePath, pomPath).getNormalizedAbsolutePath();
            mvnCommand.append(" -f " + absolutePathToPom);
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
            TFile sonargraphFile = new TFile(workspacePath, systemFile);
            if (!sonargraphFile.exists())
            {
                RecorderLogger.logToConsoleOutput(logger, Level.SEVERE,
                        "Specified Sonargraph system file '" + sonargraphFile.getNormalizedAbsolutePath() + "' does not exist!");
            }
            mvnCommand.append(PROPERTY_PREFIX).append("file=").append(sonargraphFile.getNormalizedAbsolutePath());
        }

        if ((descriptor.getLicense() != null) && (descriptor.getLicense().length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("license=").append(descriptor.getLicense());
        }
        else if ((descriptor.getActivationCode() != null) && (descriptor.getActivationCode().length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("activationCode=").append(descriptor.getActivationCode());
        }
        else
        {
            RecorderLogger.logToConsoleOutput(logger, Level.SEVERE, "You have to either specify a license file or activation code!");
        }
        mvnCommand.append(PROPERTY_PREFIX).append("prepareForJenkins=true");
        mvnCommand.append(PROPERTY_PREFIX).append("reportDirectory=").append(absoluteReportDir);
        mvnCommand.append(PROPERTY_PREFIX).append("reportName=").append(SONARGRAPH_REPORT_FILE_NAME);
        mvnCommand.append(PROPERTY_PREFIX).append("reportType=HTML");
        if ((systemFile != null) && (systemFile.length() > 0))
        {
            mvnCommand.append(PROPERTY_PREFIX).append("useSonargraphWorkspace=").append(useSonargraphWorkspace);
        }
        mvnCommand.append(PROPERTY_PREFIX).append("prepareForSonar=").append(prepareForSonar);
        return mvnCommand.toString();
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
            productType = formData.getString("productType");
            version = formData.getString("version");
            license = formData.getString("license");
            activationCode = formData.getString("activationCode");

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

        public FormValidation doCheckVersion(@QueryParameter
        String value)
        {
            return StringUtility.validateNotNullAndRegexp(value, "^(\\d+\\.)+\\d+$") ? FormValidation.ok() : FormValidation
                    .error("Please enter a valid version");
        }

        public FormValidation doCheckLicense(@QueryParameter
        String value)
        {
            boolean hasLicenseCorrectExtension = StringUtility.validateNotNullAndRegexp(value, "([a-zA-Z]:\\\\)?[\\/\\\\a-zA-Z0-9_.-]+.license$");
            if (!hasLicenseCorrectExtension)
            {
                return FormValidation.error("Please enter a valid path for the license");
            }

            TFile licenseFile = new TFile(value);
            if (!licenseFile.exists())
            {
                return FormValidation.error("Please specify a path of an existing license file");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSystemFile(@QueryParameter
        String value)
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
            MavenInstallation[] installations = Hudson.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations();
            for (MavenInstallation installation : installations)
            {
                items.add(installation.getName(), installation.getHome());
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

        public ListBoxModel doFillVersionItems()
        {
            ListBoxModel items = new ListBoxModel();
            for (ProductVersion version : ProductVersion.values())
            {
                items.add(version.getId(), version.getId());
            }
            return items;
        }
    }
}
