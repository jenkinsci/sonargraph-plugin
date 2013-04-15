package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.io.IOException;
import java.util.HashMap;

public interface IMetricHistoryProvider
{
    /**
     * @return HashMap with the CSV file values.
     */
    public HashMap<Integer, Integer> readMetrics() throws IOException;

    /**
     * @return HashMap with the CSV file values of a specific metric.
     */
    public HashMap<Integer, Double> readMetrics(SonargraphMetrics metric) throws IOException;

    /**
     * Appends a sonargraph metric for a specific build.
     * @param buildNumber Number of the build where the metric was gathered
     * @param metricValue Value of the metric
     */
    public void writeMetric(Integer buildNumber, Integer metricValue) throws IOException;

    /**
     * Appends all supported metrics for a specific build.
     * @param buildNumber Number of the build where the metric was gathered
     * @param metricValues Ordered map containing the supported metrics and their values for the current build.
     */
    public void writeMetrics(Integer buildNumber, HashMap<SonargraphMetrics, String> metricValues) throws IOException;

    public String getStorageName();
}