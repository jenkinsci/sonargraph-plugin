package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;

public class SonargraphNumberFormat
{
    public static Number parse(String number)
    {
        if (number.length() > 0)
        {
            if (number.matches(".*[a-zA-Z]+.*"))
            {
                return new NoNumber(number);
            }

            /*
             * Here we check for people using comma instead of period and
             * the other way around. Usually people do not enter grouping 
             * separators when entering numbers. So it is safe to assume
             * that a comma used instead of a period is meant to be a
             * decimal separator.
             */
            DecimalFormatSymbols symbols = StringUtility.getFloatFormat().getDecimalFormatSymbols();
            char groupingSep = symbols.getGroupingSeparator();
            assert groupingSep == ',' : "Grouping separator must be ','";

            char decimalSep = symbols.getDecimalSeparator();
            assert decimalSep == '.' : "Decimal separator must be '.'";

            int groupingCount = 0;
            int decimalCount = 0;
            int charactersBeforeFirstGrouping = 0;

            for (int i = number.length(); i > 0; i--)
            {
                char c = number.charAt(i - 1);

                if (c != groupingSep)
                {
                    if (groupingCount == 0)
                    {
                        charactersBeforeFirstGrouping++;
                    }
                    if (c == decimalSep)
                    {
                        decimalCount++;
                    }
                }
                else
                {
                    groupingCount++;
                }
            }
            if ((groupingCount == 1) && (decimalCount == 0) && (charactersBeforeFirstGrouping != 3))
            {
                number = number.replace(groupingSep, decimalSep);
            }

            Number result = StringUtility.getFloatFormat().parse(number, new ParsePosition(0));

            if (result == null)
            {
                result = new NoNumber(number);
            }

            return result;
        }
        return NoNumber.NULL_NUMBER;
    }
}
