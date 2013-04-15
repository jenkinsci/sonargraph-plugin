package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;

public class NoDataChartTest
{
    private static final String CORRUPT_CSV_FILE_NAME = "./src/test/resources/corrupt.csv";

    public static void main(String[] args)
    {
        ChartTestUtil charUtil = new ChartTestUtil();
        charUtil.testBarChart(CORRUPT_CSV_FILE_NAME);
        //        charUtil.testDiscreteLineChart(CSV_LARGE_FILE_NAME);
        //        charUtil.testAreaChart(CSV_LARGE_FILE_NAME);
    }
}
