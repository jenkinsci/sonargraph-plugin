package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Level;

import au.com.bytecode.opencsv.CSVReader;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphReport;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;
import de.schlichtherle.truezip.file.TFileWriter;

/**
 * Handles operations on a CSV file.
 * @author esteban
 */
//TODO Improvement: Store the values in memory, so that the file does not have to be parsed on every graphics generation.  
public class CSVFileHandler implements IMetricHistoryProvider
{
    /** Default separator for the CSV file. */
    private static final char SEPARATOR = ';';
    private static LinkedHashMap<SonargraphMetrics, Integer> s_columnMapping;
    private final TFile m_file;

    //    private Map<SonargraphMetrics, List<Double>> m_data = new HashMap<SonargraphMetrics, List<Double>>());

    /**
     * CAUTION: Think really hard before changing the ordering of the metrics! It will corrupt the history of previous builds! 
     */
    static
    {
        s_columnMapping = new LinkedHashMap<SonargraphMetrics, Integer>();

        int i = 1;
        s_columnMapping.put(SonargraphMetrics.STRUCTURAL_DEBT_INDEX, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_VIOLATIONS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_CYCLIC_NAMESPACES, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES, i++);
        s_columnMapping.put(SonargraphMetrics.CONSISTENCY_PROBLEMS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_TASKS, i++);
        s_columnMapping.put(SonargraphMetrics.BIGGEST_CYCLE_GROUP, i++);
        s_columnMapping.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, i++);
        s_columnMapping.put(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES, i++);
    }

    public CSVFileHandler(TFile csvFile)
    {
        m_file = csvFile;
        if (!m_file.exists())
        {
            try
            {
                m_file.createNewFile();
                TFileWriter fileWriter = new TFileWriter(m_file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                String headerLine = "buildNumber;";

                for (SonargraphMetrics metric : s_columnMapping.keySet())
                {
                    headerLine += metric.getStandardName() + SEPARATOR;
                }
                headerLine = headerLine.substring(0, headerLine.length() - 1);
                bufferedWriter.write(headerLine);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            catch (IOException ex)
            {
                SonargraphLogger.INSTANCE.log(Level.SEVERE,
                        "Failed to create CSV file '" + m_file.getNormalizedAbsolutePath() + "': " + ex.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.jenkinsplugin.persistence.IMetricHistoryProvider#readMetrics()
     */
    @Deprecated
    public HashMap<Integer, Integer> readMetrics() throws IOException
    {
        HashMap<Integer, Integer> sonargraphDataset = new HashMap<Integer, Integer>();
        if (!m_file.exists())
        {
            return sonargraphDataset;
        }
        CSVReader csvReader = new CSVReader(new TFileReader(m_file), SEPARATOR);
        String[] nextLine;
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null)
        {
            sonargraphDataset.put(Integer.parseInt(nextLine[0]), Integer.parseInt(nextLine[1]));
        }
        csvReader.close();

        return sonargraphDataset;
    }

    public HashMap<Integer, Double> readMetrics(SonargraphMetrics metric) throws IOException
    {
        HashMap<Integer, Double> sonargraphDataset = new HashMap<Integer, Double>();
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
            CSVReader csvReader = new CSVReader(new TFileReader(m_file), SEPARATOR);
            String[] nextLine;
            Number value;
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            int column = s_columnMapping.get(metric);
            Integer buildNumber;
            csvReader.readNext(); //We do nothing with the header line.
            while ((nextLine = csvReader.readNext()) != null)
            {
                String stringValue = null;
                try
                {
                    stringValue = nextLine[column].trim();
                    if (stringValue.equals(SonargraphReport.NOT_EXISTING_VALUE))
                    {
                        SonargraphLogger.INSTANCE.log(Level.INFO, "Skipping value for metric '" + metric.getStandardName() + "' for build number '"
                                + nextLine[0] + "'; it did not exist in Sonargraph XML report.");
                        continue;
                    }
                    value = format.parse(stringValue);
                    buildNumber = Integer.parseInt(nextLine[0]);
                    sonargraphDataset.put(buildNumber, value.doubleValue());
                }
                catch (ParseException ex)
                {
                    SonargraphLogger.INSTANCE.log(
                            Level.WARNING,
                            "The value of metric '" + metric.getStandardName() + "' for build number '" + nextLine[0]
                                    + "' is not a valid number. Found '" + stringValue + "' but expected a Number. File '"
                                    + m_file.getNormalizedAbsolutePath() + "' might be corrupt:" + "\n" + ex.getMessage());
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {
                    SonargraphLogger.INSTANCE.log(Level.WARNING,
                            "The value of metric '" + metric.getStandardName() + "' for build number '" + nextLine[0] + "' was not found. File '"
                                    + m_file.getNormalizedAbsolutePath() + "' might be corrupt:" + "\n" + ex.getMessage());
                }
            }
            csvReader.close();
        }
        catch (IOException ioe)
        {
            SonargraphLogger.INSTANCE.log(Level.WARNING, "Exception occurred while reading from file '" + m_file.getNormalizedAbsolutePath() + "':\n"
                    + ioe.getMessage());

        }
        return sonargraphDataset;
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.jenkinsplugin.persistence.IMetricHistoryProvider#writeMetric(java.lang.Integer, java.lang.Integer)
     */
    @Deprecated
    public void writeMetric(Integer buildNumber, Integer metricValue) throws IOException
    {
        TFileWriter fileWriter = new TFileWriter(m_file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        String lineToAppend = String.valueOf(buildNumber) + SEPARATOR + String.valueOf(metricValue);
        bufferedWriter.write(lineToAppend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public void writeMetrics(Integer buildNumber, HashMap<SonargraphMetrics, String> metricValues) throws IOException
    {
        TFileWriter fileWriter = new TFileWriter(m_file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuilder line = new StringBuilder(buildNumber.toString());

        for (SonargraphMetrics metric : s_columnMapping.keySet())
        {
            line.append(SEPARATOR);
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
        return new LinkedHashMap<SonargraphMetrics, Integer>(s_columnMapping);
    }

    private boolean isRightIndexForMetric(SonargraphMetrics metric)
    {
        int realMetricIndex = -1;
        try
        {
            CSVReader csvReader = new CSVReader(new TFileReader(m_file), SEPARATOR);
            String[] headerLine = csvReader.readNext();
            realMetricIndex = Arrays.asList(headerLine).indexOf(metric.getStandardName());
            csvReader.close();
        }
        catch (IOException e)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "I/O Error reading CSV when validating the index for the metric '" + metric.getStandardName()
                    + "': " + e.getMessage());

        }
        return s_columnMapping.get(metric).intValue() == realMetricIndex;
    }

    public String getStorageName()
    {
        return m_file.getNormalizedAbsolutePath();
    }

}
