package com.hello2morrow.sonargraph.jenkinsplugin.model;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Object that allows to generate charts with discrete values in the X-axis.
 * @author esteban
 *
 */
public class BarPlot extends AbstractPlot
{
    public BarPlot(IMetricHistoryProvider datasetProvider)
    {
        super(datasetProvider);
    }

    @Override
    public JFreeChart createChartInternal(String chartTitle, String categoryName, String yAxisName, CategoryDataset dataset)
    {
        return ChartFactory.createBarChart(chartTitle, categoryName, yAxisName, dataset, PlotOrientation.VERTICAL, true, false, false);
    }

    @Override
    protected void applyRendering(CategoryPlot plot)
    {
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setSeriesPaint(0, DATA_COLOR);
    }
}
