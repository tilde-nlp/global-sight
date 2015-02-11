<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
	String lbnext = bundle.getString("lb_next");
    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL += "&action=" + CompanyConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_company");
    }
    else
    {
        saveURL += "&action=" + CompanyConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_company");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CompanyConstants.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(CompanyConstants.NAMES);
    Company company = (Company)sessionMgr.getAttribute(CompanyConstants.COMPANY);
    String companyName = "";
    String email = (String)request.getAttribute(CompanyConstants.EMAIL);
    String desc = "";
    String checked = "checked";//default
    String tmAccessControl = "";//default
    String tbAccessControl = "";//default
    String ssoChecked = "";//default
    String isSsoChecked = "false";//default
    String ssoIdpUrl = "";
    String sessionTime = "";
    boolean isReviewOnly = false;
    String enableTM3Checked = "checked";
    String separateLmTuTuvTablesChecked = "";
    if (company != null)
    {
        companyName = company.getName();
        desc = company.getDescription();
        email = company.getEmail();
        sessionTime = company.getSessionTime();
        
        if (desc == null) desc = "";
        if (email == null) email = "";
        if (sessionTime==null) sessionTime="";
        
        boolean enableIPFilte = company.getEnableIPFilter();
        if (enableIPFilte==false) {
        	checked = "";
        }
        
        boolean enableTMAcessControl = company.getEnableTMAccessControl();
        if (enableTMAcessControl) {
            tmAccessControl = "checked";
        }
        
        boolean enableTBAcessControl = company.getEnableTBAccessControl();
        if (enableTBAcessControl) {
            tbAccessControl = "checked";
        }
          
        if (company.getEnableSSOLogin())
        {
            ssoChecked = "checked";
            isSsoChecked = "true";
        }
        
        ssoIdpUrl = company.getSsoIdpUrl();
        ssoIdpUrl = ssoIdpUrl == null ? "" : ssoIdpUrl;
        
        if (company.getTmVersion().getValue() != 3)
            enableTM3Checked = "";
        
        if (company.getSeparateTmTuTuvTables() == 1){
            separateLmTuTuvTablesChecked = "checked";
        }
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_company")%>";
var guideNode="companies";
var helpFile = "<%=bundle.getString("help_companies_basic_screen")%>";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        companyForm.action = "<%=cancelURL%>";
        companyForm.submit();
    }
    else if (formAction == "save")
    {
    	if (confirmForm() && confirmTime())
		{
    		var tbox = document.getElementById("to");
    		if (tbox.options.length == 0)
    		{
    			alert("<c:out value='${alert}'/>");
    			return false;
    		}
    		for(var i=0;i<tbox.options.length;i++)
    		{
    			tbox.options[i].selected=true;
    		}
            
        	companyForm.action = "<%=saveURL%>";
            companyForm.submit();
		}
    }
}

//
// Check required fields(SSO, email, name).
// Check duplicate activity name.
//
function confirmForm()
{
	// check sso
	var ssoLogonElem = companyForm.enableSsoLogonField;
	if(ssoLogonElem!=null && ssoLogonElem.checked)
    {
        var idpUrl = companyForm.ssoIdpUrlField.value;
        if (isEmptyString(idpUrl))
        {
        	alert("<%=bundle.getString("msg_sso_input_valid_idpurl")%>");
            return false;
        }
    }
	
	// Check Email Field
	var emailElem = document.getElementById("emailId");
	var sysNotificationEnable = "<%=request.getAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED)%>";
    if("true" == sysNotificationEnable)
    {
    	var email = stripBlanks(emailElem.value);
    	if(email.length > 0 && !validEmail(email))
    	{
    		alert("<%=bundle.getString("jsmsg_email_invalid")%>");
            return false;
    	}
    }
	
	// check name
    if (!companyForm.nameField) 
    {
        // can't change name on edit
        return true;
    }
    if (isEmptyString(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_company_name"))%>");
        companyForm.nameField.value = "";
        companyForm.nameField.focus();
        return false;
    }   
    
    //Check if the company name is one of key words
    var companyName = ATrim(companyForm.nameField.value).toLowerCase();
	var words = new Array("com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "con", "prn", "aux", "nul", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");
	var tmp = "", tmpPrefix = "";
	for (x in words) {
	  tmp = words[x];
	  tmpPrefix = tmp + ".";
	  if (companyName == tmp || companyName.indexOf(tmpPrefix) == 0) {
		alert("<%=EditUtil.toJavascript(bundle.getString("msg_invalid_company_name"))%>");
		return false;
	  }
	}
    
    if (hasSpecialChars(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String comName = (String)names.get(i);
%>
            if ("<%=comName%>".toLowerCase() == companyForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company"))%>");
                return false;
            }
<%
        }
    }
%>

	return true;
}

function confirmTime()
{
	var sessionTime = companyForm.sessionTimeField.value;
	{
		if (sessionTime!='')
		{
			if(isNumeric(sessionTime))
			{
				sessionTime = parseInt(sessionTime)
				if (sessionTime > 480 || sessionTime < 30)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
					return false;
				}
			}
			else
			{
				alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
				return false;
			}
		}
	}
    return true;
}

function isNumeric(str){
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}

function onEnableSSOSwitch()
{
	onEnableSSO(companyForm.enableSsoLogonField.checked);
}

function onEnableSSO(checked)
{
	var ele = document.getElementById("ssoIdpUrlCC");
	var display = checked ? "" : "none";
	ele.style.display = display;
}

function doOnload()
{
    loadGuides();

    var edit = eval("<%=edit%>");
    if (edit)
    {
        companyForm.<%=CompanyConstants.DESC%>.focus();
    }
    else
    {
        companyForm.<%=CompanyConstants.NAME%>.focus();
    }

    var enableSSO = <%=request.getAttribute(SystemConfigParamNames.ENABLE_SSO)%>;
    if(!enableSSO)
    {
		document.getElementById("ssoCheck").style.display = "none";
		document.getElementById("ssoIdpUrlCC").style.display = "none";
    }
    else
    {
    	onEnableSSO(eval("<%=isSsoChecked%>"));
    }   
}

function move(f,t) {
	var fbox = document.getElementById(f);
	var tbox = document.getElementById(t);
	for(var i=0; i<fbox.options.length; i++) {
		if(fbox.options[i].selected && fbox.options[i].value != "") {
			var no = new Option();
			no.value = fbox.options[i].value;
			no.text = fbox.options[i].text;
			no.title = fbox.options[i].title;
			tbox.options[tbox.options.length] = no;
			fbox.options[i].value = "";
			fbox.options[i].text = "";
			fbox.options[i].title = "";
   		}
	}
	BumpUp(fbox);
	SortD(tbox);
}

function BumpUp(box)  {
	for(var i=0; i<box.options.length; i++) {
		if(box.options[i].value == "")  {
			for(var j=i; j<box.options.length-1; j++)  {
				box.options[j].value = box.options[j+1].value;
				box.options[j].text = box.options[j+1].text;
				box.options[j].title = box.options[j+1].title;
			}
			var ln = i;
			break;
		}
	}
	if(ln < box.options.length)  {
		box.options.length -= 1;
		BumpUp(box);
   	}
}

function SortD(box){
	var temp_opts = new Array();
	var temp = new Object();
	for(var i=0; i<box.options.length; i++){
		temp_opts[i] = box.options[i];
	}

	for(var x=0; x<temp_opts.length-1; x++){
		for(var y=(x+1); y<temp_opts.length; y++){
			if(temp_opts[x].text.toLowerCase() > temp_opts[y].text.toLowerCase()){
				temp = temp_opts[x].text;
				temp_opts[x].text = temp_opts[y].text;
	      		temp_opts[y].text = temp;
	      		
	      		temp = temp_opts[x].value;
	      		temp_opts[x].value = temp_opts[y].value;
	      		temp_opts[y].value = temp;

	      		temp = temp_opts[x].title;
	      		temp_opts[x].title = temp_opts[y].title;
	      		temp_opts[y].title = temp;
	      	}
	   	}
	}

	for(var j=0; j<box.options.length; j++){
		box.options[j].value = temp_opts[j].value;
		box.options[j].text = temp_opts[j].text;
		box.options[j].title = temp_opts[j].title;
	}
}

function isLetterAndNumber(str){
	var reg = new RegExp("^[A-Za-z0-9 _,.-]+$");
	return (reg.test(str));
}

function isChinese(str){
	return str.match(/[\u4e00-\u9fa5]/g);
}

function addTo()
{
	var txt = document.getElementById("newCategory").value;
	if(txt.indexOf(",")>0)
	{
		alert("<%=bundle.getString("msg_company_category_invalid") %>");
		return;
	}
	if(Trim(txt) != "")
	{
		txt = Trim(txt);
		if (!isLetterAndNumber(txt) && !isChinese(txt))
		{
			alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
			return false;
		}
		
		var toBox = document.getElementById("to");
		var fromBox = document.getElementById("from");
		for (var i=0;i<toBox.options.length;i++)
		{
			if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		for (var j=0;j<fromBox.options.length;j++)
		{
			if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		var op = new Option();
		op.value = txt;
		op.text = txt;
		op.title = txt;
		toBox.options[toBox.options.length] = op;
		document.getElementById("newCategory").value = "";

		SortD(toBox);
	}
}

function Trim(str)
{
	if(str=="") return str;
	var newStr = ""+str;
	RegularExp = /^\s+|\s+$/gi;
	return newStr.replace( RegularExp,"" );
}
</script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="companyForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
            <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
            <td>
                <% if (edit) { %>
                    <%=companyName%>
                <% } else { %>
                    <input type="text" name="<%=CompanyConstants.NAME%>" maxlength="40" size="30" value="<%=companyName%>">
                <% } %>
            </td>
            <td valign="center">
                <% if (!edit) { %>
                    <%=bundle.getString("lb_valid_name")%>
                <% } %>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td colspan="2">
                <textarea rows="6" cols="40" style="width:350px;" name="<%=CompanyConstants.DESC%>"><%=desc%></textarea>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_email")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.EMAIL%>" id="emailId" value="<%=email%>">
            </td>
        </tr>
        
        <tr>
        	<td valign="top"><%=bundle.getString("lb_session_timeout")%>&nbsp;(<%=bundle.getString("lb_minutes")%>):</td>
        	<td colspan="2">
                <input type="text" name="<%=CompanyConstants.SESSIONTIME%>" maxlength="3" size="20" value="<%=sessionTime%>">&nbsp;(30-480)
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableIPFilter")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableIPFilterId" name="<%=CompanyConstants.ENABLE_IP_FILTER%>" <%=checked%>/>
            </td>
        </tr>
        
        <tr id="ssoCheck">
            <td valign="top"><%=bundle.getString("lb_sso_enableSSO")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableSsoLogonId" onclick="onEnableSSOSwitch()" name="<%=CompanyConstants.ENABLE_SSO_LOGON%>" <%=ssoChecked%>/>
            </td>
        </tr>
        <tr id="ssoIdpUrlCC">
            <td valign="top"><%=bundle.getString("lb_sso_IdpUrl")%>:</td>
            <td colspan="2">
                <input type="textfield" name="<%=CompanyConstants.SSO_IDP_URL%>" maxlength="256" size="30" value="<%=ssoIdpUrl%>">
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_tm_tm3_enable")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="<%=CompanyConstants.TM3_VERSION%>" name="<%=CompanyConstants.TM3_VERSION%>" value="3" <%=enableTM3Checked%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTMAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTMAccessControl" name="<%=CompanyConstants.ENABLE_TM_ACCESS_CONTROL%>" <%=tmAccessControl%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTBAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTBAccessControl" name="<%=CompanyConstants.ENABLE_TB_ACCESS_CONTROL%>" <%=tbAccessControl%>/>
            </td>
        </tr>
        
        <tr valign="top">
    		<td colspan=3>
    			<br/><div class="standardText"><c:out value="${helpMsg}"/>:</div>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="from" name="from" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${fromList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('from','to')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('to','from')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="to" name="to" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${toList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="newCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addTo()">
        			</td>
        		</tr>
      			</table>
    		</td>
  		</tr>
        
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
                <input type="button" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
            </td>
        </tr>

      </table>
    </td>
  </tr>
  
</table>
</form>
</div>
</body>
</html>