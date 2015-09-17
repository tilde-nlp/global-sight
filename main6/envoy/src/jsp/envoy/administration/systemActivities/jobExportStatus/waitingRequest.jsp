<%@page import="com.globalsight.everest.util.comparator.ExportRequestComparator"%>
<%@page import="com.globalsight.everest.foundation.SearchCriteriaParameters"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         java.util.ResourceBundle,
         java.util.Date,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.CreateRequestComparator,
         java.util.Enumeration"
         session="true"%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="importing" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="exporting" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="loginBlock" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="offline" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />             
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String importingUrl = importing.getPageURL();
    String exportingUrl = exporting.getPageURL();
    String jobCreationStatus = bundle
            .getString("lb_job_creation_status");
    String loginBlockConfigUrl = loginBlock.getPageURL();
    String offlineUrl = offline.getPageURL();
    ArrayList<String> keyList = (ArrayList<String>)request.getAttribute("selectedKeys");
	if (keyList == null)
		keyList = new ArrayList<String>();
%>
<HTML>
    <HEAD>
        <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
        <TITLE><%=bundle.getString("lb_job_export_status")%></TITLE>
        <script SRC="/globalsight/includes/utilityScripts.js"></script>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formvalidation.js"></SCRIPT>
        <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
        <%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
        <%@ include file="/envoy/common/warning.jspIncl"%>
        <link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
        <script>
            var needWarning = false;
            var objectName = "";
            var guideNode = "import";
            var helpFile = '<%=bundle.getString("help_waiting_requests")%>';
            function handleSelectAll() {
                if (MyForm && MyForm.selectAll) {
                    if (MyForm.selectAll.checked) {
                        checkAll('MyForm');
                        setButtonState();
                    }
                    else {
                        clearAll('MyForm');
                        setButtonState();
                    }
                }
            }

            function cancelFiles() {
                var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_remove_creation_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=remove";
                    MyForm.submit();
                }
            }
            
            function upPriority() {
            	var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_change_order_import_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=upPriority";
                    MyForm.submit();
                }
            }
            
            function downPriority() {
            	var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_change_order_import_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=downPriority";
                    MyForm.submit();
                }
            }
            
            function topPriority() {
            	var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_change_order_import_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=topPriority";
                    MyForm.submit();
                }
            }
            
            function bottomPriority() {
            	var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_change_order_import_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=bottomPriority";
                    MyForm.submit();
                }
            }
            
            $(function() {
				var obj = $("#contentTd")[0];
				var h = obj.offsetHeight;
				var kh = h - 60;
				$("#sortTd")[0].style.height = kh + "px";
				
				<%for (String key : keyList) {%>
				    $("#<%=key%>").attr("checked",'true');
				<%}%>
			});
            
            function changePageSize(size) {
                MyForm.action = "<%=selfUrl%>&numOfPageSize=" + size;
                MyForm.submit();
            }
        </script>
    </HEAD>
    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
        <%@ include file="/envoy/common/header.jspIncl"%>
        <%@ include file="/envoy/common/navigation.jspIncl"%>
        <%@ include file="/envoy/wizards/guides.jspIncl"%>
        <STYLE>
            .list {
                border: 1px solid<%=skin.getProperty("skin.list.borderColor")%>;
            }

            .headerCell {
                padding-right: 10px;
                padding-top: 2px;
                padding-bottom: 2px;
            }
            
            .pointer {
			  cursor:pointer
			}
        </STYLE>
        <DIV ID="contentLayer"
             STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
            <SPAN CLASS="mainHeading"> <%=bundle.getString("lb_system_activities")%></SPAN> <br> <span
                class="standardText"><br><%=bundle.getString("lb_system_activities_help")%></span>
            <div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=importingUrl%>"> <%=bundle.getString("lb_job_creation_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td class="tableHeadingListOn">
                            <img src="/globalsight/images/tab_left_blue.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=selfUrl%>"> <%=bundle.getString("lb_job_export_status")%></a> <img
                               src="/globalsight/images/tab_right_blue.gif" border="0" />
                        </td>
                         <td width="2"></td>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=offlineUrl%>"> <%=bundle.getString("lb_offline_upload_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=loginBlockConfigUrl%>"> <%=bundle.getString("lb_login_block_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                    </tr>
                </table>
            </div>
            <div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td id="jobWorkflowsTab" class="tableHeadingListOn">
                            <img src="/globalsight/images/tab_left_blue.gif" border="0" />
                            <a class="sortHREFWhite"
                               href="<%=selfUrl%>"> <%=bundle.getString("lb_job_create_wait")%></a> <img
                               src="/globalsight/images/tab_right_blue.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td id="jobWorkflowsTab" class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=exportingUrl%>"> <%=bundle.getString("lb_job_create_export")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                    </tr>
                </table>
            </div>
            <div class="standardText">
                <amb:tableNav bean="requestDefine" key="requestDefineKey" pageUrl="self" />
                <div align='right'>
                    <c:out value="${tableNav}" escapeXml="false"></c:out>
                    </div>
                    <FORM NAME="MyForm" METHOD="POST">
                    <table width="100%">
					<tr>
						<td width="40px" valign="top" align="right">
							<table style="width: 100%; height: 100%;">
								<tr>
									<td align="right" style="padding-top: 0px;"><input
										type="button" value="<%=bundle.getString("lb_move_to_top")%>" onclick='topPriority()'  class="pointer"/></td>
								</tr>
								<tr>
									<td id="sortTd" align="right"><img
										src='/globalsight/images/sort-up(big).gif'
										style='margin-bottom: 5px' onclick='upPriority()'  class="pointer"></img><br>
										<img src='/globalsight/images/sort-down(big).gif'
										onclick='downPriority()'  class="pointer"></img></td>
								</tr>
								<tr>
									<td align="right"><input type="button"
										value="<%=bundle.getString("lb_move_to_bottom")%>" onclick='bottomPriority()'  class="pointer"/></td>
								</tr>
							</table>

						</td>
						<td valign="top" id="contentTd">
                        <amb:table bean="requestDefine" id="requestVo" key="requestDefineKey"
                                   dataClass="com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobExportState.RequestFile"
                                   pageUrl="self" emptyTableMsg="msg_waiting_request_none">
                            <amb:column label="checkbox">
                                <INPUT TYPE=checkbox NAME=key VALUE="${requestVo.key}" id="${requestVo.key}">
                        </amb:column>
                         <amb:column label="lb_company" sortBy="<%=ExportRequestComparator.Company%>">
                            ${requestVo.company}
                        </amb:column>
                        <amb:column label="lb_job_id" sortBy="<%=ExportRequestComparator.JOB_ID%>">
                            ${requestVo.jobId}
                        </amb:column>
                        <amb:column label="lb_job_name" sortBy="<%=ExportRequestComparator.JOB_NAME%>">
                            ${requestVo.jobName}
                        </amb:column>
                        <amb:column label="lb_file" sortBy="<%=ExportRequestComparator.FILE_NAME%>" width="40%" style="text-align: left; word-break: break-all; word-wrap: break-word;">
                            ${requestVo.file}
                        </amb:column>
                        <amb:column label="lb_project" sortBy="<%=ExportRequestComparator.FILE_PROFILE%>">
                            ${requestVo.project}
                        </amb:column>
                        <amb:column label="lb_targetLange" sortBy="<%=ExportRequestComparator.LOCALE%>">
                            ${requestVo.workflowLocale}
                        </amb:column>
                        <amb:column label="lb_date_request" sortBy="<%=ExportRequestComparator.REQUEST_TIME%>">
                            ${requestVo.requestTime}
                        </amb:column>
                        <amb:column label="lb_priority">
                            ${requestVo.sortIndex}
                        </amb:column>
                    </amb:table>
                    <div align='right' style="padding-top: 5px;" class="standardText">
                    <c:out value="${tableNav2}" escapeXml="false" ></c:out>
                    <c:out value="${tableNav}" escapeXml="false" ></c:out>
                     <amb:tableNav bean="requestDefine" key="requestDefineKey" pageUrl="self" scope="10,20,50" />
                  </div>
                   </td>
					</tr>
				</table>
                   
                  
                </FORM>
                <DIV ID="ButtonLayer" ALIGN="LEFT">
                     <INPUT TYPE="BUTTON" NAME="Error" VALUE="<%=bundle.getString("lb_remove")%>"
                                onClick="cancelFiles();"  class="pointer">
                </DIV>
            </DIV>
    </BODY>
</HTML>
