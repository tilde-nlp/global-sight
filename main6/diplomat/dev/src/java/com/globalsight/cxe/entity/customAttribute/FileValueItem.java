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

package com.globalsight.cxe.entity.customAttribute;

import com.globalsight.everest.persistence.PersistentObject;

public class FileValueItem extends PersistentObject
{
    private static final long serialVersionUID = -7882465710014091185L;
    private String path;
    private JobAttribute jobAttribute;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public JobAttribute getJobAttribute()
    {
        return jobAttribute;
    }

    public void setJobAttribute(JobAttribute jobAttribute)
    {
        this.jobAttribute = jobAttribute;
    }
}
