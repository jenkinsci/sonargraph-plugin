package com.hello2morrow.sonargraph.jenkinsplugin.controller.util;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

import com.hello2morrow.sonargraph.jenkinsplugin.model.AbstractPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.TimeSeriesPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.XYLineAndShapePlot;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.CSVFileHandler;

import de.schlichtherle.truezip.file.TFile;

public class ChartTestUtil
{
    private static final String BUILD = "Build";

    public void testXYChart(String csvPath, SonargraphMetrics metric, int maximumNumberOfDataPoints)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new TFile(csvPath));
        XYLineAndShapePlot plot = new XYLineAndShapePlot(csvFileHandler);

        createChart(plot, metric, maximumNumberOfDataPoints, true);
    }

    public void testTimeSeriesChart(String csvPath, SonargraphMetrics metric, int maximumNumberOfDataPoints)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new TFile(csvPath));
        TimeSeriesPlot plot = new TimeSeriesPlot(csvFileHandler, 25);
        createChart(plot, metric, maximumNumberOfDataPoints, true);

    }

    private void createChart(AbstractPlot plot, SonargraphMetrics metric, int maxDataPoints, boolean hideLegend)
    {
        JFreeChart chart = plot.createXYChart(metric, BUILD, maxDataPoints, true);
        ChartFrame frame1 = new ChartFrame(metric.getShortDescription(), chart);
        frame1.setVisible(true);
        frame1.setSize(500, 350);
    }
}
