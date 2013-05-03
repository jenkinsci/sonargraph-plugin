package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberFormatTest
{

    @Test
    public void testParse()
    {
        assertEquals(new Long(3700), SonargraphNumberFormat.parse("3,700"));
        assertEquals(new Double(3.75), SonargraphNumberFormat.parse("3.75"));
    }
}
