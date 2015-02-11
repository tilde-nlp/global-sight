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

package com.globalsight.terminology.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.apache.regexp.RE;

import sun.io.MalformedInputException;

import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.l18n.L18nable;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

/**
 * Reads CSV files and produces Entry objects.
 */
public class CsvReader
    implements IReader, TermbaseExceptionMessages, L18nable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CsvReader.class);

    //
    // Private Member Variables
    //
    private Termbase m_termbase;
    private ImportOptions m_options;
    private int m_entryCount;

    private CsvReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;
    private ResourceBundle bundle;

    //
    // Constructors
    //

    public CsvReader (ImportOptions p_options, Termbase p_termbase)
    {
        m_termbase = p_termbase;
        setImportOptions(p_options);
    }

    //
    // Interface Implementation -- IReader
    //

    public void setImportOptions(ImportOptions p_options)
    {
        m_options = p_options;
    }


    public boolean hasNext()
    {
        // Ensure the thread is running
        startThread();

        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        // No more results, clean up
        stopThread();
        return false;
    }

    public ReaderResult next()
    {
        return m_result;
    }

    /**
     * Analyzes the import file and returns an updated ImportOptions
     * object with a status whether the file is syntactically correct,
     * the number of expected entries, and column descriptors in case
     * of CSV files.
     */
    public ImportOptions analyze()
    {
        m_entryCount = 0;

        try
        {
            analyzeFile(m_options.getFileName());

            m_options.setStatus(m_options.ANALYZED);
            m_options.setExpectedEntryCount(m_entryCount);
        }
        catch (Exception ex)
        {
            m_options.setError(ex.getMessage());
        }

        return m_options;
    }

    //
    // Private Methods
    //
    private void startThread()
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (100);
            m_thread = new CsvReaderThread(m_results, m_options, m_termbase);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();
            m_results = null;
            m_thread = null;
        }
    }


    /**
     * Reads a CSV file and checks the columns. If there's any error
     * in the file, an exception with a descriptive message is thrown.
     */
    private void analyzeFile(String p_url)
        throws Exception
    {
        // sets expected number of entries as side-effect
        checkFileEncoding(p_url);
        analyzeColumns(p_url);
    }

    /**
     * Reads an input file once, checking the encoding. If an encoding
     * error occurs, an exception is thrown. As a side effect, the
     * lines are counted and the expected number of entries is set to
     * that count.
     *
     * @exception MalformedInputException file had an invlid encoding
     */
    private void checkFileEncoding(String p_url)
        throws Exception
    {
        LineNumberReader reader = null;
        int skippedLines = 0;

        try
        {
            reader = getReader(p_url);
            reader.setLineNumber(0);

            String line;
            while ((line = reader.readLine()) != null)
            {
                if (ImportUtil.isEmptyLine(line))
                {
                    ++skippedLines;
                }
            }

            // is this off by one?
            m_entryCount = reader.getLineNumber() - skippedLines;

            if (m_options.isIgnoreHeader() && m_entryCount > 1)
            {
                --m_entryCount;
            }

            m_options.setExpectedEntryCount(m_entryCount);
        }
        catch (MalformedInputException ex)
        {
            throw new Exception("Invalid file encoding");
        }
        catch (IOException ex)
        {
            throw new Exception("Error reading file (" +
                ex.getMessage() + ")");
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Exception ignore) {}
            }
        }
    }

    private void analyzeColumns(String p_url)
        throws Exception
    {
        LineNumberReader reader = null;
        String defaultName = "Column";
        try
        {
            defaultName = bundle.getString("lb_column");
        }
        catch(Exception e)
        {
            //ignore it.
        }

        try
        {
            reader = getReader(p_url);
            reader.setLineNumber(0);

            String line;
            do
            {
                line = reader.readLine();
            }
            while (line != null && ImportUtil.isEmptyLine(line));

            String separator = m_options.getSeparator();
            
            boolean isFirstLine = true;
            
            //Added for encoding comma within quoted string
            CsvReaderUtil csvRUtil = new CsvReaderUtil();
            boolean _IfEncode = false;
            _IfEncode = csvRUtil.ifEncodeForCSV(line);
            if(_IfEncode)
            {
            	line = csvRUtil.encodeForCSV(line);
            }

            // Build a regexp from the separator char and be careful
            // to protect special chars like '|' (make them "\|").
            RE regexp = ImportUtil.getDelimiterRegexp(separator);

            String[] columns = regexp.split(line + " ");
            int numColumns = columns.length;
            
            //Added for decoding comma within columns
            if(_IfEncode)
            {
            	columns = csvRUtil.decodeForCSV(columns);
            }

            // Remove excessive whitespace.
            for (int i = 0; i < columns.length; ++i)
            {
                //columns[i] = columns[i].trim();
            	columns[i] = csvRUtil.csvDelQuotation(columns[i]);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("line " + reader.getLineNumber() +
                    " (" + columns.length + " columns) `" + line + "'");
            }

            String[] columnNames;
            String[] columnExamples;

            if (m_options.isIgnoreHeader())
            {
                // there is a header line that we can use to label columns
                columnNames = columns;

                for (int i = 0; i < columnNames.length; ++i)
                {
                    if (columnNames[i].length() == 0)
                    {
                        columnNames[i] = defaultName + " " + (i + 1);
                    }
                }

                do
                {
                    // read a second, non-empty line
                    line = reader.readLine();
                }
                while (line != null && ImportUtil.isEmptyLine(line));
            }
            else
            {
                // no header line, use default names "Column n"
                columnNames = new String[numColumns];

                for (int i = 0; i < numColumns; ++i)
                {
                    columnNames[i] = defaultName + (i + 1);
                }
            }

            if (line != null)
            {
                _IfEncode = csvRUtil.ifEncodeForCSV(line);
                if(_IfEncode)
                {
                	line  = csvRUtil.encodeForCSV(line);
                }
            	columns   = regexp.split(line + " ");
            	if(_IfEncode)
                {
                	columns = csvRUtil.decodeForCSV(columns);
                }
            	
                // Remove excessive whitespace.
                for (int i = 0; i < columns.length; ++i)
                {
                    //columns[i] = columns[i].trim();
                	columns[i] = csvRUtil.csvDelQuotation(columns[i]);
                }

                columnExamples = columns;

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("line " + reader.getLineNumber() +
                        " (" + columns.length + " columns) `" + line + "'");
                }
            }
            else
            {
                // This really means we don't have enough data -
                // reuse old columns for now
                columnExamples = columns;
            }

            if (columnNames.length != columnExamples.length)
            {
                throw new Exception("line " + reader.getLineNumber() +
                    ": inconsistent column count");
            }

            buildColumnOptions(columnNames, columnExamples);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Error analyzing import file", ex);
            throw new Exception("Error reading file (" +
                ex.getMessage() + ")");
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Exception ignore) {}
            }
        }
    }

    private void buildColumnOptions(String[] p_names, String[] p_examples)
    {
        com.globalsight.terminology.importer.ImportOptions options =
            (com.globalsight.terminology.importer.ImportOptions)m_options;

        options.clearColumns();

        for (int i = 0; i < p_names.length; ++i)
        {
            com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor col =
                options.createColumnDescriptor();

            col.m_position = i;
            col.m_name = p_names[i].trim();
            col.m_example = p_examples[i].trim();
            col.m_type = "skip";
            col.m_termLanguage = "unknown";
            col.m_encoding = "unknown";
            col.m_associatedColumn = "-1";

            options.addColumn(col);
        }
    }

    private LineNumberReader getReader(String p_url)
        throws IOException
    {
        String encoding = m_options.getEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            encoding = "windows-1252";
        }
        encoding = CodesetMapper.getJavaEncoding(encoding);

        return new LineNumberReader(new InputStreamReader(
            new FileInputStream(p_url), encoding));
    }

    @Override
    public void setBundle(ResourceBundle bundle)
    {
        this.bundle = bundle;
    }

}