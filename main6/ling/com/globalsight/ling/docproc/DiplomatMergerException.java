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
package com.globalsight.ling.docproc;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

public class DiplomatMergerException
    extends GeneralException
{
    public DiplomatMergerException(int p_exceptionId)
    {
        super(GeneralExceptionConstants.COMP_MERGER, p_exceptionId);
    }

    public DiplomatMergerException(int p_componentId, int p_exceptionId)
    {
        super(p_componentId, p_exceptionId);
    }

    public DiplomatMergerException(int p_componentId, int p_exceptionId,
      int p_messageId)
    {
        super(p_componentId, p_exceptionId, p_messageId);
    }

    public DiplomatMergerException(int p_componentId, int p_exceptionId,
      int p_messageId, Exception p_originalException)
    {
        super(p_componentId, p_exceptionId, p_messageId, p_originalException);
    }

    public DiplomatMergerException(int p_componentId, int p_exceptionId,
      Exception p_originalException)
    {
        super(p_componentId, p_exceptionId, p_originalException);
    }

    public DiplomatMergerException(int p_componentId, int p_exceptionId,
      String p_message)
    {
        super(p_componentId, p_exceptionId, p_message);
    }

    public DiplomatMergerException(int p_exceptionId,
                                   Exception p_originalException)
    {
        super(GeneralExceptionConstants.COMP_MERGER, p_exceptionId,
          p_originalException);
    }

    public DiplomatMergerException(int p_exceptionId,
      String p_message)
    {
        super(GeneralExceptionConstants.COMP_MERGER, p_exceptionId, p_message);
    }

    public DiplomatMergerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    public DiplomatMergerException(String p_messageKey,
                                   String[] p_messageArguments,
                                   Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }

    /**
     * Returns a String that represents the value of this object.
     * @return a string representation of the receiver
     */
    public String toString()
    {
        // Insert code to print the receiver here.

        // This implementation forwards the message to super. You may
        // replace or supplement this.
        return super.toString();
    }
}
