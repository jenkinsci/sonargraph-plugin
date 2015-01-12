package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.DataBoundConstructor;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public class ChartForMetric extends AbstractDescribableImpl<ChartForMetric>
{
    private final String metricName;

    /**
     * Creates a new instance of {@link ChartForMetric}.
     *
     * @param metricName
     *            the name of the metric to use
     */
    @DataBoundConstructor
    public ChartForMetric(final String metricName)
    {
        super();
        this.metricName = metricName;
    }

    /**
     * Returns the name of the metric.
     *
     */
    public String getMetricName()
    {
        return metricName;
    }

    /**
     * Dummy descriptor for {@link ChartForMetric}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ChartForMetric>
    {
        /**
         * Returns the available metrics. These values will be shown in the list
         * box of the config.jelly view part (ChartForMetric/config.jelly).
         */
        public ListBoxModel doFillMetricNameItems()
        {
            ListBoxModel items = new ListBoxModel();
            for (SonargraphMetrics metric : SonargraphMetrics.getAvailableMetrics())
            {
                items.add(metric.getDescription(), metric.getStandardName());
            }
            return items;
        }

        @Override
        public String getDisplayName()
        {
            return StringUtility.EMPTY_STRING;
        }
    }
}
