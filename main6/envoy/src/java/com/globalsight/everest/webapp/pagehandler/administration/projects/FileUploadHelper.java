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

package com.globalsight.everest.webapp.pagehandler.administration.projects;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfiguration;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FileUploadHelper
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            FileUploadHelper.class.getName());


    private final static int    MAX_LINE_LENGTH = 4096;
    private final static String IMPORT_OPTIONS = "importoptions";

    private final static String FILE_UPLOAD_DIR = "_Projects_";

    public static String DOCROOT = "/";

    static {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            DOCROOT = root;
        }
        catch (Throwable e)
        {
            CATEGORY.error(
                "cannot create directory " + DOCROOT);
        }
    }


    private Hashtable   m_fields = new Hashtable();
    private String      m_contentType = null;
    private String      m_filename = null;
    private String      m_filepath = null;
    private String      m_savedFilepath = null;
    private File        m_savedFile = null;

    public String getFieldValue (String p_fieldName)
    {
        if (p_fieldName == null)
        {
            return null;
        }
        else
        {
            return (String) m_fields.get(p_fieldName);
        }
    }

    public String getFilename ()
    {
        return m_filename;
    }

    public String getFilepath ()
    {
        return m_filepath;
    }

    private void setSavedFilepath(String p_filePath)
    {
        m_savedFilepath = p_filePath;
    }

    public String getSavedFilepath()
    {
        return m_savedFilepath;
    }

    public String getContentType ()
    {
        return m_contentType;
    }

    public void setImportOptions(String s)
    {
        m_fields.put(IMPORT_OPTIONS, s);
    }

    public String getImportOptions()
    {
        String value = getFieldValue(IMPORT_OPTIONS);

        if (value == null)
        {
            return "";
        }
        else
        {
            return value;
        }
    }

    public Enumeration getFields ()
    {
        return m_fields.keys();
    }

    private void setFilename (String p_filenameLine)
    {
        int start = 0;

        if (p_filenameLine != null &&
            (start = p_filenameLine.indexOf("filename=\"")) != -1)
        {
            m_filepath = p_filenameLine.substring(start + 10,
                p_filenameLine.length() - 1);

            //  Handle Windows v/s Unix file path
            //
            if ((start = m_filepath.lastIndexOf('\\')) > -1)
            {
                m_filename = m_filepath.substring(start + 1);
            }
            else if ((start = m_filepath.lastIndexOf('/')) > -1)
            {
                m_filename = m_filepath.substring(start + 1);
            }
            else
            {
                m_filename = m_filepath;
            }
        }
    }

    private void setContentType (String p_contentLine)
    {
        int start = 0;

        if (p_contentLine != null &&
            (start = p_contentLine.indexOf(": ")) != -1)
        {
            m_contentType = p_contentLine.substring(start + 2);
        }
        else
        {
            m_contentType = "application/octet-stream";
        }
    }

    public void doUpload (HttpServletRequest p_request)
        throws Exception
    {
        try
        {
            byte[]              inBuf = new byte[MAX_LINE_LENGTH];
            int                 bytesRead;
            ServletInputStream  in;
            File                outFile = null;
            String              contentType;
            String              boundary;

            CATEGORY.debug("Start uploading file");

            //  Let's make sure that we have the right type of content
            //
            contentType = p_request.getContentType();
            if (contentType == null ||
                !contentType.toLowerCase().startsWith("multipart/form-data"))
            {
                throw new Exception("form did not use " +
                    "ENCTYPE=multipart/form-data but `" + contentType + "'");
            }

            //  Extract the boundary string in this request. The
            //  boundary string is part of the content type string
            //
            int bi = contentType.indexOf("boundary=");
            if (bi == -1)
            {
                throw new Exception("no boundary string found in request");
            }
            else
            {
                // 9 := len("boundary=")
                boundary = contentType.substring(bi + 9);

                //  The real boundary has additional two dashes in
                //  front
                //
                boundary = "--" + boundary;
            }

            in = p_request.getInputStream();
            bytesRead = in.readLine(inBuf, 0, inBuf.length);

            if (bytesRead < 3)
            {
                //  Not enough content was send as part of the post
                //
                throw new Exception("incomplete request (not enough data)");
            }

            while (bytesRead != -1)
            {
                String lineRead = new String(inBuf, 0, bytesRead, "utf-8");

                if (lineRead.startsWith("Content-Disposition: form-data; name=\""))
                {
                    if (lineRead.indexOf("filename=\"") != -1)
                    {
                        //  This is a file part

                        //  Get file name
                        setFilename(lineRead.substring(0, lineRead.length() - 2));

                        CATEGORY.debug("Found upload file " + getFilename());

                        //  Get content type line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);
                        lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");
                        setContentType(lineRead);

                        //  Read and ignore the blank line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);

                        //  Create a temporary file to store the
                        //  contents in it for now. We might not have
                        //  additional information, such as TUV id for
                        //  building the complete file path. We will
                        //  save the contents in this file for now and
                        //  finally rename it to correct file name.
                        //
                        outFile = File.createTempFile("GSProjectUpload", null);
                        FileOutputStream fos = new FileOutputStream(outFile);
                        BufferedOutputStream bos =
                            new BufferedOutputStream(fos, MAX_LINE_LENGTH * 4);

                        //  Read through the file contents and write
                        //  it out to a local temp file.
                        //
                        boolean writeRN = false;
                        while ((bytesRead =
                            in.readLine(inBuf, 0, inBuf.length)) != -1)
                        {
                            //  Let's first check if we are already on
                            //  boundary line
                            //
                            if (bytesRead > 2 &&
                                inBuf[0] == '-' &&
                                inBuf[1] == '-')
                            {
                                lineRead = new String(inBuf, 0, bytesRead,
                                    "utf-8");
                                if (lineRead.startsWith(boundary))
                                    break;
                            }

                            //  Write out carriage-return, new-line
                            //  pair which might have been left over
                            //  from last write.
                            //
                            if (writeRN)
                            {
                                bos.write(new byte[] {(byte)'\r', (byte)'\n'});
                                writeRN = false;
                            }

                            //  The ServletInputStream.readline() adds
                            //  "\r\n" bytes for the last line of the
                            //  file contents.  If we find this pair
                            //  as the last bytes we need to delay
                            //  writing it until the next go, since it
                            //  could very well be the last line of
                            //  file content.
                            //
                            if (bytesRead >= 2 &&
                                inBuf[bytesRead - 2] == '\r' &&
                                inBuf[bytesRead - 1] == '\n')
                            {
                                bos.write(inBuf, 0, bytesRead - 2);
                                writeRN = true;
                            }
                            else
                            {
                                bos.write(inBuf, 0, bytesRead);
                            }
                        }

                        bos.flush();
                        bos.close();
                        fos.close();
                    }
                    else
                    {
                        //  This is the field part

                        //  First get the field name
                        //
                        int    start = lineRead.indexOf("name=\"");
                        int    end = lineRead.indexOf("\"", start + 7);
                        String fieldName = lineRead.substring(start + 6, end);

                        //  Read and ignore the blank line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);

                        //  String Buffer to keep the field value
                        //
                        StringBuffer fieldValue = new StringBuffer();

                        boolean writeRN = false;
                        while ((bytesRead =
                            in.readLine(inBuf, 0, inBuf.length)) != -1)
                        {
                            lineRead = new String(inBuf, 0, bytesRead, "utf-8");

                            //  Let's first check if we are already on
                            //  boundary line
                            //
                            if (bytesRead > 2 &&
                                inBuf[0] == '-' &&
                                inBuf[1] == '-')
                            {
                                if (lineRead.startsWith(boundary))
                                    break;
                            }

                            //  Write out carriage-return, new-line
                            //  pair which might have been left over
                            //  from last write.
                            //
                            if (writeRN)
                            {
                                fieldValue.append("\r\n");
                                writeRN = false;
                            }

                            //  The ServletInputStream.readline() adds
                            //  "\r\n" bytes for the last line of the
                            //  field value.  If we find these pair as
                            //  the last bytes we need to delay
                            //  writing it until the next go, since it
                            //  could very well be the last line of
                            //  field value.
                            //
                            if (bytesRead > 2 &&
                                inBuf[bytesRead - 2] == '\r' &&
                                inBuf[bytesRead - 1] == '\n')
                            {
                                fieldValue.append(lineRead.substring(
                                    0, lineRead.length() - 2));
                                writeRN = true;
                            }
                            else
                            {
                                fieldValue.append(lineRead);
                            }
                        }

                        // Add field to collection of field
                        //
                        CATEGORY.debug("Found field " + fieldName +
                            " value = " + fieldValue.toString());

                        m_fields.put(fieldName, fieldValue.toString());
                    }
                }

                bytesRead = in.readLine(inBuf, 0, inBuf.length);
            }

            //  By now, we should have all parameter values needed to
            //  contruct a correct filepath to save the uploaded
            //  images
            //
            if (outFile != null && outFile.exists())
            {

                //  First, define and create image upload directory
                //  structure, if not already done so
                //
                setSavedFilepath(FILE_UPLOAD_DIR + "/");
                File savedDir = new File(DOCROOT + getSavedFilepath());
                savedDir.mkdirs();

                //  Create a destination file and rename/move the file
                //  from temporary location to upload directory
                //
                m_savedFile = new File(savedDir, getFilename());
                renameFile(outFile, m_savedFile);

                setSavedFilepath(m_savedFile.getAbsolutePath());
            }
        }
        catch (SecurityException se)
        {
            throw new Exception(se.getMessage());
        }
        catch (IOException ioe)
        {
            throw new Exception (ioe.getMessage());
        }
    }


    /**
     * Renames a file from its originalName to a newName.
     * NOTE: java.io.File.renameTo() has some known bugs
     * that apparently are still unfixed in 1.3 so it cannot
     * be used.
     * <br>
     * This implementation simply copies originalName to newName
     * and then deletes the originalName file.
     * @param p_originalName -- source file name
     * @param p_newName -- destination file name
     */
    public static void renameFile(File p_originalName, File p_newName)
        throws IOException
    {
        FileInputStream fis = new FileInputStream(p_originalName);
        FileOutputStream fos = new FileOutputStream(p_newName);
        byte buffer[] = new byte[2056];
        boolean keepReading = true;
        int numBytesRead = -1;
        while (keepReading)
        {
            numBytesRead = fis.read(buffer);
            if (numBytesRead == -1)
                keepReading = false;
            else
                fos.write(buffer, 0, numBytesRead);
        }

        fis.close();
        fos.close();
        p_originalName.delete();
    }
}
