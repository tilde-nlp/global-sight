/**
 * Ambassador.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface Ambassador extends java.rmi.Remote {
    public boolean isInstalled() throws java.rmi.RemoteException;
    public java.lang.String getUserInfo(java.lang.String p_accessToken, java.lang.String p_userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getConnection(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getStatus(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String login(java.lang.String p_username, java.lang.String p_password) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getServerVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public int modifyUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public int createUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String acceptTask(java.lang.String p_accessToken, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String rejectTask(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_rejectComment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_orgSid, java.lang.String p_newSid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_orgSid, java.lang.String p_newSid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String sid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String sid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void updateTaskState(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_state) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String cancelWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String addJobComment(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_userId, java.lang.String p_comment, byte[] p_file, java.lang.String p_fileName, java.lang.String p_access) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUserTimeZone(java.lang.String p_accessToken, java.lang.String p_userName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getTargetLocales(java.lang.String p_accessToken, java.lang.String p_sourceLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void discardJob(java.lang.String p_accessToken, java.lang.String p_userIdToDiscardJob, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String dispatchWorkflow(java.lang.String p_accessToken, java.lang.String p_wfIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String completeTask(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_destinationArrow) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String cancelJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String archiveJob(java.lang.String p_accessToken, java.lang.String p_jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getCountsByJobState(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void uploadFile(java.lang.String accessToken, java.lang.String jobName, java.lang.String filePath, java.lang.String fileProfileId, byte[] content) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void uploadFile(java.lang.String accessToken, java.lang.String jobName, java.lang.String filePath, java.lang.String fileProfileId, java.lang.String content) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void uploadFile(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.util.HashMap getXliffFileProfile(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllTMProfiles(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String addComment(java.lang.String p_accessToken, long p_objectId, int p_objectType, java.lang.String p_userId, java.lang.String p_comment, byte[] p_file, java.lang.String p_fileName, java.lang.String p_access) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllProjects(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobAttribute(java.lang.String accessToken, long jobId, java.lang.String attInternalName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void setJobAttribute(java.lang.String accessToken, long jobId, java.lang.String attInternalName, java.lang.Object value) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.util.HashMap searchEntriesInBatch(java.lang.String p_accessToken, java.lang.Long p_remoteTmProfileId, java.util.HashMap p_segmentMap, java.lang.Long p_sourceLocaleId, java.util.HashMap p_btrgLocal2LevLocalesMap, java.lang.Boolean p_translatable, java.lang.Boolean p_escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String isSupportCurrentLocalePair(java.lang.String p_accessToken, java.lang.String p_fileProfileId, java.lang.String p_srcLangCountry, java.lang.String p_trgLangCountry) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUniqueJobName(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUniqueJobName(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadOriginalSourceFile(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createEditionJob(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.JobException, com.globalsight.www.webservices.WebServiceException, com.globalsight.www.webservices.GeneralException;
    public void sendSegmentCommentBack(java.lang.String p_accessToken, java.util.HashMap p_segmentComments) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String cancelJobById(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAcceptedTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getCurrentTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getTasksInJob(java.lang.String p_accessToken, long p_jobId, java.lang.String p_taskName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllProjectsByUser(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createJob(java.lang.String accessToken, java.lang.String jobName, java.lang.String comment, java.lang.String filePaths, java.lang.String fileProfileIds, java.lang.String targetLocales, java.lang.String attributeXml) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createJob(java.lang.String accessToken, java.lang.String jobName, java.lang.String comment, java.lang.String filePaths, java.lang.String fileProfileIds, java.lang.String targetLocales) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createJob(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobAndWorkflowInfo(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getLocalizedDocuments(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_wfId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getLocalizedDocuments(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String exportWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String exportJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUserUnavailabilityReport(java.lang.String p_accessToken, java.lang.String p_activityName, java.lang.String p_sourceLocale, java.lang.String p_targetLocale, int p_month, int p_year) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String passDCTMAccount(java.lang.String p_accessToken, java.lang.String docBase, java.lang.String dctmUserName, java.lang.String dctmPassword) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createDocumentumJob(java.lang.String p_accessToken, java.lang.String jobName, java.lang.String fileProfileId, java.lang.String objectId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void cancelDocumentumJob(java.lang.String p_accessToken, java.lang.String objectId, java.lang.String jobId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getDownloadableJobs(java.lang.String p_accessToken, java.lang.String p_msg) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String helloWorld() throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getFileProfileInformation(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllActivityTypes(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllUsers(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllLocalePairs(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getLocalizedDocuments_old(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getFileProfileInfoEx(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllTermbases(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void saveTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_sourceLocale, java.lang.String p_sourceTerm, java.lang.String p_targetLocale, java.lang.String p_targetTerm) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void editTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_sourceLocale, java.lang.String p_sourceTerm, java.lang.String p_targetLocale, java.lang.String p_targetTerm, java.lang.Object connection) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String searchTBEntries(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_searchString, java.lang.String p_sourceLocale, java.lang.String p_targetLocale, double p_matchType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void deleteTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_searchString, java.lang.String p_sourceLocale, java.lang.String p_targetLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getFirstTu(java.lang.String accessToken, java.lang.String tmName, java.lang.String companyName, java.lang.String sourceLocale, java.lang.String targetLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String nextTus(java.lang.String accessToken, java.lang.String sourceLocale, java.lang.String targetLocale, java.lang.String maxSize, java.lang.String tuIdToStart) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String nextTus(java.lang.String accessToken, java.lang.String tmName, java.lang.String companyName, java.lang.String sourceLocale, java.lang.String targetLocale, java.lang.String maxSize, java.lang.String tuIdToStart) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String editTu(java.lang.String accessToken, java.lang.String tmx) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String editTu(java.lang.String accessToken, java.lang.String tmName, java.lang.String companyName, java.lang.String tmx) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllPermissionsByUser(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getSourceLocales(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getPriorityByID(java.lang.String p_accessToken, java.lang.String p_l10NID) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAttributesByJobId(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAttributesByJobId(java.lang.String p_accessToken, java.lang.Long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAttributesByProjectId(java.lang.String p_accessToken, long p_projectId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public long getProjectIdByFileProfileId(java.lang.String p_accessToken, long p_fileProfileId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public long getProjectIdByFileProfileId(java.lang.String p_accessToken, java.lang.Long p_fileProfileId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void uploadAttributeFiles(java.lang.String p_accessToken, java.lang.String jobName, java.lang.String attInternalName, java.lang.String fileName, byte[] bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadCommentReferenceFiles(java.util.HashMap p_args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadEditionFileBack(java.lang.String p_accessToken, java.lang.String p_originalTaskId, java.lang.String p_fileName, byte[] p_bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String importOfflineTargetFiles(java.lang.String p_accessToken, java.lang.String p_originalTaskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobsByTimeRange(java.lang.String accessToken, java.lang.String startTime) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobsByTimeRange(java.lang.String accessToken, java.lang.String startTime, long projectId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchWorkflowRelevantInfoByJobs(java.lang.String p_accessToken, java.lang.String jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getTasksInJobs(java.lang.String p_accessToken, java.lang.String jobIds, java.lang.String p_taskName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllL10NProfiles(java.lang.String accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getWorkflowPath(java.lang.String p_accessToken, long workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String downloadXliffOfflineFile(java.lang.String accessToken, java.lang.String taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String downloadXliffOfflineFile(java.lang.String accessToken, java.lang.String taskId, java.lang.String lockedSegEditType, boolean isIncludeXmlNodeContextInformation) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getAllProjectTMs(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void createJobOnInitial(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException, com.globalsight.www.webservices.GeneralException;
    public java.lang.String uploadFileForInitial(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobStatus(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobExportFiles(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobExportWorkflowFiles(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String workflowLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String cancelJobs(java.lang.String p_accessToken, java.lang.String p_jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getImportExportStatus(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getGSVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String importOfflineKitFiles(java.lang.String p_accessToken, java.lang.String p_originalTaskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getOfflineFileUploadStatus(java.lang.String accessToken, java.lang.String taskId, java.lang.String filename) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String isExistedPermission(java.lang.String p_accessToken, java.lang.String p_permissionName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchCompanyInfo(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobIdsPerCompany(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsByRange(java.lang.String p_accessToken, int p_offset, int p_count, boolean p_isDescOrder) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsByState(java.lang.String p_accessToken, java.lang.String p_state, int p_offset, int p_count, boolean p_isDescOrder) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsByCreator(java.lang.String p_accessToken, java.lang.String p_creatorUserName, int p_offset, int p_count, boolean p_isDescOrder) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsPerCompany(java.lang.String p_accessToken, java.lang.String[] p_jobIds, boolean p_returnSourcePageInfo, boolean p_returnWorkflowInfo, boolean p_returnJobAttributeInfo) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsPerCompany(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchJobsPerCompany(java.lang.String p_accessToken, java.lang.String[] p_jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchWorkflowRelevantInfo(java.lang.String p_accessToken, java.lang.String p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String fetchFileForPreview(java.lang.String p_accessToken, java.lang.String p_jobId, java.lang.String p_targetLocaleId, java.lang.String p_sourcePageId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getCommentFiles(java.lang.String p_accessToken, java.lang.String p_commentObjectType, java.lang.String p_jobOrTaskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadTmxFile(java.lang.String p_accessToken, java.lang.String p_fileName, java.lang.String p_tmName, byte[] p_contentsInBytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public void importTmxFile(java.lang.String p_accessToken, java.lang.String p_tmName, java.lang.String p_syncMode) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String jobsSkipActivity(java.lang.String p_accessToken, java.lang.String p_workflowId, java.lang.String p_activity) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String taskReassign(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String[] p_users) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String jobsAddLanguages(java.lang.String p_accessToken, long p_jobId, java.lang.String p_wfInfos) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String jobsWorkflowCanBeAdded(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateTranslationEditReport(java.lang.String p_accessToken, java.lang.String p_jobId, java.lang.String p_targetLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateCharacterCountReport(java.lang.String p_accessToken, java.lang.String p_jobIds, java.lang.String p_targetLocales) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateReviewersCommentReport(java.lang.String p_accessToken, java.lang.String p_jobIds, java.lang.String p_targetLocales, boolean p_includeCompactTags) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateReviewersCommentSimplifiedReport(java.lang.String p_accessToken, java.lang.String p_jobIds, java.lang.String p_targetLocales, boolean p_includeCompactTags) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateDITAQAReport(java.lang.String p_accessToken, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String generateQAChecksReport(java.lang.String p_accessToken, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType, java.lang.String p_fileName, byte[] bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String importWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, java.lang.String p_identifyKey, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String createJobGroup(java.lang.String p_accessToken, java.lang.String groupName, java.lang.String projectName, java.lang.String sourceLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String addJobToGroup(java.lang.String p_accessToken, java.lang.String groupId, java.lang.String jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getTmExportStatus(java.lang.String p_accessToken, java.lang.String p_identifyKey) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String exportTM(java.lang.String p_accessToken, java.lang.String p_tmName, java.lang.String p_languages, java.lang.String p_startDate, java.lang.String p_finishDate, java.lang.String p_exportFormat, java.lang.String p_exportedFileName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUsernameFromSession(java.lang.String p_accessToken) throws java.rmi.RemoteException;
}
