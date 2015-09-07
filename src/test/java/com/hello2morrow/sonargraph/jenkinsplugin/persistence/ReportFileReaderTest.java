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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.IOException;

import org.junit.Test;

import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphReport;

public class ReportFileReaderTest
{
    private static final String reportFileName = "src/test/resources/sonargraph-sonar-report_multiple-buildunits.xml";
    private static final String fakeDir = "fakeDir/ReporFileName.xml";
    private static final String nonExistingFile = "src/test/resources/report_error.xml";

    @Test
    public void testReadSonargraphReport() throws IOException, InterruptedException
    {
        IReportReader reader = new ReportFileReader();
        assertNull(reader.readSonargraphReport(new FilePath((VirtualChannel) null, fakeDir)));
        assertNull(reader.readSonargraphReport(new FilePath((VirtualChannel) null, nonExistingFile)));

        SonargraphReport sonargraphReport = reader.readSonargraphReport(new FilePath((VirtualChannel) null, reportFileName));

        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_VIOLATIONS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_TASKS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_TARGET_FILES));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.STRUCTURAL_DEBT_INDEX));
        assertNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.AVERAGE_COMPONENT_DEPENDENCY));

        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CYCLIC_NAMESPACES));

        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY));
        assertNotNull(sonargraphReport.getSystemMetricValue(SonargraphMetrics.BIGGEST_CYCLE_GROUP));

        assertEquals("AlarmClock-3-levels", sonargraphReport.getName());
        assertEquals("2", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_VIOLATIONS));
        assertEquals("0", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_NOT_ASSIGNED_TYPES));
        assertEquals("1", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CYCLIC_WARNINGS));
        assertEquals("0", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_METRIC_WARNINGS));
        assertEquals("0", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_WORKSPACE_WARNINGS));
        assertEquals("0", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS));
        assertEquals("0", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_TASKS));
        assertEquals("7", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_TARGET_FILES));
        assertEquals("46", sonargraphReport.getSystemMetricValue(SonargraphMetrics.STRUCTURAL_DEBT_INDEX));
        assertEquals("459", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_INSTRUCTIONS));
        assertEquals("2", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CYCLIC_NAMESPACES));

        assertEquals("2.33", sonargraphReport.getSystemMetricValue(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY));
        assertEquals("2", sonargraphReport.getSystemMetricValue(SonargraphMetrics.BIGGEST_CYCLE_GROUP));
    }

    @Test
    public void testSourceFileCycleIgnoredForBiggestPackageCycleAnalysis() throws IOException, InterruptedException
    {
        IReportReader reader = new ReportFileReader();
        SonargraphReport sonargraphReport = reader.readSonargraphReport(new FilePath((VirtualChannel) null,
                "./src/test/resources/sonargraph-architect-report_different_cyclegroups.xml"));

        assertEquals("23", sonargraphReport.getSystemMetricValue(SonargraphMetrics.NUMBER_OF_CYCLIC_ELEMENTS));
        assertEquals("5", sonargraphReport.getSystemMetricValue(SonargraphMetrics.BIGGEST_CYCLE_GROUP));
    }
}
