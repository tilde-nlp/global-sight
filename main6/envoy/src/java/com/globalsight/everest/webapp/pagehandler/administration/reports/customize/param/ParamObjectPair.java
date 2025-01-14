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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param;

import java.text.DateFormat;
import java.util.List;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.workflowmanager.Workflow;

public abstract class ParamObjectPair {
    
    private Param m_param;
    
    private ParamObjectPair()
    {
        //Nothing here.
    }
    
    ParamObjectPair(Param p_param) {
        this.m_param = p_param;
    }
    
    public Param getParam() {
        return this.m_param;
    }
    
    public abstract List getResult(Job p_job, 
                                   Workflow p_workflow, 
                                   DateFormat p_format,
                                   ProjectWorkflowData workflowData);
    
    public abstract List getTotal(ProjectWorkflowData workflowData);
}

