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
package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

public class LargeCsvFileCreator
{
    private static final long TIME_SPACING_MILLIS = 1000 * 60 * 60 * 3; //sec * min * hour * x
    private static final int NUMBER_OF_VALUES = 40;

    public static void main(String[] args) throws IOException
    {
        File csvFile = new File("./src/test/resources/LargeCsvFile.csv");
        if (csvFile.exists())
        {
            csvFile.delete();
        }
        CSVFileHandler fileHandler = new CSVFileHandler(csvFile);

        writeMetrics(fileHandler);
    }

    private static void writeMetrics(CSVFileHandler fileHandler) throws IOException
    {
        Random structuralDebt = new Random(103);
        Random numberOfViolations = new Random(10);
        Random numberOfInstructions = new Random(10000);
        Random numberOfMetricWarnings = new Random(200);
        Random numberOfCyclicNamespaces = new Random(30);
        Random numberofCyclicWarnings = new Random(2);
        Random numberOfNotAssignedTypes = new Random(10);
        Random numberOfConsistencyProblems = new Random(4);
        Random numberOfWorkspaceWarnings = new Random(3);
        Random numberOfTasks = new Random(5);
        Random biggestCycleGroup = new Random(30);
        Random highestAvd = new Random();
        Random numberOfInternalTypes = new Random(3000);

        DecimalFormat format = new DecimalFormat("#.##");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        format.setDecimalFormatSymbols(symbols);

        Calendar dateTime = new GregorianCalendar(2010, 11, 24, 22, 55, 20);
        long time = dateTime.getTimeInMillis();
        for (int i = 1; i <= NUMBER_OF_VALUES; i++)
        {
            long timestamp = time + (TIME_SPACING_MILLIS * i);
            Map<SonargraphMetrics, String> metricValues = new HashMap<SonargraphMetrics, String>();
            metricValues.put(SonargraphMetrics.STRUCTURAL_DEBT_INDEX, new Integer(structuralDebt.nextInt(8000)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_VIOLATIONS, new Integer(numberOfViolations.nextInt(20000)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, new Integer(numberOfInstructions.nextInt(10000000)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS, new Integer(numberOfMetricWarnings.nextInt(2000)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_CYCLIC_NAMESPACES, new Integer(numberOfCyclicNamespaces.nextInt(340)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, new Integer(numberofCyclicWarnings.nextInt(567)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES, new Integer(numberOfNotAssignedTypes.nextInt(500)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS, new Integer(numberOfConsistencyProblems.nextInt(50)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS, new Integer(numberOfWorkspaceWarnings.nextInt(70)).toString());
            metricValues.put(SonargraphMetrics.NUMBER_OF_TASKS, new Integer(numberOfTasks.nextInt(424)).toString());
            metricValues.put(SonargraphMetrics.BIGGEST_CYCLE_GROUP, new Integer(biggestCycleGroup.nextInt(500)).toString());
            Double acd = new Double(highestAvd.nextDouble() * 150);
            acd = acd % 100;
            String acdValue = format.format(acd);
            metricValues.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, acdValue);
            metricValues.put(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES, new Integer(numberOfInternalTypes.nextInt(100000)).toString());

            fileHandler.writeMetricValues(i, timestamp, metricValues);
        }
    }
}
