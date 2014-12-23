package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.QueryParameter;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public abstract class AbstractBuildStepDescriptor extends BuildStepDescriptor<Publisher>
{
    public AbstractBuildStepDescriptor()
    {
        super();
        load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType)
    {
        return true;
    }

    public ListBoxModel doFillArchitectureViolationsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillUnassignedTypesActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillCyclicElementsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillThresholdViolationsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillArchitectureWarningsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillWorkspaceWarningsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillWorkItemsActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillEmptyWorkspaceActionItems()
    {
        return createListWithActions();
    }

    public ListBoxModel doFillmetricsToDisplay()
    {
        ListBoxModel items = new ListBoxModel();
        for (SonargraphMetrics metric : SonargraphMetrics.values())
        {
            items.add(metric.getDescription(), metric.getStandardName());
        }
        return items;
    }

    private ListBoxModel createListWithActions()
    {
        ListBoxModel items = new ListBoxModel();
        for (BuildActionsEnum action : BuildActionsEnum.values())
        {
            items.add(action.getActionName(), action.getActionCode());
        }
        return items;
    }

    public FormValidation doCheckReportDirectory(@QueryParameter
    String value)
    {
        return StringUtility.validateNotNullAndRegexp(value, "[\\/\\\\a-zA-Z0-9_.-]+") ? FormValidation.ok() : FormValidation
                .error("Please enter a valid path for the report directory");
    }
}
