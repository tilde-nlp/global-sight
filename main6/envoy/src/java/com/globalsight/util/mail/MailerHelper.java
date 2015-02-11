/**
 *  Copyright 2011 Welocalize, Inc. 
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
package com.globalsight.util.mail;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.util.GlobalSightLocale;

// Mail Help Class
public class MailerHelper
{
    private static final Logger s_logger = Logger.getLogger(MailerHelper.class
            .getName());

    MailerHelper()
    {
    }

    /**
     * Gets the Email HTML body.
     */
    public static String getHTMLContext(String p_message)
    {
        StringBuffer result = new StringBuffer();
        result.append("<html><head>")
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">")
                .append("<style type=\"text/css\">")
                .append("body {font-family: Arial, Helvetica, sans-serif; font-size: 10pt; line-height: 15pt;}")
                .append(".classBold{font-weight:bold; font-size: 10.5pt;}")
                .append("table { border-collapse: collapse; }")
                .append("table, th, td {border: 1px solid black; padding:3px;}")
                .append("</style>")
                .append("</head><body>")
                .append(p_message.replace("\r\n", "<br/>").replace("\n",
                        "<br/>")).append("</body></html>");

        return result.toString();
    }

    /**
     * Gets the Email text/plain body.
     */
    public static String getTextContext(String p_message)
    {
        String result = p_message.replace("<span class=\"classBold\">", "")
                .replace("</span>", "");
        result = replaceURL(result);
        return result;
    }

    /**
     * For HTML Email body, URL looks like
     * "<a href="http://www.welocalize.com" target="
     * _blank">http://www.welocalize.com</a>", But Plain/text just need URL,
     * like "http://www.welocalize.com".
     */
    public static String replaceURL(String p_message)
    {
        String urlRegex = "((http|https)://([\\w-]+\\.)+[\\w-]+[:\\d]*(/[\\w- ./?%&=]*)?)";
        String regex = "<a href=\"" + urlRegex + "\" target=\"_blank\">"
                + urlRegex + "</a>";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(p_message);
        while (m.find())
        {
            if (m.group(1).equals(m.group(5)))
            {
                p_message = p_message.replace(m.group(), m.group(1));
            }
        }

        return p_message;
    }

    public static String getSendFrom(String p_comId)
    {
        return getSendFrom(p_comId, (String) null);
    }

    public static String getSendFrom(String p_comId, User p_user)
    {
        return getSendFrom(p_comId, p_user.getUserName());
    }

    public static String getSendFrom(String p_comId, String p_userName)
    {
        Company com = null;
        if (p_comId != null)
        {
            com = CompanyWrapper.getCompanyById(p_comId);
        }

        EmailInformation user = null;
        try
        {
            if (p_userName != null && p_userName.trim().length() > 0)
            {
                user = ServerProxy.getUserManager().getEmailInformationForUser(
                        p_userName);
            }
        }
        catch (Exception e)
        {
            s_logger.error("There is an Exception in MailerHelper.getSendFrom "
                    + "with username:" + p_userName + " and companyId:"
                    + p_comId);
        }

        return getSendFrom(com, user);
    }

    public static String getSendFrom(String p_comId, EmailInformation p_user)
    {
        Company com = null;
        if (p_comId != null)
        {
            com = CompanyWrapper.getCompanyById(p_comId);
        }

        return getSendFrom(com, p_user);
    }

    /**
     * Get FROM Address of Email, by Company and User
     * 
     * @param p_com
     *            Company
     * @param p_user
     *            User
     * 
     *            IF result != null && p_user != null
     * @return [<Firstname> <Lastname>] <Company Email Address> IF result !=
     *         null && p_user == null
     * @return <Company Email Address> IF result == null && p_user != null
     * @return Company Email Address which the user belongs to IF result == null
     *         && p_user == null
     * @return Super Company Email Address
     */
    public static String getSendFrom(Company p_com, EmailInformation p_user)
    {
        String result = getCompanyEmail(p_com);
        if (result != null && p_user != null)
        {
            result = p_user.getUserFullName() + " <" + result + ">";
        }
        else if (result == null && p_user != null)
        {
            result = getCompanyEmail(p_user);
        }
        else if (result == null && p_user == null)
        {
            result = getSuperAdminEmail();
        }

        return result;
    }

    /**
     * Get company email from User. If user is a super user, then get "current"
     * company name(but this can't ensure the "current" company name is what we
     * want in the situation of super user works for a sub company,this is out
     * of current method's scope).
     */
    public static String getCompanyEmail(EmailInformation p_user)
    {
        String companyName = p_user.getCompanyName();
        if (CompanyWrapper.isSuperCompanyName(companyName))
        {
            String temp = CompanyWrapper.getCurrentCompanyName();
            if (temp != null)
            {
                companyName = temp;
            }
        }
        return getCompanyEmail(companyName);
    }

    public static String getCompanyEmail(String p_companyName)
    {
        Company com = CompanyWrapper.getCompanyByName(p_companyName);
        return getCompanyEmail(com);
    }

    public static String getCompanyEmail(Company p_com)
    {
        String result = p_com.getEmail();
        if (result == null || result.trim().length() == 0)
        {
            result = getSuperAdminEmail();
        }

        return (result == null || result.trim().length() == 0) ? null : result
                .trim();
    }

    // Get compnayId String from TaskEmailInfo
    public static String getCompanyId(TaskEmailInfo p_emailInfo)
    {
        String companyIdStr = p_emailInfo.getCompanyId();
        if (companyIdStr != null)
        {
            return companyIdStr;
        }

        String projectName = p_emailInfo.getProjectName();

        if (projectName != null && projectName.length() > 0)
        {
            try
            {
                Project proj = ServerProxy.getProjectHandler()
                        .getProjectByName(projectName);
                companyIdStr = String.valueOf(proj.getCompanyId());
                if (companyIdStr != null)
                {
                    return companyIdStr;
                }
            }
            catch (Exception e)
            {
                s_logger.error(
                        "There is an Exception in getCompanyId, by Project", e);
            }
        }

        String jobName = p_emailInfo.getJobName();
        if (jobName != null && jobName.length() > 0)
        {
            try
            {
                Job job = ServerProxy.getJobHandler().getJobByJobName(jobName);
                companyIdStr = String.valueOf(job.getCompanyId());
                if (companyIdStr != null)
                {
                    return companyIdStr;
                }
            }
            catch (Exception e)
            {
                s_logger.error("There is an Exception in getCompanyId, by Job",
                        e);
            }
        }

        return companyIdStr;
    }

    public static String getSuperAdminEmail()
    {
        String result = null;
        try
        {
            result = ServerProxy
                    .getSystemParameterPersistenceManager()
                    .getAdminSystemParameter(SystemConfigParamNames.ADMIN_EMAIL)
                    .getValue();
        }
        catch (Exception e)
        {
            s_logger.error("Fail to get super 'admin.email'.", e);
        }

        if (result != null && result.trim().length() > 0)
        {
            return result;
        }
        else
        {
            return "globalsight@domain.com";
        }
    }

    public static String getLocalePair(GlobalSightLocale p_srcLocale,
            GlobalSightLocale p_trgLocale, Locale p_uiLocale)
    {
        StringBuffer result = new StringBuffer();
        result.append(p_srcLocale.getDisplayName(p_uiLocale));
        result.append(" / ");
        result.append(p_trgLocale.getDisplayName(p_uiLocale));

        return result.toString();
    }
}