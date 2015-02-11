/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.OfflineEditorManagerException;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.taskmanager.TaskSearchParameters;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.SearchHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowJbpmPersistenceHandler;
import com.globalsight.everest.workflow.WorkflowProcessAdapter;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * TaskListHandler is responsible for: 1. Displaying a list of tasks based on
 * the state. There are currently four states: Available tasks -- In Progress
 * tasks -- Finished tasks -- Rejected tasks -- 2. Sorting on the column user
 * clicks on.
 */
public class TaskListHandler extends PageHandler
{
    private static final Logger log = Logger.getLogger(TaskListHandler.class);

    public static final String TASK_SEARCH_RESULT = "taskSearchResults_";
    private static JobHandler jobHandler = null;

    private static List<String> specialStates = new ArrayList<String>();
    static
    {
        specialStates.add(Job.ADD_FILE);
        specialStates.add(Job.DELETE_FILE);
        specialStates.add(Job.BATCHRESERVED);
        
        try
        {
            jobHandler = ServerProxy.getJobHandler();
        }
        catch (Exception e)
        {
            log.error("Cannot get jobHandler", e);
        }
    }

    private static final String BASE_BEAN = "tasks";

    //
    // PUBLIC CONSTRUCTORS
    //
    public TaskListHandler()
    {
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     * @throws NamingException
     * @throws GeneralException
     * @throws TaskException
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            TaskException, GeneralException
    {
        boolean errorOccur = false;
        StringBuffer errorMsg = new StringBuffer(
                "Downloading offline toolkit failed with exception : ");
        HttpSession sess = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) sess
                .getAttribute(SESSION_MANAGER);
        User user = TaskHelper.getUser(sess);
        String action = p_request.getParameter(TASK_ACTION);
        List<OEMProcessStatus> statusList = new ArrayList<OEMProcessStatus>();
        if (TAST_ACTION_DOWNLOADALL.equals(action))
        {
            try
            {
                downloadAllOfflineFiles(sess, user, p_request, p_response, statusList);
                return;
            }
            catch (Exception e)
            {
                log.error(e.toString(), e);
                errorOccur = true;
                if (statusList != null && statusList.size() > 0)
                {
                    OEMProcessStatus ops = statusList.get(0);
                    errorMsg.append("<br /><br />");
                    errorMsg.append(refineErrorMsg(e));
                    ops.setErrorOccured(true);
                    ops.speakRed(ops.getCounter(), errorMsg.toString(), null);
                    p_response.reset();
                    p_response.setStatus(204);
                    p_response.flushBuffer();
                    return;
                }
                else
                {
                    errorMsg.append("\\r\\n\\r\\n");
                    errorMsg.append(refineErrorMsg(e));
                }
            }
        }
        else if (TAST_ACTION_DOWNLOADALL_COMBINED.equals(action))
        {
            try
            {
                downloadAllOfflineFilesCombined(sess, user, p_request, p_response, statusList);
                return;
            }
            catch (Exception e)
            {
                log.error(e.toString(), e);
                errorOccur = true;
                if (statusList != null && statusList.size() > 0)
                {
                    OEMProcessStatus ops = statusList.get(0);
                    errorMsg.append("<br /><br />");
                    errorMsg.append(refineErrorMsg(e));
                    ops.setErrorOccured(true);
                    ops.speakRed(ops.getCounter(), errorMsg.toString(), null);
                    p_response.reset();
                    p_response.setStatus(204);
                    p_response.flushBuffer();
                    return;
                }
                else
                {
                    errorMsg.append("\\r\\n\\r\\n");
                    errorMsg.append(refineErrorMsg(e));
                }
            }
        }
        else if (TASK_ACTION_GETTASKSTATUS.equals(action))
        {
            StringBuffer result = new StringBuffer();
            Task task = (Task) TaskHelper.retrieveObject(sess, WORK_OBJECT);
            if (task.getIsUploading() == 'Y')
            {
                result.append("{");
                result.append("\"taskId\":").append(task.getId()).append(",");
                result.append("\"uploadStatus\":").append(
                        "\"" + OfflineConstants.TASK_UPLOADSTATUS_UPLOADING
                                + "\"");
                result.append("}");
            }
            else
            {
                result.append("{}");
            }
            // Task task = (Task) TaskHelper.retrieveObject(sess, WORK_OBJECT);
            // long taskId = task.getId();
            // Map<Long, TaskBO> taskBOMap = (Map<Long, TaskBO>)
            // TaskHelper.retrieveObject(sess, SESSION_MAP_TASKBO);
            // TaskBO taskBO = null;
            // if (taskBOMap != null)
            // {
            // taskBO = (TaskBO) taskBOMap.get(taskId);
            // }
            // String result = TaskHelper.getTaskStatus(taskBO);
            p_response.getWriter().write(result.toString());
            return;
        }
        // for GBS-1939
        else if (TASK_ACTION_SELECTED_TASKSSTATUS.equals(action))
        {
            String result;
            result = selectedTasksStatusJSON(p_request, user);
            p_response.getWriter().write(result);
            return;
        }
        // Zero word count task should be ignored when offline download.
        else if ("filterZeroWCTasksForOfflineDownload".equals(action))
        {
            String result = filterZeroWorcCountTasksForOfflineDownload(sess,
                    user, p_request);
            p_response.getWriter().write(result);
            return;
        }

        try
        {
            handleTaskRequest(p_request, p_response, sess, user);
        }
        catch (NamingException ne)
        {
            // TODO error info
            log.error(ne.getMessage(), ne);
        }
        selectTasksForDisplay(p_request, sess, user);

        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,
                BASE_BEAN);
        sessionMgr.setAttribute("taskList_errorMsg",
                errorOccur ? errorMsg.toString() : "");

        myInvokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
    
    private String refineErrorMsg(Exception oriExp)
    {
        String oriMsg = oriExp.toString();
        if (oriExp instanceof OfflineEditorManagerException
                && oriMsg != null && oriMsg.contains("<BR>"))
        {
            int index = oriMsg.indexOf("<BR>");
            return oriMsg.substring(0, index);
        }
        
        if (oriExp instanceof OfflineEditorManagerException
                && ((OfflineEditorManagerException) oriExp).getOriginalMessage() != null)
        {
            return ((OfflineEditorManagerException) oriExp).getOriginalMessage();
        }
        
        return oriMsg;
    }

    /**
     * Return job id if jobHandler is null, otherwise return l10n profile id
     * @param jobId
     * @return
     * @throws  
     * @throws Exception 
     */
    public static long getL10nProfileIdByJobId(long jobId)
    {
        if (jobHandler == null)
        {
            return jobId;
        }
        else
        {
            L10nProfile l10nP = null;
            try
            {
                l10nP = jobHandler.getL10nProfileByJobId(jobId);
            }
            catch (Exception e)
            {
                log.error("Cannot get L10nProfile by job id : " + jobId, e);
            }
            
            return l10nP == null ? jobId : l10nP.getId();
        }
    }

    /**
     * Returns the JSP Page to use as the Error Page if an error happens when
     * using this PageHandler
     */
    public String getErrorPage()
    {
        return WebAppConstants.ACTIVITY_ERROR_PAGE;
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {

        return new TaskSearchControlFlowHelper(p_request, p_response);
    }

    private void handleTaskRequest(HttpServletRequest p_request,
            HttpServletResponse p_response, HttpSession p_session, User p_user)
            throws ServletException, IOException, TaskException,
            GeneralException, NamingException
    {
        String userId = p_user.getUserId();

        String action = p_request.getParameter(TASK_ACTION);

        if (TASK_ACTION_ACCEPTALL.equals(action))
        {
            acceptAllTasks(p_session, p_request, p_user);
        }

        if (TASK_ACTION_FINISH.equals(action))
        {
            Task task = (Task) TaskHelper
                    .retrieveObject(p_session, WORK_OBJECT);
            String nextActivityName = p_request.getParameter("arrow");
            TaskHelper.clearDelayTimeTable(p_session, userId,
                    String.valueOf(task.getId()));
            TaskHelper.completeTask(userId, task, nextActivityName);
            TaskHelper.removeMRUTask(p_request, p_session, task, p_response);
            // Auto-accept next task.
            TaskHelper.autoAcceptNextTask(task, nextActivityName);
        }
        else if (RECREATE_EDITION_JOB.equals(action))
        {
            TaskImpl task = (TaskImpl) TaskHelper.retrieveObject(p_session,
                    WORK_OBJECT);
            WorkflowManagerLocal wfm = new WorkflowManagerLocal();
            wfm.recreateGSEdtionJob(task);
        }
        else if (TASK_ACTION_REJECT.equals(action))
        {
            rejectTask(p_request, p_response, p_session, p_user);
        }
        else if (TASK_ACTION_ACCEPT_AND_DOWNLOAD.equals(action))
        {
            acceptAndDownload(p_request, p_response, p_session, p_user, false);
        }
        else if ("DownloadCombined".equals(action))
        {
            acceptAndDownload(p_request, p_response, p_session, p_user, true);
        }
        else if (TASK_ACTION_BATCH_COMPLETE_WORKFLOW.equals(action))
        {
            final String taskIds = p_request.getParameter("taskParam");
            final String uid = userId;
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    // run the complete workflow process in a new thread in
                    // order to refresh the page quickly without waiting for the
                    // whole process to be completed
                    completeWorkflow(taskIds, uid);
                }
            };
            Thread t = new MultiCompanySupportedThread(runnable);
            t.start();

        }
        else if (TASK_ACTION_BATCH_COMPLETE_ACTIVITY.equals(action))
        {
            completeActivity(p_session, p_request, p_response, p_user);
        }
        else
        {
            action = p_request.getParameter("action");
            if ("save".equals(action))
            {
                saveReplace(p_request, p_session);
            }
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void downloadAllOfflineFilesCombined(HttpSession session, User user,
            HttpServletRequest request, HttpServletResponse response,
            List<OEMProcessStatus> statusList) throws GeneralException,
            NamingException, IOException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.removeElement(DOWNLOAD_STATUS);
        Vector<String> downloadOfflineFilesOptions = getDownloadOption(session);
        SendDownloadFileHelper help = new SendDownloadFileHelper();
        Locale locale = (Locale) session.getAttribute(UILOCALE);
        String uiLocale = locale.getLanguage() + "_" + locale.getCountry();

        int fileFormat = help.getFileFormat(downloadOfflineFilesOptions.get(0));
        int editorId = help.getEditorId(downloadOfflineFilesOptions.get(1));
        String encoding = downloadOfflineFilesOptions.get(2);
        int ptagFormat = help.getPtagFormat(downloadOfflineFilesOptions.get(3));
        int platformId = help.getPlatformId(request, editorId);
        int resInsMode = help
                .getResourceInsertionMode(downloadOfflineFilesOptions.get(4));
        String displayExactMatch = downloadOfflineFilesOptions.get(6);
//        String consolidateTM = downloadOfflineFilesOptions.get(7);
//        String consolidateTerm = downloadOfflineFilesOptions.get(8);
        String terminology = downloadOfflineFilesOptions.get(9);
        // populate 100
        String populate100 = downloadOfflineFilesOptions.get(10);
        // populate fuzzy target segments (only for Bilingual RTF)
        String populateFuzzy = downloadOfflineFilesOptions.get(11);
        // need consolidate output file (for XLF format)
//        String consolidateXLF = downloadOfflineFilesOptions.get(12);
        String changeCreationIdForMt = downloadOfflineFilesOptions.get(13);
        String includeRepetitions = downloadOfflineFilesOptions.get(14);

        File tmpFile = File.createTempFile("GSDownloadAllOffline", null);
        JobPackageZipper zipper = new JobPackageZipper();
        zipper.createZipFile(tmpFile);
        TaskSearchParameters params = new TaskSearchParameters();
        params.setUser(user);
        params.setSessionId(session.getId());
        params.setActivityState(new Integer(Task.STATE_ACCEPTED));
        Collection<Task> tasks = null;
        try
        {
            tasks = ServerProxy.getTaskManager().getTasks(params);
        }
        catch (Exception e)
        {
            log.error("Can not find any task!");
        }

        List<Long> alljobIds = new ArrayList<Long>();
        List<Long> alltaskIds = new ArrayList<Long>();
        List<Long> alljobIdsSort = new ArrayList<Long>();
        List<Job> alljobs = new ArrayList<Job>();
        List<Long> allpageIdList = new ArrayList<Long>();
        List<String> allpageNameList = new ArrayList<String>();
        List<Boolean> allcanUseUrlList = new ArrayList<Boolean>();
        List<Long> allprimarySourceFiles = new ArrayList<Long>();
        List<Long> allstfList = new ArrayList<Long>();
        List allsupportFileList = new  ArrayList();
        HashMap<Long, Long> allPSF_tasks = new HashMap<Long, Long>();
        HashMap<Long, Long> allSTF_tasks = new HashMap<Long, Long>();
        HashMap<Long, Long> allPage_tasks = new HashMap<Long, Long>();
        
        Vector excludeTypes = null;
        int downloadEditAll = 0;
        GlobalSightLocale srcLocale = null;
        GlobalSightLocale targetLocale = null;
        String targetLocaleStr = null;
        long workflowId = -1;

        String taskIds = request.getParameter("taskParam");
        List<Long> selectedTaskIds = getSelectedTaskIds(taskIds);
        for (Task task : tasks)
        {
            // GBS-630: verify whether the activity is checked on UI.
            if (!selectedTaskIds.contains(task.getId()))
            {
                continue;
            }

            long jobId = task.getJobId();
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            alljobIds.add(jobId);
            alljobIdsSort.add(jobId);
            alljobs.add(job);
            alltaskIds.add(task.getId());

            List<Long> pageIdList = new ArrayList<Long>();
            List<String> pageNameList = new ArrayList<String>();
            List<Boolean> canUseUrlList = new ArrayList<Boolean>();

            help.getAllPageIdList(task, pageIdList, pageNameList);
            if (pageIdList != null && pageIdList.size() <= 0)
            {
                pageIdList = null;
                pageNameList = null;
            }

            // can use url list (legacy stuff we never used but are
            // keeping for the future)
            if (pageIdList != null)
            {
                for (int i = 0; i < pageIdList.size(); i++)
                {
                    canUseUrlList.add(Boolean.FALSE);
                    allPage_tasks.put(pageIdList.get(i), task.getId());
                }
                
                allpageIdList.addAll(pageIdList);
                allpageNameList.addAll(pageNameList);
                allcanUseUrlList.addAll(canUseUrlList);
            }
            
            if (excludeTypes == null)
            {
                workflowId = task.getWorkflow().getId();
                L10nProfile l10nProfile = task.getWorkflow().getJob().getL10nProfile();
                if (l10nProfile.getTmChoice() == LocProfileStateConstants.ALLOW_EDIT_TM_USAGE)
                {
                    downloadEditAll = help.getEditAllState(downloadOfflineFilesOptions.get(15),
                            l10nProfile);
                }
                excludeTypes = l10nProfile.getTranslationMemoryProfile().getJobExcludeTuTypes();
                targetLocaleStr = task.getTargetLocale().toString();
                srcLocale = task.getSourceLocale();
                targetLocale = task.getTargetLocale();
            }
            List<Long> primarySourceFiles = help.getAllPSFList(task);
            List<Long> stfList = help.getAllSTFList(task);
            List supportFileList = help.getAllSupportFileList(task);
            
            if (primarySourceFiles != null)
            {
                allprimarySourceFiles.addAll(primarySourceFiles);
                
                for (Long uuid : primarySourceFiles)
                {
                    allPSF_tasks.put(uuid, task.getId());
                }
            }
            if (stfList != null)
            {
                allstfList.addAll(stfList);
                
                for (Long uuid : stfList)
                {
                    allSTF_tasks.put(uuid, task.getId());
                }
            }
            if (supportFileList != null)
                allsupportFileList.addAll(supportFileList);
        }
        
        Collections.sort(alljobIdsSort);
        String jobName = "Jobs_" + alljobIdsSort.get(0) + "-"
                + alljobIdsSort.get(alljobIdsSort.size() - 1);
        
        if (allprimarySourceFiles.size() == 0)
            allprimarySourceFiles = null;
        if (allstfList.size() == 0)
            allstfList = null;
        if (allsupportFileList.size() == 0)
            allsupportFileList = null;
        
        // create combined offline files
        DownloadParams downloadParams = new DownloadParams(jobName, null,
                "", Long.toString(workflowId), null,
                allpageIdList, allpageNameList, allcanUseUrlList,
                allprimarySourceFiles, allstfList, editorId, platformId,
                encoding, ptagFormat, uiLocale, srcLocale,
                targetLocale, true, fileFormat, excludeTypes,
                downloadEditAll, allsupportFileList, resInsMode, user);
        downloadParams.setConsolidateTmxFiles(true);
        downloadParams.setConsolidateTermFiles(true);
        downloadParams.setTermFormat(terminology);
        downloadParams.setAllJobs(alljobs);
        downloadParams.setAllTaskIds(alltaskIds);
        downloadParams.setAllPage_tasks(allPage_tasks);
        downloadParams.setAllPSF_tasks(allPSF_tasks);
        downloadParams.setAllSTF_tasks(allSTF_tasks);
        downloadParams.setDisplayExactMatch(displayExactMatch);
        downloadParams.setPopulate100("yes".equalsIgnoreCase(populate100));
        downloadParams.setPopulateFuzzy("yes"
                .equalsIgnoreCase(populateFuzzy));
        downloadParams.setNeedConsolidate(true);
        downloadParams.setNeedCombined(true);
        downloadParams.setIncludeRepetitions("yes"
                .equalsIgnoreCase(includeRepetitions));
        downloadParams.setChangeCreationIdForMTSegments("yes"
                .equalsIgnoreCase(changeCreationIdForMt));
        downloadParams.setZipper(zipper);
        
        try
        {
            downloadParams.verify();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        
        OfflineEditManager odm = ServerProxy.getOfflineEditManager();
        OEMProcessStatus status = new OEMProcessStatus(downloadParams);
        statusList.add(status);
        odm.attachListener(status);
        sessionMgr.setAttribute(DOWNLOAD_STATUS, status);
        odm.runProcessDownloadRequest(downloadParams);

        zipper.closeZipFile();
        
        String zipFileName = jobName + "_" + targetLocaleStr + ".zip";
        CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
        commentFilesDownload.sendFileToClient(request, response, zipFileName,
                tmpFile);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void downloadAllOfflineFiles(HttpSession session, User user,
            HttpServletRequest request, HttpServletResponse response,
            List<OEMProcessStatus> statusList) throws GeneralException,
            NamingException, IOException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.removeElement(DOWNLOAD_STATUS);
        Vector<String> downloadOfflineFilesOptions = getDownloadOption(session);
        SendDownloadFileHelper help = new SendDownloadFileHelper();
        Locale locale = (Locale) session.getAttribute(UILOCALE);
        String uiLocale = locale.getLanguage() + "_" + locale.getCountry();

        int fileFormat = help.getFileFormat(downloadOfflineFilesOptions.get(0));
        int editorId = help.getEditorId(downloadOfflineFilesOptions.get(1));
        String encoding = downloadOfflineFilesOptions.get(2);
        int ptagFormat = help.getPtagFormat(downloadOfflineFilesOptions.get(3));
        int platformId = help.getPlatformId(request, editorId);
        int resInsMode = help
                .getResourceInsertionMode(downloadOfflineFilesOptions.get(4));
        String displayExactMatch = downloadOfflineFilesOptions.get(6);
        String consolidateTM = downloadOfflineFilesOptions.get(7);
        String consolidateTerm = downloadOfflineFilesOptions.get(8);
        String terminology = downloadOfflineFilesOptions.get(9);
        // populate 100
        String populate100 = downloadOfflineFilesOptions.get(10);
        // populate fuzzy target segments (only for Bilingual RTF)
        String populateFuzzy = downloadOfflineFilesOptions.get(11);
        // need consolidate output file (for XLF format)
        String consolidateXLF = downloadOfflineFilesOptions.get(12);
        String changeCreationIdForMt = downloadOfflineFilesOptions.get(13);
        String includeRepetitions = downloadOfflineFilesOptions.get(14);

        File tmpFile = File.createTempFile("GSDownloadAllOffline", null);
        String zipFileName = "DownloadAllOfflineFiles.zip";
        JobPackageZipper zipper = new JobPackageZipper();
        zipper.createZipFile(tmpFile);

        TaskSearchParameters params = new TaskSearchParameters();
        params.setUser(user);
        params.setSessionId(session.getId());
        params.setActivityState(new Integer(Task.STATE_ACCEPTED));
        Collection<Task> tasks = null;
        try
        {
            tasks = ServerProxy.getTaskManager().getTasks(params);
        }
        catch (Exception e)
        {
            log.error("Can not find any task!");
        }

        String taskIds = request.getParameter("taskParam");
        List<Long> selectedTaskIds = getSelectedTaskIds(taskIds);

        int taskTotalSize = selectedTaskIds.size() != 0 ? selectedTaskIds.size()
                : tasks.size();
        int taskCounter = 0;
        OEMProcessStatus status = new OEMProcessStatus();
        statusList.add(status);
        for (Task task : tasks)
        {
            // GBS-630: verify whether the activity is checked on UI
            if (!selectedTaskIds.contains(task.getId()))
            {
                continue;
            }

            List<Long> pageIdList = new ArrayList<Long>();
            List<String> pageNameList = new ArrayList<String>();
            List<Boolean> canUseUrlList = new ArrayList<Boolean>();

            help.getAllPageIdList(task, pageIdList, pageNameList);
            if (pageIdList != null && pageIdList.size() <= 0)
            {
                pageIdList = null;
                pageNameList = null;
            }

            // can use url list (legacy stuff we never used but are
            // keeping for the future)
            if (pageIdList != null)
            {
                for (int i = 0; i < pageIdList.size(); i++)
                {
                    canUseUrlList.add(Boolean.FALSE);
                }
            }

            long workflowId = task.getWorkflow().getId();
            L10nProfile l10nProfile = task.getWorkflow().getJob()
                    .getL10nProfile();
            int downloadEditAll = 4;
            if (l10nProfile.getTmChoice() == LocProfileStateConstants.ALLOW_EDIT_TM_USAGE)
            {
                downloadEditAll = help.getEditAllState(downloadOfflineFilesOptions.get(15),
                        l10nProfile);
            }
            Vector excludeTypes = l10nProfile.getTranslationMemoryProfile()
                    .getJobExcludeTuTypes();
            List primarySourceFiles = help.getAllPSFList(task);
            List stfList = help.getAllSTFList(task);
            List supportFileList = help.getAllSupportFileList(task);

            // String srcLocale = task.getSourceLocale().getLanguageCode() + "_"
            // + task.getSourceLocale().getCountryCode();
            // String targetLocale = task.getTargetLocale().getLanguage() + "_"
            // + task.getTargetLocale().getCountryCode();

            DownloadParams downloadParams = new DownloadParams(task.getJobName(), null,
                    "", Long.toString(workflowId), Long.toString(task.getId()),
                    pageIdList, pageNameList, canUseUrlList,
                    primarySourceFiles, stfList, editorId, platformId,
                    encoding, ptagFormat, uiLocale, task.getSourceLocale(),
                    task.getTargetLocale(), true, fileFormat, excludeTypes,
                    downloadEditAll, supportFileList, resInsMode, user);
            downloadParams.setConsolidateTmxFiles("yes"
                    .equalsIgnoreCase(consolidateTM));
            downloadParams.setConsolidateTermFiles("yes"
                    .equalsIgnoreCase(consolidateTerm));
            downloadParams.setTermFormat(terminology);
            downloadParams
                    .setJob(ServerProxy.getJobHandler().getJobById(task.getJobId()));
            downloadParams.setDisplayExactMatch(displayExactMatch);
            downloadParams.setPopulate100("yes".equalsIgnoreCase(populate100));
            downloadParams.setPopulateFuzzy("yes"
                    .equalsIgnoreCase(populateFuzzy));
            downloadParams.setNeedConsolidate("yes"
                    .equalsIgnoreCase(consolidateXLF));
            downloadParams.setIncludeRepetitions("yes"
                    .equalsIgnoreCase(includeRepetitions));
            downloadParams.setChangeCreationIdForMTSegments("yes"
                    .equalsIgnoreCase(changeCreationIdForMt));
            
            downloadParams.setZipper(zipper);
            try
            {
                downloadParams.verify();
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            
            taskCounter++;
            if (taskCounter < taskTotalSize)
            {
                status.setMultiTasks(true);
                status.setTaskCounter(taskCounter);
                status.setTaskTotalSize(taskTotalSize);
                status.setCounter(taskCounter);
                status.setTotalFiles(taskTotalSize);
            }
            else
            {
                int curSize = OEMProcessStatus.findNumberOfFiles(downloadParams);
                status.setMultiTasks(false);
                status.setCounter((taskTotalSize - 1) * curSize);
                status.setTotalFiles(taskTotalSize * curSize);
                status.setPercentage(status.getTaskPercentage());
            }
            sessionMgr.setAttribute(DOWNLOAD_STATUS, status);
            
            OfflineEditManager odm = ServerProxy.getOfflineEditManager();
            odm.attachListener(status);
            odm.runProcessDownloadRequest(downloadParams);
        }

        zipper.closeZipFile();
        CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
        commentFilesDownload.sendFileToClient(request, response, zipFileName,
                tmpFile);
    }

    private Vector<String> getDownloadOption(HttpSession session)
    {
        Vector<String> downloadOptions = new Vector<String>();
        for (int i = 0; i < DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                .size(); i++)
        {
            String downloadOption = DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTIONS
                    .get(i);
            downloadOptions.add(getUserParameter(session, downloadOption)
                    .getValue());
        }
        return downloadOptions;
    }

    /**
     * Completes all the activities to end the workflow.
     */
    private void completeWorkflow(String taskIds, String userId)
    {
        StringTokenizer tokenizer = new StringTokenizer(taskIds);
        try
        {
            while (tokenizer.hasMoreTokens())
            {
                String taskId = tokenizer.nextToken();
                TaskImpl task = HibernateUtil.get(TaskImpl.class,
                        Long.parseLong(taskId));
                if (task != null)
                {
                    int taskState = task.getState();

                    WorkflowTaskInstance wti = ServerProxy.getWorkflowServer()
                            .getWorkflowTaskInstance(
                                    task.getWorkflow().getId(), task.getId());
                    task.setWorkflowTask(wti);
                    if (taskState == Task.STATE_ACTIVE)
                    {
                        // accept current activity
                        ServerProxy.getTaskManager().acceptTask(userId, task,
                                true);
                    }

                    // to complete the next activities in the default path
                    // the last element is the arrow to Exit node
                    List<Object> nextNodesInDefaultPath = WorkflowProcessAdapter
                            .nextNodesInDefaultPath(task.getWorkflow().getId(),
                                    wti.getTaskId());
                    if (nextNodesInDefaultPath.size() > 1)
                    {
                        // complete current activity
                        ServerProxy.getTaskManager().completeTask(userId, task,
                                null, "SKIPPING");
                        saveSkipVariable(task);

                        for (int i = 0; i < nextNodesInDefaultPath.size(); i++)
                        {
                            Object next = nextNodesInDefaultPath.get(i);
                            if (next instanceof WorkflowTaskInstance)
                            {
                                WorkflowTaskInstance nextWti = (WorkflowTaskInstance) next;
                                TaskImpl nextTask = HibernateUtil.get(
                                        TaskImpl.class,
                                        new Long(nextWti.getTaskId()));
                                nextTask.setWorkflowTask(nextWti);

                                ServerProxy.getTaskManager().acceptTask(userId,
                                        nextTask, true);

                                if (i == nextNodesInDefaultPath.size() - 2)
                                {
                                    // complete this activity to Exit
                                    WorkflowArrow arrow = (WorkflowArrow) nextNodesInDefaultPath
                                            .get(nextNodesInDefaultPath.size() - 1);
                                    ServerProxy.getTaskManager().completeTask(
                                            userId, nextTask, arrow.getName(),
                                            "LAST_SKIPPING");
                                }
                                else
                                {
                                    ServerProxy.getTaskManager().completeTask(
                                            userId, nextTask, null, "SKIPPING");
                                }
                                saveSkipVariable(nextTask);
                            }
                        }
                    }
                    else
                    {
                        // complete current activity as the last one, to Exit
                        if (nextNodesInDefaultPath.size() == 1)
                        {
                            WorkflowArrow arrow = (WorkflowArrow) nextNodesInDefaultPath
                                    .get(0);
                            ServerProxy.getTaskManager().completeTask(userId,
                                    task, arrow.getName(), "LAST_SKIPPING");
                            saveSkipVariable(task);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new TaskException(TaskException.MSG_FAILED_TO_COMPLETE_TASK,
                    null, e);
        }
    }

    /**
     * Saves the skipped activity variable to database.
     */
    private void saveSkipVariable(Task task)
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        try
        {
            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                    .getTaskInstance(task.getId(), ctx);
            WorkflowJbpmPersistenceHandler.saveSkipVariable(taskInstance, task
                    .getWorkflow().getId());
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * for GBS-1939 Get selectedTasksStatus,and send it to client by JSON data
     */
    private String selectedTasksStatusJSON(HttpServletRequest p_request,
            User p_user)
    {
        String taskIds = p_request.getParameter("taskParam");
        StringTokenizer tokenizer = new StringTokenizer(taskIds);
        StringBuffer isFinishedTaskId = new StringBuffer();
        StringBuffer isUploadingJobName = new StringBuffer();
        try
        {
            while (tokenizer.hasMoreTokens())
            {
                String taskId = tokenizer.nextToken();
                TaskImpl task = HibernateUtil.get(TaskImpl.class,
                        Long.parseLong(taskId));

                if (task != null)
                {
                    if (task.getIsUploading() == 'Y')
                    {
                        isUploadingJobName.append("[JobID:")
                                .append(task.getJobId()).append(",JobName:")
                                .append(task.getJobName()).append("],");
                    }
                    else
                    {
                        isFinishedTaskId.append(taskId).append(" ");
                    }
                }
            }
            String result = "";
            if (isUploadingJobName.length() == 0)
            {
                result = "{\"isFinishedTaskId\":\""
                        + isFinishedTaskId.toString().trim() + "\"}";
            }
            else if (isFinishedTaskId.length() == 0)
            {
                result = "{\"isUploadingJobName\":\""
                        + isUploadingJobName.substring(0,
                                isUploadingJobName.length() - 1) + "\"}";
            }
            else
            {
                result = "{\"isFinishedTaskId\":\""
                        + isFinishedTaskId.toString().trim()
                        + "\",\"isUploadingJobName\":\""
                        + isUploadingJobName.substring(0,
                                isUploadingJobName.length() - 1) + "\"}";
            }

            return result;
        }
        catch (Exception e)
        {
            throw new TaskException(TaskException.MSG_FAILED_TO_COMPLETE_TASK,
                    null, e);
        }
    }

    /**
     * Completes the activity to next one. For a condition node, will advance to
     * the next activity in default path.
     */
    private void completeActivity(HttpSession p_session,
            HttpServletRequest p_request, HttpServletResponse p_response,
            User p_user)
    {
        String userId = p_user.getUserId();
        String taskIds = p_request.getParameter("taskParam");
        StringTokenizer tokenizer = new StringTokenizer(taskIds);
        try
        {
            while (tokenizer.hasMoreTokens())
            {
                String taskId = tokenizer.nextToken();
                TaskImpl task = HibernateUtil.get(TaskImpl.class,
                        Long.parseLong(taskId));

                if (task != null)
                {
                    int taskState = task.getState();
                    TaskHelper.clearDelayTimeTable(p_session, userId, taskId);

                    WorkflowTaskInstance wti = ServerProxy.getWorkflowServer()
                            .getWorkflowTaskInstance(
                                    task.getWorkflow().getId(), task.getId());
                    task.setWorkflowTask(wti);
                    if (taskState == Task.STATE_ACTIVE)
                    {
                        // accept current activity
                        ServerProxy.getTaskManager().acceptTask(userId, task,
                                false);
                    }

                    ServerProxy.getTaskManager().completeTask(userId, task,
                            null, null);
                    TaskHelper.removeMRUTask(p_request, p_session, task,
                            p_response);

                    // Auto-accept next task.
                    TaskHelper.autoAcceptNextTask(task, null);
                }
            }
        }
        catch (Exception e)
        {
            throw new TaskException(TaskException.MSG_FAILED_TO_COMPLETE_TASK,
                    null, e);
        }
    }

    /**
     * Accept all tasks which state is Task.STATE_ACTIVE
     * 
     * @param p_session
     * @param user
     * @throws TaskException
     * @throws RemoteException
     * @throws GeneralException
     * @throws NamingException
     */
    private void acceptAllTasks(HttpSession p_session,
            HttpServletRequest p_request, User p_user) throws TaskException,
            RemoteException, GeneralException, NamingException
    {
        TaskSearchParameters params = new TaskSearchParameters();
        params.setUser(p_user);
        params.setSessionId(p_session.getId());
        params.setActivityState(new Integer(Task.STATE_ACTIVE));

        String taskIds = p_request.getParameter("taskParam");
        StringTokenizer tokenizer = new StringTokenizer(taskIds);
        while (tokenizer.hasMoreTokens())
        {
            String taskId = tokenizer.nextToken();
            TaskImpl task = HibernateUtil.get(TaskImpl.class,
                    Long.parseLong(taskId));
            if (task != null)
            {
                WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
                        .getWorkflowTaskInstance(p_user.getUserId(),
                                task.getId(), Task.STATE_ACTIVE);
                task.setWorkflowTask(wfTask);

                if (!task.getAllAssignees().contains(p_user.getUserId()))
                    continue;

                if (task.getState() != Task.STATE_ACTIVE)
                    continue;

                TaskHelper.acceptTask(p_user.getUserId(), task);
            }
        }
    }

    /**
     * Iterates through the tasks and returns a set of all the langs for the
     * tasks
     * 
     * @param p_tasks
     *            -- list of tasks
     * @return HashSet
     */
    private HashSet<String> getTaskLangs(List<Task> p_tasks)
    {
        HashSet<String> taskLangs = new HashSet<String>();
        if (p_tasks == null)
            return taskLangs;

        for (Task t : p_tasks)
        {
            Workflow wf = t.getWorkflow();
            if (wf != null)
            {
                taskLangs.add(wf.getTargetLocale().toString());
            }
        }

        return taskLangs;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void getTasks(HttpServletRequest p_request, HttpSession p_session,
            User p_user) throws ServletException, IOException,
            EnvoyServletException

    {
        int taskState = 0;
        boolean translatorLogin = false;
        String listType = (String) p_request.getParameter("listType");
        String taskStatus = (String) p_request.getParameter("taskStatus");
        Integer previousTaskState = (Integer) p_session
                .getAttribute(TASK_STATE);
        if (previousTaskState == null)
        {
            previousTaskState = new Integer(Task.STATE_ACTIVE);
            if (listType == null)
            {
                // Must be translator login.
                translatorLogin = true;
            }
        }

        // get request values
        if (listType != null)
        {
            p_request.setAttribute("listType", listType);
            if (listType.equals("stateOnly"))
            {
                taskState = Integer
                        .parseInt(p_request.getParameter(TASK_STATE));
            }
            else if (listType.equals("advSearch")
                    || listType.equals("miniSearch"))
            {
                taskState = extractIntegerValue(
                        p_request
                                .getParameter(JobSearchConstants.STATUS_OPTIONS),
                        previousTaskState.intValue());
            }
            else if (listType.equals("lastSearch"))
            {
                Integer lastTaskSearch = (Integer) p_session
                        .getAttribute("lastTaskSearchState");
                taskState = lastTaskSearch.intValue();
            }
        }
        else
        {
            // From anything but search, last search, state menu, or sort
            taskState = previousTaskState.intValue();

            // listType is not "all states".
            if (taskState > 0 && taskStatus != null)
            {

                taskState = Integer.parseInt(taskStatus);
                previousTaskState = new Integer(taskState);
            }
        }

        int sortColumn = extractIntegerValue(
                p_request.getParameter(MYACT_COL_SORT_ID), -1);

        int isRefresh = extractIntegerValue(p_request.getParameter(IS_REFRESH),
                0);

        String isPaging = (String) p_request.getParameter(TASK_LIST_START);

        // get session values - used to remember and switch sort directions
        Integer previousSortColumn = (Integer) p_session
                .getAttribute(MYACT_COL_SORT_ID);
        Boolean sortAsc = (Boolean) p_session.getAttribute(MYACT_SORT_ASC);
        String request_sortAsc = p_request.getParameter(MYACT_SORT_ASC);

        // Toggle sort direction:
        // The Sort direction is now ALWAYS present on the request.
        // The sort arrow links built in the JSP submit the reverse sort order
        // Toggle sort direction if:
        // - NOT invoked from a navagation/menu selection (sortColumn = -1)
        // - previous sort values ARE defined
        // - NOT an auto-refresh request
        // - NOT clicking on the paging widget
        // - and finally, this **IS** the same column and screen as before
        if (sortAsc != null && request_sortAsc != null
                && !sortAsc.toString().equals(request_sortAsc)
                && previousSortColumn != null && previousTaskState != null
                && isRefresh != 1 && isPaging == null
                && sortColumn == previousSortColumn.intValue()
                && taskState == previousTaskState.intValue())
        {
            sortAsc = new Boolean(!sortAsc.booleanValue());
            // remember it across the session
            p_session.setAttribute(MYACT_SORT_ASC, sortAsc);
        }
        else
        // init or reset sort values
        {
            // task state - always reset
            previousTaskState = new Integer(taskState);
            p_session.setAttribute(TASK_STATE, previousTaskState);

            // sort column
            if (sortColumn == -1)
            {
                // create default sort column if not present
                if (previousSortColumn == null)
                {
                    previousSortColumn = new Integer(
                            WorkflowTaskDataComparator.JOB_ID);
                    p_session.setAttribute(MYACT_COL_SORT_ID,
                            previousSortColumn);
                }
                // if sortColumn == -1 then set sort col to previous
                sortColumn = previousSortColumn.intValue();
            }
            else
            {
                // reset previous sort column
                previousSortColumn = new Integer(sortColumn);
                p_session.setAttribute(MYACT_COL_SORT_ID, previousSortColumn);
            }

            // session sort direction
            // - only init if undefined
            if (sortAsc == null)
            {
                sortAsc = new Boolean(false); // Ascending == true, Descending
                // == false
                p_session.setAttribute(MYACT_SORT_ASC, sortAsc);
            }

            // initial request sort direction
            if (request_sortAsc == null)
            {
                request_sortAsc = sortAsc.toString();
            }
        }

        String taskListStartStr = p_request.getParameter(TASK_LIST_START);
        int taskListStart = extractIntegerValue(taskListStartStr, 0);
        if (taskListStartStr != null)
        {
            p_session.setAttribute("taskListStart", taskListStart);
        }

        p_request.setAttribute("action", p_request.getParameter("action"));
        List<Task> tasks = null;
        TaskSearchParameters searchParams = null;
        if (listType == null)
        {
            String lastListType = (String) p_session.getAttribute("listType");
            if (lastListType != null
                    && (lastListType.equals("advSearch") || lastListType
                            .equals("miniSearch")))
            {
                // Get search parameters from session
                try
                {
                    searchParams = getSessionSearchParams(p_request, p_session);

                    // Checks state changed or not.
                    Map params = searchParams.getParameters();
                    Integer state = (Integer) params.get(new Integer(
                            TaskSearchParameters.STATE));
                    if (previousTaskState != state)
                    {
                        searchParams.setActivityState(previousTaskState);
                    }
                }
                catch (GeneralException e)
                {
                    throw new EnvoyServletException(e);
                }
            }
        }
        else if (listType.equals("advSearch") || listType.equals("miniSearch"))
        {
            // Get search parameters from request
            try
            {
                searchParams = getRequestSearchParams(p_request, p_session,
                        listType);
            }
            catch (GeneralException e)
            {
                throw new EnvoyServletException(e);
            }
            p_session.setAttribute("listType", listType);
        }
        else if (listType.equals("stateOnly"))
        {
            searchParams = new TaskSearchParameters();
            searchParams.setActivityState(new Integer(taskState));
            p_session.setAttribute("listType", listType);
            p_session.setAttribute("lastState", new Integer(taskState));
        }

        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(USER);
        int tasksPerPage = 20;
        SystemConfiguration sc = SystemConfiguration.getInstance();
        tasksPerPage = sc
                .getIntParameter(SystemConfigParamNames.RECORDS_PER_PAGE_TASKS);
        int start = (Integer) (p_session.getAttribute("taskListStart") == null ? 0
                : p_session.getAttribute("taskListStart"));
        if (searchParams == null)
        {
            searchParams = new TaskSearchParameters();
            searchParams.setActivityState(new Integer(taskState));
        }

        tasks = getTasks(user, searchParams, start, start + tasksPerPage,
                p_request, taskState, sortColumn, sortAsc);

        while (start > 0 && start > tasks.size())
        {
            start -= tasksPerPage;
            p_session.setAttribute("taskListStart", start);
        }

        if (tasks == null)
        {
            tasks = new Vector<Task>();
        }

        if (tasks.size() == 0)
        {
            ResourceBundle bundle = getBundle(p_session);
            if (translatorLogin)
            {
                // Try In Progress as a 2nd choice
                taskState = Task.STATE_ACCEPTED;
                tasks = TaskHelper.getTasks(p_user.getUserId(), taskState);
            }
            else if (listType == null)
            {
                p_request.setAttribute("noresults",
                        bundle.getString("msg_activity_search_no_available")
                                + bundle.getString("msg_activity_search_try"));
            }
            else if (listType.equals("stateOnly"))
            {
                Integer stateI = (Integer) p_session.getAttribute(TASK_STATE);
                int state = stateI.intValue();
                String stateStr = null;
                if (state == Task.STATE_ACTIVE)
                    stateStr = bundle.getString("lb_available");
                else if (state == Task.STATE_ACCEPTED)
                    stateStr = bundle.getString("lb_inprogress");
                else if (state == Task.STATE_COMPLETED)
                    stateStr = bundle.getString("lb_finished");
                else if (state == Task.STATE_REJECTED)
                    stateStr = bundle.getString("lb_rejected");

                p_request.setAttribute(
                        "noresults",
                        bundle.getString("msg_activity_search_no_state") + " '"
                                + stateStr + "'.\n"
                                + bundle.getString("msg_activity_search_try"));
            }
            else
            {
                p_request.setAttribute("noresults",
                        bundle.getString("msg_activity_search_no_match"));
            }
        }
        p_request.setAttribute(MYACT_COL_SORT_ID, new Integer(sortColumn));
        p_request.setAttribute(MYACT_SORT_ASC, request_sortAsc);
        sessionMgr.setAttribute(TASK_LIST, tasks);
        p_request.setAttribute(TASK_STATE, new Integer(taskState));
        p_request.setAttribute(
                "taskListStart",
                p_session.getAttribute("taskListStart") == null ? 0 : p_session
                        .getAttribute("taskListStart"));
        p_request.setAttribute("languageSet", getTaskLangs(tasks));
    }

    /**
     * Removes all tasks if the job state is adding files or delete files
     */
    private void removeSpecialTasks(List<TaskImpl> tasks)
    {

        for (int i = tasks.size() - 1; i >= 0; i--)
        {
            TaskImpl task = tasks.get(i);
            Job job = task.getWorkflow().getJob();
            if (specialStates.contains(job.getState()))
            {
                tasks.remove(task);
            }
        }
    }

    /* Select all tasks that should be displayed under the appropriate tab, */
    /* restricting to those which are part of non-archived workflows */
    private void selectTasksForDisplay(HttpServletRequest p_request,
            HttpSession p_session, User p_user) throws ServletException,
            IOException, EnvoyServletException

    {
        String listType = (String) p_request.getParameter("listType");
        if (listType == null)
        {
            String lastListType = (String) p_session.getAttribute("listType");
            listType = lastListType;
        }
        String isPaging = (String) p_request
                .getParameter(WebAppConstants.TASK_LIST_START);
        String isSort = p_request
                .getParameter(WebAppConstants.MYACT_COL_SORT_ID);
        if (isPaging == null && isSort == null)
        {
            p_session.removeAttribute(TASK_SEARCH_RESULT);
        }

        if (p_request.getParameter("init") != null)
        {
            p_session.setAttribute("taskListStart", 0);
        }

        if ("stateOnly".equals(listType) || "miniSearch".equals(listType)
                || "advSearch".equals(listType))
        {
            getTasks(p_request, p_session, p_user);
            p_session.setAttribute("listType", listType);
        }
        else
        {
            getTasksForDisplay(p_request, p_session, p_user);
        }
    }

    /* Mark the selected task as rejected by the current user; update the */
    /* database and insert a new comment explaining the reason */
    private void rejectTask(HttpServletRequest p_request,
            HttpServletResponse p_response, HttpSession p_session, User p_user)
            throws ServletException, IOException, EnvoyServletException
    {
        Task task = (Task) TaskHelper.retrieveObject(p_session, WORK_OBJECT);
        WorkflowTaskInstance wfti = ((TaskImpl) task).getWorkflowTask();
        task = HibernateUtil.get(TaskImpl.class, task.getId());
        task.setWorkflowTask(wfti);

        String commentInput = p_request.getParameter(TASK_COMMENT);
        String comment = EditUtil.utf8ToUnicode(commentInput);
        TaskHelper.rejectTask(p_user.getUserId(), task, comment);

//        TaskHelper.storeObject(p_session, WebAppConstants.WORK_OBJECT, task);
        HibernateUtil.update(task);
        // As task has been rejected, this task has no relation with current
        // user any more.
		SessionManager sessionMgr = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		sessionMgr.removeElement(WebAppConstants.WORK_OBJECT);
        // update the MRU list with the changed state
        TaskHelper.updateMRUtask(p_request, p_session, task, p_response,
                Task.STATE_REJECTED);
    }

    /* Convert the given string into an integer value; if null, or an error */
    /* occurs, return the given default value instead */
    private int extractIntegerValue(String p_string, int p_defaultValue)
    {
        int intVal = p_defaultValue;
        if (p_string != null)
        {
            try
            {
                intVal = Integer.parseInt(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return intVal;
    }

    /* Sort the given list according to the given sort column and locale. */
    private void sortTasks(List p_list, int p_sortColumn,
            GlobalSightLocale p_gsl, boolean p_sortAsc)
    {
        java.util.Collections.sort(p_list, new WorkflowTaskDataComparator(
                p_sortColumn, p_gsl, p_sortAsc));
    }

    private void acceptAndDownload(HttpServletRequest p_request,
            HttpServletResponse p_response, HttpSession p_session, User p_user,
            boolean isCombined)
            throws ServletException, IOException, TaskException,
            GeneralException, NamingException
    {
        String taskIds = p_request.getParameter("taskParam");
        if (taskIds != null && !"".equals(taskIds))
        {
            acceptAllTasks(p_session, p_request, p_user);
            p_request.setAttribute("taskParam", taskIds);
        }
        else
        {
            // Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            // get task state (determines from which tab, the task details is
            // requested)
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10);// -10 as
            // default

            // Get task
            Task task = TaskHelper.getTask(p_user.getUserId(), taskId,
                    taskState);
            try
            {
                // Accept the task
                TaskHelper.acceptTask(p_user.getUserId(), task);
            }
            catch (NamingException ne)
            {

            }

            // update task in session
            if (task != null)
            {
                TaskHelper.updateTaskInSession(p_session, p_user.getUserId(),
                        task.getId());
                // update the MRU list with the changed state
                TaskHelper.updateMRUtask(p_request, p_session, task,
                        p_response, Task.STATE_ACCEPTED);
            }
        }
        // signal to the TaskList.jsp that the task was accepted and that
        // we are requesting the jsp to automatically invoke download
        // once the page is refreshed (see <BODY onload).
        p_request.setAttribute(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD,
                new Boolean(true));
        
        if (isCombined)
        {
            p_request.setAttribute("isDownloadCombined", new Boolean(true));
        }
    }

    private void saveReplace(HttpServletRequest p_request, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException
    {
        // save the results from a search/replace
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String companyId = (String) sessionMgr.getAttribute(COMPANY_ID);
        if (companyId != null)
        {
            SearchHandlerHelper.replace(
                    (List) sessionMgr.getAttribute("tuvInfos"),
                    Long.parseLong(companyId));
        }
    }

    private TaskSearchParameters getRequestSearchParams(
            HttpServletRequest request, HttpSession session, String listType)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(USER);
        TaskSearchParameters sp = new TaskSearchParameters();
        sp.setUser(user);
        sp.setSessionId(session.getId());
        String userId = user.getUserId();

        StringBuffer taskSearch = new StringBuffer();
        try
        {
            // set parameters
            // job name
            String buf = (String) request
                    .getParameter(JobSearchConstants.NAME_FIELD);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setJobName(buf);
                sp.setJobNameCondition((String) request
                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                taskSearch
                        .append(JobSearchConstants.NAME_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                taskSearch.append(":");
                taskSearch.append(JobSearchConstants.NAME_FIELD + "=" + buf);
                taskSearch.append(":");
            }

            // status
            buf = (String) request
                    .getParameter(JobSearchConstants.STATUS_OPTIONS);
            sp.setActivityState(new Integer(buf));
            taskSearch.append(JobSearchConstants.STATUS_OPTIONS + "="
                    + request.getParameter(JobSearchConstants.STATUS_OPTIONS));
            taskSearch.append(":");
            session.setAttribute("lastTaskSearchState", new Integer(buf));

            // company
            // For "All Status" issue
            buf = CompanyWrapper.getCurrentCompanyName();
            if (CompanyWrapper.isSuperCompanyName(buf)
                    && !UserUtil.isSuperPM(userId))
            {
                buf = (String) request
                        .getParameter(JobSearchConstants.COMPANY_OPTIONS);
            }
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setCompanyName(buf);
                taskSearch.append(JobSearchConstants.COMPANY_OPTIONS + "="
                        + buf);
                taskSearch.append(":");
            }

            if (listType.equals("miniSearch"))
            {
                String cookieName = JobSearchConstants.MINI_TASK_SEARCH_COOKIE
                        + userId.hashCode();
                Cookie cookie = new Cookie(cookieName, taskSearch.toString());
                sessionMgr.setAttribute(cookieName, cookie);
                session.setAttribute(JobSearchConstants.LAST_TASK_SEARCH_TYPE,
                        JobSearchConstants.MINI_TASK_SEARCH_COOKIE);
                return sp;
            }
            else
                session.setAttribute(JobSearchConstants.LAST_TASK_SEARCH_TYPE,
                        JobSearchConstants.TASK_SEARCH_COOKIE);

            // job id
            buf = (String) request.getParameter(JobSearchConstants.ID_FIELD);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setJobId(buf);
                sp.setJobIdCondition(request
                        .getParameter(JobSearchConstants.ID_OPTIONS));
                taskSearch.append(JobSearchConstants.ID_OPTIONS + "="
                        + request.getParameter(JobSearchConstants.ID_OPTIONS));
                taskSearch.append(":");
                taskSearch.append(JobSearchConstants.ID_FIELD + "=" + buf);
                taskSearch.append(":");
            }

            // activity name
            buf = (String) request
                    .getParameter(JobSearchConstants.ACT_NAME_FIELD);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setActivityName(buf);
                taskSearch
                        .append(JobSearchConstants.ACT_NAME_FIELD + "=" + buf);
                taskSearch.append(":");
            }

            // source locale
            buf = (String) request.getParameter(JobSearchConstants.SRC_LOCALE);
            if (buf != null && !buf.equals("-1"))
            {
                sp.setSourceLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                taskSearch.append(JobSearchConstants.SRC_LOCALE + "=" + buf);
                taskSearch.append(":");
            }
            // target locale
            buf = (String) request.getParameter(JobSearchConstants.TARG_LOCALE);
            if (buf != null && !buf.equals("-1"))
            {
                sp.setTargetLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                taskSearch.append(JobSearchConstants.TARG_LOCALE + "=" + buf);
                taskSearch.append(":");
            }

            // priority
            buf = (String) request
                    .getParameter(JobSearchConstants.PRIORITY_OPTIONS);
            if (buf != null && !buf.equals("-1"))
            {
                sp.setPriority(buf);
                taskSearch.append(JobSearchConstants.PRIORITY_OPTIONS + "="
                        + buf);
                taskSearch.append(":");
            }

            // Acceptance Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.ACCEPTANCE_START);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setAcceptanceStart(new Integer(buf));
                sp.setAcceptanceStartCondition(request
                        .getParameter(JobSearchConstants.ACCEPTANCE_START_OPTIONS));
                taskSearch.append(JobSearchConstants.ACCEPTANCE_START + "="
                        + buf);
                taskSearch.append(":");
                taskSearch
                        .append(JobSearchConstants.ACCEPTANCE_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.ACCEPTANCE_START_OPTIONS));
                taskSearch.append(":");
            }

            // Acceptance Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.ACCEPTANCE_END);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setAcceptanceEnd(new Integer(buf));
                taskSearch
                        .append(JobSearchConstants.ACCEPTANCE_END + "=" + buf);
                taskSearch.append(":");
            }

            // Acceptance Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.ACCEPTANCE_END_OPTIONS);
            if (buf != null && buf.length() != 0)
            {
                sp.setAcceptanceEndCondition(buf);
                taskSearch.append(JobSearchConstants.ACCEPTANCE_END_OPTIONS
                        + "=" + buf);
                taskSearch.append(":");
            }

            // Completion Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_START);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setEstCompletionStart(new Integer(buf));
                sp.setEstCompletionStartCondition(request
                        .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                taskSearch.append(JobSearchConstants.EST_COMPLETION_START + "="
                        + buf);
                taskSearch.append(":");
                taskSearch
                        .append(JobSearchConstants.EST_COMPLETION_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                taskSearch.append(":");
            }
            // Completion Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END);
            if (buf != null && buf.trim().length() != 0)
            {
                sp.setEstCompletionEnd(new Integer(buf));
                taskSearch.append(JobSearchConstants.EST_COMPLETION_END + "="
                        + buf);
                taskSearch.append(":");
            }

            // Completion Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END_OPTIONS);
            if (buf != null && buf.length() != 0)
            {
                sp.setEstCompletionEndCondition(buf);
                taskSearch.append(JobSearchConstants.EST_COMPLETION_END_OPTIONS
                        + "=" + buf);
                taskSearch.append(":");
            }

            String cookieName = JobSearchConstants.TASK_SEARCH_COOKIE
                    + userId.hashCode();
            Cookie cookie = new Cookie(cookieName, taskSearch.toString());
            sessionMgr.setAttribute(cookieName, cookie);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return sp;
    }

    private TaskSearchParameters getSessionSearchParams(
            HttpServletRequest request, HttpSession session)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(USER);
        TaskSearchParameters sp = new TaskSearchParameters();
        sp.setUser(user);
        sp.setSessionId(session.getId());
        try
        {
            Cookie cookie = TaskSearchHandlerHelper.getTaskSearchCookie(
                    session, request);

            if (cookie == null)
            {
                sp.setActivityState(new Integer(Task.STATE_ACTIVE));
                return sp;
            }
            String cookieValue = URLDecoder.decode(cookie.getValue());
            StringTokenizer strtok = new StringTokenizer(cookieValue, ":");
            while (strtok.hasMoreTokens())
            {
                String tok = strtok.nextToken();
                int idx = tok.indexOf("=");
                String key = tok.substring(0, idx);
                String value = tok.substring(idx + 1);
                if (key.equals(JobSearchConstants.NAME_FIELD))
                {
                    if (!value.equals(""))
                        sp.setJobName(value);
                }
                else if (key.equals(JobSearchConstants.NAME_OPTIONS))
                {
                    sp.setJobNameCondition(value);
                }
                else if (key.equals(JobSearchConstants.COMPANY_OPTIONS))
                {
                    if (!value.equals(""))
                        sp.setCompanyName(value);
                }
                else if (key.equals(JobSearchConstants.ID_FIELD))
                {
                    if (!value.equals(""))
                        sp.setJobId(value);
                }
                else if (key.equals(JobSearchConstants.ID_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setJobIdCondition(value);
                }
                else if (key.equals(JobSearchConstants.ACT_NAME_FIELD))
                {
                    if (!value.equals(""))
                    {
                        sp.setActivityName(value);
                    }

                }
                else if (key.equals(JobSearchConstants.STATUS_OPTIONS))
                {
                    sp.setActivityState(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.SRC_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setSourceLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.TARG_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setTargetLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.PRIORITY_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setPriority(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_START))
                {
                    if (!value.equals(""))
                        sp.setAcceptanceStart(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setAcceptanceStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_END))
                {
                    if (!value.equals(""))
                        sp.setAcceptanceEnd(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setAcceptanceEndCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_START))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionStart(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_END))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionEnd(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionEndCondition(value);
                }
            }
            return sp;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void myInvokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        // Populate the links on the search results page.
        Enumeration en = p_pageDescriptor.getLinkNames();

        while (en.hasMoreElements())
        {
            String linkName = (String) en.nextElement();
            String pageName = p_pageDescriptor.getPageName();

            // create a navigation bean for each link
            NavigationBean bean = new NavigationBean(linkName, pageName);

            // each navigation bean will be labelled with the name of
            // the link
            p_request.setAttribute(linkName, bean);
        }

        // turn off cache. do both. "pragma" for the older browsers.
        p_response.setHeader("Pragma", "yes-cache"); // HTTP 1.0
        p_response.setHeader("Cache-Control", "yes-cache"); // HTTP 1.1
        p_response.addHeader("Cache-Control", "yes-store"); // tell proxy not to
        // cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away

        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_pageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    private void getTasksForDisplay(HttpServletRequest p_request,
            HttpSession p_session, User p_user) throws ServletException,
            IOException, EnvoyServletException
    {
        int taskState = 0;
        boolean translatorLogin = false;
        String listType = (String) p_request.getParameter("listType");
        String taskStatus = (String) p_request.getParameter("taskStatus");
        Integer previousTaskState = (Integer) p_session
                .getAttribute(TASK_STATE);
        if (previousTaskState == null)
        {
            previousTaskState = new Integer(Task.STATE_ACTIVE);
            if (listType == null)
            {
                // Must be translator login.
                translatorLogin = true;
            }
        }

        // get request values
        if (listType != null)
        {
            p_request.setAttribute("listType", listType);
            if (listType.equals("stateOnly"))
            {
                taskState = Integer
                        .parseInt(p_request.getParameter(TASK_STATE));
            }
            else if (listType.equals("advSearch")
                    || listType.equals("miniSearch"))
            {
                taskState = extractIntegerValue(
                        p_request
                                .getParameter(JobSearchConstants.STATUS_OPTIONS),
                        previousTaskState.intValue());
            }
            else if (listType.equals("lastSearch"))
            {
                Integer lastTaskSearch = (Integer) p_session
                        .getAttribute("lastTaskSearchState");
                taskState = lastTaskSearch.intValue();
            }
        }
        else
        {
            // From anything but search, last search, state menu, or sort
            taskState = previousTaskState.intValue();

            // listType is not "all states".
            if (taskState > 0 && taskStatus != null)
            {

                taskState = Integer.parseInt(taskStatus);
                previousTaskState = new Integer(taskState);
            }
        }

        int sortColumn = extractIntegerValue(
                p_request.getParameter(MYACT_COL_SORT_ID), -1);

        int isRefresh = extractIntegerValue(p_request.getParameter(IS_REFRESH),
                0);

        String isPaging = (String) p_request.getParameter(TASK_LIST_START);

        // get session values - used to remember and switch sort directions
        Integer previousSortColumn = (Integer) p_session
                .getAttribute(MYACT_COL_SORT_ID);
        Boolean sortAsc = (Boolean) p_session.getAttribute(MYACT_SORT_ASC);
        String request_sortAsc = p_request.getParameter(MYACT_SORT_ASC);

        // Toggle sort direction:
        // The Sort direction is now ALWAYS present on the request.
        // The sort arrow links built in the JSP submit the reverse sort order
        // Toggle sort direction if:
        // - NOT invoked from a navagation/menu selection (sortColumn = -1)
        // - previous sort values ARE defined
        // - NOT an auto-refresh request
        // - NOT clicking on the paging widget
        // - and finally, this **IS** the same column and screen as before
        if (sortAsc != null && request_sortAsc != null
                && !sortAsc.toString().equals(request_sortAsc)
                && previousSortColumn != null && previousTaskState != null
                && isRefresh != 1 && isPaging == null
                && sortColumn == previousSortColumn.intValue()
                && taskState == previousTaskState.intValue())
        {
            sortAsc = new Boolean(!sortAsc.booleanValue());
            // remember it across the session
            p_session.setAttribute(MYACT_SORT_ASC, sortAsc);
        }
        else
        // init or reset sort values
        {
            // task state - always reset
            previousTaskState = new Integer(taskState);
            p_session.setAttribute(TASK_STATE, previousTaskState);

            // sort column
            if (sortColumn == -1)
            {
                // create default sort column if not present
                if (previousSortColumn == null)
                {
                    previousSortColumn = new Integer(
                            WorkflowTaskDataComparator.JOB_ID);
                    p_session.setAttribute(MYACT_COL_SORT_ID,
                            previousSortColumn);
                }
                // if sortColumn == -1 then set sort col to previous
                sortColumn = previousSortColumn.intValue();
            }
            else
            {
                // reset previous sort column
                previousSortColumn = new Integer(sortColumn);
                p_session.setAttribute(MYACT_COL_SORT_ID, previousSortColumn);
            }

            // session sort direction
            // - only init if undefined
            if (sortAsc == null)
            {
                sortAsc = new Boolean(false); // Ascending == true, Descending
                // == false
                p_session.setAttribute(MYACT_SORT_ASC, sortAsc);
            }

            // initial request sort direction
            if (request_sortAsc == null)
            {
                request_sortAsc = sortAsc.toString();
            }
        }

        String taskListStartStr = p_request.getParameter(TASK_LIST_START);
        int taskListStart = extractIntegerValue(taskListStartStr, 0);
        if (taskListStartStr != null
                || WorkflowConstants.TASK_ALL_STATES != taskState)
        {
            p_session.setAttribute("taskListStart", taskListStart);
        }

        p_request.setAttribute("action", p_request.getParameter("action"));
        List tasks = null;
        if (listType == null)
        {
            String lastListType = (String) p_session.getAttribute("listType");
            if (lastListType != null
                    && (lastListType.equals("advSearch") || lastListType
                            .equals("miniSearch")))
            {
                // Get search parameters from session
                try
                {
                    TaskSearchParameters searchParams = getSessionSearchParams(
                            p_request, p_session);

                    // Checks state changed or not.
                    Map params = searchParams.getParameters();
                    Integer state = (Integer) params.get(new Integer(
                            TaskSearchParameters.STATE));
                    if (previousTaskState != state)
                    {
                        searchParams.setActivityState(previousTaskState);
                    }
                    tasks = (List) ServerProxy.getTaskManager().getTasks(
                            searchParams);
                }
                catch (GeneralException e)
                {
                    throw new EnvoyServletException(e);
                }
            }
            else
            {
                tasks = TaskHelper.getTasks(p_user.getUserId(), taskState);
            }
        }
        else if (listType.equals("lastSearch"))
        {
            // Get search parameters from session
            try
            {
                TaskSearchParameters searchParams = getSessionSearchParams(
                        p_request, p_session);
                tasks = (List) ServerProxy.getTaskManager().getTasks(
                        searchParams);
            }
            catch (GeneralException e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else if (listType.equals("advSearch") || listType.equals("miniSearch"))
        {
            // Get search parameters from request
            try
            {
                TaskSearchParameters searchParams = getRequestSearchParams(
                        p_request, p_session, listType);
                tasks = (List) ServerProxy.getTaskManager().getTasks(
                        searchParams);
            }
            catch (GeneralException e)
            {
                throw new EnvoyServletException(e);
            }
            p_session.setAttribute("listType", listType);
        }
        else if (listType.equals("stateOnly"))
        {
            TaskSearchParameters sp = new TaskSearchParameters();
            SessionManager sessionMgr = (SessionManager) p_session
                    .getAttribute(SESSION_MANAGER);
            User user = (User) sessionMgr.getAttribute(USER);
            sp.setUser(user);
            sp.setSessionId(p_session.getId());
            sp.setActivityState(new Integer(taskState));
            /*
             * tasks = TaskHelper.getTasks(p_session.getId(),
             * p_user.getUserId(), taskState);
             */

            tasks = (List) ServerProxy.getTaskManager().getTasks(sp);
            p_session.setAttribute("listType", listType);
            p_session.setAttribute("lastState", new Integer(taskState));
        }

        if (tasks == null)
        {
            tasks = new Vector();
        }
        else
        {
            removeSpecialTasks(tasks);
        }

        if (tasks.size() > 0)
        {
            Locale uiLocale = (Locale) p_session.getAttribute(UILOCALE);
            GlobalSightLocale gl = new GlobalSightLocale(
                    uiLocale.getLanguage(), uiLocale.getCountry(), true);
            Iterator it = tasks.iterator();
            while (it.hasNext())
            {
                Task t = (Task) it.next();
                Workflow wf = t.getWorkflow();

                if (wf != null)
                {
                    String wfState = wf.getState();
                    if ((Workflow.CANCELLED).equals(wfState)
                            || (Workflow.ARCHIVED).equals(wfState))
                    {
                        it.remove();
                    }
                }
            }

            sortTasks(tasks, sortColumn, gl, sortAsc.booleanValue());
        }
        if (tasks.size() == 0)
        {
            ResourceBundle bundle = getBundle(p_session);
            if (translatorLogin)
            {
                // Try In Progress as a 2nd choice
                taskState = Task.STATE_ACCEPTED;
                tasks = TaskHelper.getTasks(p_user.getUserId(), taskState);
            }
            else if (listType == null)
            {
                p_request.setAttribute("noresults",
                        bundle.getString("msg_activity_search_no_available")
                                + bundle.getString("msg_activity_search_try"));
            }
            else if (listType.equals("stateOnly"))
            {
                Integer stateI = (Integer) p_session.getAttribute(TASK_STATE);
                int state = stateI.intValue();
                String stateStr = null;
                if (state == Task.STATE_ACTIVE)
                    stateStr = bundle.getString("lb_available");
                else if (state == Task.STATE_ACCEPTED)
                    stateStr = bundle.getString("lb_inprogress");
                else if (state == Task.STATE_COMPLETED)
                    stateStr = bundle.getString("lb_finished");
                else if (state == Task.STATE_REJECTED)
                    stateStr = bundle.getString("lb_rejected");

                p_request.setAttribute(
                        "noresults",
                        bundle.getString("msg_activity_search_no_state") + " '"
                                + stateStr + "'.\n"
                                + bundle.getString("msg_activity_search_try"));
            }
            else
            {
                p_request.setAttribute("noresults",
                        bundle.getString("msg_activity_search_no_match"));
            }
        }
        p_request.setAttribute(MYACT_COL_SORT_ID, new Integer(sortColumn));
        p_request.setAttribute(MYACT_SORT_ASC, request_sortAsc);
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(TASK_LIST, tasks);
        p_request.setAttribute(TASK_STATE, new Integer(taskState));
        p_request.setAttribute(
                "taskListStart",
                p_session.getAttribute("taskListStart") == null ? 0 : p_session
                        .getAttribute("taskListStart"));
        p_request.setAttribute("languageSet", getTaskLangs(tasks));
    }

    private List<Task> getTasks(User user, TaskSearchParameters searchParams,
            int start, int end, HttpServletRequest request, int state,
            int sortColumn, boolean asc)
    {
        HttpSession session = request.getSession();
        List<TaskVo> vos = (List<TaskVo>) session
                .getAttribute(TASK_SEARCH_RESULT);
        if (vos == null)
        {
            vos = TaskSearchUtil.search(user, searchParams);
            session.setAttribute(TASK_SEARCH_RESULT, vos);
        }

        if (vos.size() > 1)
        {
            Collections.sort(vos, new TaskSearchComparator(sortColumn, asc));
        }

        int perPage = end - start;
        while (start > 0 && start > vos.size())
        {
            start -= perPage;
        }

        List<Task> tasks = new ArrayList<Task>(vos.size());
        Map<Long, GlobalSightLocale> locales = new HashMap<Long, GlobalSightLocale>();

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean isProjectManager = TaskSearchUtil.isProjectManager(user);
        boolean canManageProject = perms
                .getPermissionFor(Permission.PROJECTS_MANAGE);

        for (int i = 0; i < start; i++)
        {
            TaskVo vo = vos.get(i);
            tasks.add(getTask(vo, locales));
        }

        for (int i = start; i < vos.size(); i++)
        {
            TaskVo vo = vos.get(i);
            TaskImpl t = HibernateUtil.get(TaskImpl.class, vo.getTaskId());
            tasks.add(t);

            if (WorkflowConstants.TASK_ALL_STATES == state)
            {
                TaskSearchUtil.setState(t, user.getUserId());
            }
            else
            {
                t.setState(state);
            }

            if (isProjectManager || canManageProject)
            {
                TaskSearchUtil.setAllAssignees(t);
            }

            if (i == end)
                break;
        }

        for (int i = end + 1; i < vos.size(); i++)
        {
            TaskVo vo = vos.get(i);
            tasks.add(getTask(vo, locales));
        }
        return tasks;
    }

    private TaskImpl getTask(TaskVo vo, Map<Long, GlobalSightLocale> locales)
    {
        TaskImpl t = new TaskImpl();
        t.setId(vo.getTaskId());
        WorkflowImpl w = new WorkflowImpl();
        w.setId(vo.getWorkflowId());
        GlobalSightLocale locale = locales.get(vo.getLocaleId());
        if (locale == null)
        {
            locale = HibernateUtil.get(GlobalSightLocale.class,
                    vo.getLocaleId());
            locales.put(vo.getLocaleId(), locale);
        }
        w.setTargetLocale(locale);
        JobImpl job = new JobImpl();
        job.setId(vo.getJobId());
        w.setJob(job);
        t.setWorkflow(w);

        return t;
    }

    /**
     * When offline download, zero word count tasks should be ignored.
     */
    private String filterZeroWorcCountTasksForOfflineDownload(
            HttpSession session, User user, HttpServletRequest p_request)
    {
        String taskIds = p_request.getParameter("taskParam");
        List<Long> selectedTaskIds = getSelectedTaskIds(taskIds);

        TaskManager taskManager = ServerProxy.getTaskManager();
        for (Iterator<Long> it = selectedTaskIds.iterator(); it.hasNext();)
        {
            try
            {
                Task task = taskManager.getTask(it.next());
                if (task.getWorkflow().getTotalWordCount() == 0)
                {
                    it.remove();
                }
            }
            catch (Exception ignore)
            {

            }
        }

        StringBuilder ids = new StringBuilder(); 
        if (!selectedTaskIds.isEmpty())
        {
            for (Long id : selectedTaskIds)
            {
                ids.append(" ").append(id);
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("{\"selectedTaskIds\":\"")
                .append(selectedTaskIds.isEmpty() ? "0" : ids.toString().trim())
                .append("\"}");

        return sb.toString();
    }

    /**
     * Get selected task ID List.
     * @param taskParam -- selected task IDs in string like "1001 1002 1003".
     */
    private List<Long> getSelectedTaskIds(String taskParam)
    {
        List<Long> selectedTaskIds = new ArrayList<Long>();
        if (taskParam != null && !"".equals(taskParam))
        {
            String taskId = "";
            StringTokenizer tokenizer = new StringTokenizer(taskParam);
            while (tokenizer.hasMoreTokens())
            {
                taskId = tokenizer.nextToken();
                selectedTaskIds.add(Long.parseLong(taskId));
            }
        }
        
        return selectedTaskIds;
    }
}