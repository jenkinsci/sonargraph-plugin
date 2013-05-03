package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumberUtilityTest
{

    @Test
    public void testRound()
    {
        double value = 16.06005;
        assertTrue(16.06 == NumberUtility.round(value, 2));

        assertTrue(14.001 == NumberUtility.round(14.0005403, 3));
    }

}
