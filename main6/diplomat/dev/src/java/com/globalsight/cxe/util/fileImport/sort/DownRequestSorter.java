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
package com.globalsight.cxe.util.fileImport.sort;

import java.util.HashMap;

import com.globalsight.cxe.message.CxeMessage;

public class DownRequestSorter extends RequestSorter
{

    @SuppressWarnings("rawtypes")
    @Override
    protected CxeMessage getNextMessage(int i)
    {
        i++;

        // ignore the message that has been updated.
        while (i < ms.size())
        {
            CxeMessage cm = ms.get(i);
            HashMap p = cm.getParameters();
            if (!keys.contains(p.get("uiKey")))
                break;
            i++;
        }
        
        if (i >= ms.size())
            return null;

        return ms.get(i);
    }

    @Override
    protected boolean isUp()
    {
        return false;
    }

}
