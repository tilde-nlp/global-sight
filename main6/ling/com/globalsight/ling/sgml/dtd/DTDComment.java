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
package com.globalsight.ling.sgml.dtd;

import java.io.*;

/** Represents a comment in the DTD
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:36 $ by $Author: yorkjin $
 */
public class DTDComment
    implements DTDOutput
{
    /** The comment text */
    public String text;

    public DTDComment()
    {
    }

    public DTDComment(String theText)
    {
        text = theText;
    }

    public String toString()
    {
        return text;
    }

    public void write(PrintWriter out)
        throws IOException
    {
        out.print("<!--");
        out.print(text);
        out.println("-->");
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDComment)) return false;

        DTDComment other = (DTDComment) ob;
        if ((text == null) && (other.text != null)) return false;
        if ((text != null) && !text.equals(other.text)) return false;

        return true;
    }

    /** Sets the comment text */
    public void setText(String theText)
    {
        text = theText;
    }

    /** Returns the comment text */
    public String getText()
    {
        return text;
    }
}
