package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

public class NumberUtility
{
    public static double round(double value, int decimalPlaces)
    {
        double factor = Math.pow(10.0, decimalPlaces);
        long longValue = Math.round(value * factor);
        double result = longValue / factor;
        return result;
    }

}
