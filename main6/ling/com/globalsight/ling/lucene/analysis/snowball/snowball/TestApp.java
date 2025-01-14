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
package com.globalsight.ling.lucene.analysis.snowball.snowball;

import java.lang.reflect.Method;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class TestApp
{
    public static void main(String[] args)
        throws Throwable
    {
        if (args.length < 2)
        {
            exitWithUsage();
        }

        Class stemClass = Class.forName(
            "com.globalsight.ling.lucene.analysis.snowball.snowball.ext." +
            args[0] + "Stemmer");
        SnowballProgram stemmer = (SnowballProgram)stemClass.newInstance();

        Reader reader;
        reader = new InputStreamReader(new FileInputStream(args[1]));
        reader = new BufferedReader(reader);

        StringBuffer input = new StringBuffer();

        OutputStream outstream = System.out;

        if (args.length > 2 && args[2].equals("-o"))
        {
            outstream = new FileOutputStream(args[3]);
        }
        else if (args.length > 2)
        {
            exitWithUsage();
        }

        Writer output = new OutputStreamWriter(outstream);
        output = new BufferedWriter(output);

        int repeat = 1;
        if (args.length > 4)
        {
            repeat = Integer.parseInt(args[4]);
        }

        int character;
        while ((character = reader.read()) != -1)
        {
            char ch = (char) character;
            if (Character.isWhitespace(ch))
            {
                if (input.length() > 0)
                {
                    stemmer.setCurrent(input.toString());

                    for (int i = repeat; i != 0; i--)
                    {
                        stemmer.stem();
                    }

                    output.write(stemmer.getCurrent());
                    output.write('\n');

                    input.delete(0, input.length());
                }
            }
            else
            {
                input.append(Character.toLowerCase(ch));
            }
        }

        output.flush();
    }

    static private void exitWithUsage()
    {
        System.err.println("Usage: TestApp <stemmer name> <input file> [-o <output file>]");
        System.exit(1);
    }
}
