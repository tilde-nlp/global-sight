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
package com.globalsight.ling.jtidy;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


/**
 * Tidy Output factory.
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public final class OutFactory
{

    /**
     * Don't instantiate.
     */
    private OutFactory()
    {
        // unused
    }

    /**
     * Returns the appropriate Out implementation.
     * @param config configuration instance
     * @param stream output stream
     * @return out instance
     */
    public static Out getOut(Configuration config, OutputStream stream)
    {
        try
        {
            return new OutJavaImpl(config, config.getOutCharEncodingName(), stream);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unsupported encoding: " + e.getMessage());
        }
    }
}
