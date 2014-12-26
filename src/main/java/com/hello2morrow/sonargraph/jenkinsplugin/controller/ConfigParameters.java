package com.hello2morrow.sonargraph.jenkinsplugin.controller;

public enum ConfigParameters
{
    ACTION_URL_NAME("sonargraph"),
    SONARGRAPH_ICON("/plugin/sonargraph-plugin/icons/Sonargraph-Architect.png"),
    ACTION_DISPLAY_NAME("Sonargraph"),
    REPORT_BUILDER_DISPLAY_NAME("Sonargraph Report Generation & Analysis"),
    REPORT_ANALYZER_DISPLAY_NAME("Sonargraph Report Analysis"),
    JOB_FOLDER("job/"),
    HTML_REPORT_ACTION_URL("sonargraph-html-report"),
    METRIC_HISTORY_CSV_FILE_PATH("sonargraph.csv"),
    CHARTS_FOR_METRICS_CSV_FILE_PATH("charts_for_metrics.csv"),
    SONARGRAPH_HTML_REPORT_FILE_NAME("/sonargraph-report.html"),
    REPORT_HISTORY_FOLDER("sonargraphReportHistory");

    private String m_value;

    private ConfigParameters(String value)
    {
        m_value = value;
    }

    public String getValue()
    {
        return m_value;
    }
}