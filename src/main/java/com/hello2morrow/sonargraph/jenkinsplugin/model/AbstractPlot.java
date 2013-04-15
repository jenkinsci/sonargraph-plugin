package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;

public abstract class AbstractPlot
{
    private static final Color BACK_GROUND_COLOR = Color.WHITE;
    private static final Color GRID_LINE_COLOR = Color.BLACK;
    private static final String TREND = "Trend";
    private static final int TRUNCATE_DATA_THRESHOLD = 12;
    protected static final Paint DATA_COLOR = new Color(0, 179, 0);

    protected IMetricHistoryProvider m_datasetProvider;

    public AbstractPlot(IMetricHistoryProvider datasetProvider)
    {
        super();
        assert datasetProvider != null : "Parameter 'datasetProvider' of method 'DiscreteLinePlot' must not be null";
        m_datasetProvider = datasetProvider;
    }

    /**
     * Creates a Dataset from a CSV file.
     * @param lineName Name of the line that will be drawn in the chart.
     */
    protected final CategoryDataset createDataset(String lineName, SonargraphMetrics metric) throws IOException
    {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        HashMap<Integer, Double> dataset = m_datasetProvider.readMetrics(metric);
        List<Integer> buildNumbers = new ArrayList<Integer>();
        buildNumbers.addAll(dataset.keySet());
        Collections.sort(buildNumbers);
        SonargraphLogger.INSTANCE.fine(buildNumbers.size() + " data points found for metric '" + metric.getStandardName() + "' in file '"
                + m_datasetProvider.getStorageName() + "'");

        if (buildNumbers.size() > TRUNCATE_DATA_THRESHOLD)
        {
            buildNumbers = buildNumbers.subList(buildNumbers.size() - TRUNCATE_DATA_THRESHOLD, buildNumbers.size());
        }
        for (Integer buildNumber : buildNumbers)
        {
            result.addValue(dataset.get(buildNumber), lineName, buildNumber);
        }

        return result;
    }

    /**
     * Creates a chart for a Sonargraph metric 
     * @param categoryName Name for the X-Axis, representing a category
     * @return Chart built with the given parameters.
     */
    public final JFreeChart createChart(SonargraphMetrics metric, String categoryName)
    {
        CategoryDataset dataset = null;
        try
        {
            dataset = createDataset(TREND, metric);
        }
        catch (IOException ioe)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "Failed to read metrics from data file '" + m_datasetProvider.getStorageName() + "'");
        }
        JFreeChart chart = createChartInternal(metric.getDescription(), categoryName, metric.getShortDescription(), dataset);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        if (dataset == null)
        {
            plot.setNoDataMessage("There was an error loading data for metric '" + metric.getShortDescription() + "'");
        }
        else if (dataset.getColumnCount() == 0)
        {
            plot.setNoDataMessage("No data found for metric '" + metric.getShortDescription() + "'");
        }

        if ((dataset == null) || (dataset.getColumnCount() == 0))
        {
            plot.setNoDataMessagePaint(Color.RED);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
        }
        applyRendering(plot);
        setRangeAxis(metric.isNaturalNumber(), plot);
        applyStandardPlotColors(plot);
        chart.removeLegend();
        return chart;
    }

    protected abstract JFreeChart createChartInternal(String chartTitle, String categoryName, String yAxisName, CategoryDataset dataset);

    protected abstract void applyRendering(CategoryPlot plot);

    private void setRangeAxis(boolean hideDecimals, CategoryPlot plot)
    {
        if (hideDecimals)
        {
            plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }
    }

    private void applyStandardPlotColors(CategoryPlot plot)
    {
        plot.setBackgroundPaint(BACK_GROUND_COLOR);
        plot.setDomainGridlinePaint(GRID_LINE_COLOR);
        plot.setRangeGridlinePaint(GRID_LINE_COLOR);
    }

}