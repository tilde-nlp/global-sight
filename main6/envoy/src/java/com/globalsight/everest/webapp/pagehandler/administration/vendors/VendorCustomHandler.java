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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.foundation.User;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;

public class VendorCustomHandler extends PageHandler
{
    
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        String action = (String)request.getParameter("action");

        Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
        User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        FieldSecurity securitiesHash = (FieldSecurity)
            sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);

        if ("custom".equals(action))
        {
            String id = (String) request.getParameter("id");
            sessionMgr.setAttribute("edit", "true");
        }
        else if ("next".equals(action))
        {
            // save project info
            VendorHelper.saveProjects(vendor, request);

            sessionMgr.setAttribute("customPageTitle", CustomPageHelper.getPageTitle());
        }
        else if ("prev".equals(action))
        {
            // save security info
            VendorHelper.saveSecurity(securitiesHash, request);

            sessionMgr.setAttribute("customPageTitle", CustomPageHelper.getPageTitle());
        }
        request.setAttribute("pageContent",
                 CustomPageHelper.getPageContent(session, vendor.getCustomFields(),
                 (String)securitiesHash.get(VendorSecureFields.CUSTOM_FIELDS)));

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }
}
