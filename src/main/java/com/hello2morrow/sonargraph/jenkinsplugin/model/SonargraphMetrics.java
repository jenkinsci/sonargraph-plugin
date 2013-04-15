package com.hello2morrow.sonargraph.jenkinsplugin.model;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

/**
 * Enumerator which contains the different metric names.
 * 
 * @author esteban
 * 
 */
public enum SonargraphMetrics
{
    /*---------------- START: Metrics that are going to be in the graphics section ------------------*/

    STRUCTURAL_DEBT_INDEX("Structural Debt Index (SDI)", "SDI", true),

    NUMBER_OF_VIOLATIONS("Violating Type Dependencies", "Violating Type Dependencies", true),

    AVERAGE_COMPONENT_DEPENDENCY("Average component dependency (ACD)", "ACD", false),

    NUMBER_OF_INSTRUCTIONS("Byte Code Instructions", "Byte Code Instructions", true),

    NUMBER_OF_METRIC_WARNINGS("Threshold Violations", "Threshold Violations", true),

    /**
     * Derived metrics not present in XML report:
     */
    BIGGEST_CYCLE_GROUP("Biggest Package Cycle Group", "Cycle Group Size", true),

    HIGHEST_AVERAGE_COMPONENT_DEPENDENCY("Highest ACD", "Highest ACD", false),

    /*---------------- END: Metrics that are going to be in the graphics section -------------------- */
    NUMBER_OF_CYCLIC_NAMESPACES("Number of Cyclic Packages", "Cyclic Packages", true),

    NUMBER_OF_CYCLIC_ELEMENTS("Number of Cyclic Elements", "Cyclic Elements", true),

    NUMBER_OF_NOT_ASSIGNED_TYPES("Number of Unassigned Types", "Unassigned Types", true),

    NUMBER_OF_CONSISTENCY_PROBLEMS("Number of Consistency Warnings", "Consistency Warnings", true),

    NUMBER_OF_WORKSPACE_WARNINGS("Number of Workspace Warnings", "Workspace Warnings", true),

    NUMBER_OF_TASKS("Number of tasks", "Tasks", true),

    NUMBER_OF_TARGET_FILES("Number of Java Target Files", "Target Files", true),

    NUMBER_OF_INTERNAL_TYPES("Number of Types", "Types", true);

    private String m_description;
    private String m_shortDescription;
    private boolean m_isNaturalNumber;

    private SonargraphMetrics(String description, String shortDescription, boolean isNaturalNumber)
    {
        m_description = description;
        m_shortDescription = shortDescription;
        m_isNaturalNumber = isNaturalNumber;
    }

    /**
     * @return Metric's standard name.
     */
    public String getStandardName()
    {
        return StringUtility.convertConstantNameToMixedCaseString(name(), true, false);
    }

    public String getDescription()
    {
        return m_description;
    }

    /**
     * @return true if metric can have values > 0, whole numbers; false otherwise, e.g. for ACD
     */
    public boolean isNaturalNumber()
    {
        return m_isNaturalNumber;
    }

    public static SonargraphMetrics fromStandardName(String standardName) throws IllegalArgumentException
    {
        assert standardName != null : "'standardName' must not be null";
        assert standardName.length() > 0 : "'standardName' must not be empty";
        String name = StringUtility.convertMixedCaseStringToConstantName(standardName);
        return SonargraphMetrics.valueOf(name);
    }

    public String getShortDescription()
    {
        return m_shortDescription;
    }
}
