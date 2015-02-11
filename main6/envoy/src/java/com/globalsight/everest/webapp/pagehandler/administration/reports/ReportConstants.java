package com.globalsight.everest.webapp.pagehandler.administration.reports;

public interface ReportConstants
{
    public static final String REPORTS_SUB_DIR = "GlobalSight/Reports";
    public static final String REPORTS_NAME = "GSReports";
    public static final String GENERATE_REPORT = "generateReport";
    public static final String GENERATE_REPORTS = "generateReports";
    public static final String GET_REPORT = "getReport";
    public static final String ACTION_GET_PERCENT = "getPercent";
    public static final String ACTION_GET_REPORTSDATA = "getReportsData";
    public static final String ACTION_CANCEL_REPORTS = "cancelReports";
    public static final String ACTION_CANCEL_REPORT = "cancelReport";
    public static final String ACTION_REFRESH_PROGRESS = "refreshProgress";
    public static final String ACTION_GENERATE_SUMMARY_PERCENT = "generateSummaryReport";
    
    // Reports Type
    public static final String ONLINE_JOBS_REPORT = "OnlineJobsReport";
    public static final String DETAILED_WORDCOUNTS_REPORT = "DetailedWordCountsReport";
    public static final String REVIEWERS_COMMENTS_REPORT = "ReviewersCommentsReport";
    public static final String COMMENTS_ANALYSIS_REPORT = "CommentsAnalysisReport";
    public static final String CHARACTER_COUNT_REPORT = "CharacterCountReport";
    public static final String TRANSLATIONS_EDIT_REPORT = "TranslationsEditReport";
    public static final String SUMMARY_REPORT = "SummaryReport";
    
    // Attribute name in request/session
    public static final String JOB_IDS = "inputJobIDS";
    public static final String REPORT_TYPE = "reportType";
    public static final String REPORTJOBINFO_LIST = "reportsJobInfoList";
    public static final String TARGETLOCALE_LIST = "targetLocalesList";
    public static final String PROJECT_LIST = "reportsProjectList";
    public static final String L10N_PROFILES = "l10nProfiles";
    
    // Excel parameter name
    public static final String CATEGORY_LIST = "categoryList";
    
    public static final String ERROR_PAGE = "/envoy/administration/reports/error.jsp";
}