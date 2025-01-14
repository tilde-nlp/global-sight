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
package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;

import org.apache.log4j.Logger;


public interface Filter
{
    public static final Logger CATEGORY = Logger
            .getLogger(Filter.class);

    ArrayList<Filter> getFilters(long companyId);

    String toJSON(long companyId);

    boolean checkExistsNew(String filterName, long companyId);
    
    boolean checkExistsEdit(long filterId, String filterName, long companyId);

    String getFilterTableName();

    String getFilterName();

    long getId();
}
