package com.hello2morrow.sonargraph.jenkinsplugin.model;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ShapeUtilities;

/**
 * Object that allows to generate charts with discrete values in the X-axis.
 * @author esteban
 *
 */
public class DiscreteLinePlot extends AbstractPlot
{

    public DiscreteLinePlot(IMetricHistoryProvider datasetProvider)
    {
        super(datasetProvider);
    }

    @Override
    protected JFreeChart createChartInternal(String chartTitle, String categoryName, String yAxisName, CategoryDataset dataset)
    {
        return ChartFactory.createLineChart(chartTitle, categoryName, yAxisName, dataset, PlotOrientation.VERTICAL, true, false, false);
    }

    @Override
    protected void applyRendering(CategoryPlot plot)
    {
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, ShapeUtilities.createDiamond(4F));
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        renderer.setSeriesFillPaint(0, DATA_COLOR);
        renderer.setSeriesPaint(0, DATA_COLOR);
    }
}
