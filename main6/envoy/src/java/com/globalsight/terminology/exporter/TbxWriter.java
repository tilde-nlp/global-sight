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

package com.globalsight.terminology.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.dom4j.Document;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IWriter;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SessionInfo;

public class TbxWriter implements IWriter, TermbaseExceptionMessages
{
    private static final Logger CATEGORY = Logger.getLogger(TbxWriter.class);

    //
    // Private Member Variables
    //
    private com.globalsight.terminology.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;
    
    //
    // Constructors
    //
    
    public TbxWriter(ExportOptions p_options) {
    	setExportOptions(p_options);
    }
    
    //
    // Interface Implementation -- IWriter
    //
    
    /**
     * Analyzes export options and returns an updated ExportOptions
     * object with a status whether the options are syntactically
     * correct, and column descriptors in case of CSV files.
     */
	public ExportOptions analyze() {
		return m_options;
	}

	public void setExportOptions(ExportOptions options) {
		m_options = (com.globalsight.terminology.exporter.ExportOptions) options;
	}
	
	public void writeHeader(SessionInfo p_session) throws IOException {
		m_filename = m_options.getFileName();
		//Export termbase to a folder with company specified.
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String directory = ExportUtil.getExportDirectory();
        if (!companyId.equals(CompanyWrapper.SUPER_COMPANY_ID))
        {
        	directory += CompanyWrapper.getCompanyNameById(companyId) + "/";
        	new File(directory).mkdir();
        }
        String encoding = m_options.getJavaEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            throw new IOException("invalid encoding " +
                m_options.getEncoding());
        }

        // We support only Unicode encodings for XML files: UTF-8 and
        // UTF-16 (little and big endian)
        String ianaEncoding = m_options.getEncoding();
        if (ianaEncoding.toUpperCase().startsWith("UTF-16"))
        {
            ianaEncoding = "UTF-16";
        }

        String filename = directory + m_filename;

        new File(filename).delete();

        FileOutputStream fos = new FileOutputStream(filename);
        FileUtil.writeBom(fos, ianaEncoding);
        m_output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                fos, encoding)));
        m_output.print("<?xml version=\"1.0\" encoding=\"");
        m_output.print(ianaEncoding);
        m_output.println("\" ?>");
        m_output.println("<!DOCTYPE martif PUBLIC 'ISO 12200:1999A//DTD MARTIF core (DXFcdV04)//EN' 'TBXcdv04.dtd'>");
        m_output.print("<martif type=\"TBX\" xml:lang=\"");
        m_output.print(m_options.getSelectLanguage());
        m_output.println("\">");
        m_output.println("<martifHeader>");
        m_output.println("<fileDesc>");
        m_output.println("<sourceDesc>");
        m_output.println("<p>from GlobalSight termBase</p>");
        m_output.println("</sourceDesc>");
        m_output.println("</fileDesc>");
        m_output.println("<encodingDesc>");
        m_output.println("<p type=\"DCSName\">SYSTEM 'xcs.xml'</p>");
        m_output.println("</encodingDesc>");
        m_output.println("</martifHeader>");
        m_output.println("<text>");
        m_output.println("<body>");
        
        checkIOError();
	}

	public void write(ArrayList p_entries, SessionInfo p_session)
			throws IOException {
		for (int i = 0; i < p_entries.size(); ++i) {
            Object o = p_entries.get(i);
            write(o, p_session);
        }
	}

	public void write(Object p_entry, SessionInfo p_session) throws IOException {
		String entryXml = (String)p_entry;
		try {
			if (m_options.getSystemFields().equalsIgnoreCase("false"))
            {
                Entry entry = new Entry(entryXml);

                Document dom = entry.getDom();

                entry.setDom(ExportUtil.removeSystemFields(dom));

                entryXml = entry.getXml();
            }
            m_output.println(entryXml);
            checkIOError();
		} catch (TermbaseException ex) {
            throw new IOException("internal error: " + ex.toString());
        }
	}

	public void writeTrailer(SessionInfo p_session) throws IOException {
		m_output.println("</body>");
		m_output.println("</text>");
		m_output.println("</martif>");
        m_output.close();
	}
	
	//
	// private methods
	//
	
    private void checkIOError() throws IOException {
		// The JDK is so incredibly inconsistent (aka, stupid).
		// PrintWriter.println() does not throw exceptions.
		if (m_output.checkError()) {
			throw new IOException("write error");
		}
	}

}
