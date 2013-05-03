package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public class ChartTest
{
    private static final String CSV_FILE_NAME = "./src/test/resources/sonargraph.csv";
    private static final int MAX_NUMBER_OF_DATA_POINTS = 30;

    public static void main(String[] args)
    {
        ChartTestUtil chartUtil = new ChartTestUtil();
        chartUtil.testTimeSeriesChart(CSV_FILE_NAME, SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, MAX_NUMBER_OF_DATA_POINTS);
    }
}
