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

package com.globalsight.ling.tw.internal;

public class ColorInternalTag implements InternalTag
{
    public static final String COLOR_TAG_PREFIX = "<span class=\"editorSegmentInternal\">";
    public static final String COLOR_TAG_SUFFIX = "</span>";
    
    @Override
    public String getInternalTag(String internalText, String allText,
            InternalTexts texts)
    {
        StringBuilder tag = new StringBuilder();
        tag.append(COLOR_TAG_PREFIX).append(internalText).append(COLOR_TAG_SUFFIX);
        return tag.toString();
    }
}
