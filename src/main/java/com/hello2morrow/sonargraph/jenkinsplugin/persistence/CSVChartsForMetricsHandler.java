package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.ChartForMetric;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileWriter;

public class CSVChartsForMetricsHandler
{
    public void writeChartsForMetrics(TFile chartsForMetricsFile, List<ChartForMetric> chartForMetrics) throws IOException
    {
        assert chartForMetrics != null : "Parameter 'chartForMetrics' of method 'writeChartsForMetrics' must not be null";

        TFileWriter fileWriter = new TFileWriter(chartsForMetricsFile, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuilder content = new StringBuilder();

        for (ChartForMetric chartForMetric : chartForMetrics)
        {
            content.append(chartForMetric.getMetricName());
            content.append(StringUtility.CSV_SEPARATOR);
        }
        content.deleteCharAt(content.length() - 1);
        bufferedWriter.write(content.toString());
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
