/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.edit.offline.download;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.SynchronizationStatus;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.DownloadWriterInterface;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.IndexJobsWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.IndexPageWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.JobListWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.PageListWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.ResourcePageWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.SegmentIdListWriter;
import com.globalsight.everest.edit.offline.download.omegat.OmegaTConst;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflinePageDataGenerator;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.edit.offline.rtf.RTFWriterAnsi;
import com.globalsight.everest.edit.offline.ttx.TTXConstants;
import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.nativefilestore.NativeFileManager;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgr;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.FileListBuilder;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * High level API for generating offline content. The class calls upon lower
 * level classes to perform these main functions: 1. GXML to P-tag conversion.
 * 2. Converison of P-tag strings to RTF. 3. Job package generation (zipping of
 * download pages and HTML resources). *
 * <p>
 * 
 * The caller has these choices of output: 1. Zipped RTF+HTML resources. 2.
 * Zipped TXT+HTML resources. 3. Single TXT files.
 */
public class DownLoadApi implements AmbassadorDwUpConstants
{
    static private final Logger CATEGORY = Logger.getLogger(DownLoadApi.class);

    //
    // Private Constants
    //

    static private final String SEG_ID_LIST_NAME = "id_list";

    static private final String INDEX_FILE_NAME = "index";

    static private final String PAGE_LIST_FILE_NAME = "pagelist";
    
    static private final String JOB_LIST_FILE_NAME = "joblist";

    static private final String HTML_RESOURCE_ENCODING = "UTF8";

    private static final SynchronizationManager s_synchManager = getSynchronizationManager();

    //
    // Download Message Sources
    //

    /** A message resulting from SourceFile file creation */
    static public final int MSG_CATEGORY_SRC_RES_CREATION = 1;

    /** A message resulting from TargetFile file creation */
    static public final int MSG_CATEGORY_TRG_FILE_CREATION = 2;

    /** A message resulting from TM resource file creation */
    static public final int MSG_CATEGORY_TM_RES_CREATION = 3;

    /** A message resulting from Term resource file creation */
    static public final int MSG_CATEGORY_TERM_RES_CREATION = 4;

    /** A message resulting from TagInfo resource file creation */
    static public final int MSG_CATEGORY_TAG_RES_CREATION = 5;

    //
    // Download Warnings And Error Message Ids
    //

    /** A given file was created for download without errors */
    static public final int MSGID_FILE_ADD_OK = 1;

    /**
     * The ms-word2000 limit for bookmarks has been reached for a given rtf file
     */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED = 2;

    /**
     * The ms-word2000 limit for bookmarks has been reached for the Source rtf
     * file
     */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED_SRC_DOC = 3;

    /**
     * The ms-word2000 limit for bookmarks has been reached for the Target rtf
     * file
     */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED_TRG_DOC = 4;

    /** The ms-word2000 limit for bookmarks has been reached for the tm rtf file */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED_TM_DOC = 5;

    /**
     * The ms-word2000 limit for bookmarks has been reached for the term rtf
     * file
     */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED_TERM_DOC = 6;

    /**
     * The ms-word2000 limit for bookmarks has been reached for the tag rtf file
     */
    static public final int MSGID_WD2000_BM_CNT_EXCEEDED_TAG_DOC = 7;

    /** The ms-word2000 limit for fileds has been reached for a given rtf file */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED = 8;

    /**
     * The ms-word2000 limit for fileds has been reached for the source rtf file
     */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED_SRC_DOC = 9;

    /**
     * The ms-word2000 limit for fileds has been reached for the target rtf file
     */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED_TRG_DOC = 10;

    /** The ms-word2000 limit for fileds has been reached for the tm rtf file */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED_TM_DOC = 11;

    /** The ms-word2000 limit for fileds has been reached for the term rtf file */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED_TERM_DOC = 12;

    /** The ms-word2000 limit for fileds has been reached for the tag rtf file */
    static public final int MSGID_WD2000_FLD_CNT_EXCEEDED_TAG_DOC = 13;

    /** Failed to write index file */
    static public final int MSGID_FAILED_TO_WRITE_INDEX = 14;

    public static final String REPEATED_SEGMENTS_KEY = "_repeatedSegments";

    //
    // Members
    //

    private OfflinePageDataGenerator m_OPG = null;

    private IndexPageWriter m_indexPageWriter = null;

    private IndexJobsWriter m_indexJobsWriter = null;
    
    private JobListWriter m_jobListWriter = null;
    
    private PageListWriter m_pageListWriter = null;
    
    private List<PageListWriter> m_pageListWriters = null;

    private ResourcePageWriter m_downloadResourcePageWriter = null;

    private SegmentIdListWriter m_segmentIdListWriter = null;

    private ListIterator m_PSF_Ids = null;

    private ListIterator m_PSF_Names = null;

    private ListIterator m_PSF_canUseUrl = null;

    private DownloadParams m_downloadParams = null;

    private OEMProcessStatus m_status = null;

    private JobPackageZipper m_zipper = null;
    private RTFWriterAnsi m_ansiRtfWriter = null;
    private PtagPageGenerator m_ptagPageGenerator = null;
    private boolean m_firstPage = true;

    private Hashtable m_uniqueJobFileNames = null;

    private Set<Object> m_uniqueSupportFileNames = null;

    private boolean m_packageHasResources = false;

    private int m_pageCounter = 0;

    private int m_errorCode = MSGID_FILE_ADD_OK;

    private ResourceBundle m_resource = null;

    private boolean convertLF = false;
    
    private boolean isOmegaT = false;

    //
    // Constructors
    //

    public boolean isConvertLF()
    {
        return convertLF;
    }

    public void setConvertLF(boolean convertLF)
    {
        this.convertLF = convertLF;
    }

    /**
     * Default constructor.
     */
    public DownLoadApi() throws AmbassadorDwUpException
    {
        super();

        m_ptagPageGenerator = new PtagPageGenerator();
        m_zipper = new JobPackageZipper();
        m_indexPageWriter = new IndexPageWriter();
        m_pageListWriter = new PageListWriter();
        m_pageListWriters = new ArrayList<PageListWriter>();
        m_jobListWriter = new JobListWriter();
        m_indexJobsWriter = new IndexJobsWriter();
        m_downloadResourcePageWriter = new ResourcePageWriter();
        m_segmentIdListWriter = new SegmentIdListWriter();
        m_ansiRtfWriter = new RTFWriterAnsi();
        m_uniqueJobFileNames = new Hashtable();
        m_uniqueSupportFileNames = new HashSet<Object>();

        m_OPG = new OfflinePageDataGenerator();
    }

    //
    // Public Methods
    //

    /**
     * Creates a RTF download package. Files are packaged in the ZIP format.
     * 
     * @param p_params
     *            a DownloadParam's object which contains all the parameters for
     *            download.
     * @exception AmbassadorDwUpException
     */
    public void makeRtfPackage(DownloadParams p_params,
            OEMProcessStatus p_status) throws AmbassadorDwUpException,
            IOException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("makeRtfPackage::DownloadParams: " + p_params);
        }

        // download parameters verified by OfflineEditManager
        m_downloadParams = p_params;
        m_status = p_status;
        setUILocaleResources(m_downloadParams);

        if (p_params.getZipper() != null)
        {
            m_zipper = p_params.getZipper();
        }
        else
        {
            m_zipper.createZipFile(m_downloadParams.createOutputFile());
        }

        addPrimaryTargetFilesAndResources();
        addSecondaryTargetFiles();
        addSourceFiles();
        addSupportFiles();
        addFinalHtmlResourceMainIndex();
        // addOutboxDirectory(); BJB: not needed - SaveAs not required anymore
        addHelpPages();
        if (p_params.getZipper() == null)
        {
            m_zipper.closeZipFile();
        }

    }
    
    /**
     * Make OmegaT package for OmegaT application
     * @param p_params
     * @param p_status
     * @throws AmbassadorDwUpException
     * @throws IOException
     */
    public void makeOmegaTPackage(DownloadParams p_params,
            OEMProcessStatus p_status) throws AmbassadorDwUpException,
            IOException
    {
        isOmegaT = true;
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("makeOmegaTPackage::DownloadParams: " + p_params);
        }

        // download parameters verified by OfflineEditManager
        m_downloadParams = p_params;
        m_status = p_status;
        setUILocaleResources(m_downloadParams);

        if (p_params.getZipper() != null)
        {
            m_zipper = p_params.getZipper();
        }
        else
        {
            m_zipper.createZipFile(m_downloadParams.createOutputFile());
        }

        addPrimaryTargetFilesAndResources();
        addSecondaryTargetFiles();
        addSourceFiles();
        addSupportFiles();
        addFinalHtmlResourceMainIndex();
        // addOutboxDirectory(); BJB: not needed - SaveAs not required anymore
        addHelpPages();
        // for OmegaT
        addOmegaTFiles();
        if (p_params.getZipper() == null)
        {
            m_zipper.closeZipFile();
        }
        
        isOmegaT = false;
    }

    private void addOmegaTFiles() throws IOException
    {
        String parentPath = DownloadHelper.makeParentPath(m_downloadParams);
        m_zipper.writePath(parentPath + OmegaTConst.omegat_foldername);
        m_zipper.writePath(parentPath + OmegaTConst.dictionary_foldername);
        m_zipper.writePath(parentPath + OmegaTConst.target_foldername);
        m_zipper.writePath(parentPath + "tmx/");
        m_zipper.writePath(parentPath + "terminology/");
        
        String srcLocale = toOmegaTLocale(m_downloadParams.getSourceLocale());
        String tgtLocale = toOmegaTLocale(m_downloadParams.getTargetLocale());
        
        // write omegaT filter xml
        String filter_xml = OmegaTConst.filter_xml;
        m_zipper.writePath(parentPath + OmegaTConst.omegat_filterxml_file);
        InputStream is = new ByteArrayInputStream(filter_xml.getBytes("UTF-8"));
        m_zipper.writeFile(is);
        is.close();
        
        // write project infor
        String projectInfo = OmegaTConst.omegat_project.replace("(source_lang)", srcLocale);
        projectInfo = projectInfo.replace("(target_lang)", tgtLocale);
        m_zipper.writePath(parentPath + OmegaTConst.omegat_project_file);
        is = new ByteArrayInputStream(projectInfo.getBytes("UTF-8"));
        m_zipper.writeFile(is);
        is.close();
    }

    private String toOmegaTLocale(GlobalSightLocale gslocale)
    {
        String tmp = gslocale.getLanguage() + "-" + gslocale.getCountry();
        tmp = tmp.toUpperCase();
        
        return tmp;
    }

    /**
     * Creates an RTF extracted paragraph view package. Files are packaged in
     * the ZIP format.
     * 
     * @param p_params
     *            a DownloadParam's object which contains all the parameters for
     *            download.
     * @exception AmbassadorDwUpException
     */
    public void makeEmbeddedWordClientPackage(DownloadParams p_params,
            OEMProcessStatus p_status) throws AmbassadorDwUpException,
            IOException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("makeEmbeddedWordClientPackage::DownloadParams: "
                    + p_params);
        }

        // download parameters verified by OfflineEditManager
        m_downloadParams = p_params;
        m_status = p_status;
        setUILocaleResources(m_downloadParams);
        if (p_params.getZipper() != null)
        {
            m_zipper = p_params.getZipper();
        }
        else
        {
            m_zipper.createZipFile(m_downloadParams.createOutputFile());
        }

        addPrimaryTargetFilesAndResources();
        if (!m_status.isAborted())
        {
            addSecondaryTargetFiles();
            addSourceFiles();
            addSupportFiles();
            // addOutboxDirectory(); BJB: not needed - SaveAs not required
            // anymore
            addHelpPages();

            addStaticResourceFile(MSWORD_TEMPLATE_FNAME,
                    "MSWord/TemplateRelease/",
                    DownloadHelper.makePTFParentPath(p_params));
        }

        if (p_params.getZipper() == null)
        {
            m_zipper.closeZipFile();
        }
    }

    /**
     * Creates a TEXT download package. Files are package in the ZIP format.
     * 
     * @param p_params
     *            a DownloadParam's object which contains all the parameters for
     *            download.
     * @exception AmbassadorDwUpException
     */
    public void makeTxtPackage(DownloadParams p_params,
            OEMProcessStatus p_status) throws AmbassadorDwUpException,
            IOException
    {
        OfflinePageData offlinePage = null;

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("makeTxtPackage::DownloadParams: " + p_params);
        }

        // download parameters verified by OfflineEditManager
        m_downloadParams = p_params;
        m_status = p_status;
        setUILocaleResources(m_downloadParams);
        if (p_params.getZipper() != null)
        {
            m_zipper = p_params.getZipper();
        }
        else
        {
            m_zipper.createZipFile(m_downloadParams.createOutputFile());
        }

        addPrimaryTargetFilesAndResources();
        addSecondaryTargetFiles();
        addSourceFiles();
        addSupportFiles();
        addFinalHtmlResourceMainIndex();
        // addOutboxDirectory(); BJB: not needed - SaveAs not required anymore
        addHelpPages();

        if (p_params.getZipper() == null)
        {
            m_zipper.closeZipFile();
        }
    }

    /**
     * Creates a support-file only download package. Files are packaged in the
     * ZIP format.
     * 
     * @param p_params
     *            a DownloadParam's object which contains all the parameters for
     *            download.
     * @exception AmbassadorDwUpException
     */
    public void makeSupportFilesOnlyPackage(DownloadParams p_params,
            OEMProcessStatus p_status) throws AmbassadorDwUpException,
            IOException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("makeSupportFilesOnlyPackage::DownloadParams: "
                    + p_params);
        }

        // download parameters verified by OfflineEditManager
        m_downloadParams = p_params;
        m_status = p_status;
        setUILocaleResources(m_downloadParams);
        if (p_params.getZipper() != null)
        {
            m_zipper = p_params.getZipper();
        }
        else
        {
            m_zipper.createZipFile(m_downloadParams.createOutputFile());
        }
        addSupportFiles();
        if (p_params.getZipper() == null)
        {
            m_zipper.closeZipFile();
        }
    }

    /**
     * NOTE: DO NOT REMOVE: DOWNLOAD OF SINGLE PAGES WAS REMOVED FROM THE UI
     * SOME TIME AGO. SO THIS CODE HAS NOT BEEN MAINTAINED. IT IS BEING KEPT FOR
     * FUTURE REFERENCE
     * 
     * Creates a single text download page. Files are NOT packaged.
     * 
     * @param p_params
     *            a DownloadParam's object which contains all the parameters for
     *            download.
     * @exception AmbassadorDwUpException
     * 
     *                public void makeTxtPages(DownloadParams p_params,
     *                OfflineEditManager p_offlineEditManager) throws
     *                AmbassadorDwUpException { if (CATEGORY.isDebugEnabled()) {
     *                CATEGORY.debug( "makeTxtPages::DownloadParams: " +
     *                p_params.toString()); } // download parameters verified by
     *                OfflineEditManager // no localized string for this
     *                properties m_downloadResource =
     *                loadProperties(getClass().getName(), Locale.ENGLISH);
     * 
     *                m_downloadParams = p_params; m_offlineEditManager =
     *                p_offlineEditManager; m_PSF_Ids =
     *                m_downloadParams.getPageListIterator(); m_PSF_Names =
     *                m_downloadParams.getPageNameListIterator();
     *                m_PSF_canUseUrl =
     *                m_downloadParams.getCanUseUrlListIterator();
     *                m_downloadParams = p_params; OfflinePageData offlinePage =
     *                null;
     * 
     *                m_firstPage = true; while((offlinePage =
     *                getNextExtractedOfflinePage()) != null) {
     *                formatOfflineSegments(offlinePage,
     *                m_downloadParams.getTagDisplayFormatID());
     * 
     *                writeTxtPage(m_downloadParams.createOutputFile(),
     *                offlinePage); offlinePage = null; // Garbage Collect
     *                m_firstPage = false; } }
     */

    /**
     * Wraps the code to convert segments to ptag segments.
     * 
     * @param p_pageData
     *            the offline page data to convert
     * @param params
     *            the format to convert to
     */
    private void formatOfflineSegments(OfflinePageData p_pageData,
            DownloadParams params) throws AmbassadorDwUpException
    {
        PtagPageGenerator generator = new PtagPageGenerator();
        generator.convertAllGxmlToPtag(p_pageData, params);
    }

    /**
     * Creates a unique primary download file name.
     * 
     * To avoid naming collisions, we keep track of the file names used so far.
     * 
     * Changed for GBS-2036, the unique file name have been generated in
     * m_downloadParams
     * 
     * @param p_page
     *            - all the data for a given download page.
     * @param p_ext
     *            - the desired extension for the the new name.
     * @return unique primary download file name
     */
    @SuppressWarnings("unchecked")
    public String getUniqueExtractedPTFName(OfflinePageData p_page, String p_ext)
    {
        if (p_page == null)
        {
            return null;
        }
        String ext = ((p_ext == null) ? "" : "." + p_ext);
        String pageId = p_page.getPageId();
        String uniqueName = m_downloadParams.getUniqueFileNames().get(pageId);
        if (uniqueName == null)
        {
            int idx = p_page.getPageName().lastIndexOf('.');
            if (p_page.getPageName().length() <= 0)
            {
                // no page name
                uniqueName = "UnknownPageName";
            }
            else if (idx <= 0)
            {
                // no original extension
                uniqueName = p_page.getPageName();
            }
            else
            {
                uniqueName = p_page.getPageName().substring(0, idx);
            }
        }
        uniqueName = uniqueName + ext;
        return uniqueName;
    }

    /** ***** Begin Writers ****** */

    /**
     * Adds all Primary Target files and their associated resources. Note:
     * download is driven by the source page ids and the target locale.
     */
    @SuppressWarnings("unchecked")
    private void addPrimaryTargetFilesAndResources()
            throws AmbassadorDwUpException, IOException
    {

        TargetPage trgPage = null;
        String curTargetFname = null;
        List<OfflinePageData> datas = new ArrayList<OfflinePageData>();
        OfflinePageData pageData = new OfflinePageData();
        OfflinePageData repPageData = new OfflinePageData();
        boolean isConsolidate = false;
        boolean isCombined = false;
        Workflow wf = null;
        List<String> addedRepeatedDisplaySegmentIDs = new ArrayList<String>();

        if (m_downloadParams.hasPrimaryFiles())
        {
            m_PSF_Ids = m_downloadParams.getPageListIterator();
            m_PSF_Names = m_downloadParams.getPageNameListIterator();
            m_PSF_canUseUrl = m_downloadParams.getCanUseUrlListIterator();
            m_firstPage = true;

            page_loop: while (m_PSF_Ids.hasNext())
            {
                // Advance all the primary page list iterators.
                // Note: we assume m_PSF_Names and m_PSF_canUseUrl
                // are the same size as m_PSF_Ids.
                Long srcPageId = (Long) m_PSF_Ids.next();
                String srcPageName = (String) m_PSF_Names.next();
                boolean srcCanUseUrl = ((Boolean) m_PSF_canUseUrl.next())
                        .booleanValue();

                try
                {
                    trgPage = ServerProxy.getPageManager().getTargetPage(
                            srcPageId.longValue(),
                            m_downloadParams.getTargetLocale().getId());
                }
                catch (Exception ex)
                {
                    throw new AmbassadorDwUpException(
                            AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                            ex);
                }

                // GXML editor synchronization - if source page is
                // being updated, can't download target page.
                try
                {
                    SynchronizationStatus status = s_synchManager
                            .getStatus(trgPage.getIdAsLong());

                    if (status != null
                            && status.getStatus().equals(
                                    SynchronizationStatus.GXMLUPDATE_STARTED))
                    {
                        StringBuffer sb = new StringBuffer(
                                m_resource.getString("msg_dnld_file_edited"));
                        sb.append(" ").append(srcPageName);

                        m_pageCounter++;
                        m_status.speakRed(m_pageCounter, sb.toString(), null);

                        continue page_loop;
                    }
                }
                catch (Exception ex)
                {
                    throw new AmbassadorDwUpException(
                            AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                            ex);
                }

                // write the primary file based on the extraction type
                if (trgPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                {
                    PageData data = null;
                    try
                    {
                        data = m_OPG.getDownloadPageData(srcPageId.toString(),
                                srcPageName, srcCanUseUrl, m_downloadParams,
                                trgPage);
                    }
                    catch (GeneralException ex)
                    {
                        throw new AmbassadorDwUpException(ex);
                    }
                    // Add the target file
                    OfflinePageData OPD = data.getOfflinePageData();
                    if (m_firstPage)
                    {
                        pageData = OPD;
                        wf = ServerProxy.getWorkflowManager().getWorkflowById(
                                Long.parseLong(OPD.getWorkflowId()));
                        pageData.setInContextMatchWordCount(wf
                                .getInContextMatchWordCount());
                    }
                    // Add tmx file before formatting, because formating will
                    // cause native code missing.
                    MatchTypeStatistics matchs = data.getMatchTypeStatistics();

                    Job job = trgPage.getSourcePage().getRequest().getJob();
                    
                    // For auto action job, the last page will not get the job,
                    // so use the job which was set in downloaParams.
                    if (job == null)
                    {
                        if (m_downloadParams.getJob() != null)
                        {
                            job = m_downloadParams.getJob();
                        }
                    }
                    
                    if (OPD.getTaskId() == null)
                    {
                        Hashtable tasks = trgPage.getWorkflowInstance().getTasks();
                        List<Long> taskids = m_downloadParams.getAllTaskIds();
                        
                        for (Long taskId : taskids)
                        {
                            if (tasks.containsKey(taskId))
                            {
                                OPD.setTaskId(taskId.toString());
                                break;
                            }
                        }
                    }
                    
                    OPD.setJobId(job.getId());
                    OPD.setJobName(job.getJobName());
                    OPD.setCompanyId(job.getCompanyId());
                    OPD.setServerInstanceID(ServerUtil.getServerInstanceID());
                    repPageData.setJobId(job.getId());
                    repPageData.setJobName(job.getJobName());
                    repPageData.setCompanyId(job.getCompanyId());
                    repPageData.setServerInstanceID(ServerUtil.getServerInstanceID());

                    boolean isUseInContext = job.getL10nProfile()
                            .getTranslationMemoryProfile()
                            .getIsContextMatchLeveraging();
                    boolean isInContextMatch = false;
                    try
                    {
                        isInContextMatch = PageHandler.isInContextMatch(job,
                                isUseInContext);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(
                                "Can not get the value of in context match", e);
                    }
                    boolean isDefaultContextMatch = PageHandler
                            .isDefaultContextMatch(job);

                    ArrayList<BaseTmTuv> splittedTuvs = new ArrayList<BaseTmTuv>();
                    try
                    {
                        ArrayList<Tuv> srcTuvs = new ArrayList<Tuv>();
                        srcTuvs = SegmentTuvUtil.getSourceTuvs(trgPage
                                .getSourcePage());
                        splittedTuvs = StatisticsService.splitSourceTuvs(
                                srcTuvs, trgPage.getGlobalSightLocale(),
                                job.getCompanyId());
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error("Can not get source tuvs.", e);
                    }
                    if (isInContextMatch)
                    {
                        OPD = updateOfflinePageData(OPD, matchs, splittedTuvs);
                    }
                    else if (isDefaultContextMatch
                            && trgPage.getIsDefaultContextMatch())
                    {
                        OPD = updateDefaultContextOfflinePageDate(OPD, matchs);
                    }

                    if (m_firstPage)
                    {
                        pageData.setJobId(job.getId());
                        pageData.setJobName(job.getJobName());

                        List<Job> allJobs = m_downloadParams.getAllJob();
                        if (allJobs != null && allJobs.size() > 0)
                        {
                            StringBuffer jobIds = new StringBuffer();
                            StringBuffer jobNames = new StringBuffer();
                            
                            for (Job tempJob : allJobs)
                            {
                                jobIds.append(tempJob.getId()).append(",");
                                jobNames.append(tempJob.getJobName()).append(",");
                            }
                            
                            pageData.setAllJobIds(jobIds.substring(0, jobIds.length() - 1));
                            pageData.setAllJobNames(jobNames.substring(0, jobNames.length() - 1));
                            repPageData.setAllJobIds(jobIds.substring(0, jobIds.length() - 1));
                            repPageData.setAllJobNames(jobNames.substring(0, jobNames.length() - 1));
                        }
                    }
                    
                    datas.add(OPD);
                    formatOfflineSegments(OPD, m_downloadParams);

                    if (m_downloadParams.isIncludeRepetitions())
                    {
                        try
                        {
                            if (repPageData.getPageId() == null
                                    || "".equals(repPageData.getPageId()))
                            {
                                repPageData.setPageId(OPD.getPageId());
                            }
                            else
                            {
                                repPageData.setPageId(repPageData.getPageId()
                                        + "," + OPD.getPageId());
                            }
                            Vector<OfflineSegmentData> slist = filterRepeatedSegments(
                                    OPD.getSegmentList(), job.getCompanyId(),
                                    addedRepeatedDisplaySegmentIDs);
                            repPageData.getSegmentList().addAll(slist);
                            HashMap smap = filterRepeatedSegmentMap(
                                    OPD.getSegmentMap(), slist);
                            repPageData.getSegmentMap().putAll(smap);
                            repPageData.setExactMatchWordCount(repPageData
                                    .getExactMatchWordCount()
                                    + OPD.getExactMatchWordCount());
                            repPageData.setFuzzyMatchWordCount(repPageData
                                    .getFuzzyMatchWordCount()
                                    + OPD.getFuzzyMatchWordCount());
                            repPageData.setNoMatchWordCount(repPageData
                                    .getNoMatchWordCount()
                                    + OPD.getNoMatchWordCount());
                            wf = ServerProxy
                                    .getWorkflowManager()
                                    .getWorkflowById(
                                            Long.parseLong(OPD.getWorkflowId()));
                            repPageData.setInContextMatchWordCount(repPageData
                                    .getInContextMatchWordCount()
                                    + wf.getInContextMatchWordCount());

                            repPageData.setPageName(job.getJobName());
                            repPageData.setSourceLocaleName(OPD
                                    .getSourceLocaleName());
                            repPageData.setTargetLocaleName(OPD
                                    .getTargetLocaleName());

                            repPageData.setEncoding(OPD.getEncoding());
                            repPageData.setDocumentFormat(OPD
                                    .getDocumentFormat());
                            repPageData.setPlaceholderFormat(OPD
                                    .getPlaceholderFormat());
                            repPageData.setWorkflowId(OPD.getWorkflowId());
                            repPageData.setTaskId(OPD.getTaskId());
                            repPageData.setAnnotationThreshold(OPD.getAnnotationThreshold());
                        }
                        catch (Exception e)
                        {
                            CATEGORY.error("Cannot init repetitions data.", e);
                        }
                    }
                    
                    if (m_downloadParams.isNeedCombined())
                    {
                        isCombined = true;
                        pageData.setTaskIds(m_downloadParams.getAllTaskIds());
                        repPageData.setTaskIds(m_downloadParams.getAllTaskIds());
                    }

                    if (m_downloadParams.isNeedConsolidate())
                    {
                        isConsolidate = true;
                        if (!pageData.getPageId().equals(OPD.getPageId()))
                        {
                            pageData.setPageId(pageData.getPageId() + ","
                                    + OPD.getPageId());
                            // pageData.setPageName(pageData.getPageName() + ","
                            // + OPD.getPageName());
                            pageData.getSegmentList().addAll(
                                    OPD.getSegmentList());
                            pageData.getSegmentMap()
                                    .putAll(OPD.getSegmentMap());
                            pageData.setExactMatchWordCount(pageData
                                    .getExactMatchWordCount()
                                    + OPD.getExactMatchWordCount());
                            pageData.setFuzzyMatchWordCount(pageData
                                    .getFuzzyMatchWordCount()
                                    + OPD.getFuzzyMatchWordCount());
                            pageData.setNoMatchWordCount(pageData
                                    .getNoMatchWordCount()
                                    + OPD.getNoMatchWordCount());
                            wf = ServerProxy
                                    .getWorkflowManager()
                                    .getWorkflowById(
                                            Long.parseLong(OPD.getWorkflowId()));
                            pageData.setInContextMatchWordCount(pageData
                                    .getInContextMatchWordCount()
                                    + wf.getInContextMatchWordCount());
                        }
                        OPD.setPageName(job.getJobName());
                        curTargetFname = job.getJobName();
                    }
                    else
                    {
                        curTargetFname = addExtractedPrimaryTargetPage(OPD,
                                m_downloadParams);
                        // addResourceFiles(curTargetFname, OPD,
                        // m_downloadParams);
                    }
                    addResourceFiles(curTargetFname, OPD, m_downloadParams);

                    m_firstPage = false;

                    if (m_status.isAborted())
                    {
                        break;
                    }
                }
                else if (trgPage.getPrimaryFileType() == PrimaryFile.UNEXTRACTED_FILE)
                {
                    String taskId = m_downloadParams.getTaskID();
                    HashMap<Long, Long> pagesTasks = m_downloadParams.getAllPage_tasks();
                    
                    if (pagesTasks != null && pagesTasks.containsKey(srcPageId))
                    {
                        taskId = pagesTasks.get(srcPageId).toString();
                    }
                    
                    
                    PrimaryFile primaryFile = trgPage.getPrimaryFile();
                    addUnextractedPrimaryTargetFile(primaryFile,
                            trgPage.getIdAsLong(), taskId);
                }
                else
                {
                    // Another primary download type that needs to be
                    // implemented.
                }
            }

            if (isConsolidate)
            {
                Vector segments = pageData.getSegmentList();
                Collections.sort(segments, new Comparator()
                {
                    @Override
                    public int compare(Object o1, Object o2)
                    {
                        OfflineSegmentData osd1 = (OfflineSegmentData) o1;
                        OfflineSegmentData osd2 = (OfflineSegmentData) o2;
                        if (osd1 == null && osd2 == null)
                            return 0;
                        if (osd1 == null)
                            return -1;
                        if (osd2 == null)
                            return 1;
                        long segId1 = 0l;
                        long segId2 = 0l;
                        try
                        {
                            segId1 = Long.parseLong(osd1.getDisplaySegmentID());
                        }
                        catch (Exception e)
                        {
                        }
                        try
                        {
                            segId2 = Long.parseLong(osd2.getDisplaySegmentID());
                        }
                        catch (Exception e)
                        {
                        }
                        if (segId1 == segId2)
                            return 0;
                        else if (segId1 > segId2)
                            return 1;
                        else
                            return -1;

                    }
                    
                    public boolean equals(Object obj)
                    {
                        return compare(this, obj) == 0;
                    }
                });
                pageData.setSegmentList(segments);
                pageData.setDocumentFormat("multi-format");
                pageData.setConsolated(true);
                curTargetFname = addExtractedPrimaryTargetPage(pageData,
                        m_downloadParams);
                // m_firstPage = true;
                // addResourceFiles(curTargetFname, pageData, m_downloadParams);
            }

            if (m_downloadParams.isIncludeRepetitions())
            {
                if (repPageData.getSegmentList() == null
                        || repPageData.getSegmentList().size() == 0)
                {
                    String msg = "Ignored repeated segments file as there are no repeated segments";

                    m_status.setTotalFiles(m_status.getTotalFiles() - 1);
                    m_status.speak(m_pageCounter, msg);
                }
                else
                {
                    repPageData.setIsRepetitions(true);
                    addExtractedRepetitions(repPageData, m_downloadParams);
                }
            }

            // Adds tmx files.
            if (!m_downloadParams.isConsolidateTmxFiles())
            {
                for (OfflinePageData data : datas)
                {
                    addTmxFile(data, m_downloadParams);
                }
            }
            else
            {
                addTmxFile(consolidate(datas), m_downloadParams);
            }

            // Adds terminology files.
            if (m_downloadParams.createTermFiles())
            {
                if (m_downloadParams.isConsolidateTermFiles())
                {
                    if (!datas.isEmpty())
                    {
                        addTermFile(consolidate(datas), m_downloadParams);
                    }
                }
                else
                {
                    for (OfflinePageData data : datas)
                    {
                        addTermFile(data, m_downloadParams);
                    }
                }
            }
        }
    }

    private HashMap filterRepeatedSegmentMap(HashMap segmentMap, Vector slist)
    {
        HashMap result = new HashMap();

        if (slist == null || slist.size() == 0)
        {
            return result;
        }

        if (segmentMap == null || segmentMap.size() == 0)
        {
            return result;
        }

        for (Object obj : slist)
        {
            OfflineSegmentData sdata = (OfflineSegmentData) obj;
            String segmentId = sdata.getDisplaySegmentID();

            if (segmentMap.containsKey(segmentId))
            {
                result.put(segmentId, sdata);
            }
        }

        return result;
    }

    private Vector<OfflineSegmentData> filterRepeatedSegments(
            Vector<OfflineSegmentData> segmentList, long companyId,
            List<String> addedRepeatedDisplaySegmentIDs) throws Exception
    {
        Vector<OfflineSegmentData> result = new Vector<OfflineSegmentData>();

        if (segmentList == null || segmentList.size() == 0)
        {
            return result;
        }

        for (Object obj : segmentList)
        {
            OfflineSegmentData sdata = (OfflineSegmentData) obj;
            // Use "displaySegmentID" instead of repeated TUV ID for sub cases.
            String displaySegID = sdata.getDisplaySegmentID();

            Tuv trgTuv = sdata.getTargetTuv();
            if (trgTuv != null && trgTuv.isRepeated()
                    && !addedRepeatedDisplaySegmentIDs.contains(displaySegID))
            {
                addedRepeatedDisplaySegmentIDs.add(displaySegID);
                result.add(sdata);
            }
        }

        return result;
    }

    private OfflinePageData consolidate(List<OfflinePageData> datas)
    {
        OfflinePageData consolidatedData = new OfflinePageData();
        consolidatedData.setPageName(m_downloadParams.getFullJobName());
        HashSet<OfflineSegmentData> allDatas = new HashSet<OfflineSegmentData>();
        boolean inited = false;

        for (OfflinePageData data : datas)
        {
            if (!inited)
            {
                inited = true;
                consolidatedData
                        .setSourceLocaleName(data.getSourceLocaleName());
                consolidatedData
                        .setTargetLocaleName(data.getTargetLocaleName());
            }

            allDatas.addAll(data.getSegmentList());
        }

        Vector<OfflineSegmentData> segments = new Vector<OfflineSegmentData>();
        segments.addAll(allDatas);

        Collections.sort(segments, new Comparator<OfflineSegmentData>()
        {
            @Override
            public int compare(OfflineSegmentData data1,
                    OfflineSegmentData data2)
            {
                return data1.getDisplaySourceText().compareTo(
                        data2.getDisplaySourceText());
            }
        });

        consolidatedData.setSegmentList(segments);

        return consolidatedData;
    }

    @SuppressWarnings("unchecked")
    private OfflinePageData updateDefaultContextOfflinePageDate(
            OfflinePageData opd, MatchTypeStatistics matchs)
    {
        Vector vector = opd.getSegmentList();

        for (int i = 0; i < vector.size(); i++)
        {
            OfflineSegmentData segment = (OfflineSegmentData) vector.get(i);

            if (segment.getMatchValue() == 100
                    && segment.getTargetTuv().getState().getValue() == TuvState.EXACT_MATCH_LOCALIZED
                            .getValue())
            {
                segment = (OfflineSegmentData) vector.get(i);

                if (opd.getTMEditType() == AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH
                        || opd.getTMEditType() == AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE)
                    segment.setWriteAsProtectedSegment(false);
                else
                    segment.setWriteAsProtectedSegment(true);

                segment.setDisplayMatchType("Default Context Exact Match");
                vector.set(i, segment);
            }
        }
        opd.setSegmentList(vector);
        return opd;
    }

    @SuppressWarnings("unchecked")
    private OfflinePageData updateOfflinePageData(OfflinePageData opd,
            MatchTypeStatistics matchs, ArrayList splittedTuvs)
    {
        Job job = m_downloadParams.getRightJob();
        long companyId = (job == null ? opd.getCompanyId() : job.getCompanyId());
        Vector vector = opd.getSegmentList();
        Vector excludedTypes = m_downloadParams.getExcludedTypeNames();
        for (int i = 0; i < vector.size(); i++)
        {
            OfflineSegmentData segment = (OfflineSegmentData) vector.get(i);
            String subId = "0";
            if (!"".equals(segment.getSubflowId()))
            {
                subId = segment.getSubflowId();
            }
            if (LeverageUtil.isIncontextMatch(i, splittedTuvs, null, matchs,
                    new Vector(), subId, companyId))
            {
                segment = (OfflineSegmentData) vector.get(i);
                if (opd.getTMEditType() == AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH
                        || opd.getTMEditType() == AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE)
                    segment.setWriteAsProtectedSegment(false);
                else {
                    if (segment.getTargetTuv().getState().equals(TuvState.LOCALIZED))
                        segment.setWriteAsProtectedSegment(false);
                    else
                        segment.setWriteAsProtectedSegment(true);
                }
                segment.setDisplayMatchType("Context Exact Match");
                vector.set(i, segment);
            }
        }

        for (int i = 0; i < splittedTuvs.size(); i++)
        {
            if (LeverageUtil.isIncontextMatch(i, splittedTuvs, null, matchs,
                    excludedTypes, companyId))
            {
                long id = ((SegmentTmTuv) splittedTuvs.get(i)).getId();
                for (int j = 0; j < vector.size(); j++)
                {
                    OfflineSegmentData segment = (OfflineSegmentData) vector
                            .get(j);
                    Tuv tuv = segment.getSourceTuv();
                    // Only apply this to "main" segment
                    if (tuv != null && tuv.getId() == id
                            && "".equals(segment.getSubflowId()))
                    {
                        if (opd.getTMEditType() != AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH
                                && opd.getTMEditType() != AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE
                                && !segment.getTargetTuv().getState()
                                        .equals(TuvState.LOCALIZED))
                            segment.setWriteAsProtectedSegment(true);

                        segment.setDisplayMatchType("Context Exact Match");
                        break;
                    }
                }
            }
        }

        opd.setSegmentList(vector);
        return opd;
    }

    /**
     * NOTE: DO NOT REMOVE: DOWNLOAD OF SINGLE PAGES WAS REMOVED FROM THE UI
     * SOME TIME AGO. SO THIS CODE HAS NOT BEEN MAINTAINED. IT IS BEING KEPT FOR
     * FUTURE REFERENCE
     * 
     * Creates a single offline text file.
     * 
     * @param p_outputStream
     *            the file output stream.
     * @param p_page
     *            the data from which to create the file. private void
     *            writeSingleTxtPage(OutputStream p_outputStream,
     *            OfflinePageData p_page) throws AmbassadorDwUpException {
     *            p_page.writeOfflineTextFile(p_outputStream, m_downloadParams);
     *            }
     */

    @SuppressWarnings("unused")
    private void addOutboxDirectory() throws AmbassadorDwUpException
    {
        m_zipper.writePath(DownloadHelper.makeOutboxPath(m_downloadParams));
    }

    /** ***** End Writers ****** */

    /** ***** Begin zipper methods ****** */

    private void addTmxFile(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException,
            IOException
    {
        // TODO Well if the fullPlainPath will lost all by customer,the code and
        // it related should be removed
        String tmxPlainPath = null;
        String tmx14bPath = null;
        String fname = null;
        String fullPlainPath = null;
        String full14bPath = null;
        int resMode = p_downloadParams.getResInsOption();
        
        if (p_downloadParams.isNeedCombined())
        {
            fname = p_downloadParams.getFullJobName() + "_"
                    + p_downloadParams.getTargetLocale().toString() + "." + FILE_EXT_TMX_NO_DOT;
        }
        else
        {
            fname = getUniqueExtractedPTFName(p_page, FILE_EXT_TMX_NO_DOT);
        }
        
        if (resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_14B)
        {
            tmx14bPath = DownloadHelper.makeTmx14bParentPath(p_downloadParams);
            full14bPath = tmx14bPath + fname;
        }
        else if (resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN)
        {
            tmxPlainPath = DownloadHelper
                    .makeTmxPlainParentPath(p_downloadParams);
            fullPlainPath = tmxPlainPath + fname;
        }
        else if (resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH)
        {

            tmx14bPath = DownloadHelper.makeTmx14bParentPath(p_downloadParams);
            full14bPath = tmx14bPath + fname;
        }

        if (full14bPath != null || fullPlainPath != null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);
            m_pageCounter++;
            m_status.speak(m_pageCounter, sb.toString());

            if (full14bPath != null)
            {
                m_zipper.writePath(full14bPath);
                m_zipper.writeTmxPage(p_page, p_downloadParams,
                        TmxUtil.TMX_LEVEL_TWO, convertLF);
            }

            if (fullPlainPath != null)
            {
                m_zipper.writePath(fullPlainPath);
                m_zipper.writeTmxPage(p_page, p_downloadParams,
                        TmxUtil.TMX_LEVEL_ONE, convertLF);
            }
        }
    }

    private void addTermFile(OfflinePageData page, DownloadParams downloadParams)
            throws IOException
    {
        String format = downloadParams.getTermFormat();
        String fname;
        String ext = getTermFileExtension(format);
        if (downloadParams.isNeedCombined())
        {
            fname = downloadParams.getFullJobName() + "_"
                    + downloadParams.getTargetLocale().toString() + "." + ext;
        }
        else
        {
            fname = getUniqueExtractedPTFName(page, ext);
        }
        
        if (!OfflineConstants.TERM_NONE.equals(format))
        {
            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);
            m_pageCounter++;
            m_status.speak(m_pageCounter, sb.toString());

            String path = DownloadHelper.makeTermParentPath(downloadParams);
            m_zipper.writePath(path + fname);
            m_zipper.writeTermPage(page, downloadParams);
        }
    }

    private String getTermFileExtension(String format)
    {
        if (OfflineConstants.TERM_HTML.equals(format))
        {
            return "html";
        }
        else if (OfflineConstants.TERM_TBX.equals(format))
        {
            return "tbx";
        }
        else if (OfflineConstants.TERM_TXT.equals(format))
        {
            return "txt";
        }
        return "xml";
    }

    private String addExtractedRepetitions(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException,
            IOException
    {
        String inboxPath = DownloadHelper.makePTFParentPath(p_downloadParams);
        String fname = (p_downloadParams.isNeedCombined() ? p_downloadParams
                .getFullJobName() + "_" + p_downloadParams.getTargetLocale()
                : p_downloadParams.getJob().getJobName())
                + REPEATED_SEGMENTS_KEY;
        StringBuffer fullPath = new StringBuffer(inboxPath);

        if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_RTF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_TRADOSRTF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED)
        {
            fullPath.append(fname);
            fullPath.append(".");
            fullPath.append(FILE_EXT_RTF_NO_DOT);

            if (m_downloadParams.isUnicodeRTF())
            {
                m_pageCounter++;

                StringBuffer sb = new StringBuffer();
                sb.append(m_resource.getString("msg_dnld_adding_file"));
                sb.append(fname);
                sb.append(".");
                sb.append(FILE_EXT_RTF_NO_DOT);

                m_status.speak(m_pageCounter, sb.toString());

                m_zipper.writePath(fullPath.toString());
                m_zipper.writeUnicodeRtfPage(p_page, p_downloadParams);
            }
            else
            {
                CATEGORY.error("DownloadApi::addExtractedRepetitions() - "
                        + "*** The Ansi RTF writer is no longer supported. ***");

                throw new AmbassadorDwUpException(new Exception(
                        "The Ansi RTF writer is no longer supported."));

                // DISABLED WRITER - DO NOT REMOVE COMMENT
                // m_zipper.writePath(fullPath);
                // m_zipper.writeAnsiRtfPage(p_page, m_ansiRtfWriter,
                // p_downloadParams);
            }
        }
        else if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_XLF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_OMEGAT)
        {
            fullPath.append(fname);
            fullPath.append(".");
            fullPath.append(FILE_EXT_XLIFF_NO_DOT);

            m_pageCounter++;

            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);
            sb.append(".");
            sb.append(FILE_EXT_XLIFF_NO_DOT);

            m_status.speak(m_pageCounter, sb.toString());

            m_zipper.writePath(fullPath.toString());
            m_zipper.writeUnicodeXliffPage(p_page, p_downloadParams);
        }

        return fname;
    }

    private String addExtractedPrimaryTargetPage(OfflinePageData p_page,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException,
            IOException
    {
        String inboxPath = DownloadHelper.makePTFParentPath(p_downloadParams);
        String fname = "";
        StringBuffer fullPath = new StringBuffer(inboxPath);

        if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_RTF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_TRADOSRTF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED)
        {
            if (p_downloadParams.isNeedCombined())
            {
                fname = p_downloadParams.getFullJobName() + "_"
                        + p_downloadParams.getTargetLocale().toString() + "." + FILE_EXT_RTF_NO_DOT;
            }
            else if (p_downloadParams.isNeedConsolidate())
            {
                fname = p_downloadParams.getJob().getJobName() + "."
                        + FILE_EXT_RTF_NO_DOT;
            }
            else
            {
                fname = getUniqueExtractedPTFName(p_page, FILE_EXT_RTF_NO_DOT);
            }
            fullPath.append(fname);

            if (m_downloadParams.isUnicodeRTF())
            {
                m_pageCounter++;

                StringBuffer sb = new StringBuffer();
                sb.append(m_resource.getString("msg_dnld_adding_file"));
                sb.append(fname);

                m_status.speak(m_pageCounter, sb.toString());

                m_zipper.writePath(fullPath.toString());
                m_zipper.writeUnicodeRtfPage(p_page, p_downloadParams);
            }
            else
            {
                CATEGORY.error("DownloadApi::addExtractedPrimaryTargetPage() - "
                        + "*** The Ansi RTF writer is no longer supported. ***");

                throw new AmbassadorDwUpException(new Exception(
                        "The Ansi RTF writer is no longer supported."));

                // DISABLED WRITER - DO NOT REMOVE COMMENT
                // m_zipper.writePath(fullPath);
                // m_zipper.writeAnsiRtfPage(p_page, m_ansiRtfWriter,
                // p_downloadParams);
            }
        }

        else if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_XLF
                || p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_OMEGAT)
        {
            if (p_downloadParams.isNeedCombined())
                fname = p_downloadParams.getFullJobName() + "_"
                        + p_downloadParams.getTargetLocale().toString() + "." + FILE_EXT_XLIFF_NO_DOT;
            else if (p_downloadParams.isNeedConsolidate())
                fname = p_downloadParams.getJob().getJobName() + "."
                        + FILE_EXT_XLIFF_NO_DOT;
            else
                fname = getUniqueExtractedPTFName(p_page, FILE_EXT_XLIFF_NO_DOT);
            fullPath.append(fname);

            m_pageCounter++;

            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);

            m_status.speak(m_pageCounter, sb.toString());

            m_zipper.writePath(fullPath.toString());
            m_zipper.writeUnicodeXliffPage(p_page, p_downloadParams);
        }

        else if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_TTX)
        {
            fname = getUniqueExtractedPTFName(p_page, FILE_EXT_TTX_NO_DOT);
            fullPath.append(fname);

            m_pageCounter++;

            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);

            m_status.speak(m_pageCounter, sb.toString());

            m_zipper.writePath(fullPath.toString());
            m_zipper.writeUnicodeTTXPage(p_page, p_downloadParams);
        }

        else if (p_downloadParams.getFileFormatId() == DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)
        {
            String uniquePTFname = getUniqueExtractedPTFName(p_page,
                    FILE_EXT_RTF_NO_DOT);

            // new resource names:
            String uniqueBinResFname = DownloadHelper.makeBinResFname(p_page);
            String uniqueResIdxFname = DownloadHelper.makeResIdxFname(p_page);

            // original 1.0 separate resource names:
            // String uniqueSrcResFname = DownloadHelper.makeSrcDocName(p_page);
            // String uniqueTmResFname = DownloadHelper.makeTmDocName(p_page);
            // String uniqueTagResFname =
            // DownloadHelper.makeTagInfoDocName(p_page);
            // String uniqueTermResFname = p_page.hasTermResources() ?
            // DownloadHelper.makeTermDocName(p_page) : "";

            fname = uniquePTFname;
            fullPath.append(fname);

            m_pageCounter++;
            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);

            m_status.speak(m_pageCounter, sb.toString());

            m_zipper.writePath(fullPath.toString());
            m_errorCode = m_zipper.writeParaViewWorkDoc(p_page,
                    p_downloadParams, uniqueBinResFname, uniqueResIdxFname);

            if (m_errorCode != MSGID_FILE_ADD_OK)
            {
                adjustLastMsg(MSG_CATEGORY_TRG_FILE_CREATION);

                m_status.speakRed(m_status.getTotalFiles(), getErrorMessage(),
                        m_resource.getString("msg_dnld_abort"));
                m_status.setAbort();
            }
        }
        else
        // TEXT download format
        {
            fname = getUniqueExtractedPTFName(p_page, FILE_EXT_TXT);
            fullPath.append(fname);

            m_pageCounter++;

            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(fname);

            m_status.speak(m_pageCounter, sb.toString());

            m_zipper.writePath(fullPath.toString());
            m_zipper.writeTxtPage(p_page, m_downloadParams);
        }

        return fname;
    }

    /**
     * Writes an unextracted PrimaryTagetFile to the Zip stream.
     */
    private void addUnextractedPrimaryTargetFile(PrimaryFile p_pf, Long p_fileId, String taskId)
            throws AmbassadorDwUpException, IOException
    {
        try
        {
            UnextractedFile uf = (UnextractedFile) p_pf;
            String path = DownloadHelper.makeUnextractedFilePath(p_pf,
                    p_fileId, m_downloadParams, taskId);
            // fix for 1491, superuser
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            File file = null;
            if (currentCompanyId.equals("1"))
            {
                String companyId = String.valueOf(ServerProxy.getPageManager()
                        .getTargetPage(p_fileId).getWorkflowInstance()
                        .getCompanyId());
                file = new File(
                        AmbFileStoragePathUtils
                                .getUnextractedParentDir(companyId),
                        uf.getStoragePath());
            }
            else
            {
                file = new File(
                        AmbFileStoragePathUtils.getUnextractedParentDir(),
                        uf.getStoragePath());
            }

            FileInputStream fis = new FileInputStream(file);

            m_zipper.writePath(path);
            m_zipper.writeFile(fis);
            fis.close();

            m_pageCounter++;

            StringBuffer sb = new StringBuffer();
            sb.append(m_resource.getString("msg_dnld_adding_file"));
            sb.append(new File(path).getName());

            m_status.speak(m_pageCounter, sb.toString());
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(
                    AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR, ex);
        }
    }

    /**
     * Writes the selected PrimarySourceFiles(PSFs) to the Zip stream.
     */
    private void addSourceFiles() throws AmbassadorDwUpException
    {
        if (m_downloadParams.hasSourceFiles())
        {
            List psfIds = m_downloadParams.getPSFileIds();
            HashMap<Long, Long> psfsTasks = m_downloadParams.getAllPSF_tasks();
            
            try
            {
                NativeFileManager nfMgr = ServerProxy.getNativeFileManager();

                for (Iterator it = psfIds.iterator(); it.hasNext();)
                {
                    // get unextracted source file info
                    Long id = (Long) it.next();

                    SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                            id.longValue());
                    UnextractedFile uf = (UnextractedFile) sp.getPrimaryFile();

                    // write contents
                    // fix for GBS-1491, super user
                    String currentCompanyId = CompanyThreadLocal.getInstance()
                            .getValue();
                    File file = null;
                    if (currentCompanyId.equals("1"))
                    {
                        String companyId = String.valueOf(sp.getCompanyId());
                        file = new File(
                                AmbFileStoragePathUtils
                                        .getUnextractedParentDir(companyId),
                                uf.getStoragePath());

                    }
                    else
                    {
                        file = new File(
                                AmbFileStoragePathUtils
                                        .getUnextractedParentDir(),
                                uf.getStoragePath());
                    }
                    
                    String taskId = m_downloadParams.getTaskID();
                    if (psfsTasks != null && psfsTasks.containsKey(id))
                    {
                        taskId = psfsTasks.get(id).toString();
                    }

                    FileInputStream fis = new FileInputStream(file);
                    File f = new File(
                            DownloadHelper.makePSFParentPath(m_downloadParams),
                            DownloadHelper.makeUnextractedFileName(
                                    sp.getIdAsLong(), uf.getStoragePath(),
                                    PRIMARY_SUFFIX, m_downloadParams, taskId));
                    m_zipper.writePath(f.getPath());
                    m_zipper.writeFile(fis);
                    fis.close();
                }
            }
            catch (Exception ex)
            {
                CATEGORY.warn("Could not add source file to job package "
                        + "(the zip file).", ex);
                // not fatal to offline job
            }
        }
    }

    /**
     * Writes the selected SecondaryTargetFiles(STFs) to the Zip stream.
     */
    private void addSecondaryTargetFiles() throws AmbassadorDwUpException,
            IOException
    {
        if (m_downloadParams.hasSecondaryFiles())
        {
            List stfIds = m_downloadParams.getSTFileIds();
            HashMap<Long, Long> stfsTasks = m_downloadParams.getAllSTF_tasks();

            try
            {
                SecondaryTargetFileMgr stfMgr = ServerProxy
                        .getSecondaryTargetFileManager();
                NativeFileManager nativeFileMgr = ServerProxy
                        .getNativeFileManager();
                String companyId = String.valueOf(m_downloadParams.getRightJob()
                        .getCompanyId());
                for (Iterator it = stfIds.iterator(); it.hasNext();)
                {
                    // get stf object
                    Long stfId = (Long) it.next();
                    SecondaryTargetFile stf = stfMgr
                            .getSecondaryTargetFile(stfId.longValue());
                    
                    String taskId = m_downloadParams.getTaskID();
                    if (stfsTasks != null && stfsTasks.containsKey(stfId))
                    {
                        taskId = stfsTasks.get(stfId).toString();
                    }

                    // write stf contents
                    FileInputStream fis = new FileInputStream(
                            nativeFileMgr.getFile(stf, companyId));
                    File f = new File(
                            DownloadHelper.makeSTFParentPath(m_downloadParams),
                            DownloadHelper.makeUnextractedFileName(
                                    stf.getIdAsLong(), stf.getStoragePath(),
                                    SECONDARY_SUFFIX, m_downloadParams, taskId));
                    m_zipper.writePath(f.getPath());
                    m_zipper.writeFile(fis);
                    fis.close();

                    m_pageCounter++;

                    StringBuffer sb = new StringBuffer();
                    sb.append(m_resource.getString("msg_dnld_adding_file"));
                    sb.append(f.getName());

                    m_status.speak(m_pageCounter, sb.toString());
                }
            }
            catch (Exception ex)
            {
                throw new AmbassadorDwUpException(
                        AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                        ex);
            }
        }
    }

    /**
     * Writes the users selected support file(s) to the Zip stream. The relative
     * path to a support file is reconstructed under the support files
     * subdirectory of the offline package.
     */
    private void addSupportFiles() throws AmbassadorDwUpException, IOException
    {
        if (m_downloadParams.hasSupportFiles())
        {
            FileInputStream fis = null;
            String rootDir = DownloadHelper
                    .makeSupportFileParentPath(m_downloadParams);
            String companyId = String.valueOf(m_downloadParams.getRightJob()
                    .getCompanyId());
            String fname = "";
            Iterator it = m_downloadParams.getSupportFilesList().iterator();
            List<String> addedFile = new ArrayList<String>();

            while (it.hasNext())
            {
                GlossaryFile gf = (GlossaryFile) it.next();
                File fromFile = new File(
                        DownloadHelper
                                .getSupportFileAbsolutePath(gf, companyId));
                String filepath = fromFile.getPath();

                if (addedFile.contains(filepath))
                {
                    m_pageCounter++;
                    m_status.speak(m_pageCounter, "");
                }
                else
                {
                    try
                    {
                        fis = new FileInputStream(fromFile);
                    }
                    catch (Exception ex)
                    {
                        throw new AmbassadorDwUpException(
                                AmbassadorDwUpExceptionConstants.GENERAL_IO_READ_ERROR,
                                ex);
                    }

                    // write to this unique file name
                    fname = getUniqueSupportFileName(gf.getFilename());
                    StringBuffer sb = new StringBuffer();
                    sb.append(rootDir);
                    sb.append(fname);
                    m_zipper.writePath(sb.toString());
                    m_zipper.writeFile(fis);
                    m_pageCounter++;

                    sb = new StringBuffer();
                    sb.append(m_resource.getString("msg_dnld_adding_file"));
                    sb.append(fname);

                    addedFile.add(filepath);
                    m_status.speak(m_pageCounter, sb.toString());
                }
            }
        }
    }

    private void addResourceFiles(String p_wrkDocFname, OfflinePageData p_OPD,
            DownloadParams p_dnldParams) throws AmbassadorDwUpException,
            IOException
    {
        // add the resource file
        switch (m_downloadParams.getFileFormatId())
        {
            case DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE:
                addParaViewOneResourceFiles(p_wrkDocFname, p_OPD, p_dnldParams);
                break;
            default:
                generateResourceHtml(p_OPD);
                String htmlPath = DownloadHelper
                        .makeResParentPath(m_downloadParams);
                addHtmlResourcePage(htmlPath, p_OPD);
                break;
        }
    }

    private void addParaViewOneResourceFiles(String p_wrkDocFname,
            OfflinePageData p_page, DownloadParams p_downloadParams)
            throws AmbassadorDwUpException, IOException
    {
        String uniqueBinResFname = DownloadHelper.makeBinResFname(p_page);
        String uniqueResIdxFname = DownloadHelper.makeResIdxFname(p_page);
        String resPath = DownloadHelper.makeResParentPath(p_downloadParams);

        // New Binary writer
        m_zipper.writePath(resPath + uniqueBinResFname);
        m_errorCode = m_zipper.writeParaViewBinaryRes(p_wrkDocFname, p_page,
                p_downloadParams, uniqueBinResFname, uniqueResIdxFname);

        if (m_errorCode != MSGID_FILE_ADD_OK)
        {
            adjustLastMsg(MSG_CATEGORY_TM_RES_CREATION);

            // we no longer abort, instead we warn of matches that are left out
            m_status.warn(getErrorMessage(), m_resource.getString("lb_warning"));
        }
        else
        {
            // New Index writer (for the binary file written above)
            // Writes the last index currently held in memory.
            m_zipper.writePath(resPath + uniqueResIdxFname);
            m_errorCode = m_zipper.writeParaViewBinaryResIdx();

            if (m_errorCode != MSGID_FILE_ADD_OK)
            {
                // we no longer abort, instead we warn of matches that are left
                // out
                m_status.warn(getErrorMessage(),
                        m_resource.getString("lb_warning"));
            }
        }

        // Keep: Defunct source RTF doc resource writer. This writer is still
        // available.
        // Source file
        // String uniqueSrcDocName = DownloadHelper.makeSrcDocName(p_page);
        // m_zipper.writePath(resPath + uniqueSrcDocName);
        // m_errorCode = m_zipper.writeParaViewSrcDoc(p_page, p_downloadParams);
        // if (m_errorCode != MSGID_FILE_ADD_OK)
        // {
        // adjustLastMsg(MSG_CATEGORY_SRC_RES_CREATION);
        // m_status.speakRed(m_status.getTotalFiles(), getErrorMessage(),
        // m_resource.getString("msg_dnld_abort"));
        // m_status.setAbort();
        // return;
        // }

        // Keep: Defunct tm RTF doc resource writer. This writer is still
        // available.
        // Tm file
        // String uniqueTmResFname = DownloadHelper.makeTmDocName(p_page);
        // m_zipper.writePath(resPath + uniqueTmResFname);
        // m_errorCode = m_zipper.writeParaViewTmDoc(p_page, p_downloadParams);
        // if (m_errorCode != MSGID_FILE_ADD_OK)
        // {
        // adjustLastMsg(MSG_CATEGORY_TM_RES_CREATION);
        // // we no longer abort, instead we warn of matches that are left out
        // m_status.warn(getErrorMessage(), m_resource.getString("lb_warning"));
        // }

        // Keep: Defunct info RTF doc resource writer. This writer is still
        // available.
        // Tag info file
        // String uniqueTagInfoDocName =
        // DownloadHelper.makeTagInfoDocName(p_page);
        // m_zipper.writePath(resPath + uniqueTagInfoDocName);
        // m_errorCode = m_zipper.writeParaViewTagInfoDoc(p_page,
        // p_downloadParams);
        // if (m_errorCode != MSGID_FILE_ADD_OK)
        // {
        // adjustLastMsg(MSG_CATEGORY_TAG_RES_CREATION);
        // // do not abort, chances are they will not use tag resources.
        // m_status.warn(getErrorMessage(), m_resource.getString("lb_warning"));
        // }

        // Keep: Defunct Terminology RTF doc resource writer. This writer is
        // still available.
        // Terminology file
        // String uniqueTermResFname = p_page.hasTermResources() ?
        // DownloadHelper.makeTermDocName(p_page) : "";
        //
        // if (p_page.hasTermResources())
        // {
        // m_zipper.writePath(resPath + uniqueTermResFname);
        // m_errorCode = m_zipper.writeParaViewTermDoc(p_page,
        // p_downloadParams);
        // if(m_errorCode != MSGID_FILE_ADD_OK)
        // {
        // adjustLastMsg(MSG_CATEGORY_TERM_RES_CREATION);
        // // we no longer abort, instead we warn of matches that are left out
        // m_status.warn(getErrorMessage(), m_resource.getString("lb_warning"));
        // }
        // }

    }

    /**
     * Creates the associated TM/Term resource and segment ID list (HTML files).
     * 
     * @exception AmbassadorDwUpException
     */
    private void addHtmlResourcePage(String p_htmlPath, OfflinePageData p_page)
            throws AmbassadorDwUpException
    {
        m_zipper.writePath(p_htmlPath + p_page.getPageId() + "/"
                + p_page.getPageId() + FILE_EXT_HTML);
        m_zipper.writeHtmlPage(m_downloadResourcePageWriter,
                HTML_RESOURCE_ENCODING);

        m_zipper.writePath(p_htmlPath + p_page.getPageId() + "/"
                + SEG_ID_LIST_NAME + FILE_EXT_HTML);
        m_zipper.writeHtmlPage(m_segmentIdListWriter, HTML_RESOURCE_ENCODING);

        m_downloadResourcePageWriter.reset();
        m_segmentIdListWriter.reset();
    }

    /**
     * Writes the main index file for the offline HTML resource pages.
     * 
     * @exception AmbassadorDwUpException
     */
    private void addFinalHtmlResourceMainIndex() throws AmbassadorDwUpException
    {
        if (m_packageHasResources)
        {
            // GBS-633 Downloaded folder naming convention:
            // <jobname>_<targetlocale> instead of <jobname>_<task id> before
            String path = m_downloadParams.getTruncatedJobName()
                    + FILE_NAME_BREAK + getTargetLocaleCode(m_downloadParams)
                    + "/";

            if (m_downloadParams.isNeedCombined())
            {
                m_zipper.writePath(path + INDEX_FILE_NAME + FILE_EXT_HTML);
                m_zipper.writeHtmlPage(m_indexJobsWriter, HTML_RESOURCE_ENCODING);
                m_indexJobsWriter.reset();

                // add job list
                m_zipper.writePath(path + JOB_LIST_FILE_NAME + FILE_EXT_HTML);
                m_zipper.writeHtmlPage(m_jobListWriter, HTML_RESOURCE_ENCODING);
                m_jobListWriter.reset();
                
                // add job - page list
                for (int i = 0; i < m_pageListWriters.size(); i++)
                {
                    PageListWriter pageListWriter = m_pageListWriters.get(i);
                    long jobId = pageListWriter.getJobId();
                    String jobsDir = DownloadWriterInterface.JOBS_DIR;
                    m_zipper.writePath(path + jobsDir + "/" + jobId + FILE_EXT_HTML);
                    m_zipper.writeHtmlPage(pageListWriter, HTML_RESOURCE_ENCODING);
                }
                m_pageListWriters.clear();
            }
            else
            {
                m_zipper.writePath(path + INDEX_FILE_NAME + FILE_EXT_HTML);
                m_zipper.writeHtmlPage(m_indexPageWriter, HTML_RESOURCE_ENCODING);
                m_indexPageWriter.reset();

                m_zipper.writePath(path + PAGE_LIST_FILE_NAME + FILE_EXT_HTML);
                m_zipper.writeHtmlPage(m_pageListWriter, HTML_RESOURCE_ENCODING);
                m_pageListWriter.reset();
            }
        }
    }

    private String getTargetLocaleCode(DownloadParams p_downloadParams)
    {
        String targetLocale = p_downloadParams.getTargetLocale().getLanguage()
                + "_" + p_downloadParams.getTargetLocale().getCountryCode();

        return targetLocale;
    }

    /**
     * Writes the offline help files to the Zip stream. TODO: get mutilple files
     * under a given locale dir
     */
    private void addHelpPages() throws AmbassadorDwUpException
    {
        String docRoot = null;

        try
        {
            docRoot = SystemConfiguration.getInstance().getStringParameter(
                    SystemConfigParamNames.WEB_SERVER_DOC_ROOT);
        }
        catch (Exception ex)
        {
            CATEGORY.warn("Cannot write download help files", ex);
            // not fatal to offline job
        }

        // get help source and target paths
        StringBuffer sbHelpTrgPath = new StringBuffer();
        sbHelpTrgPath.append(DownloadHelper
                .makeHelpParentPath(m_downloadParams));
        sbHelpTrgPath.append("/A/B/C");

        StringBuffer sbHelpSrcPath = new StringBuffer();
        sbHelpSrcPath.append(docRoot);
        sbHelpSrcPath.append(m_resource.getString("help_dir_root"));

        if (m_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)
        {
            sbHelpSrcPath.append(m_resource
                    .getString("help_dir_offline_paraview"));
        }
        else
        {
            sbHelpSrcPath.append(m_resource
                    .getString("help_dir_offline_listview"));
        }

        // get CSS source and target paths
        String cssTrgPath = DownloadHelper.makeHelpParentPath(m_downloadParams);
        StringBuffer sbCssSrcPath = new StringBuffer();
        sbCssSrcPath.append(docRoot);
        sbCssSrcPath.append(m_resource.getString("help_dir_root"));

        // get offline target path
        // String toPathRoot =
        // DownloadHelper.makeHelpParentPath(m_downloadParams);

        // write help files
        FileListBuilder builder = new FileListBuilder();
        builder.add(sbHelpSrcPath.toString(), ".htm");
        builder.add(sbHelpSrcPath.toString(), ".html");
        File file = builder.getNextFile();
        while (file != null)
        {
            addStaticResourceFile(file, sbHelpTrgPath.toString());
            file = builder.getNextFile();
        }

        // write CSS files
        file = new File(sbCssSrcPath.toString(), "default.css");
        addStaticResourceFile(file, cssTrgPath);
        file = new File(sbCssSrcPath.toString(), "default_ns.css");
        addStaticResourceFile(file, cssTrgPath);

        // write the "GS.ini" file to offline kit for TTX.
        int fileFormat = m_downloadParams.getFileFormatId();
        if (fileFormat == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TTX)
        {
            File globalsight_ear = (new File(docRoot)).getParentFile();
            String gsIniFile = globalsight_ear.getPath()
                    + "/lib/classes/resources/" + TTXConstants.GS_INI_FILE;
            gsIniFile = gsIniFile.replace("\\", "/").replace("/",
                    File.separator);
            addStaticResourceFile(new File(gsIniFile), cssTrgPath);
        }
    }

    /**
     * Writes the the selected utility files.
     */
    private void addStaticResourceFile(File p_file, String p_toPath)
            throws AmbassadorDwUpException
    {
        FileInputStream input = null;
        try
        {
            input = new FileInputStream(p_file);

            if (input == null)
            {
                CATEGORY.warn("Could not locate static resource: " + p_file);
                // not fatal to offline job
            }
            else
            {
                m_zipper.writePath(p_toPath + "/" + p_file.getName());
                m_zipper.writeFile(input);
            }

        }
        catch (Exception ex)
        {
            CATEGORY.warn("cannot write static resource to zip stream.", ex);
            // not fatal to offline job
        }
        finally
        {
            try
            {
                if (input != null)
                {
                    input.close();
                }
            }
            catch (IOException e)
            {
                CATEGORY.warn("cannot close static resource to zip stream.", e);
            }
        }
    }

    /**
     * Writes the the selected utility files.
     */
    private void addStaticResourceFile(String p_fname,
            String p_fromRelativeClassPath, String p_toPath)
            throws AmbassadorDwUpException
    {
        try
        {
            StringBuffer sbFrom = new StringBuffer();
            sbFrom.append(p_fromRelativeClassPath);
            sbFrom.append("/");
            sbFrom.append(p_fname);

            StringBuffer sbTo = new StringBuffer();
            sbTo.append(p_toPath);
            sbTo.append(p_fname);

            InputStream input = DownLoadApi.class.getResourceAsStream(sbFrom
                    .toString());
            if (input == null)
            {
                CATEGORY.warn("Could not locate static resource: " + sbFrom);
                // not fatal to offline job
            }
            else
            {
                m_zipper.writePath(sbTo.toString());
                m_zipper.writeFile(input);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.warn("cannot write static resource to zip stream.", ex);
            // not fatal to offline job
        }

    }

    /** ***** End zipper methods ****** */

    /** ***** Begin misc. utility methods ****** */

    /**
     * Creates a unique support file name.
     * 
     * To avoid naming collisions, we keep track of the file names used so far.
     * If a duplicate occurs, we append the page id to make it unique. For
     * example, the job may include two index.html files imported from two
     * different subdirectories. One would be named index.txt or index.rtf
     * (depending on whther we are downloading text or rtf) and the other would
     * be index_[pageId].txt or index_[pageId].rtf
     * 
     * @param p_name
     *            - the support files original name.
     * @return java.lang.String
     */
    private String getUniqueSupportFileName(String p_name)
    {
        if (p_name == null)
        {
            return null;
        }

        // check that this the name is unique in this download
        if (!m_uniqueSupportFileNames.contains(p_name))
        {
            m_uniqueSupportFileNames.add(p_name);
            return p_name;
        }
        else
        // make it unique
        {
            int i = 1;
            StringBuffer uniqueName = new StringBuffer(p_name);
            uniqueName.insert(0, new Integer(i));

            while (m_uniqueSupportFileNames.contains(uniqueName))
            {
                i++;
                uniqueName = new StringBuffer(p_name);
                uniqueName.insert(0, new Integer(i));
            }

            m_uniqueSupportFileNames.add(uniqueName);

            return uniqueName.toString();
        }
    }

    /**
     * Initializes the HTML writers
     */
    private void initHtmlResourceWriters(String p_uiLocale)
            throws AmbassadorDwUpException
    {
        m_indexPageWriter.reset();
        // m_indexPageWriter does not require locale

        m_pageListWriter.reset();
        m_pageListWriter.setUiLocale(p_uiLocale);

        m_downloadResourcePageWriter.reset();
        m_downloadResourcePageWriter.setUiLocale(p_uiLocale);

        m_segmentIdListWriter.reset();
        m_segmentIdListWriter.setUiLocale(p_uiLocale);
    }

    /**
     * Generates/appends all HTML components related to this page data.
     * 
     * @param p_page
     *            com.globalsight.everest.edit.offline.OfflinePageData
     */
    private void generateResourceHtml(OfflinePageData p_page)
            throws AmbassadorDwUpException
    {
        // initHtmlResourceWriters(m_downloadParams.getUiLocale());

        // only the first offline page is displayed in the initial frame
        if (m_firstPage)
        {
            m_indexPageWriter.processOfflinePageData(p_page);
            m_indexJobsWriter.processOfflinePageData(p_page);
        }

        m_pageListWriter.processOfflinePageData(p_page);
        m_jobListWriter.processOfflinePageData(p_page);
        m_downloadResourcePageWriter.processOfflinePageData(p_page);
        m_segmentIdListWriter.processOfflinePageData(p_page);
        m_packageHasResources = true;
        
        if (m_pageListWriters == null)
        {
            m_pageListWriters = new ArrayList<PageListWriter>();
        }
        
        boolean processed = false;
        long jobId = p_page.getJobId();
        for (PageListWriter plistWriter : m_pageListWriters)
        {
            if (plistWriter.isAddable(jobId))
            {
                plistWriter.processOfflinePageData(p_page);
                processed = true;
            }
        }
        
        if (!processed)
        {
            PageListWriter plistWriter = new PageListWriter();
            plistWriter.useJobs(true);
            plistWriter.processOfflinePageData(p_page);
            m_pageListWriters.add(plistWriter);
        }
    }

    /** Maps the generic message to a more specific one. */
    private void adjustLastMsg(int p_category)
    {
        switch (m_errorCode)
        {
            case MSGID_WD2000_BM_CNT_EXCEEDED:
                switch (p_category)
                {
                    case MSG_CATEGORY_SRC_RES_CREATION:
                        m_errorCode = MSGID_WD2000_BM_CNT_EXCEEDED_SRC_DOC;
                        break;
                    case MSG_CATEGORY_TRG_FILE_CREATION:
                        m_errorCode = MSGID_WD2000_BM_CNT_EXCEEDED_TRG_DOC;
                        break;
                    case MSG_CATEGORY_TM_RES_CREATION:
                        m_errorCode = MSGID_WD2000_BM_CNT_EXCEEDED_TM_DOC;
                        break;
                    case MSG_CATEGORY_TERM_RES_CREATION:
                        m_errorCode = MSGID_WD2000_BM_CNT_EXCEEDED_TERM_DOC;
                        break;
                    case MSG_CATEGORY_TAG_RES_CREATION:
                        m_errorCode = MSGID_WD2000_BM_CNT_EXCEEDED_TAG_DOC;
                        break;
                    default:
                        break;
                }
                break;

            case MSGID_WD2000_FLD_CNT_EXCEEDED:
                switch (p_category)
                {
                    case MSG_CATEGORY_SRC_RES_CREATION:
                        m_errorCode = MSGID_WD2000_FLD_CNT_EXCEEDED_SRC_DOC;
                        break;
                    case MSG_CATEGORY_TRG_FILE_CREATION:
                        m_errorCode = MSGID_WD2000_FLD_CNT_EXCEEDED_TRG_DOC;
                        break;
                    case MSG_CATEGORY_TM_RES_CREATION:
                        m_errorCode = MSGID_WD2000_FLD_CNT_EXCEEDED_TM_DOC;
                        break;
                    case MSG_CATEGORY_TERM_RES_CREATION:
                        m_errorCode = MSGID_WD2000_FLD_CNT_EXCEEDED_TERM_DOC;
                        break;
                    case MSG_CATEGORY_TAG_RES_CREATION:
                        m_errorCode = MSGID_WD2000_FLD_CNT_EXCEEDED_TAG_DOC;
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    private void setUILocaleResources(DownloadParams p_params)
    {
        SystemResourceBundle rb = SystemResourceBundle.getInstance();

        m_resource = rb.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                GlobalSightLocale.makeLocaleFromString(p_params.getUiLocale()));
    }

    /**
     * Returns a error or warning message for the current error code ID.
     */
    public String getErrorMessage()
    {
        String key = null;

        switch (m_errorCode)
        {
            case MSGID_WD2000_BM_CNT_EXCEEDED:
                key = "msg_wd2000BmCntExceeded";
                break;
            case MSGID_WD2000_BM_CNT_EXCEEDED_SRC_DOC:
                key = "msg_wd2000BmCntExceededSrcDoc";
                break;
            case MSGID_WD2000_BM_CNT_EXCEEDED_TRG_DOC:
                key = "msg_wd2000BmCntExceededTrgDoc";
                break;
            case MSGID_WD2000_BM_CNT_EXCEEDED_TM_DOC:
                key = "msg_wd2000BmCntExceededTmDoc";
                break;
            case MSGID_WD2000_BM_CNT_EXCEEDED_TERM_DOC:
                key = "msg_wd2000BmCntExceededTermDoc";
                break;
            case MSGID_WD2000_BM_CNT_EXCEEDED_TAG_DOC:
                key = "msg_wd2000BmCntExceededTagDoc";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED:
                key = "msg_wd2000FldCntExceeded";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED_SRC_DOC:
                key = "msg_wd2000FldCntExceededSrcDoc";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED_TRG_DOC:
                key = "msg_wd2000FldCntExceededTrgDoc";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED_TM_DOC:
                key = "msg_wd2000FldCntExceededTmDoc";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED_TERM_DOC:
                key = "msg_wd2000FldCntExceededTermDoc";
                break;
            case MSGID_WD2000_FLD_CNT_EXCEEDED_TAG_DOC:
                key = "msg_wd2000FldCntExceededTagDoc";
                break;
            case MSGID_FAILED_TO_WRITE_INDEX:
                key = "msg_filedToWriteResIndexFile";
                break;
            default:
                key = null;
                break;
        }

        return key != null ? m_resource.getString(key)
                : "Unknown error or warning " + m_errorCode + " (msgKey=null).";
    }

    static private SynchronizationManager getSynchronizationManager()
    {
        try
        {
            return ServerProxy.getSynchronizationManager();
        }
        catch (Exception ex)
        {
            CATEGORY.error("Internal error: cannot receive offline/online "
                    + "synchronization messages", ex);
        }

        return null;
    }

    /** ***** End misc. utility methods ****** */
}