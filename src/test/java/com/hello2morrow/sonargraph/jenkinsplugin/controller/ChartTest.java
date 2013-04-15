package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import com.hello2morrow.sonargraph.jenkinsplugin.controller.util.ChartTestUtil;

public class ChartTest
{

    private static final String CSV_FILE_NAME = "./src/test/resources/sonargraphCharting.csv";

    public static void main(String[] args)
    {
        ChartTestUtil charUtil = new ChartTestUtil();
        charUtil.testBarChart(CSV_FILE_NAME);
        //        charUtil.testDiscreteLineChart(CSV_FILE_NAME);
        //        charUtil.testAreaChart(CSV_FILE_NAME);
    }

}
