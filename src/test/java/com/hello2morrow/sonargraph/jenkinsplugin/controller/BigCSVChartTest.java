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
