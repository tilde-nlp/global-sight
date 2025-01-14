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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserCalendarComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The page handler for displaying the list of "system" calendars,
 * "user" calendars, and the holidays.
 */

public class UserCalendarHandler extends PageHandler
    implements CalendarConstants
{
    private int m_userCalsPerPage = 10;
    
    public UserCalendarHandler()
    {
        try 
        {
            SystemConfiguration sysconfig = SystemConfiguration.getInstance();
            m_userCalsPerPage = sysconfig.getIntParameter(
                SystemConfigParamNames.CALENDERING_DISPLAY_PER_PAGE);
        }
        catch (Exception e)
        {
            // will use initialized defaults
        }
    }

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
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);

        String action = request.getParameter(ACTION);
        
        if (CANCEL_ACTION.equals(action))
        {
            // clean session manager
            clearSessionExceptTableInfo(session, USER_CAL_KEY);
        }
        else if (SAVE_ACTION.equals(action)) 
        {
            saveCal(request, session);
        }
        initUserCalTable(request, session);
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }


    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new UserCalendarControlFlowHelper(p_request, p_response);
    }

    /**
     * Save the calendar and clear the session.
     */
    private void saveCal(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        CalendarHelper.modifyUserCalendar(request, session, cal);
        clearSessionExceptTableInfo(session, USER_CAL_KEY);
    }


    private void initUserCalTable(HttpServletRequest request,
                                     HttpSession session)
    throws EnvoyServletException
    {

        // Get list of user calendars
        List cals = CalendarHelper.getAllUserCalendars();

        Locale uiLocale = (Locale)session.getAttribute(
            WebAppConstants.UILOCALE);
        setTableNavigation(request, session, cals, 
                            new UserCalendarComparator(uiLocale),
                            m_userCalsPerPage,
                            USER_CAL_LIST, USER_CAL_KEY);

    }
}
