package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;

public class CSVFileHandlerTest
{
    private final String csvTestFilePath = "src/test/resources/sonargraph.csv";
    private final String nonExistingFilePath = "src/test/resources/non-existing.csv";
    private final String corruptFilePath = "src/test/resources/corrupt.csv";
    private final HashMap<Integer, Integer> loadedHashMapInt = new HashMap<Integer, Integer>();
    private final HashMap<Integer, Double> loadedHashMapDouble = new HashMap<Integer, Double>();
    private TFile nowExistentFile;
    private static final char SEPARATOR = ';';

    @Before
    public void setUp() throws IOException
    {
        loadedHashMapInt.put(31, 3);
        loadedHashMapInt.put(32, 3);
        loadedHashMapInt.put(33, 3);
        loadedHashMapInt.put(34, 3);
        loadedHashMapInt.put(35, 3);

        for (Integer build : loadedHashMapInt.keySet())
        {
            loadedHashMapDouble.put(build, (double) loadedHashMapInt.get(build));
        }

        removeFiles();
    }

    @After
    public void tearDown() throws IOException
    {
        removeFiles();
    }

    private void removeFiles() throws IOException
    {
        TFile files[] = new TFile[] { new TFile(nonExistingFilePath), nowExistentFile };

        for (TFile file : files)
        {
            if ((file != null) && file.exists())
            {
                file.rm();
            }
        }

    }

    @Test
    public void testCSVFileCreation() throws IOException
    {
        CSVFileHandler fileHandler = new CSVFileHandler(new TFile(nonExistingFilePath));
        String shoudBeTheFirstLine = "buildNumber;";
        for (SonargraphMetrics metric : fileHandler.getColumnMapping().keySet())
        {
            shoudBeTheFirstLine += metric.getStandardName() + SEPARATOR;
        }
        shoudBeTheFirstLine = shoudBeTheFirstLine.substring(0, shoudBeTheFirstLine.length() - 1);

        CSVReader csvReader = new CSVReader(new TFileReader(new TFile(nonExistingFilePath)), SEPARATOR);
        assertArrayEquals(shoudBeTheFirstLine.split(String.valueOf(SEPARATOR)), csvReader.readNext());
        csvReader.close();

    }

    @Test
    public void testReadSonargraphCSVFile() throws IOException
    {
        HashMap<Integer, Integer> testDataset = new HashMap<Integer, Integer>();
        nowExistentFile = new TFile(nonExistingFilePath);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nowExistentFile);
        testDataset = csvFileHandler.readMetrics();
        assertEquals(0, testDataset.size());

        csvFileHandler = new CSVFileHandler(new TFile(csvTestFilePath));
        testDataset = csvFileHandler.readMetrics();
        assertEquals(5, testDataset.size());
        assertEquals(loadedHashMapInt, testDataset);
    }

    @Test
    public void testReadMetrics() throws IOException
    {
        HashMap<Integer, Double> testDataset = new HashMap<Integer, Double>();
        TFile nonExistingFile = new TFile(nonExistingFilePath);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nonExistingFile);

        testDataset = csvFileHandler.readMetrics(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(0, testDataset.size());

        csvFileHandler = new CSVFileHandler(new TFile(csvTestFilePath));

        testDataset = csvFileHandler.readMetrics(SonargraphMetrics.NUMBER_OF_VIOLATIONS);
        assertEquals(0, testDataset.size());

        testDataset = csvFileHandler.readMetrics(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(5, testDataset.size());
        assertEquals(loadedHashMapDouble, testDataset);
    }

    @Test
    public void testNoExceptionsExpectedReadingMetrics() throws IOException
    {
        HashMap<Integer, Double> testDataset = new HashMap<Integer, Double>();
        TFile corruptFile = new TFile(corruptFilePath);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(corruptFile);
        try
        {
            testDataset = csvFileHandler.readMetrics(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY);
        }
        catch (Exception ex)
        {
            fail("No exception ParseException should be thrown");
        }
        assertNull(testDataset.get(83));

        try
        {
            testDataset = csvFileHandler.readMetrics(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES);
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            fail("No exception ArrayIndexOutOfBoundsException should be thrown");
        }
        assertNull(testDataset.get(83));
    }

    //TODO: Try to eliminate coupling with the filesystem for this test case.
    @Test
    public void testWriteMetricToFile() throws IOException
    {
        nowExistentFile = new TFile(nonExistingFilePath);

        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nowExistentFile);
        csvFileHandler.writeMetric(36, 3);
        assertTrue(nowExistentFile.exists());

        CSVReader csvReader = new CSVReader(new TFileReader(nowExistentFile), SEPARATOR);
        ArrayList<String[]> lines = new ArrayList<String[]>();
        String[] line;
        csvReader.readNext(); //Do nothing with the first line
        while ((line = csvReader.readNext()) != null)
        {
            lines.add(line);
        }
        csvReader.close();
        assertEquals(1, lines.size());
        String[] expectedLine = { "36", "3" };
        assertArrayEquals(expectedLine, lines.get(0));
    }

    //TODO: Try to eliminate coupling with the filesystem for this test case.
    @Test
    public void testWriteMetricsToFile() throws IOException
    {
        nowExistentFile = new TFile(nonExistingFilePath);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nowExistentFile);

        HashMap<SonargraphMetrics, String> buildMetrics = new HashMap<SonargraphMetrics, String>();
        buildMetrics.put(SonargraphMetrics.CONSISTENCY_PROBLEMS, "3");
        buildMetrics.put(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, "7");
        buildMetrics.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, "2.6");
        buildMetrics.put(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, "200");
        csvFileHandler.writeMetrics(1, buildMetrics);
        CSVReader csvReader = new CSVReader(new TFileReader(nowExistentFile), SEPARATOR);
        csvReader.readNext(); //Do nothing with the first line
        String[] line = csvReader.readNext();
        csvReader.close();
        //1, -, -, 200, -, -, 7, -, 3, -, -, -, -, 2.6
        String[] expectedLine = { "1", "-", "-", "200", "-", "-", "7", "-", "3", "-", "-", "-", "2.6", "-" };
        assertArrayEquals(expectedLine, line);
    }
}
