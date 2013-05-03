package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public class NoDataChartTest
{
    private static final String CORRUPT_CSV_FILE_NAME = "./src/test/resources/corrupt.csv";

    public static void main(String[] args)
    {
        ChartTestUtil charUtil = new ChartTestUtil();
        charUtil.testXYChart(CORRUPT_CSV_FILE_NAME, SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, 30);
    }
}
