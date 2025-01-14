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

package com.globalsight.everest.persistence.request;

/**
 * Specifies the names of all the named queries for Request.
 */
public interface WorkflowRequestQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return a request with the given id.
     * <p>
     * Arguments: 1: Request id.
     */
    public static String WORKFLOW_REQUEST_BY_ID = "getWorkflowRequestById";
    /**
    *  A named query to return a request list with the given job id
    */
    public static String WORKFLOW_REQUEST_LIST_BY_JOB_ID = "getWorkflowRequestListByJobId";

}
