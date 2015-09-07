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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import au.com.bytecode.opencsv.CSVReader;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;
import com.hello2morrow.sonargraph.jenkinsplugin.model.BuildDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.InvalidDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.NotExistingDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphReport;

/**
 * Handles operations on a CSV file.
 * @author esteban
 */
//TODO Improvement: Store the values in memory, so that the file does not have to be parsed on every graphics generation.  
public class CSVFileHandler implements IMetricHistoryProvider
{
    private static final int TIMESTAMP_COLUMN = 1;
    private static final int BUILDNUMBER_COLUMN = 0;
    /** Default separator for the CSV file. */
    private static final String BUILDNUMBER_COLUMN_NAME = "buildNumber";
    private static final String TIMESTAMP_COLUMN_NAME = "timestamp";

    private static final Map<SonargraphMetrics, Integer> COLUMN_MAPPING;

    private File m_file;
    static
    {
        /**!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         * CAUTION: Think really hard before changing the ordering of the metrics! It will corrupt the history of previous builds! 
         * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         */
        int startIndex = 2;
        COLUMN_MAPPING = new LinkedHashMap<SonargraphMetrics, Integer>();
        COLUMN_MAPPING.put(SonargraphMetrics.STRUCTURAL_DEBT_INDEX, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_VIOLATIONS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_CYCLIC_NAMESPACES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_TASKS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.BIGGEST_CYCLE_GROUP, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES, startIndex++);

        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_VIOLATING_REFERENCES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_VIOLATING_TYPES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.STRUCTURAL_EROSION_REFERENCE_LEVEL, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.STRUCTURAL_EROSION_TYPE_LEVEL, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_CYCLIC_ELEMENTS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.OVERALL_NUMBER_OF_TYPE_DEPENDENCIES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_SOURCE_FILES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_BUILD_UNITS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_INTERNAL_NAMESPACES, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_STATEMENTS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_FIX_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_IGNORED_VIOLATIONS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_IGNORED_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_REFACTORINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_DUPLICATE_CODE_BLOCKS_WARNINGS, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.NUMBER_OF_INTERSECTIONS, startIndex++);

        COLUMN_MAPPING.put(SonargraphMetrics.HIGHEST_NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY, startIndex++);
        COLUMN_MAPPING.put(SonargraphMetrics.HIGHEST_RELATIVE_AVERAGE_COMPONENT_DEPENDENCY, startIndex++);
    }

    public CSVFileHandler(File csvFile)
    {
        m_file = csvFile;
        if (!m_file.exists())
        {
            try
            {
                m_file.createNewFile();
                FileWriter fileWriter = new FileWriter(m_file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(createHeaderLine());
                bufferedWriter.newLine();
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            catch (IOException ex)
            {
                SonargraphLogger.INSTANCE.log(Level.SEVERE, "Failed to create CSV file '" + m_file.getAbsolutePath() + "': " + ex.getMessage());
            }
        }
    }

    public String createHeaderLine()
    {
        StringBuilder headerLine = new StringBuilder(BUILDNUMBER_COLUMN_NAME).append(StringUtility.CSV_SEPARATOR);
        headerLine.append(TIMESTAMP_COLUMN_NAME);

        for (SonargraphMetrics metric : COLUMN_MAPPING.keySet())
        {
            headerLine.append(StringUtility.CSV_SEPARATOR);
            headerLine.append(metric.getStandardName());
        }
        return headerLine.toString();
    }

    public List<IDataPoint> readMetricValues(SonargraphMetrics metric) throws IOException
    {
        List<IDataPoint> sonargraphDataset = new ArrayList<IDataPoint>();
        if (!isRightIndexForMetric(metric))
        {
            return sonargraphDataset;
        }

        if (!m_file.exists())
        {
            return sonargraphDataset;
        }

        try
        {
            CSVReader csvReader = new CSVReader(new FileReader(m_file), StringUtility.CSV_SEPARATOR);
            String[] nextLine;
            int column = COLUMN_MAPPING.get(metric);
            csvReader.readNext(); //We do nothing with the header line.
            while ((nextLine = csvReader.readNext()) != null)
            {
                if (nextLine.length == 0)
                {
                    //No values contained in line
                    continue;
                }

                processLine(nextLine, column, sonargraphDataset, metric, NumberFormat.getInstance(Locale.US));
            }
            csvReader.close();
        }
        catch (IOException ioe)
        {
            SonargraphLogger.INSTANCE.log(Level.WARNING,
                    "Exception occurred while reading from file '" + m_file.getAbsolutePath() + "':\n" + ioe.getMessage());

        }
        return sonargraphDataset;
    }

    protected void processLine(String[] nextLine, int column, List<IDataPoint> sonargraphDataset, SonargraphMetrics metric, NumberFormat numberFormat)
    {
        int buildNumber;
        Number value;
        try
        {
            buildNumber = Integer.parseInt(nextLine[BUILDNUMBER_COLUMN]);
        }
        catch (NumberFormatException ex)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "Build number '" + nextLine[BUILDNUMBER_COLUMN]
                    + "' could not be parsed to an integer value.");
            return;
        }

        long timestamp;
        try
        {
            timestamp = Long.parseLong(nextLine[TIMESTAMP_COLUMN]);
        }
        catch (NumberFormatException ex)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "Timestamp '" + nextLine[TIMESTAMP_COLUMN] + "' could not be parsed to a long value.");
            return;
        }

        String stringValue = null;
        try
        {
            stringValue = nextLine[column].trim();
            if (stringValue.equals(SonargraphReport.NOT_EXISTING_VALUE))
            {
                SonargraphLogger.INSTANCE.log(Level.INFO, "Skipping value for metric '" + metric.getStandardName() + "' for build number '"
                        + nextLine[0] + "'; it did not exist in Sonargraph XML report.");
                sonargraphDataset.add(new NotExistingDataPoint(buildNumber));
                return;
            }
            value = numberFormat.parse(stringValue);
            sonargraphDataset.add(new BuildDataPoint(buildNumber, value.doubleValue(), timestamp));
        }
        catch (NumberFormatException ex)
        {
            SonargraphLogger.INSTANCE.log(Level.WARNING, "The value of metric '" + metric.getStandardName() + "' for build number '" + nextLine[0]
                    + "' is not a valid number. Found '" + stringValue + "' but expected a Number. File '" + m_file.getAbsolutePath()
                    + "' might be corrupt:" + "\n" + ex.getMessage());
            sonargraphDataset.add(new InvalidDataPoint(buildNumber));
        }
        catch (ParseException ex)
        {
            SonargraphLogger.INSTANCE.log(Level.WARNING, "The value of metric '" + metric.getStandardName() + "' for build number '" + nextLine[0]
                    + "' is not a valid number. Found '" + stringValue + "' but expected a Number. File '" + m_file.getAbsolutePath()
                    + "' might be corrupt:" + "\n" + ex.getMessage());
            sonargraphDataset.add(new InvalidDataPoint(buildNumber));
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            SonargraphLogger.INSTANCE.log(Level.WARNING, "The value of metric '" + metric.getStandardName() + "' for build number '" + nextLine[0]
                    + "' was not found. File '" + m_file.getAbsolutePath() + "' might be corrupt:" + "\n" + ex.getMessage());
            sonargraphDataset.add(new NotExistingDataPoint(buildNumber));
        }

    }

    public void writeMetricValues(Integer buildNumber, long timestamp, Map<SonargraphMetrics, String> metricValues) throws IOException
    {
        FileWriter fileWriter = new FileWriter(m_file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuilder line = new StringBuilder(buildNumber.toString()).append(StringUtility.CSV_SEPARATOR);

        line.append(timestamp);
        for (SonargraphMetrics metric : COLUMN_MAPPING.keySet())
        {
            line.append(StringUtility.CSV_SEPARATOR);
            String value = metricValues.get(metric);
            if (value == null)
            {
                line.append(SonargraphReport.NOT_EXISTING_VALUE);
            }
            else
            {
                line.append(value);
            }
        }
        bufferedWriter.write(line.toString());
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public LinkedHashMap<SonargraphMetrics, Integer> getColumnMapping()
    {
        return new LinkedHashMap<SonargraphMetrics, Integer>(COLUMN_MAPPING);
    }

    private boolean isRightIndexForMetric(SonargraphMetrics metric)
    {
        int realMetricIndex = -1;
        try
        {
            CSVReader csvReader = new CSVReader(new FileReader(m_file), StringUtility.CSV_SEPARATOR);
            String[] headerLine = csvReader.readNext();
            realMetricIndex = Arrays.asList(headerLine).indexOf(metric.getStandardName());
            csvReader.close();
        }
        catch (IOException e)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "I/O Error reading CSV when validating the index for the metric '" + metric.getStandardName()
                    + "': " + e.getMessage());

        }

        if (COLUMN_MAPPING.get(metric) == null)
        {
            return false;
        }
        return COLUMN_MAPPING.get(metric).intValue() == realMetricIndex;
    }

    public String getStorageName()
    {
        return m_file.getAbsolutePath();
    }

}
