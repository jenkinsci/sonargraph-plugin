package com.hello2morrow.sonargraph.jenkinsplugin.controller.util;

import java.util.Map;

public class StaplerRequestUtil
{

    private StaplerRequestUtil()
    {
        //must not be instantiated
    }

    public static String getSimpleValue(String parameterName, Map<String, String[]> params)
    {
        assert parameterName != null && parameterName.length() > 0 : "Parameter 'parameterName' of method 'getValue' must not be empty";
        assert params != null : "Parameter 'params' of method 'getValue' must not be null";

        String[] value = params.get(parameterName);
        if (value == null || value.length == 0)
        {
            return null;
        }

        return value[0];
    }
}
