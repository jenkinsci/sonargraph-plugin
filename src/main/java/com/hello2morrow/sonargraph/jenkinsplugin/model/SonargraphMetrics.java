package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    NUMBER_OF_INSTRUCTIONS("Byte Code Instructions", "Byte Code Instructions", true),
    NUMBER_OF_METRIC_WARNINGS("Threshold Violations", "Threshold Violations", true),

    /**
     * Derived metrics not present in XML report:
     */
    BIGGEST_CYCLE_GROUP("Biggest Package Cycle Group", "Cycle Group Size", true),
    HIGHEST_AVERAGE_COMPONENT_DEPENDENCY("Highest ACD", "Highest ACD", false),

    /*---------------- END: Metrics that are going to be in the graphics section -------------------- */

    NUMBER_OF_CYCLIC_NAMESPACES("Number of Cyclic Packages", "Cyclic Packages", true),
    NUMBER_OF_CYCLIC_WARNINGS("Number of Cyclic Warnings", "Cyclic Warnings", true),
    NUMBER_OF_NOT_ASSIGNED_TYPES("Number of Unassigned Types", "Unassigned Types", true),
    NUMBER_OF_CONSISTENCY_PROBLEMS("Number of Consistency Problems", "Consistency Problems", true),
    NUMBER_OF_WORKSPACE_WARNINGS("Number of Workspace Warnings", "Workspace Warnings", true),
    NUMBER_OF_TASKS("Number of tasks", "Tasks", true),
    NUMBER_OF_TARGET_FILES("Number of Java Target Files", "Target Files", true),
    NUMBER_OF_INTERNAL_TYPES("Number of Types", "Types", true),

    NUMBER_OF_VIOLATING_REFERENCES("Number of Violating References", "Violating References", true),
    NUMBER_OF_VIOLATING_TYPES("Number of Violating Types", "Violating Types", true),
    STRUCTURAL_EROSION_REFERENCE_LEVEL("Structural Erosion (Reference Level)", "Structural Erosion (Reference Level)", true),
    STRUCTURAL_EROSION_TYPE_LEVEL("Structural Erosion (Type Level)", "Structural Erosion (Type Level)", true),
    NUMBER_OF_CYCLIC_ELEMENTS("Number of Cyclic Elements", "Cyclic Elements", true),
    OVERALL_NUMBER_OF_TYPE_DEPENDENCIES("Number of Type Dependencies (all)", "Type Dependencies (all)", true),
    NUMBER_OF_SOURCE_FILES("Number of Java Source Files", "Source Files", true),
    NUMBER_OF_BUILD_UNITS("Number of Build Units", "Build Units", true),
    NUMBER_OF_INTERNAL_NAMESPACES("Number of Internal Packages", "Internal Packages", true),
    NUMBER_OF_STATEMENTS("Number of Statements", "Statements", true),
    NUMBER_OF_FIX_WARNINGS("Number of Fix Warning Tasks", "Fix Warning Tasks", true),
    NUMBER_OF_IGNORED_VIOLATIONS("Number of ignored violations", "Ignored Violations", true),
    NUMBER_OF_IGNORED_WARNINGS("Number of ignored warnings", "Ignored Warnings", true),
    NUMBER_OF_REFACTORINGS("Number of refactoring tasks", "Refactoring Tasks", true),
    NUMBER_OF_WARNINGS("Number of warnings (all)", "Warnings (all)", true),
    NUMBER_OF_DUPLICATE_CODE_BLOCKS_WARNINGS("Number of warnings (duplicate code blocks)", "Duplicate Code Blocks", true),
    NUMBER_OF_INTERSECTIONS("Number of intersections", "Intersections", true),

    //Derived metrics
    HIGHEST_NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY("Highest NCCD", "Highest NCCD", false),
    HIGHEST_RELATIVE_AVERAGE_COMPONENT_DEPENDENCY("Highest Relative Average Component Dependency (RACD)", "Highest RACD", false),

    //Metrics not persisted in the CVS file
    AVERAGE_COMPONENT_DEPENDENCY("Average component dependency (ACD)", "ACD", false),
    NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY("Normalized cumulative component dependency (NCCD)", "NCCD", false),
    RELATIVE_AVERAGE_COMPONENT_DEPENDENCY("Relative Average Component Dependency (RACD)", "RACD", false);

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
    
    public static List<SonargraphMetrics> getAvailableMetrics()
    {
        List<SonargraphMetrics> values = new ArrayList<SonargraphMetrics>(Arrays.asList(SonargraphMetrics.values()));
        values.remove(SonargraphMetrics.AVERAGE_COMPONENT_DEPENDENCY);
        values.remove(SonargraphMetrics.NORMALIZED_CUMULATIVE_COMPONENT_DEPENDENCY);
        values.remove(SonargraphMetrics.RELATIVE_AVERAGE_COMPONENT_DEPENDENCY);
        
        return Collections.unmodifiableList(values);
    }
}
