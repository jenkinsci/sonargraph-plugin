/*
 * Sonar Sonargraph Plugin
 * Copyright (C) 2009, 2010, 2011 hello2morrow GmbH
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;
import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphNumberFormat;
import com.hello2morrow.sonargraph.jenkinsplugin.model.IReportReader;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphMetrics;
import com.hello2morrow.sonargraph.jenkinsplugin.model.SonargraphReport;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.ReportContext;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.XsdAttribute;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.XsdAttributeCategory;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.XsdAttributeRoot;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.XsdConsistencyProblems;
import com.hello2morrow.sonargraph.jenkinsplugin.xsd.XsdCycleGroup;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * Utility class for reading in the Sonargraph Report.
 * 
 * @author Ingmar
 * 
 */
public class ReportFileReader implements IReportReader
{
    private static final String PACKAGE_CYCLE_GROUP_IDENTIFIER = "Physical package";

    public ReportFileReader()
    {
        super();
    }

    public SonargraphReport readSonargraphReport(final TFile sonargraphReportFile)
    {
        if ((sonargraphReportFile == null) || !sonargraphReportFile.exists())
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "No file path provided for reading sonargraph report");
            return null;
        }

        SonargraphLogger.INSTANCE.log(Level.INFO, "Reading Sonargraph metrics report from: " + sonargraphReportFile.getNormalizedAbsolutePath());
        ReportContext report = null;
        SonargraphReport sonargraphReport = null;
        InputStream input = null;
        ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            input = new TFileInputStream(sonargraphReportFile);
            Thread.currentThread().setContextClassLoader(ReportFileReader.class.getClassLoader());
            JAXBContext context = JAXBContext.newInstance("com.hello2morrow.sonargraph.jenkinsplugin.xsd");
            Unmarshaller u = context.createUnmarshaller();
            report = (ReportContext) u.unmarshal(input);
            sonargraphReport = createSonargraphReportFromXml(report);
        }
        catch (JAXBException e)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "JAXB Problem in " + sonargraphReportFile.getNormalizedAbsolutePath(), e);
        }
        catch (IOException e)
        {
            SonargraphLogger.INSTANCE.log(Level.SEVERE, "Cannot open Sonargraph report: " + sonargraphReportFile.getNormalizedAbsolutePath() + ".");
            SonargraphLogger.INSTANCE.log(Level.SEVERE,
                    "Maven integration: Did you run the maven sonargraph goal before with the POM option <prepareForJenkins>true</prepareForJenkins>"
                            + " or with the commandline option -Dsonargraph.prepareForJenkins=true?");
            SonargraphLogger.INSTANCE.log(Level.SEVERE,
                    "Ant integration: Did you create the Sonargraph XML report with the option prepareForJenkins set to true? ");
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    SonargraphLogger.INSTANCE.log(Level.SEVERE, "Cannot close " + sonargraphReportFile.getNormalizedAbsolutePath(), e);
                }
            }
        }
        return sonargraphReport;
    }

    private SonargraphReport createSonargraphReportFromXml(ReportContext xmlReport)
    {
        SonargraphReport report = new SonargraphReport(xmlReport.getName());

        // Read the attribute categories of the software system
        for (XsdAttributeCategory category : xmlReport.getAttributes().getAttributeCategory())
        {
            for (XsdAttribute attribute : category.getAttribute())
            {
                try
                {
                    SonargraphMetrics metric = SonargraphMetrics.fromStandardName(attribute.getStandardName());
                    if (metric != null)
                    {
                        report.addSystemMetric(metric, attribute.getValue());
                    }
                    else
                    {
                        report.addSystemMetric(metric, SonargraphReport.NOT_EXISTING_VALUE);
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    SonargraphLogger.INSTANCE.log(Level.FINE, "Unsupported metric '" + attribute.getStandardName() + "'");
                }
            }
        }

        for (XsdAttributeRoot attrRoot : xmlReport.getBuildUnits().getBuildUnit())
        {
            for (XsdAttributeCategory category : attrRoot.getAttributeCategory())
            {
                for (XsdAttribute attribute : category.getAttribute())
                {
                    SonargraphMetrics metric = null;
                    try
                    {
                        metric = SonargraphMetrics.fromStandardName(attribute.getStandardName());
                        report.addBuildUnitMetric(attrRoot.getName(), metric, attribute.getValue());
                    }
                    catch (IllegalArgumentException ex)
                    {
                        SonargraphLogger.INSTANCE.log(Level.FINE, "Unsupported metric '" + attribute.getStandardName() + "'");
                        continue;
                    }
                }
            }
        }

        int biggestCycleGroupSize = 0;
        for (XsdCycleGroup group : xmlReport.getCycleGroups().getCycleGroup())
        {
            if (!PACKAGE_CYCLE_GROUP_IDENTIFIER.equals(group.getNamedElementGroup()))
            {
                continue;
            }
            int size = group.getCyclePath().size();
            if (size > biggestCycleGroupSize)
            {
                biggestCycleGroupSize = size;
            }
        }
        report.addSystemMetric(SonargraphMetrics.BIGGEST_CYCLE_GROUP, new Integer(biggestCycleGroupSize).toString());

        report.calculateDerivedMetrics();

        Integer consistencyProblems = 0;
        XsdConsistencyProblems consProblems = xmlReport.getConsistencyProblems();
        consistencyProblems = SonargraphNumberFormat.parse(consProblems.getNumberOf()).intValue();
        report.addSystemMetric(SonargraphMetrics.NUMBER_OF_CONSISTENCY_PROBLEMS, consistencyProblems.toString());

        return report;
    }
}
