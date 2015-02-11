/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bussiness.process;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.JobInfo;

/**
 * Special downloading/post process for "Use case 04" 
 * The job info(jobName, fpName, targetLocales) comes from ZIP file.
 * 
 * @author Joey
 * 
 */
public class Usecase04PostProcess extends Usecase01PostProcess
{
    public boolean process(JobInfo jobInfo, CompanyConfiguration cpConfig)
    {
        return super.process(jobInfo, cpConfig);
    }
}
