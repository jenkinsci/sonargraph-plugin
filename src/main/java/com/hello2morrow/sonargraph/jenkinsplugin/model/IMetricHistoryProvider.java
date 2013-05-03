package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IMetricHistoryProvider
{
    /**
     * @return List of data points for a specific metric.
     */
    public List<IDataPoint> readMetricValues(SonargraphMetrics metric) throws IOException;

    /**
     * Appends all supported metrics for a specific build.
     * @param buildNumber Number of the build where the metric was gathered
     * @param timestamp when the build has been executed
     * @param metricValues map containing the supported metrics and their values for the current build.
     */
    public void writeMetricValues(Integer buildNumber, long timestamp, Map<SonargraphMetrics, String> metricValues) throws IOException;

    public String getStorageName();
}