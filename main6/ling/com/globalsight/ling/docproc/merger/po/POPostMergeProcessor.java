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
package com.globalsight.ling.docproc.merger.po;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;

/**
 * This class post processes a merged plaintext document: every
 * Unix-style LF is replaced by Windows-style CRLF.
 */
public class POPostMergeProcessor
    implements PostMergeProcessor
{
    private static Logger c_category =
        Logger.getLogger(
            POPostMergeProcessor.class);

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String, java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException
    {
        StringBuffer result = new StringBuffer();

        boolean b_skip = false;
        for (int i = 0, max = p_content.length(); i < max; i++)
        {
            char ch = p_content.charAt(i);

            if (ch == '\r')
            {
                b_skip = true;
            }
            else if (ch == '\n')
            {
                if (!b_skip)
                {
                    result.append('\r');
                }

                b_skip = false;
            }

            result.append(ch);
        }

        return result.toString();
    }
}
