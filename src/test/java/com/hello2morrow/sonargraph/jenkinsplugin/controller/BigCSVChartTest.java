package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public class BigCSVChartTest
{
    private static final String CSV_LARGE_FILE_NAME = "./src/test/resources/LargeCsvFile.csv";
    private static final int NUMBER_OF_DATA_POINTS = 300;

    public static void main(String[] args)
    {
        ChartTestUtil chartUtil = new ChartTestUtil();
        chartUtil.testTimeSeriesChart(CSV_LARGE_FILE_NAME, SonargraphMetrics.STRUCTURAL_DEBT_INDEX, NUMBER_OF_DATA_POINTS);

        chartUtil.testXYChart(CSV_LARGE_FILE_NAME, SonargraphMetrics.STRUCTURAL_DEBT_INDEX, 25);
    }
}
