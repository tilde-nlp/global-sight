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

package com.globalsight.everest.edit.offline.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * Primarily a data class which holds all the data for a single offline segment
 * entry. The entry item can either be a parent segment or a sub segment of a
 * parent segment. Both are treated as plain segments.
 * 
 * NOTE: This class is currently shared by both download and upload. As such,
 * some methods only apply to upload and some to download.
 */
public class OfflineSegmentData implements Serializable
{
    private static final long serialVersionUID = 5007464538350680545L;

    static private final Logger CATEGORY = Logger.getLogger(OfflineSegmentData.class);

    /**
     * A back reference to the OfflinePageData that holds this object.
     */
    private OfflinePageData m_opd = null;

    // values for a source entry (parent or subflow)
    private String m_displaySegmentId;
    // only valid if this is a subflow
    private String m_displayParentOfSubTagName;
    private StringBuffer m_displaySourceText;
    private String m_displaySegmentFormat;
    private String m_segmentType;
    private Hashtable m_ptag2NativeMap;
    private boolean m_targetHasBeenEdited = false;
    private boolean m_tagCheckSuccesful = true;
    private boolean m_hasTerminology = false;
    private boolean m_hasTMMatches = false;
    private boolean m_hasMTMatches = false;
    // GBS-3776
    private boolean m_hasRefTmsTMMatches = false;
    private boolean m_hasRefTmsMTMatches = false;

    // values for a target entry (parent or subflow)
    private Long m_trgTuvId = null;
    private StringBuffer m_displayTargetText;
    private boolean m_isDisplayAsProtected;
    private boolean protectedChangeable = true;
    /** Match score of the inserted text. */
    private float m_matchScore;
    private String m_displayMatchType = null;
    private int m_matchTypeId = AmbassadorDwUpConstants.MATCH_TYPE_UNDEFINED;
    private boolean m_touched = false;
    private boolean m_isCopyOfSource = false;
    private boolean m_isLastSegment = false;
    private boolean m_isTuStartOfNewPara = false;
    private boolean m_refFound = false;
    private String displayPageName = null;
    private long pageId = -1;

    // list of trailing segments that are merged to this - the first segment.
    private ArrayList m_mergedSegIds = null;

    // Fuzzy matches -the original list of fuzzy LeverageMatches
    private List m_originalFuzzyLeverageMatchList = null;
    private List m_originalFuzzyLeverageMatchRefTmsList = null;
    private List notCountTags;

    /**
     * A list of fuzzy ptag strings derived from the original leverage match
     * list. This list is generated by the PtagPageGenerator class during
     * download.
     */
    private List m_displayFuzzyList = null;

    /** Term matches found in one or more termbase repositories. */
    private List m_termList = null;

    // For downloading tmx file
    private Tuv m_targetTuv = null;

    // For in-context match
    private Tuv m_sourceTuv = null;

    // For XLF/OmegaT TK, if target "state" attribute is "translated"...
    private boolean isStateTranslated = false;

    //
    // Constructors
    //
    public OfflineSegmentData()
    {
        this("", "", "", "", "", -1, "", AmbassadorDwUpConstants.MATCH_TYPE_UNDEFINED, null, null,
                false, null);
    }

    /**
     * Constructor.
     * 
     * @param p_id
     *            the segment or subflow id
     */
    public OfflineSegmentData(String p_id)
    {
        this(p_id, "", "", "", "", -1, "", AmbassadorDwUpConstants.MATCH_TYPE_UNDEFINED, null, null,
                false, null);
    }

    /**
     * Constructor.
     * 
     * @param p_id
     *            the unique segment id [parent]:(optional [ptagId]:[subId])
     * @param p_segFmt
     *            the native data type (page format)
     * @param p_segType
     *            the segment type (item type)
     * @param p_srcText
     *            the source text
     * @param p_trgText
     *            the target text
     * @param p_matchScore
     *            the leverage match score for the inserted text
     * @param p_matchType
     *            a UI display string for the leverage match type
     * @param p_matchTypeId
     *            leverage match type identifier, should be one of
     *            AmbassadorDwUpConstants: MATCH_TYPE_EXACT, MATCH_TYPE_FUZZY,
     *            MATCH_TYPE_NOMATCH
     * @param p_fuzzyList
     *            the fuzzy matches
     * @param p_isProtected
     *            if true the segment is "presented" as protected to the user.
     *            If false, it "appears" unprotected.
     */
    public OfflineSegmentData(String p_id, String p_segFmt, String p_segType, String p_srcText,
            String p_trgText, float p_matchScore, String p_matchType, int p_matchTypeId,
            List p_fuzzyList, List p_fuzzyRefTmsList, boolean p_isProtected, List p_termList)
    {
        super();

        // source
        m_displaySegmentId = p_id;
        m_displaySegmentFormat = p_segFmt.trim();
        m_segmentType = p_segType.trim();
        m_displaySourceText = new StringBuffer(p_srcText);
        m_ptag2NativeMap = new Hashtable();

        // target
        m_displayTargetText = new StringBuffer(p_trgText);
        // score for the inserted text
        m_matchScore = p_matchScore;
        m_displayMatchType = p_matchType.trim();
        m_matchTypeId = p_matchTypeId;
        m_isDisplayAsProtected = p_isProtected;

        // TM
        setOriginalFuzzyLevergeMatchList(p_fuzzyList);
        setOriginalFuzzyLevergeMatchRefTmsList(p_fuzzyRefTmsList);

        // Terms
        setTerminologyList(p_termList);
    }

    //
    // Public Methods
    //
    protected void setBackReference(OfflinePageData p_opd)
    {
        m_opd = p_opd;
    }

    public Issue getSegmentIssue()
    {
        Issue result = null;

        if (m_opd != null)
        {
            // RTF Para view: 1_2, RTF List view: 1:[f1]:2
            String[] tmp = m_displaySegmentId.split("[_:]");
            String tuId = tmp[0];
            String subId = "0";
            if (tmp.length == 3)
            {
                subId = tmp[2];
            }
            else if (tmp.length == 2)
            {
                subId = tmp[1];
            }

            String key = tuId + CommentHelper.SEPARATOR + subId;

            HashMap map = m_opd.getIssuesMap();
            result = (Issue) map.get(key);

            if (CATEGORY.isDebugEnabled())
            {
                System.out.println("Looking up issue " + key + " (dispid=" + m_displaySegmentId
                        + ") " + (result != null ? "found" : "not found"));
            }
        }

        return result;
    }

    /**
     * Sets the trg TuvId that the target is associated with.
     * 
     * @param p_id
     *            the target tuv Id
     */
    protected void setTrgTuvId(Long p_id)
    {
        m_trgTuvId = p_id;
    }

    /**
     * Gets the trg TuvId that the target is associated with.
     * 
     * @return Returns the target tuv Id as a Long
     */
    public Long getTrgTuvId()
    {
        return m_trgTuvId;
    }

    public void setTargetTuv(Tuv p_target)
    {
        m_targetTuv = p_target;
    }

    public Tuv getTargetTuv()
    {
        return m_targetTuv;
    }

    /**
     * Sets the new segment ID. Note, this method sould only be called by
     * OfflinePageData. To rename a segment call
     * OfflinePageData.changeDisplayId().
     * 
     * @param p_newID
     *            java.lang.String
     */
    protected void setDisplayID(String p_newID)
    {
        m_displaySegmentId = p_newID;
    }

    /**
     * Sets the Parent Of Sub Tag Name.
     * 
     * @param p_tag
     *            java.lang.String
     */
    public void setDisplayParentOfSubTagName(String p_tag)
    {
        m_displayParentOfSubTagName = p_tag;
    }

    /**
     * Sets the segment format.
     * 
     * @param p_newSegmentFormat
     *            java.lang.String
     */
    public void setDisplaySegmentFormat(String p_newSegmentFormat)
    {
        m_displaySegmentFormat = p_newSegmentFormat.trim();
    }

    /**
     * Sets the segment type.
     * 
     * @param p_newSegmentType
     *            java.lang.String
     */
    public void setSegmentType(String p_newSegmentType)
    {
        m_segmentType = p_newSegmentType.trim();
    }

    /**
     * Sets the match value.
     */
    public void setMatchValue(float p_matchValue)
    {
        m_matchScore = p_matchValue;
    }

    /**
     * Sets the display match type string.
     */
    public void setDisplayMatchType(String p_displayMatchType)
    {
        m_displayMatchType = p_displayMatchType.trim();
    }

    /**
     * Sets the general match type ID.
     * 
     * @param p_matchTypeId
     *            must be one of the following AmbasadorDwUpConstants:
     *            MATCH_TYPE_EXACT, MATCH_TYPE_FUZZY, MATCH_TYPE NOMATCH
     */
    public void setMatchType(int p_matchTypeId)
    {
        m_matchTypeId = p_matchTypeId;
    }

    /**
     * Sets the touched state. This should only be set according to the return
     * value of tuv.isLocalized()
     */
    public void setTouched(Tuv p_trgTuv)
    {
        m_touched = p_trgTuv.isLocalized();
    }

    /**
     * Sets the original fuzzy leverage match list from selected reference tms.
     * 
     * @since GBS-3776
     */
    public void setOriginalFuzzyLevergeMatchRefTmsList(List p_list)
    {
        m_originalFuzzyLeverageMatchRefTmsList = p_list;
        if (p_list != null && p_list.size() > 0)
        {
            for (int i = 0; i < p_list.size(); i++)
            {
                LeverageMatch lm = (LeverageMatch) p_list.get(i);
                if (!StringUtil.isEmpty(lm.getMtName()))
                {
                    m_hasRefTmsMTMatches = true;
                }
                else
                {
                    m_hasRefTmsTMMatches = true;
                }
                // break for performance.
                if (m_hasRefTmsTMMatches && m_hasRefTmsMTMatches)
                    break;
            }
        }
    }

    /**
     * Sets the original fuzzy leverage match list.
     * 
     * @param p_originalFuzzyMatchList
     *            list of LeveregeMatches to be used as fuzzy matches.
     */
    public void setOriginalFuzzyLevergeMatchList(List p_list)
    {
        m_originalFuzzyLeverageMatchList = p_list;
        if (p_list != null && p_list.size() > 0)
        {
            for (int i = 0; i < p_list.size(); i++)
            {
                LeverageMatch lm = (LeverageMatch) p_list.get(i);
                if (!StringUtil.isEmpty(lm.getMtName()))
                {
                    m_hasMTMatches = true;
                }
                else
                {
                    m_hasTMMatches = true;
                }
                // break for performance.
                if (m_hasTMMatches && m_hasMTMatches)
                    break;
            }
        }
    }

    /**
     * Sets the list of fuzzy ptag match strings.
     * 
     * NOTE: During download, the PtagPageGenerator class converts the original
     * gxml fuzzy matches to ptagged version and then sets them using this
     * method. The type of ptag (compact/verbose) is determined by the download
     * parameters.
     * 
     * @param p_ptagFuzzyMatchList
     *            list of ptag strings.
     */
    public void setDisplayFuzzyList(List p_list)
    {
        m_displayFuzzyList = p_list;
    }

    /**
     * Sets the list of terms found in the termbase repository.
     * 
     * @param p_list
     *            term matches for the segment.
     */
    public void setTerminologyList(List p_list)
    {
        m_termList = p_list;
        m_hasTerminology = (p_list != null && p_list.size() > 0);
    }

    /**
     * Get the list of leverge term matches for the segment.
     * 
     * @return list of TermLeverageMatchResult objects.
     */
    public List getTermLeverageMatchList()
    {
        return m_termList;
    }

    /**
     * Sets the list of segment IDs that are merged under segment.
     */
    public void setMergedIds(ArrayList p_displayMergedSegIds)
    {
        m_mergedSegIds = p_displayMergedSegIds;
    }

    /**
     * Sets the source text.
     * 
     * @param newSourceText
     *            java.lang.String
     */
    public void setDisplaySourceText(String p_newSourceText)
    {
        m_displaySourceText = new StringBuffer(p_newSourceText);
    }

    /**
     * Appends the source text.
     * 
     * @param moreSourceText
     *            java.lang.String
     */
    public void appendDisplaySourceText(String p_moreSourceText)
    {
        m_displaySourceText.append(p_moreSourceText);
    }

    /**
     * Sets the target text.
     * 
     * @param newTargetText
     *            java.lang.String
     */
    public void setDisplayTargetText(String p_newTargetText)
    {
        m_displayTargetText = new StringBuffer(p_newTargetText);
    }

    /**
     * Appends the target text.
     * 
     * This method is used in conjunction with OfflinePageData to store a
     * tokenized segment. The offline file parser used by OfflinePageData's load
     * methods tokenizes segments (into linebreaks and text) as they are read
     * from an upload file. Line breaks are processed separately and then
     * appended with this method.
     * 
     * @param moreTargetText
     *            java.lang.String
     */
    public void appendDisplayTargetText(String p_moreTargetText)
    {
        m_displayTargetText.append(p_moreTargetText);
    }

    /**
     * Sets the PTag to native hashtable.
     * 
     * @param p_Map
     *            - the map as a {PTag - Native} hash.
     */
    public void setPTag2NativeMap(Hashtable p_Map)
    {
        m_ptag2NativeMap = p_Map;
    }

    /**
     * Sets the protection display style for the segment
     * 
     * @param p_protected
     *            when true the segment is displayed as protected.
     */
    public void setWriteAsProtectedSegment(boolean p_protected)
    {
        m_isDisplayAsProtected = p_protected;
    }

    public boolean isProtectedChangeable()
    {
        return protectedChangeable;
    }

    public void setProtectedChangeable(boolean protectedChangeable)
    {
        this.protectedChangeable = protectedChangeable;
    }

    /**
     * Used to record whether the ptag form of the uploaded target segment has
     * changed between the download and upload. Set during offline error
     * checking.
     * 
     * @param p_changed
     *            true means the segment has been altered offline.
     */
    public void setTargetHasBeenEdited(boolean p_changed)
    {
        m_targetHasBeenEdited = p_changed;
    }

    public void setCopyOfSource(boolean p_isCopy)
    {
        m_isCopyOfSource = p_isCopy;
    }

    public void setLastSegment(boolean p_isLastSegment)
    {
        m_isLastSegment = p_isLastSegment;
    }

    public void setIsStartOfNewPara(boolean p_isStartOfNewPara)
    {
        m_isTuStartOfNewPara = p_isStartOfNewPara;
    }

    public boolean isCopyOfSource()
    {
        return m_isCopyOfSource;
    }

    public boolean isLastSegment()
    {
        return m_isLastSegment;
    }

    public boolean isStartOfNewPara()
    {
        return (m_isTuStartOfNewPara || isSubflowSegment());
    }

    public boolean isMerged()
    {
        // Note: the merged id list always contains the parent id even
        // if not merged. See pageSegments.
        return (m_mergedSegIds != null && m_mergedSegIds.size() > 1);
    }

    /**
     * Get the list of segment IDs that are merged under segment.
     */
    public ArrayList getMergedIds()
    {
        return m_mergedSegIds;
    }

    /**
     * Returns the segment ID string.
     * 
     * @return java.lang.String
     */
    public String getDisplaySegmentID()
    {
        return m_displaySegmentId.trim();
    }

    /**
     * Returns the segment format ID.
     * 
     * @return java.lang.String
     */
    public String getDisplaySegmentFormat()
    {
        return m_displaySegmentFormat;
    }

    /**
     * Returns the segment type.
     * 
     * @return java.lang.String
     */
    public String getSegmentType()
    {
        return m_segmentType;
    }

    /**
     * Returns the match value.
     */
    public float getMatchValue()
    {
        return m_matchScore;
    }

    /**
     * Gets the match type indicator string for display in the offline files.
     * The proper string is determined from the score + protection.
     * 
     * @return the indicator display string
     */
    public String getDisplayMatchType()
    {
        return m_displayMatchType;
    }

    /**
     * Gets general match type id.
     * 
     * @return one of the following AmbasadorDwUpConstants: MATCH_TYPE_EXACT,
     *         MATCH_TYPE_FUZZY, MATCH_TYPE NOMATCH
     */
    public int getMatchTypeId()
    {
        return m_matchTypeId;
    }

    /**
     * Gets the touched state.
     */
    public boolean isTouched()
    {
        return m_touched;
    }

    /**
     * Gets the original list of fuzzy LeverageMatches.
     */
    public List getOriginalFuzzyLeverageMatchList()
    {
        return m_originalFuzzyLeverageMatchList;
    }

    /**
     * Gets the original list of fuzzy LeverageMatches from selected reference
     * tms.
     * 
     * @since GBS-3776
     */
    public List getOriginalFuzzyLeverageMatchRefTmsList()
    {
        return m_originalFuzzyLeverageMatchRefTmsList;
    }

    /**
     * Gets the list of ptag fuzzy match strings.
     */
    public List getDisplayFuzzyMatchList()
    {
        return m_displayFuzzyList;
    }

    /**
     * Returns the Source text.
     * 
     * @return java.lang.String
     */
    public String getDisplaySourceText()
    {
        return m_displaySourceText.toString();
    }

    /**
     * Returns the Source text with lineFeeds converted.
     * 
     * @param p_newLinebreak
     *            a string representing the desired line break
     * @return java.lang.String
     */
    public String getDisplaySourceTextWithNewLinebreaks(String p_newLineBreak)
    {
        return convertLineBreaks(m_displaySourceText.toString(), p_newLineBreak);
    }

    /**
     * Returns the target text.
     * 
     * @return java.lang.String
     */
    public String getDisplayTargetText()
    {
        return m_displayTargetText.toString();
    }

    /**
     * Returns the Target text with lineFeeds converted.
     * 
     * @param p_newLinebreak
     *            a string representing the desired line break
     * @return java.lang.String
     */
    public String getDisplayTargetTextWithNewLineBreaks(String p_newLineBreak)
    {
        return convertLineBreaks(m_displayTargetText.toString(), p_newLineBreak);
    }

    /**
     * Returns the PTag to native hashtable as an html table.
     * 
     * @return String - an HTML table.
     */
    public String getDisplayPTag2NativeMapAsHtml()
    {
        return HtmlTableWriter.getSortedHtmlTable(m_ptag2NativeMap);
    }

    /**
     * Returns the PTag to native hashtable.
     * 
     * @return Hashtable
     */
    public Hashtable getPTag2NativeMap()
    {
        return m_ptag2NativeMap;
    }

    /**
     * Tells if a segment will be written as a protected segment.
     * 
     * @return true when the segment is displayed as protected. Otherwise false.
     */
    public boolean isWriteAsProtectedSegment()
    {
        return m_isDisplayAsProtected;
    }

    /**
     * Used to record whether the ptag form of the uploaded target segment has
     * changed between the download and upload. Set during offline error
     * checking.
     * 
     * @return true means the segment has been altered offline.
     */
    public boolean hasTargetBeenEdited()
    {
        return m_targetHasBeenEdited;
    }

    /**
     * Determines if a segment has terminology choices.
     * 
     * @return true when the segment has terminology. Otherwise false.
     */
    public boolean hasTerminology()
    {
        return m_hasTerminology;
    }

    /**
     * Determines if a segment has TM choices.
     * 
     * @return true when the segment has TM mathces. Otherwise false.
     */
    public boolean hasTMMatches()
    {
        return m_hasTMMatches;
    }

    public boolean hasMTMatches()
    {
        return m_hasMTMatches;
    }

    /**
     * Checks if a segment has TM matches from selected reference tms.
     * 
     * @since GBS-3776
     */
    public boolean hasRefTmsTMMatches()
    {
        return m_hasRefTmsTMMatches;
    }

    /**
     * Checks if a segment has MT matches from selected reference tms.
     * 
     * @since GBS-3776
     */
    public boolean hasRefTmsMTMatches()
    {
        return m_hasRefTmsMTMatches;
    }

    /**
     * Determines if a segment is a subflow.
     * 
     * @return true when the segment is a subflow. Otherwise false.
     */
    public boolean isSubflowSegment()
    {
        return OfflineEditHelper.isSubflowSegmentId(m_displaySegmentId);
    }

    /**
     * Parses composite segment-Id display string and returns the root portion
     * of the display Id - converted to a Tu id.
     * 
     * @return long - the TU id.
     */
    public Long getTuIdAsLong()
    {
        return new Long(getRootSegmentId());
    }

    /**
     * Parses composite segment-Id display strings and returns the root portion
     * of the display Id.
     * 
     * @return string - the TU id.
     */
    public String getRootSegmentId()
    {
        int idx;

        if ((idx = m_displaySegmentId.indexOf(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER)) != -1
                || (idx = m_displaySegmentId
                        .indexOf(AmbassadorDwUpConstants.BOOKMARK_SEG_ID_DELIM)) != -1)
        {
            return m_displaySegmentId.substring(0, idx);
        }

        return m_displaySegmentId;
    }

    /**
     * Gets the subflow id portion of the offline segment Id directly from the
     * segment name (id).
     * 
     * @return string
     */
    public String getSubflowId()
    {
        int idx;

        // To cover both upload and download, we must determine this
        // from the ID string itself.

        // Because, during upload, we only have the id string as read
        // from the uploaded file.

        if ((idx = m_displaySegmentId
                .lastIndexOf(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER)) != -1)
        {
            return m_displaySegmentId.substring(idx + 1);
        }
        else if ((idx = m_displaySegmentId
                .lastIndexOf(AmbassadorDwUpConstants.BOOKMARK_SEG_ID_DELIM)) != -1)
        {
            return m_displaySegmentId.substring(idx + 1);
        }

        return "";
    }

    /**
     * Gets the parent tag name.
     * 
     * Note: Currently, this is only valid during download and only if
     * this.isSubflowSegment(). We currently do not need this tag name for
     * upload and therefore we do not waist our time trying to parse it out of
     * the uploaded segment name.
     * 
     * @return string
     */
    public String getSubflowParentTagName()
    {
        return m_displayParentOfSubTagName != null ? m_displayParentOfSubTagName : "";
    }

    /**
     * Returns a new string with linebreaks converted to the new line break. The
     * method recognizes and converts any mixture of Unix(\n), Mac(\r),
     * Win32(\r\n) and Unicode(\u2080) linebreaks.
     * <p>
     * 
     * @param p_str
     *            the string to be converted
     * @param p_newLineBreak
     *            the new line break sequence
     * @return String
     */
    public static String convertLineBreaks(String p_str, String p_newLineBreak)
    {
        // if nothing to do, return same string.
        if ((p_str == null || p_str.length() <= 0)
                || (p_newLineBreak == null || p_newLineBreak.length() <= 0))
        {
            return p_str;
        }

        int len = p_str.length();
        char curChar, nextChar;
        StringBuffer buff = new StringBuffer();

        // Convert newline sequences
        // Note: this can be an assortment of several newlines in a row
        //
        // Unicode line break : \u2080
        // PLATFORM_UNIX : "\n";
        // PLATFORM_MAC : "\r";
        // PLATFORM_WIN32: "\r\n";

        for (int i = 0; i < len; i++)
        {
            curChar = p_str.charAt(i);

            if (curChar == '\u2080')
            {
                buff.append(p_newLineBreak);
            }
            else if (curChar == '\n') // Unix
            {
                buff.append(p_newLineBreak);
            }
            else if (curChar == '\r') // Win32
            {
                buff.append(p_newLineBreak);

                // if this \r is part of a WIN32 \r\n sequence, then advance the
                // index
                if ((i + 1) < len)
                {
                    nextChar = p_str.charAt(i + 1);
                    if (nextChar == '\n')
                    {
                        i++;
                    }
                }
            }
            else
            {
                buff.append(curChar);
            }
        }

        return buff.toString();
    }

    /**
     * Used by the upload error process.
     * 
     * Sets whether an uploaded segment has been matched to an existing
     * refference segment. This is used by upload and error checking to identify
     * extra segments that may have been uploaded.
     * 
     * @param p_refFound
     *            true indicates the segment has been matched to a ref seg.
     *            Otherwise false.
     */
    public void setReferenceSegmentFound(boolean p_refFound)
    {
        m_refFound = p_refFound;
    }

    /**
     * Used by the Upload error process.
     * 
     * So the return is valid only after the error checker has been run on the
     * associated OfflinePageData that contains the segment.
     * 
     * @return true when an uploaded segment has been matched to an existing
     *         refference segment after error checking. Otherwise false.
     */
    public boolean isReferenceSegmentFound()
    {
        return m_refFound;
    }

    public List getNotCountTags()
    {
        if (notCountTags == null)
        {
            String[] tags = SegmentUtil2.getTags();
            notCountTags = new ArrayList();
            if (m_ptag2NativeMap != null && m_ptag2NativeMap.size() > 0)
            {
                Set keys = m_ptag2NativeMap.keySet();
                Iterator iterator = keys.iterator();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    String value = (String) m_ptag2NativeMap.get(key);
                    for (int i = 0; i < tags.length; i++)
                    {
                        String tag = tags[i];
                        if (value.indexOf(tag) > 0)
                        {
                            notCountTags.add(key);
                            break;
                        }
                    }
                }
            }
        }

        return notCountTags;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_displaySourceText == null) ? 0 : m_displaySourceText.toString().hashCode());
        result = prime * result
                + ((m_displayTargetText == null) ? 0 : m_displayTargetText.toString().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OfflineSegmentData other = (OfflineSegmentData) obj;
        if (m_displaySourceText == null)
        {
            if (other.m_displaySourceText != null)
                return false;
        }
        else if (!m_displaySourceText.toString().equals(other.m_displaySourceText.toString()))
            return false;
        if (m_displayTargetText == null)
        {
            if (other.m_displayTargetText != null)
                return false;
        }
        else if (!m_displayTargetText.toString().equals(other.m_displayTargetText.toString()))
            return false;
        if (m_matchScore != other.getMatchValue())
            return false;
        return true;
    }

    public Tuv getSourceTuv()
    {
        return m_sourceTuv;
    }

    public void setSourceTuv(Tuv tuv)
    {
        m_sourceTuv = tuv;
    }

    public Date getTargetTuvModifyDate(long p_jobId)
    {
        if (m_targetTuv != null)
        {
            return m_targetTuv.getLastModified();
        }

        if (m_trgTuvId > 0)
        {
            TuvImpl tuv = null;
            try
            {
                tuv = SegmentTuvUtil.getTuvById(m_trgTuvId, p_jobId);
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
            if (tuv != null)
            {
                return tuv.getLastModified();
            }
        }

        return new Date();
    }

    public void setDisplayPageName(String pagename)
    {
        displayPageName = pagename;
    }

    public String getDisplayPageName()
    {
        return displayPageName;
    }

    public void setPageId(long pageid)
    {
        pageId = pageid;
    }

    public long getPageId()
    {
        return pageId;
    }

    public boolean isTagCheckSuccesful()
    {
        return m_tagCheckSuccesful;
    }

    public void setTagCheckSuccesful(boolean m_tagCheckSuccesful)
    {
        this.m_tagCheckSuccesful = m_tagCheckSuccesful;
    }

    public boolean isStateTranslated()
    {
        return isStateTranslated;
    }

    public void setStateTranslated(boolean isStateTranslated)
    {
        this.isStateTranslated = isStateTranslated;
    }
}
