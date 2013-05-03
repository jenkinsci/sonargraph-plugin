package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.hello2morrow.sonargraph.jenkinsplugin.model.BuildDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IMetricHistoryProvider;
import com.hello2morrow.sonargraph.jenkinsplugin.model.InvalidDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.NotExistingDataPoint;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;

public class CSVFileHandlerTest
{
    private static final String CSV_FILE_PATH = "src/test/resources/sonargraph.csv";
    private static final String NON_EXISTING_CSV_FILE_PATH = "src/test/resources/non-existing.csv";
    private static final String CORRUPT_CSV_FILE_PATH = "src/test/resources/corrupt.csv";

    private final List<BuildDataPoint> referenceDataSet = new ArrayList<BuildDataPoint>();
    private TFile nowExistentFile;

    @Before
    public void setUp() throws IOException
    {
        int buildNumber = 31;
        double value = 3.0;
        referenceDataSet.add(new BuildDataPoint(buildNumber++, value, 0));
        referenceDataSet.add(new BuildDataPoint(buildNumber++, value, 0));
        referenceDataSet.add(new BuildDataPoint(buildNumber++, value, 0));
        referenceDataSet.add(new BuildDataPoint(buildNumber++, value, 0));
        referenceDataSet.add(new BuildDataPoint(buildNumber++, value, 0));

        removeFiles();
    }

    @After
    public void tearDown() throws IOException
    {
        removeFiles();
    }

    private void removeFiles() throws IOException
    {
        TFile files[] = new TFile[] { new TFile(NON_EXISTING_CSV_FILE_PATH), nowExistentFile };

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
        CSVFileHandler handler = new CSVFileHandler(new TFile(NON_EXISTING_CSV_FILE_PATH));
        String shoudBeTheFirstLine = handler.createHeaderLine();

        CSVReader csvReader = new CSVReader(new TFileReader(new TFile(NON_EXISTING_CSV_FILE_PATH)), CSVFileHandler.SEPARATOR);
        assertArrayEquals(shoudBeTheFirstLine.split(String.valueOf(CSVFileHandler.SEPARATOR)), csvReader.readNext());
        csvReader.close();
    }

    @Test
    public void testReadSonargraphCSVFile() throws IOException
    {
        nowExistentFile = new TFile(NON_EXISTING_CSV_FILE_PATH);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nowExistentFile);
        List<IDataPoint> testDataset = csvFileHandler.readMetricValues(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(0, testDataset.size());

        csvFileHandler = new CSVFileHandler(new TFile(CSV_FILE_PATH));
        testDataset = csvFileHandler.readMetricValues(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(5, testDataset.size());
        assertEquals(referenceDataSet, testDataset);
    }

    @Test
    public void testReadMetrics() throws IOException
    {
        TFile nonExistingFile = new TFile(NON_EXISTING_CSV_FILE_PATH);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nonExistingFile);

        List<IDataPoint> dataset = csvFileHandler.readMetricValues(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(0, dataset.size());

        csvFileHandler = new CSVFileHandler(new TFile(CSV_FILE_PATH));

        dataset = csvFileHandler.readMetricValues(SonargraphMetrics.NUMBER_OF_VIOLATIONS);
        assertEquals(5, dataset.size());
        for (IDataPoint point : dataset)
        {
            assertTrue(point instanceof InvalidDataPoint);
        }

        dataset = csvFileHandler.readMetricValues(SonargraphMetrics.STRUCTURAL_DEBT_INDEX);
        assertEquals(5, dataset.size());
        assertEquals(referenceDataSet, dataset);
    }

    @Test
    public void testNoExceptionsExpectedReadingMetrics() throws IOException
    {
        List<IDataPoint> testDataset = null;
        TFile corruptFile = new TFile(CORRUPT_CSV_FILE_PATH);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(corruptFile);
        try
        {
            testDataset = csvFileHandler.readMetricValues(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY);
        }
        catch (Exception ex)
        {
            fail("No exception ParseException should be thrown");
        }
        assertTrue(testDataset.get(1) instanceof InvalidDataPoint);

        try
        {
            testDataset = csvFileHandler.readMetricValues(SonargraphMetrics.NUMBER_OF_INTERNAL_TYPES);
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            fail("No exception ArrayIndexOutOfBoundsException should be thrown");
        }
        assertTrue(testDataset.get(1) instanceof NotExistingDataPoint);
    }

    //TODO: Try to eliminate coupling with the filesystem for this test case.
    @Test
    public void testWriteMetricsToFile() throws IOException
    {
        nowExistentFile = new TFile(NON_EXISTING_CSV_FILE_PATH);
        IMetricHistoryProvider csvFileHandler = new CSVFileHandler(nowExistentFile);

        HashMap<SonargraphMetrics, String> buildMetrics = new HashMap<SonargraphMetrics, String>();
        buildMetrics.put(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS, "3");
        buildMetrics.put(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS, "7");
        buildMetrics.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, "2.6");
        buildMetrics.put(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS, "200");
        long timestamp = System.currentTimeMillis();
        csvFileHandler.writeMetricValues(1, timestamp, buildMetrics);
        CSVReader csvReader = new CSVReader(new TFileReader(nowExistentFile), CSVFileHandler.SEPARATOR);
        csvReader.readNext(); //Do nothing with the first line
        String[] line = csvReader.readNext();
        csvReader.close();
        //1, -, -, 200, -, -, 7, -, 3, -, -, -, -, 2.6
        String[] expectedLine = { "1", new Long(timestamp).toString(), "-", "-", "200", "-", "-", "7", "-", "3", "-", "-", "-", "2.6", "-", "-", "-",
                "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-" };
        assertArrayEquals(expectedLine, line);
    }
}
