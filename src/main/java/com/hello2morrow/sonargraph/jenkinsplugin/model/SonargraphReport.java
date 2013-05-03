package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.SonargraphLogger;

/**
 * Plain Java object representing the Sonargraph Report. This class makes it easier to access the metrics as using the JAXB generated classes.
 * And additionally, we only have the dependency on JAXB in the persistence.
 * 
 * @author Ingmar
 *
 */
public class SonargraphReport
{
    public static final String NOT_EXISTING_VALUE = "-";
    private String m_name;
    private String m_description;
    private Map<SonargraphMetrics, String> m_systemMetrics = new HashMap<SonargraphMetrics, String>();

    private Map<String, Map<SonargraphMetrics, String>> m_buildUnitMetrics = new HashMap<String, Map<SonargraphMetrics, String>>();

    public SonargraphReport(String name)
    {
        assert (name != null) && (name.length() > 0) : "Parameter 'name' of method 'SonargraphReport' must not be empty";
        m_name = name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String description)
    {
        assert (description != null) && (description.length() > 0) : "Parameter 'description' of method 'setDescription' must not be empty";
        m_description = description;
    }

    public void addSystemMetric(SonargraphMetrics metric, String value)
    {
        m_systemMetrics.put(metric, value);
    }

    public void addBuildUnitMetric(String buildUnitName, SonargraphMetrics metric, String value)
    {
        assert (buildUnitName != null) && (buildUnitName.length() > 0) : "Parameter 'buildUnitName' of method 'addBuildUnitMetric' must not be empty";
        assert metric != null : "Parameter 'metric' of method 'addBuildUnitMetric' must not be null";
        assert (value != null) && (value.length() > 0) : "Parameter 'value' of method 'addBuildUnitMetric' must not be empty";

        if (m_buildUnitMetrics.get(buildUnitName) == null)
        {
            m_buildUnitMetrics.put(buildUnitName, new HashMap<SonargraphMetrics, String>());
        }

        Map<SonargraphMetrics, String> buildUnitMetrics = m_buildUnitMetrics.get(buildUnitName);
        buildUnitMetrics.put(metric, value);
    }

    public String getName()
    {
        return m_name;
    }

    public String getSystemMetricValue(SonargraphMetrics metric)
    {
        return m_systemMetrics.get(metric);
    }

    public String getBuildUnitMetricValue(String buildUnitName, SonargraphMetrics metric)
    {
        assert (buildUnitName != null) && (buildUnitName.length() > 0) : "Parameter 'buildUnitName' of method 'getBuildUnitMetricValue' must not be empty";
        assert metric != null : "Parameter 'metric' of method 'getBuildUnitMetricValue' must not be null";

        if (m_buildUnitMetrics.get(buildUnitName) == null)
        {
            return null;
        }

        return m_buildUnitMetrics.get(buildUnitName).get(metric);
    }

    /**
     * Triggers the calculation of the additional metrics, like biggest cycle group, highest ACD, that are not part of the XML report
     */
    public void calculateDerivedMetrics()
    {
        m_systemMetrics.put(SonargraphMetrics.HIGHEST_AVERAGE_COMPONENT_DEPENDENCY, new Double(
                getHighestValue(SonargraphMetrics.AVERAGE_COMPONENT_DEPENDENCY)).toString());
        m_systemMetrics.put(SonargraphMetrics.HIGHEST_NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY, new Double(
                getHighestValue(SonargraphMetrics.NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY)).toString());
        m_systemMetrics.put(SonargraphMetrics.HIGHEST_RELATIVE_AVERAGE_COMPONENT_DEPENDENCY, new Double(
                getHighestValue(SonargraphMetrics.RELATIVE_AVERAGE_COMPONENT_DEPENDENCY)).toString());
    }

    private double getHighestValue(SonargraphMetrics metric)
    {
        double highestValue = 0.0;

        for (String buildUnitName : m_buildUnitMetrics.keySet())
        {
            Map<SonargraphMetrics, String> buildUnitValues = m_buildUnitMetrics.get(buildUnitName);
            String value = buildUnitValues.get(metric);
            if (value == null)
            {
                continue;
            }
            try
            {
                double actualValue = Double.parseDouble(value);
                if (actualValue > highestValue)
                {
                    highestValue = actualValue;
                }
            }
            catch (NumberFormatException ex)
            {
                SonargraphLogger.INSTANCE.log(Level.SEVERE, "Value '" + value + "' for metric '" + metric.getStandardName() + "' of build unit '"
                        + buildUnitName + "' is not a number.");
            }
        }
        return highestValue;
    }
}
