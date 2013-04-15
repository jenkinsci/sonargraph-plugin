package com.hello2morrow.sonargraph.jenkinsplugin.controller.util;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

import com.hello2morrow.sonargraph.jenkinsplugin.model.AbstractPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.AreaLinePlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.BarPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.DiscreteLinePlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.CSVFileHandler;

import de.schlichtherle.truezip.file.TFile;

public class ChartTestUtil
{
    private static final String BUILD = "Build";

    public void testBarChart(String csvPath)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new TFile(csvPath));
        BarPlot plot = new BarPlot(csvFileHandler);
        createChart(plot, "Bar Chart");
    }

    public void testDiscreteLineChart(String csvPath)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new TFile(csvPath));
        DiscreteLinePlot plot = new DiscreteLinePlot(csvFileHandler);
        createChart(plot, "Line Chart");
    }

    public void testAreaChart(String csvPath)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new TFile(csvPath));
        AreaLinePlot plot = new AreaLinePlot(csvFileHandler);
        createChart(plot, "Area Chart");
    }

    private void createChart(AbstractPlot plot, String title)
    {
        JFreeChart chart = plot.createChart(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, BUILD);
        ChartFrame frame1 = new ChartFrame(title, chart);
        frame1.setVisible(true);
        frame1.setSize(400, 350);
    }
}
