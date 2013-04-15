package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;

public class BigCSVChartTest
{
    private static final String CSV_LARGE_FILE_NAME = "./src/test/resources/largeFile.csv";

    public static void main(String[] args)
    {
        ChartTestUtil charUtil = new ChartTestUtil();
        charUtil.testBarChart(CSV_LARGE_FILE_NAME);
        charUtil.testDiscreteLineChart(CSV_LARGE_FILE_NAME);
        charUtil.testAreaChart(CSV_LARGE_FILE_NAME);
    }

}
