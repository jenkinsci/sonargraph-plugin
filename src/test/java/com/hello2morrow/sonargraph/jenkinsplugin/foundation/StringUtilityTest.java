package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class StringUtilityTest
{
    @Test
    public void testConvertConstantNameToMixedCaseString()
    {
        assertEquals("NumberOfViolations", StringUtility.convertConstantNameToMixedCaseString("NUMBER_OF_VIOLATIONS", true, false));
    }

    @Test
    public void testConvertMixedCaseStringToConstantName()
    {
        assertEquals("NUMBER_OF_VIOLATIONS", StringUtility.convertMixedCaseStringToConstantName("NumberOfViolations"));
    }

    @Test
    public void testReplaceXMLWithHTMLExtension()
    {
        assertEquals("file.html", StringUtility.replaceXMLWithHTMLExtension("file.xml"));
    }

    @Test
    public void testFormatDateTime()
    {
        Calendar dateTime = new GregorianCalendar(2013, 11, 24, 22, 55, 20);
        assertEquals("2013-12-24T22:55:20", StringUtility.formatDateTime(dateTime));
    }
}
