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

package com.globalsight.diplomat.servlet.config;

public class WrapCharacterSetTable
{
    private int m_id = 0;               // primary key
    private String m_characterSet;      // character set
        
    public WrapCharacterSetTable (int p_id, String p_characterSet)
    {
        m_id = p_id;
        m_characterSet = p_characterSet;
    }
    
    public int getID() { return m_id; }
    public String getCharacterSet() { return m_characterSet; }
}