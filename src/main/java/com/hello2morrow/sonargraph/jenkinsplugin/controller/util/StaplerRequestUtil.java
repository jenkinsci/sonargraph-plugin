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
