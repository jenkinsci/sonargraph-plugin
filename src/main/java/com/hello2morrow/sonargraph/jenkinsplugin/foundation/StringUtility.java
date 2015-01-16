package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility
{
    public static final String EMPTY_STRING = "";
    public static final char CSV_SEPARATOR = ';';
    private static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // IK: For some reason the setting of the format providing a pattern does not work on my machine
    //    public static final String FLOAT_FORMAT_PATTERN = "#,##0.00";
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat();

    static
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        FLOAT_FORMAT.setDecimalFormatSymbols(symbols);
    }

    public static String convertConstantNameToMixedCaseString(String input, boolean capitalizeFirstLetter, boolean insertSpace)
    {
        assert input != null : "'input' must not be null";
        assert input.length() > 0 : "'input' must not be empty";

        StringBuilder builder = new StringBuilder();
        boolean previousWasUnderscore = capitalizeFirstLetter;
        boolean currentIsUnderscore;

        for (int i = 0; i < input.length(); i++)
        {
            char nextChar = input.charAt(i);
            currentIsUnderscore = nextChar == '_';
            if (!currentIsUnderscore)
            {
                if (previousWasUnderscore)
                {
                    if (insertSpace && (builder.length() > 0))
                    {
                        builder.append(' ');
                    }
                    builder.append(Character.toUpperCase(nextChar));
                }
                else
                {
                    builder.append(Character.toLowerCase(nextChar));
                }
            }
            previousWasUnderscore = currentIsUnderscore;
        }
        return builder.toString();
    }

    public static String convertMixedCaseStringToConstantName(String input)
    {
        assert input != null : "'input' must not be null";
        assert input.length() > 0 : "'input' must not be empty";

        StringBuilder builder = new StringBuilder();

        char previousChar = input.charAt(0);
        builder.append(Character.toUpperCase(previousChar));

        for (int i = 1; i < input.length(); i++)
        {
            char nextChar = input.charAt(i);
            if (Character.isUpperCase(nextChar) || (Character.isDigit(nextChar) && !Character.isDigit(previousChar)))
            {
                builder.append('_');
            }
            builder.append(Character.toUpperCase(nextChar));
            previousChar = nextChar;
        }
        return builder.toString();
    }

    public static boolean validateNotNullAndRegexp(String value, String pattern)
    {
        if (value == null)
        {
            return false;
        }

        if (!value.matches(pattern))
        {
            return false;
        }

        return true;
    }

    public static String addXmlExtensionIfNotPreset(String value)
    {
        Pattern extensionPattern = Pattern.compile("\\.xml$");
        Matcher extensionMatcher = extensionPattern.matcher(value);
        return extensionMatcher.find() ? value : value + ".xml";
    }

    public static String replaceXMLWithHTMLExtension(String value)
    {
        Pattern extensionPattern = Pattern.compile("\\.xml$");
        Matcher extensionMatcher = extensionPattern.matcher(value);
        return extensionMatcher.find() ? extensionMatcher.replaceFirst(".html") : value;
    }

    public static String formatDateTime(Calendar dateTime)
    {
        assert dateTime != null : "Parameter 'dateTime' of method 'formatDateTime' must not be null";

        return DATE_TIME_FORMAT.format(new Date(dateTime.getTimeInMillis()));
    }

    public static DecimalFormat getFloatFormat()
    {
        return FLOAT_FORMAT;
    }

    public static DateFormat getDateFormat()
    {
        return DATE_FORMAT;
    }

}
