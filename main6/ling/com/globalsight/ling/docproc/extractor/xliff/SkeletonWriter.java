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
package com.globalsight.ling.docproc.extractor.xliff;

import com.globalsight.ling.docproc.Output;
import java.util.*;

/**
 * Utility class used in the XML Extractor.
 */
class SkeletonWriter
    extends OutputWriter
{
    private Output output = null;

    public SkeletonWriter(Output output)
    {
        this.output = output;
    }

    public void flush()
    {
    }

    public void append(String content)
    {
        output.addSkeleton(content);
    }

    public int getOutputType()
    {
        return SKELETON;
    }
    
    public void setXliffTransPart(Map p_map) {
        
    }
}
