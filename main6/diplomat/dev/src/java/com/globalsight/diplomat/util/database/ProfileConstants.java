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
package com.globalsight.diplomat.util.database;

/**
 * A place-holder interface for defining constants that are used by several
 * classes in this package.
 */
public interface ProfileConstants
{
    /* Content mode constants */
    public static final int TRANSLATABLE = 1;
    public static final int CONTEXTUAL = 2;
    public static final int INVISIBLE = 3;
    public static final String[] CONTENT_MODES =
    {
        "translatable",
        "contextual",
        "invisible"
    };

    /* Data type constants */
    public static final String TEXT = "text";
    public static final String HTML = "html";
    public static final String XML = "xml";
    public static final String OTHER = "other";
}

