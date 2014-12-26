package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.ChartForMetric;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;
import de.schlichtherle.truezip.file.TFileWriter;

public class CSVChartsForMetricsHandler
{
    public void writeChartsForMetrics(TFile chartsForMetricsFile, List<ChartForMetric> chartsForMetrics) throws IOException
    {
        assert chartsForMetrics != null : "Parameter 'chartForMetrics' of method 'writeChartsForMetrics' must not be null";

        TFileWriter fileWriter = new TFileWriter(chartsForMetricsFile, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuilder content = new StringBuilder();

        for (ChartForMetric chartForMetric : chartsForMetrics)
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

    public String[] readChartsForMetrics(TFile chartsForMetricsFile) throws IOException
    {
        assert chartsForMetricsFile != null : "Parameter 'chartsForMetricsFile' of method 'readChartsForMetrics' must not be null";

        CSVReader csvReader = new CSVReader(new TFileReader(chartsForMetricsFile), StringUtility.CSV_SEPARATOR);
        String[] chartsForMetrics = csvReader.readNext();
        assert chartsForMetrics.length > 0 : "Charts for metrics expected";
        return chartsForMetrics;
    }
}
