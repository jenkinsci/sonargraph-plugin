/*******************************************************************************
 * Jenkins Sonargraph Plugin
 * Copyright (C) 2009-2015 hello2morrow GmbH
 * mailto: info AT hello2morrow DOT com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *******************************************************************************/
package com.hello2morrow.sonargraph.jenkinsplugin.controller.util;

import java.io.File;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

import com.hello2morrow.sonargraph.jenkinsplugin.model.AbstractPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.TimeSeriesPlot;
import com.hello2morrow.sonargraph.jenkinsplugin.model.XYLineAndShapePlot;
import com.hello2morrow.sonargraph.jenkinsplugin.persistence.CSVFileHandler;

public class ChartTestUtil
{
    private static final String BUILD = "Build";

    public void testXYChart(String csvPath, SonargraphMetrics metric, int maximumNumberOfDataPoints)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new File(csvPath));
        XYLineAndShapePlot plot = new XYLineAndShapePlot(csvFileHandler);

        createChart(plot, metric, maximumNumberOfDataPoints, true);
    }

    public void testTimeSeriesChart(String csvPath, SonargraphMetrics metric, int maximumNumberOfDataPoints)
    {
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(new File(csvPath));
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
