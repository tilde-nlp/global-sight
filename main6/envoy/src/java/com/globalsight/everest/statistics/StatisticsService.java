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

package com.globalsight.everest.statistics;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.comparator.TuvSourceContentComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides statistic services for pages.
 */
public class StatisticsService
{
    private static Logger c_logger = Logger.getLogger(StatisticsService.class);

    // private static SimpleDateFormat format = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Calculate word counts for all target pages in a workflow, and calculate
     * the repetitions in the scope of workflow.
     */
    public static void calculateTargetPagesWordCount(Workflow p_workflow,
            Vector<String> p_excludedTuTypes)
    {
        try
        {
            List<TargetPage> targetPages = getAllTargetPagesForWorkflow(p_workflow);
            Map<SegmentTmTuv, List<SegmentTmTuv>> m_uniqueSegments = new HashMap<SegmentTmTuv, List<SegmentTmTuv>>();
            Map uniqueSegments2 = new HashMap();
            int threshold = p_workflow.getJob().getLeverageMatchThreshold();

            // Touch every page's TUs to load them to improve performance.
            for (TargetPage targetPage : targetPages)
            {
                if (targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                {
                    SegmentTuUtil.getTusBySourcePageId(targetPage
                            .getSourcePage().getId());
                }
            }

            for (TargetPage targetPage : targetPages)
            {
                if (targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                {
                    SourcePage sourcePage = targetPage.getSourcePage();
                    // As here don't need XliffAlt data, don't load them to
                    // improve performance.
                    boolean needLoadExtraInfo = false;
                    ArrayList<Tuv> sTuvs = SegmentTuvUtil.getSourceTuvs(
                            sourcePage, needLoadExtraInfo);
                    ArrayList<BaseTmTuv> splittedTuvs = splitSourceTuvs(sTuvs,
                            sourcePage.getGlobalSightLocale(),
                            sourcePage.getCompanyId());
                    String sourcePageId = sourcePage.getExternalPageId();
                    boolean isDefaultContextMatch = false;
                    // Only when "Leverage Default Matches" option is selected
                    // in TM profile, it is necessary to judge if the page is
                    // "DefaultContextMatch"(GBS-2214 by York since 2011-12-13)
                    if (PageHandler.isDefaultContextMatch(p_workflow.getJob()))
                    {
                        isDefaultContextMatch = isDefaultContextMatch(
                                sourcePageId, sourcePage);
                    }
                    targetPage.setIsDefaultContextMatch(isDefaultContextMatch);
                    Long targetLocaleId = targetPage.getLocaleId();
                    boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                            .isWorldServerXliffSourceFile(
                                    sourcePage.getIdAsLong());

                    LeverageMatchLingManager lmLingManager = getLeverageMatchLingManager();

                    if (isWSXlfSourceFile)
                    {
                        lmLingManager.setIncludeMtMatches(false);
                    }

                    MatchTypeStatistics matches = lmLingManager
                            .getMatchTypesForStatistics(
                                    sourcePage.getIdAsLong(), targetLocaleId,
                                    threshold);

                    PageWordCounts targetPageWordCounts = null;
                    if (isWSXlfSourceFile)
                    {
                        targetPageWordCounts = calculateWorldServerTargetPageWordCounts(
                                splittedTuvs, matches, p_excludedTuTypes);
                        targetPage.setWordCount(targetPageWordCounts);
                    }
                    else
                    {
                        targetPageWordCounts = calculateTargetPageWordCounts(
                                splittedTuvs, matches, p_excludedTuTypes,
                                m_uniqueSegments);
                        targetPage.setWordCount(targetPageWordCounts);
                    }

                    // Update "Segment-TM,allExactMatch,ICE" word counts.
                    updateExtraColumnWordCountsForTargetPage(targetPage,
                            splittedTuvs, matches, p_excludedTuTypes);

                    HibernateUtil.update(targetPage);

                    // update TU table for repetition information.
                    Map<Long, TuImpl> cachedTus = getTusMapBySourcePage(sourcePage);
                    // touch to load target TUVs
                    SegmentTuvUtil.getTargetTuvs(targetPage);
                    updateRepetitionInfoToTu(splittedTuvs, matches,
                            uniqueSegments2, cachedTus,
                            sourcePage.getCompanyId(), targetLocaleId);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }

    /**
     * get target pages for Workflow. Not from session cache
     * 
     * @param p_workflow
     * @return
     */
    private static List<TargetPage> getAllTargetPagesForWorkflow(
            Workflow p_workflow)
    {
        List<TargetPage> tpages = new ArrayList<TargetPage>();
        long wfid = p_workflow.getId();

        try
        {
            String sql = "select tp.* from target_page tp where tp.WORKFLOW_IFLOW_INSTANCE_ID = "
                    + wfid;
            tpages = HibernateUtil.searchWithSql(TargetPage.class, sql);
            List<TargetPage> oriTPS = p_workflow.getAllTargetPages();
            int tpagesSize = tpages.size();

            // add missing target pages to Workflow (in cache)
            if (oriTPS.size() < tpagesSize)
            {
                for (TargetPage tp : tpages)
                {
                    boolean exists = false;
                    for (TargetPage tp2 : oriTPS)
                    {
                        if (tp2.getId() == tp.getId())
                        {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists)
                    {
                        p_workflow.addTargetPage(tp);
                        oriTPS = p_workflow.getAllTargetPages();

                        if (oriTPS.size() == tpagesSize)
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            c_logger.warn("Error when getAllTargetPagesForWorkflow, wfid : "
                    + wfid, ex);
            tpages = p_workflow.getAllTargetPages();
        }

        return tpages;
    }

    /**
     * Calculate the target page word counts for WorldServer XLF target page.
     */
    private static PageWordCounts calculateWorldServerTargetPageWordCounts(
            ArrayList<BaseTmTuv> sTuvs, MatchTypeStatistics p_matches,
            Vector<String> p_excludedTuTypes)
    {
        // 100% cases
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int xliffMatchWordCount = 0;
        // below 50%
        int noMatchRepetitionWordCount = 0;
        int noMatchWordCount = 0;
        // 50%--75%
        int lowFuzzyRepetitionWordCount = 0;
        int lowFuzzyWordCount = 0;
        // 75%--84%
        int medFuzzyRepetitionWordCount = 0;
        int medFuzzyWordCount = 0;
        // 85%--94%
        int medHighFuzzyRepetionWordCount = 0;
        int medHighFuzzyWordCount = 0;
        // 95%--99%
        int highFuzzyRepetionWordCount = 0;
        int highFuzzyWordCount = 0;
        // go through all segments
        int totalWordCount = 0;

        // MT relevant word counts
//        int mtTotalWordCount = 0;
        int mtExactMatchWordCount = 0;
        int mtFuzzyNoMatchWordCount = 0;
        int mtRepetitionsWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        Iterator<BaseTmTuv> si = sTuvs.iterator();
        while (si.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv) si.next();
            int wordCount = tuv.getWordCount();

            // Don't count excluded items.
            if (p_excludedTuTypes != null
                    && p_excludedTuTypes.contains(tuv.getTu().getType()))
            {
                wordCount = 0;
            }
            totalWordCount += wordCount;
            Types types = p_matches.getTypes(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());
            boolean isMtTranslation = isMtTranslation(types);
            // Calculate threshold non-related word counts
            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            String sourceContent = tuv.getTu().getSourceContent();
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                    contextMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                    segmentTmWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                    mtExactMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                    xliffMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        lowFuzzyRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    else
                    {
                        lowFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    else
                    {
                        medFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        medHighFuzzyRepetionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    else
                    {
                        medHighFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        highFuzzyRepetionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    else
                    {
                        highFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.NO_MATCH:
                default:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        noMatchRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    else
                    {
                        noMatchWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    break;
            }

            /*
             * This part is used to calculate the word counts relative to
             * threshold, they
             * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
             * ,thresholdMedFuzzyWordCount,
             * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
            if (types != null)
            {
                statisticsMatchTypeByThreshold = types
                        .getStatisticsMatchTypeByThreshold();
            }
            switch (statisticsMatchTypeByThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }

        PageWordCounts pageWordCounts = new PageWordCounts();
        // total word count
        pageWordCounts.setTotalWordCount(totalWordCount);
        // all 100% cases
        pageWordCounts.setContextMatchWordCount(contextMatchWordCount);
        pageWordCounts.setSegmentTmWordCount(segmentTmWordCount);
//        pageWordCounts.setMtExactMatchWordCount(mtExactMatchWordCount);
        pageWordCounts.setXliffExtractMatchWordCount(xliffMatchWordCount);
        // all repetitions
        pageWordCounts.setRepetitionWordCount(highFuzzyRepetionWordCount
                + medHighFuzzyRepetionWordCount + medFuzzyRepetitionWordCount
                + lowFuzzyRepetitionWordCount + noMatchRepetitionWordCount);
        // fuzzy and no match
        pageWordCounts.setHiFuzzyWordCount(highFuzzyWordCount);
        pageWordCounts.setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        pageWordCounts.setMedFuzzyWordCount(medFuzzyWordCount);
        pageWordCounts.setLowFuzzyWordCount(lowFuzzyWordCount);
        pageWordCounts.setNoMatchWordCount(noMatchWordCount);
        // threshold related
        pageWordCounts.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        pageWordCounts
                .setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        pageWordCounts
                .setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        pageWordCounts
                .setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        pageWordCounts.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);
        // MT related
        pageWordCounts.setMtTotalWordCount(mtExactMatchWordCount
                + mtFuzzyNoMatchWordCount + mtRepetitionsWordCount);
        pageWordCounts.setMtExactMatchWordCount(mtExactMatchWordCount);
        pageWordCounts.setMtFuzzyNoMatchWordCount(mtFuzzyNoMatchWordCount);
        pageWordCounts.setMtRepetitionsWordCount(mtRepetitionsWordCount);
        
        return pageWordCounts;
    }

    /**
     * Calculate the target page word counts. Repetition word counts are in job
     * scope.
     */
    private static PageWordCounts calculateTargetPageWordCounts(
            ArrayList<BaseTmTuv> sTuvs, MatchTypeStatistics p_matches,
            Vector<String> p_excludedTuTypes,
            Map<SegmentTmTuv, List<SegmentTmTuv>> m_uniqueSegments)
    {
        // 100 cases
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int xliffMatchWordCount = 0;
        int poMatchWordCount = 0;
        // below 50%
        int noMatchRepetitionWordCount = 0;
        int noMatchWordCount = 0;
        // 50%--75%
        int lowFuzzyRepetitionWordCount = 0;
        int lowFuzzyWordCount = 0;
        // 75%--84%
        int medFuzzyRepetitionWordCount = 0;
        int medFuzzyWordCount = 0;
        // 85%--94%
        int medHighFuzzyRepetionWordCount = 0;
        int medHighFuzzyWordCount = 0;
        // 95%--99%
        int highFuzzyRepetionWordCount = 0;
        int highFuzzyWordCount = 0;
        // go through all segments
        int totalWordCount = 0;

        // MT relevant word counts
//        int mtTotalWordCount = 0;
        int mtExactMatchWordCount = 0;
        int mtFuzzyNoMatchWordCount = 0;
        int mtRepetitionsWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        Map<SegmentTmTuv, List<SegmentTmTuv>> tmp = new HashMap<SegmentTmTuv, List<SegmentTmTuv>>();
        tmp.putAll(m_uniqueSegments);

        Iterator<BaseTmTuv> si = sTuvs.iterator();
        while (si.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv) si.next();
            int wordCount = tuv.getWordCount();

            // Don't count excluded items.
            if (p_excludedTuTypes != null
                    && p_excludedTuTypes.contains(tuv.getTu().getType()))
            {
                wordCount = 0;
            }
            totalWordCount += wordCount;
            Types types = p_matches.getTypes(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());
            boolean isMtTranslation = isMtTranslation(types);
            // Calculate threshold non-related word counts
            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            ArrayList<SegmentTmTuv> identicalSegments = null;
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                    contextMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                    segmentTmWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                    mtExactMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                    xliffMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    poMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        lowFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    else
                    {
                        lowFuzzyRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        medFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    else
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        medHighFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    else
                    {
                        medHighFuzzyRepetionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        highFuzzyWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    else
                    {
                        highFuzzyRepetionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    break;
                case LeverageMatchLingManager.NO_MATCH:
                default:
                    // no-match is counted only once and the rest are
                    // repetitions
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        noMatchWordCount += wordCount;
                        if (isMtTranslation)
                            mtFuzzyNoMatchWordCount += wordCount;
                    }
                    else
                    {
                        noMatchRepetitionWordCount += wordCount;
                        if (isMtTranslation)
                            mtRepetitionsWordCount += wordCount;
                    }
                    break;
            }

            /*
             * This part is used to calculate the word counts relative to
             * threshold, they
             * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
             * ,thresholdMedFuzzyWordCount,
             * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
            if (types != null)
            {
                statisticsMatchTypeByThreshold = types
                        .getStatisticsMatchTypeByThreshold();
            }
            ArrayList<SegmentTmTuv> identicalSegmentsOfThreshold = null;
            switch (statisticsMatchTypeByThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }

        PageWordCounts pageWordCounts = new PageWordCounts();
        // total word count
        pageWordCounts.setTotalWordCount(totalWordCount);
        // all 100% cases
        pageWordCounts.setContextMatchWordCount(contextMatchWordCount);
        pageWordCounts.setSegmentTmWordCount(segmentTmWordCount);
        pageWordCounts.setXliffExtractMatchWordCount(xliffMatchWordCount);
        pageWordCounts.setPoExactMatchWordCount(poMatchWordCount);
        // all repetitions
        pageWordCounts.setRepetitionWordCount(highFuzzyRepetionWordCount
                + medHighFuzzyRepetionWordCount + medFuzzyRepetitionWordCount
                + lowFuzzyRepetitionWordCount + noMatchRepetitionWordCount);
        // fuzzy and no match
        pageWordCounts.setHiFuzzyWordCount(highFuzzyWordCount);
        pageWordCounts.setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        pageWordCounts.setMedFuzzyWordCount(medFuzzyWordCount);
        pageWordCounts.setLowFuzzyWordCount(lowFuzzyWordCount);
        pageWordCounts.setNoMatchWordCount(noMatchWordCount);
        // threshold related
        pageWordCounts.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        pageWordCounts
                .setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        pageWordCounts.setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        pageWordCounts.setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        pageWordCounts.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);
        // MT related
        pageWordCounts.setMtTotalWordCount(mtExactMatchWordCount
                + mtFuzzyNoMatchWordCount + mtRepetitionsWordCount);
        pageWordCounts.setMtExactMatchWordCount(mtExactMatchWordCount);
        pageWordCounts.setMtFuzzyNoMatchWordCount(mtFuzzyNoMatchWordCount);
        pageWordCounts.setMtRepetitionsWordCount(mtRepetitionsWordCount);

        return pageWordCounts;
    }

    /**
     * Update word-counts for
     * "allExactMatchWordCounts(originally 'NoUseExactMatchWordCount')",
     * "Segment-TM word-counts","ICE".
     * 
     * No changed: Context-Match word-counts.
     * 
     * So if use ICE in TM profile: 100% is from SegmentTM, ICE is from ICE;
     * allExactMatchWordCounts = SegmentTM + ICE If use default context matches
     * in TM profile: Context Matches is from ContextMatches, 100% =
     * allExactMatchWordCounts - ContextMatches.
     */
    private static void updateExtraColumnWordCountsForTargetPage(
            TargetPage p_targetPage, ArrayList<BaseTmTuv> p_splittedSourceTuvs,
            MatchTypeStatistics p_matches, Vector<String> p_excludedTuTypes)
    {
        PageWordCounts pageWordCount = p_targetPage.getWordCount();
        // All 100% match word-count
        int totalExactMatchWordCount = pageWordCount.getSegmentTmWordCount()
                + pageWordCount.getContextMatchWordCount()
                + pageWordCount.getMtExactMatchWordCount()
                + pageWordCount.getXliffExtractMatchWordCount()
                + pageWordCount.getPoExactMatchWordCount();
        pageWordCount.setTotalExactMatchWordCount(totalExactMatchWordCount);
        pageWordCount.setNoUseInContextMatchWordCount(0);

        // Set ICE word-count
        int inContextMatchWordCount = onePageInContextMatchWordCounts(
                pageWordCount, p_splittedSourceTuvs, p_matches,
                p_excludedTuTypes,
                p_targetPage.getSourcePage().getCompanyId());
        pageWordCount.setInContextWordCount(inContextMatchWordCount);

        // Count "context-match word counts" into "segment-TM word counts"
        // In current implementation,context-match word counts always is 0.
        // Here "segment-TM" word counts means "100%" on UI(excluded ICE only).
        pageWordCount.setSegmentTmWordCount(totalExactMatchWordCount
                - inContextMatchWordCount);

        p_targetPage.setWordCount(pageWordCount);
    }

    /**
     * This method is used to update TUV table, and set repeated and repetition
     * flag.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void updateRepetitionInfoToTu(ArrayList<BaseTmTuv> sTuvs,
            MatchTypeStatistics p_matches, Map p_uniqueSegments,
            Map<Long, TuImpl> p_cachedTus, long p_companyId,
            long p_targetLocaleId)
    {
        Set<TuvImpl> repetitionTuvSet = new HashSet<TuvImpl>();
        Set<TuvImpl> unRepetitionTuvSet = new HashSet<TuvImpl>();
        for (int i = 0; i < sTuvs.size(); i++)
        {
            SegmentTmTuv curSrcTuv = (SegmentTmTuv) sTuvs.get(i);
            Types types = p_matches.getTypes(curSrcTuv.getId(),
                    ((SegmentTmTu) curSrcTuv.getTu()).getSubId());
            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            long tuId = curSrcTuv.getTu().getId();
            TuImpl currentTu = getTuFromCache(p_cachedTus, tuId,
                    p_companyId);
            TuvImpl curTrgTuv = (TuvImpl) currentTu.getTuv(
                    p_targetLocaleId, p_companyId);

            ArrayList<SegmentTmTuv> identicalSegments = null;
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    // If current target TUV has repetition flag, after
                    // "update leverage" and "update word counts" to get 100
                    // match, its REP flag should be removed.
                    curTrgTuv.setRepeated(false);
                    curTrgTuv.setRepetitionOfId(0);
                    unRepetitionTuvSet.add(curTrgTuv);
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                case MatchTypeStatistics.MED_FUZZY:
                case MatchTypeStatistics.MED_HI_FUZZY:
                case MatchTypeStatistics.HI_FUZZY:
                case MatchTypeStatistics.NO_MATCH:
                default:
                    // WorldServer XLF files are special,
                    // get repeated and repetition information from TU.
                    // Because the TU list is sorted, all repeated TUs are in
                    // front of the list
                    if ("worldserver".equalsIgnoreCase(currentTu.getGenerateFrom())
                            && "xlf".equalsIgnoreCase(currentTu.getDataType()))
                    {
                        if ("repeated".equalsIgnoreCase(currentTu.getSourceContent()))
                        {
                            curTrgTuv.setRepeated(true);
                            curTrgTuv.setRepetitionOfId(0);
                            p_uniqueSegments.put(curSrcTuv.getExactMatchKey(),
                                    curTrgTuv.getId());
                            repetitionTuvSet.add(curTrgTuv);
                        }
                        else if ("repetition".equalsIgnoreCase(currentTu
                                .getSourceContent()))
                        {
                            long repeatedTrgTuvId = 0;
                            Object obj = p_uniqueSegments.get(curSrcTuv.getExactMatchKey());
                            if (obj != null && obj instanceof Long)
                            {
                                repeatedTrgTuvId = (Long) obj;
                            }

                            /*
                             * Sometimes, World Server XLF files don't have
                             * repeated segments for repetition segments. In
                             * this case, we should treat the first repetition
                             * as repeated.
                             */
                            if (repeatedTrgTuvId == 0)
                            {
                                curTrgTuv.setRepeated(true);
                                curTrgTuv.setRepetitionOfId(0);
                                p_uniqueSegments.put(
                                        curSrcTuv.getExactMatchKey(),
                                        curTrgTuv.getId());
                                repetitionTuvSet.add(curTrgTuv);
                            }
                            else
                            {
                                curTrgTuv.setRepeated(false);
                                curTrgTuv.setRepetitionOfId(repeatedTrgTuvId);
                                repetitionTuvSet.add(curTrgTuv);
                            }
                        }
                    }
                    else
                    {
                        identicalSegments = (ArrayList) p_uniqueSegments
                                .get(curSrcTuv);
                        /*
                         * If identicalSegments is not null, that means current
                         * TU has a same segment before, then we should get the
                         * former segment, mark it as repeated, and mark the
                         * current segment as repetition.
                         * 
                         * If identicalSegments is null, that means it's the
                         * first time that current segment appears.
                         * 
                         * Considering files can be added and removed from jobs,
                         * all TUs must update.
                         */
                        SegmentTmTuv latestPreSrcTuv = null;
                        if (identicalSegments != null)
                        {
                            latestPreSrcTuv = (SegmentTmTuv) identicalSegments.get(0);
                        }
                        if (identicalSegments != null
                                && latestPreSrcTuv.getExactMatchKey() == curSrcTuv
                                        .getExactMatchKey()
                                && isFullSegmentRepitition(currentTu,
                                        curSrcTuv, latestPreSrcTuv,
                                        p_companyId))
                        {
                            TuvImpl preTrgTuv = null;
                            try
                            {
                                long preTuId = latestPreSrcTuv.getTu().getId();
                                TuImpl preTu = SegmentTuUtil.getTuById(preTuId,
                                        p_companyId);
                                preTrgTuv = (TuvImpl) preTu.getTuv(
                                        p_targetLocaleId, p_companyId);
                            }
                            catch (Exception e)
                            {
                                c_logger.error(e.getMessage(), e);
                            }

                            // Set "preTrgTuv" as repeated.
                            preTrgTuv.setRepeated(true);
                            preTrgTuv.setRepetitionOfId(0);
                            // Remove "preTrgTuv" from unRepetitionSet
                            unRepetitionTuvSet.remove(preTrgTuv);
                            // Add "preTrgTuv" to repetitionSet
                            repetitionTuvSet.add(preTrgTuv);
                            // Set "currentTargetTuv" as repetition.
                            curTrgTuv.setRepetitionOfId(preTrgTuv.getId());
                            curTrgTuv.setRepeated(false);
                            // Add "currentTargetTuv" to repetitionSet too.
                            repetitionTuvSet.add(curTrgTuv);
                        }
                        else
                        {
                            if (!repetitionTuvSet.contains(curTrgTuv))
                            {
                                // Add the target TUV into unRepetitionSet
                                curTrgTuv.setRepetitionOfId(0);
                                curTrgTuv.setRepeated(false);
                                unRepetitionTuvSet.add(curTrgTuv);

                                // Record this "srcTuv" into identicalSegments
                                identicalSegments = new ArrayList<SegmentTmTuv>();
                                identicalSegments.add(curSrcTuv);
                                p_uniqueSegments.put(curSrcTuv, identicalSegments);
                            }
                        }
                    }
                    break;
            }
        }

        try
        {
            if (unRepetitionTuvSet.size() != 0)
            {
                SegmentTuvUtil.updateTuvs(new ArrayList<TuvImpl>(
                        unRepetitionTuvSet), p_companyId);
            }
            if (repetitionTuvSet.size() != 0)
            {
                SegmentTuvUtil.updateTuvs(new ArrayList<TuvImpl>(
                        repetitionTuvSet), p_companyId);
            }
        }
        catch (Exception e)
        {
            c_logger.error(e);
        }
    }

    private static TuImpl getTuFromCache(Map<Long, TuImpl> p_cachedTus,
            long tuId, long p_companyId)
    {
        TuImpl tu = null;

        if (p_cachedTus != null && p_cachedTus.size() > 0)
        {
            tu = p_cachedTus.get(tuId);
        }
        // Ensure TU is returned, this will not be executed ideally.
        if (tu == null)
        {
            try
            {
                tu = SegmentTuUtil.getTuById(tuId, p_companyId);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }
        }

        return tu;
    }
    
    /**
     * Compare if current TUV has the same exact match key (white space ignored)
     * with that from latest previous TUV.
     * 
     * Only when the full segment (including sub segments) has the same
     * exactMatchKey, take it as repetition.
     */
    @SuppressWarnings("rawtypes")
    private static boolean isFullSegmentRepitition(TuImpl currentTu,
            SegmentTmTuv currentSrcTuv, SegmentTmTuv latestPreSrcTuv,
            long companyId)
    {
        // If current TUV has no sub segments, no need continue to check, return
        // true;
        Tuv tuv1 = currentTu.getTuv(currentSrcTuv.getLocale().getId(), companyId);
        List subEle = tuv1.getSubflowsAsGxmlElements();
        if (subEle == null || subEle.size() == 0)
        {
            return true;
        }

        // Compare 2 TuvImpl objects exact match key.
        long currentTuvExactMatchKey = tuv1.getExactMatchKey();
        long latestTuvExactMatchkey = 0;
        try
        {
            TuImpl tu2 = SegmentTuUtil.getTuById(latestPreSrcTuv.getTu()
                    .getId(), companyId);
            Tuv tuv2 = tu2.getTuv(latestPreSrcTuv.getLocale().getId(),
                    companyId);
            latestTuvExactMatchkey = tuv2.getExactMatchKey();
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }

        return (currentTuvExactMatchKey == latestTuvExactMatchkey);
    }

    /**
     * Compute the in context match in one page
     * 
     * @param wordCount
     *            The page word count
     * @param splitSourceTuvs
     *            The source tuvs in one workflow
     * @param matches
     *            The tm matches in the TM
     * @return The in context match
     */
    private static int onePageInContextMatchWordCounts(
            PageWordCounts wordCount, ArrayList<BaseTmTuv> splitSourceTuvs,
            MatchTypeStatistics matches, Vector<String> p_excludedTuTypes,
            long companyId)
    {
        int inContextMatchWordCount = 0;

        for (int i = 0, max = splitSourceTuvs.size(); i < max; i++)
        {
            if (LeverageUtil.isIncontextMatch(i, splitSourceTuvs, null,
                    matches, p_excludedTuTypes, companyId))
            {
                inContextMatchWordCount += ((SegmentTmTuv) splitSourceTuvs
                        .get(i)).getWordCount();
            }
        }

        return inContextMatchWordCount;
    }

    private static boolean isMtTranslation(Types types)
    {
        if (types == null)
            return false;

        return types.isMtTranslation();
    }

    /**
     * Takes a collection of original source Tuvs (type Tuv) and splits
     * subflows. Returns a collection of SegmentTmTuv.
     */
    @SuppressWarnings("unchecked")
    static public ArrayList<BaseTmTuv> splitSourceTuvs(
            ArrayList<Tuv> p_sourceTuvs, GlobalSightLocale p_sourceLocale,
            long companyId) throws Exception
    {
        // sort the list first, put all tuvs whose source content equals
        // "repeated" in front of the list. it will affect worldserver xlf
        // files, other files will not be impacted.
        Collections.sort(p_sourceTuvs,
                new TuvSourceContentComparator(companyId));
        // convert Tu, Tuv to PageTmTu, PageTmTuv to split segments
        ArrayList<PageTmTu> pageTmTuList = new ArrayList<PageTmTu>(
                p_sourceTuvs.size());

        for (int i = 0; i < p_sourceTuvs.size(); i++)
        {
            Tuv originalTuv = (Tuv) p_sourceTuvs.get(i);
            Tu originalTu = originalTuv.getTu(companyId);

            PageTmTu pageTmTu = new PageTmTu(originalTu.getId(), 0,
                    originalTu.getDataType(), originalTu.getTuType(),
                    !originalTu.isLocalizable());
            pageTmTu.setSourceContent(originalTu.getSourceContent());
            PageTmTuv pageTmTuv = new PageTmTuv(originalTuv.getId(),
                    originalTuv.getGxml(), p_sourceLocale);
            pageTmTuv.setSid(originalTuv.getSid());

            pageTmTu.addTuv(pageTmTuv);

            pageTmTuList.add(pageTmTu);
        }
        // make a list of splitted segment Tus
        ArrayList<SegmentTmTu> splittedTus = new ArrayList<SegmentTmTu>(
                pageTmTuList.size());

        for (PageTmTu pageTmTu : pageTmTuList)
        {
            Collection<SegmentTmTu> segmentTmTus = TmUtil.createSegmentTmTus(
                    pageTmTu, p_sourceLocale);
            splittedTus.addAll(segmentTmTus);
        }

        // make a list of SegmentTmTuv from a list of SegmentTmTu
        ArrayList<BaseTmTuv> splittedTuvs = new ArrayList<BaseTmTuv>(
                splittedTus.size());
        for (SegmentTmTu tu : splittedTus)
        {
            splittedTuvs.add(tu.getFirstTuv(p_sourceLocale));
        }

        return splittedTuvs;
    }

    static private LeverageMatchLingManager getLeverageMatchLingManager()
            throws StatisticsException
    {
        LeverageMatchLingManager result = null;

        try
        {
            result = LingServerProxy.getLeverageMatchLingManager();
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find the LeverageMatchLingManager", e);

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_FIND_LEVERAGE_MATCH_LING_MANAGER,
                    null, e);
        }

        return result;
    }

    private static boolean isDefaultContextMatch(String sourcePageId,
            SourcePage page)
    {
        String localeStr = sourcePageId.substring(0,
                sourcePageId.indexOf(File.separator));

        String temp1 = sourcePageId.substring(localeStr.length()
                + File.separator.length());

        String isWebservice = temp1.substring(0, temp1.indexOf(File.separator))
                + "";

        if (isWebservice.indexOf("webservice") > -1)
        {
            temp1 = sourcePageId.substring(localeStr.length()
                    + File.separator.length() + "webservice".length()
                    + File.separator.length());
        }

        String temp2 = temp1.substring(temp1.indexOf(File.separator));
        String queryStr = localeStr + "%" + temp2;

        if (!File.separator.equals("/"))
        {
            queryStr = queryStr.replace("\\", "\\\\\\\\");
        }
        queryStr = queryStr.replace("'", "\\'");
        String sql = "select id from source_page where external_page_id LIKE '"
                + queryStr + "' and state not in ('OUT_OF_DATE','IMPORT_FAIL')";
        List list = HibernateUtil.searchWithSql(sql, null);
        list = removeCurrent(list, page.getId());
        for (int i = 0; i < list.size(); i++)
        {
            SourcePage sp = null;
            // All TUs and TUVs of the other source page will be loaded by
            // isSameOfSourcePage, bloating the Hibernate (first-level) cache.
            // Load them in a new session so we can free them by closing the
            // session after isSameOfSourcePage finishes.
            Session newSession = HibernateUtil.getSessionFactory()
                    .openSession();
            try
            {
                long otherSourcePageId = ((BigInteger) list.get(i)).longValue();
                sp = (SourcePage) newSession.get(SourcePage.class,
                        otherSourcePageId);
                if (isSameOfSourcePage(page, sp))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                c_logger.info("Can not get source page to compare" + e);
            }
            finally
            {
                newSession.close();
            }
        }
        return false;
    }

    private static List removeCurrent(List list, long id)
    {
        if (list == null || list.size() == 0)
        {
            return new ArrayList();
        }
        for (int i = 0; i < list.size(); i++)
        {
            long idInList = ((BigInteger) list.get(i)).longValue();
            if (id == idInList)
            {
                list.remove(i);
            }
        }
        return list;
    }

    private static boolean isSameOfSourcePage(SourcePage source,
            SourcePage target)
    {
        boolean flag = false;
        if (source == null || target == null)
        {
            return flag;
        }
        long companyId = source.getCompanyId();

        ArrayList<BaseTmTuv> sourceTuvs = new ArrayList<BaseTmTuv>();
        ArrayList<BaseTmTuv> targetTuvs = new ArrayList<BaseTmTuv>();
        try
        {
            // As here don't need XliffAlt data, don't load them to improve
            // performance.
            boolean needLoadExtraInfo = false;
            ArrayList<Tuv> sourceTuvsTmp = SegmentTuvUtil.getSourceTuvs(source,
                    needLoadExtraInfo);
            sourceTuvs = splitSourceTuvs(sourceTuvsTmp,
                    source.getGlobalSightLocale(), companyId);
            ArrayList<Tuv> targetTuvsTmp = SegmentTuvUtil.getSourceTuvs(target,
                    needLoadExtraInfo);
            targetTuvs = splitSourceTuvs(targetTuvsTmp,
                    target.getGlobalSightLocale(), companyId);
        }
        catch (Exception e)
        {
            c_logger.info("Can not get Tuvs to compare" + e);
        }
        if (sourceTuvs.size() != targetTuvs.size())
        {
            return flag;
        }

        int i;
        for (i = 0; i < sourceTuvs.size(); i++)
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) sourceTuvs.get(i);
            SegmentTmTuv targetTuv = (SegmentTmTuv) targetTuvs.get(i);
            if (sourceTuv.getExactMatchKey() != targetTuv.getExactMatchKey())
            {
                flag = false;
                break;
            }
        }
        if (i == sourceTuvs.size())
        {
            flag = true;
        }
        return flag;
    }

    /**
     * Get cached TUs in map<TuID,TuImpl> to improve performance.
     */
    private static Map<Long, TuImpl> getTusMapBySourcePage(
            SourcePage p_sourcePage) throws Exception
    {
        Map<Long, TuImpl> result = new HashMap<Long, TuImpl>();

        List<TuImpl> tus = SegmentTuUtil.getTusBySourcePageId(p_sourcePage
                .getId());
        for (TuImpl tu : tus)
        {
            result.put(tu.getIdAsLong(), tu);
        }

        return result;
    }

    /**
     * Calculates the statistics for the workflows passed in. Adds the word
     * count to the workflows and commits to the database.
     * 
     * @param p_workflows
     *            - a List of workflows of which the statistics are calculated.
     *            All workflows must belong to the same job.
     */
    static public void calculateWorkflowStatistics(List<Workflow> p_workflows,
            Vector<String> p_excludedTuTypes) throws StatisticsException
    {
        if (p_workflows == null || p_workflows.size() < 1)
        {
            return;
        }

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // calculate statistics per work flow
            for (Workflow wf : p_workflows)
            {
                wf = (WorkflowImpl) session.get(WorkflowImpl.class,
                        wf.getIdAsLong());
                PageWordCounts wfWordCounts = sumTargetPageWordCount(wf);

                wf.setTotalWordCount(wfWordCounts.getTotalWordCount());

                wf.setRepetitionWordCount(wfWordCounts.getRepetitionWordCount());

                wf.setTotalExactMatchWordCount(wfWordCounts
                        .getTotalExactMatchWordCount());
                wf.setContextMatchWordCount(wfWordCounts
                        .getContextMatchWordCount());
                wf.setSegmentTmWordCount(wfWordCounts.getSegmentTmWordCount());
                wf.setInContextMatchWordCount(wfWordCounts.getInContextWordCount());
                wf.setMtExactMatchWordCount(wfWordCounts
                        .getMtExactMatchWordCount());
                wf.setNoUseInContextMatchWordCount(wfWordCounts
                        .getNoUseInContextMatchWordCount());

                wf.setNoMatchWordCount(wfWordCounts.getNoMatchWordCount());
                wf.setLowFuzzyMatchWordCount(wfWordCounts.getLowFuzzyWordCount());
                wf.setMedFuzzyMatchWordCount(wfWordCounts.getMedFuzzyWordCount());
                wf.setMedHiFuzzyMatchWordCount(wfWordCounts
                        .getMedHiFuzzyWordCount());
                wf.setHiFuzzyMatchWordCount(wfWordCounts.getHiFuzzyWordCount());

                wf.setThresholdHiFuzzyWordCount(wfWordCounts
                        .getThresholdHiFuzzyWordCount());
                wf.setThresholdLowFuzzyWordCount(wfWordCounts
                        .getThresholdLowFuzzyWordCount());
                wf.setThresholdMedFuzzyWordCount(wfWordCounts
                        .getThresholdMedFuzzyWordCount());
                wf.setThresholdMedHiFuzzyWordCount(wfWordCounts
                        .getThresholdMedHiFuzzyWordCount());
                wf.setThresholdNoMatchWordCount(wfWordCounts
                        .getThresholdNoMatchWordCount());

                session.update(wf);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args = new String[1];
            Workflow wf = (Workflow) p_workflows.get(0);
            String jobName = wf.getJob().getJobName();
            args[0] = jobName;

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_GENERATE_STATISTICS_WORKFLOW,
                    args, e);
        }
    }

    /**
     * Sum all target pages' word counts to get workflow word counts.The
     * workflow's word counts should be based on its target pages' word counts.
     * 
     * @param workflow
     * @return
     */
    private static PageWordCounts sumTargetPageWordCount(Workflow workflow)
    {
        if (workflow == null) return null;

        int totalWordCount = 0;

        int totalExactMatchWordCount = 0;
        int inContextMatchWordCount = 0;
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int mtExactMatchWordCount = 0;
        int noUseInContextMatchWordCount = 0;
        
        int totalRepetitionWoreCount = 0;

        // below 50%
        int noMatchWordCount = 0;
        // 50%--75%
        int lowFuzzyWordCount = 0;
        // 75%--84%
        int medFuzzyWordCount = 0;
        // 85%--94%
        int medHighFuzzyWordCount = 0;
        // 95%--99%
        int highFuzzyWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        PageWordCounts pageWordCounts = new PageWordCounts();
        List<TargetPage> targetPages = getAllTargetPagesForWorkflow(workflow);
        for (TargetPage targetPage : targetPages)
        {
            PageWordCounts tpWordCount = targetPage.getWordCount();
            if (tpWordCount != null)
            {
                totalWordCount += tpWordCount.getTotalWordCount();

                totalExactMatchWordCount += tpWordCount
                        .getTotalExactMatchWordCount();
                inContextMatchWordCount += tpWordCount.getInContextWordCount();
                contextMatchWordCount += tpWordCount.getContextMatchWordCount();
                segmentTmWordCount += tpWordCount.getSegmentTmWordCount();
                mtExactMatchWordCount += tpWordCount.getMtExactMatchWordCount();
                noUseInContextMatchWordCount += tpWordCount
                        .getNoUseInContextMatchWordCount();

                totalRepetitionWoreCount += tpWordCount
                        .getRepetitionWordCount();

                noMatchWordCount += tpWordCount.getNoMatchWordCount();
                lowFuzzyWordCount += tpWordCount.getLowFuzzyWordCount();
                medFuzzyWordCount += tpWordCount.getMedFuzzyWordCount();
                medHighFuzzyWordCount += tpWordCount.getMedHiFuzzyWordCount();
                highFuzzyWordCount += tpWordCount.getHiFuzzyWordCount();

                thresholdHiFuzzyWordCount += tpWordCount
                        .getThresholdHiFuzzyWordCount();
                thresholdMedHiFuzzyWordCount += tpWordCount
                        .getThresholdMedHiFuzzyWordCount();
                thresholdMedFuzzyWordCount += tpWordCount
                        .getThresholdMedFuzzyWordCount();
                thresholdLowFuzzyWordCount += tpWordCount
                        .getThresholdLowFuzzyWordCount();
                thresholdNoMatchWordCount += tpWordCount
                        .getThresholdNoMatchWordCount();
            }
        }

        // total word count
        pageWordCounts.setTotalWordCount(totalWordCount);
        // all 100% cases
        pageWordCounts.setTotalExactMatchWordCount(totalExactMatchWordCount);
        pageWordCounts.setInContextWordCount(inContextMatchWordCount);
        pageWordCounts.setContextMatchWordCount(contextMatchWordCount);
        pageWordCounts.setSegmentTmWordCount(segmentTmWordCount);
        pageWordCounts.setMtExactMatchWordCount(mtExactMatchWordCount);
        pageWordCounts.setNoUseInContextMatchWordCount(noUseInContextMatchWordCount);
        // all repetitions
        pageWordCounts.setRepetitionWordCount(totalRepetitionWoreCount);
        // fuzzy and no match
        pageWordCounts.setHiFuzzyWordCount(highFuzzyWordCount);
        pageWordCounts.setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        pageWordCounts.setMedFuzzyWordCount(medFuzzyWordCount);
        pageWordCounts.setLowFuzzyWordCount(lowFuzzyWordCount);
        pageWordCounts.setNoMatchWordCount(noMatchWordCount);
        // threshold related
        pageWordCounts.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        pageWordCounts
                .setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        pageWordCounts
                .setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        pageWordCounts
                .setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        pageWordCounts.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);

        return pageWordCounts;
    }

    
    /**
     * Calculate word-counts for all target pages and workflows in current job.
     * 
     * @param job
     */
    public static void calculateWordCountsForJob(Job job)
    {
        c_logger.info("Start calculating word counts for job " + job.getId());
        // calculateWorkflowStatistics() commits statistics to DB
        StatisticsService.calculateWorkflowStatistics(
                new ArrayList(job.getWorkflows()), job.getL10nProfile()
                        .getTranslationMemoryProfile().getJobExcludeTuTypes());

        for (Workflow workflow : job.getWorkflows())
        {
            StatisticsService.calculateTargetPagesWordCount(workflow, job
                    .getL10nProfile().getTranslationMemoryProfile()
                    .getJobExcludeTuTypes());
        }
        c_logger.info("Done calculating word counts for job " + job.getId());
    }
}