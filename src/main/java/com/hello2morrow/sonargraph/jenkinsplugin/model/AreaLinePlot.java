package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Object that allows to generate charts with discrete values in the X-axis.
 * @author esteban
 *
 */
public class AreaLinePlot extends AbstractPlot
{

    public AreaLinePlot(IMetricHistoryProvider datasetProvider)
    {
        super(datasetProvider);
    }

    @Override
    protected JFreeChart createChartInternal(String chartTitle, String categoryName, String yAxisName, CategoryDataset dataset)
    {
        return ChartFactory.createAreaChart(chartTitle, categoryName, yAxisName, dataset, PlotOrientation.VERTICAL, true, false, false);
    }

    @Override
    protected void applyRendering(CategoryPlot plot)
    {
        plot.setRangeGridlinePaint(Color.white);
        AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, DATA_COLOR);
    }
}
