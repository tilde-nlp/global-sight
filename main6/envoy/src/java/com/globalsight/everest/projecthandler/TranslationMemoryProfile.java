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

package com.globalsight.everest.projecthandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.TDATM;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.StringUtil;

public class TranslationMemoryProfile extends PersistentObject
{

    private static final long serialVersionUID = -3548514967241450885L;

    // separator used between TU types to exclude - for parsing the
    // string
    public static final String EXCLUDE_TU_TYPE_DELIMITER = "|";

    // 1 back pointer to L10nProfile
    private L10nProfile m_l10nProfile = null;

    // 2 one to many mapping from TM Profile to List of Project TMs to Leverage
    // From
    private Vector<LeverageProjectTM> m_projectTMsToLeverageFrom = new Vector<LeverageProjectTM>();

    // Use this field for modification only.
    private Vector<LeverageProjectTM> m_newProjectTMsToLeverageFrom = new Vector<LeverageProjectTM>();

    // 3
    private String m_name = null;

    // 4
    private String m_description = null;

    // 5
    private long m_projectTmIdForSave = 0;

    // 6
    private boolean m_isSaveUnLocSegToProjectTM = true;

    // 7
    private boolean m_isSaveUnLocSegToPageTM = true;

    // 8
    protected Vector<String> m_jobExcludeTuTypes = new Vector<String>();

    // 9
    private boolean m_isLeverageLocalizable = true;

    // 10
    private boolean m_isTypeSensitiveLeveraging = true;

    // 11
    private long m_typeDifferencePenalty = -1;

    // 12
    private boolean m_isCaseSensitiveLeveraging = true;

    // 13
    private long m_caseDifferencePenalty = -1;

    // 14
    private boolean m_isWhiteSpaceSensitiveLeveraging = true;

    // 15
    private long m_whiteSpaceDifferencePenalty = -1;

    // 16
    private boolean m_isCodeSensitiveLeveraging = true;

    // 17
    private long m_codeDifferencePenalty = -1;

    // 18
    private boolean m_isMultiLingualLeveraging = true;

    // 19
    private String m_multipleExactMatches;

    // 20
    private long m_multipleExactMatchesPenalty = -1;

    // 21
    private long m_fuzzyMatchThreshold;

    // 22
    private long m_numberOfMatchesReturned;

    // 23
    private boolean m_isLatestMatchForReimport = false;

    // 24
    private boolean m_isTypeSensitiveLeveragingForReimp = false;

    // 25
    private long m_typeDifferencePenaltyForReimp = -1;

    // 26
    private boolean m_isMultipleMatchesForReimp = false;

    // 27
    private long m_multipleMatchesPenalty = -1;

    // 28
    private boolean m_isExactMatchLeveraging = true;

    private boolean m_isContextMatchLeveraging = true;

    private boolean m_dynLevFromGoldTm = false;

    private boolean m_dynLevFromInProgressTm = true;

    private boolean m_dynLevFromPopulationTm = false;

    private boolean m_dynLevFromReferenceTm = false;

    private boolean m_selectRefTm = false;
    private long m_refTmPenalty = -1;
    private String m_refTMsToLeverageFrom;

    // machine translation common options
    private String m_mtEngine = null;
    private boolean m_useMT = false;
    private long m_mtConfidenceScore = 0;
    private boolean m_showInEditor = false;
    
    // for "PROMT"
    private String m_ptsurl = null;
    private String m_ptsUsername = null;
    private String m_ptsPassword = null;
    private String m_ptsUrlFlag = null;

    // For "PROMT" MT engine extra settings
    private Vector m_promtInfos = new Vector();

    public static final String LATEST_EXACT_MATCH = "LATEST";

    public static final String OLDEST_EXACT_MATCH = "OLDEST";

    public static final String DEMOTED_EXACT_MATCH = "DEMOTED";

    public boolean isMatchPercentage = true;

    public boolean isTmProcendence = false;

    private boolean autoRepair = true;
    
    // microsoft translation
    private String m_msMTUrl = null;
    private String msMTAppID = null;
    private String msMTClientID = null;
    private String msMTClientSecret = null;
    private String msMTCategory = null;
    private String msMTUrlFlag = null;
    private TDATM tdatm;
    
    // Asia Online MT
    private String aoMtUrl = null;
    private long aoMtPort = -1;
    private String aoMtUsername = null;
    private String aoMtPassword = null;
    private long aoMtAccountNumber = -1;
    // For "Asia Online" MT engine extra settings
    private Vector m_tmProfileAoInfos = new Vector();

    public String getAoMtUrl()
    {
        return aoMtUrl;
    }

    public void setAoMtUrl(String aoMtUrl)
    {
        this.aoMtUrl = aoMtUrl;
    }

    public long getAoMtPort()
    {
        return aoMtPort;
    }

    public void setAoMtPort(long aoMtPort)
    {
        this.aoMtPort = aoMtPort;
    }

    public String getAoMtUsername()
    {
        return aoMtUsername;
    }

    public void setAoMtUsername(String aoMtUsername)
    {
        this.aoMtUsername = aoMtUsername;
    }

    public String getAoMtPassword()
    {
        return aoMtPassword;
    }

    public void setAoMtPassword(String aoMtPassword)
    {
        if (TMProfileHandlerHelper.checkPassword(aoMtPassword))
        {
            this.aoMtPassword = aoMtPassword;
        }
    }

    public long getAoMtAccountNumber()
    {
        return aoMtAccountNumber;
    }

    public void setAoMtAccountNumber(long aoMtAccountNumber)
    {
        this.aoMtAccountNumber = aoMtAccountNumber;
    }

    public String getMsMTUrlFlag()
    {
        return msMTUrlFlag;
    }

    public void setMsMTUrlFlag(String msMTUrlFlag)
    {
        this.msMTUrlFlag = msMTUrlFlag;
    }

    public String getMsMTCategory()
    {
        return msMTCategory;
    }

    public void setMsMTCategory(String msMTCategory)
    {
        this.msMTCategory = msMTCategory;
    }

    public String getMsMTAppID()
    {
        return msMTAppID;
    }

    public void setMsMTAppID(String msMTAppID)
    {
        this.msMTAppID = msMTAppID;
    }

    public TranslationMemoryProfile()
    {
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    public void setProjectTmIdForSave(long p_projectTmIdForSave)
    {
        m_projectTmIdForSave = p_projectTmIdForSave;
    }

    public void setLeverageLocalizable(boolean p_isLeverageLocalizable)
    {
        m_isLeverageLocalizable = p_isLeverageLocalizable;
    }

    public void setSaveUnLocSegToProjectTM(boolean p_isSaveUnLocSegToProjectTM)
    {
        m_isSaveUnLocSegToProjectTM = p_isSaveUnLocSegToProjectTM;
    }

    public void setSaveUnLocSegToPageTM(boolean p_isSaveUnLocSegToPageTM)
    {
        m_isSaveUnLocSegToPageTM = p_isSaveUnLocSegToPageTM;
    }

    public void setExcludeTuTypes(String p_tuTypes)
    {
        if (m_jobExcludeTuTypes.size() > 0)
        {
            m_jobExcludeTuTypes.clear();
        }

        if (p_tuTypes != null)
        {
            // parse through string
            StringTokenizer st = new StringTokenizer(p_tuTypes,
                    EXCLUDE_TU_TYPE_DELIMITER);

            while (st.hasMoreTokens())
            {
                String tuType = st.nextToken();
                m_jobExcludeTuTypes.add(tuType.trim());
            }
        }
    }

    public void setTuTypes(String tuTypes)
    {
        setExcludeTuTypes(tuTypes);
    }

    public String getTuTypes()
    {
        return getJobExcludeTuTypesAsString();
    }

    public void setProjectTMToLeverageFrom(LeverageProjectTM p_leverageProjectTM)
    {
        m_projectTMsToLeverageFrom.add(p_leverageProjectTM);
    }

    public void setAllLeverageProjectTMs(Vector p_projectTMsToLeverageFrom)
    {
        m_projectTMsToLeverageFrom = p_projectTMsToLeverageFrom;
    }

    public void setNewProjectTMs(Vector p_newProjectTMs)
    {
        m_newProjectTMsToLeverageFrom = p_newProjectTMs;
    }

    public Vector getNewProjectTMs()
    {
        return m_newProjectTMsToLeverageFrom;
    }

    public void setIsTypeSensitiveLeveraging(boolean p_isTypeSensitiveLeveraging)
    {
        m_isTypeSensitiveLeveraging = p_isTypeSensitiveLeveraging;
    }

    public void setTypeDifferencePenalty(long p_typeDifferencePenalty)
    {
        m_typeDifferencePenalty = p_typeDifferencePenalty;
    }

    public void setIsCaseSensitiveLeveraging(boolean p_isCaseSensitiveLeveraging)
    {
        m_isCaseSensitiveLeveraging = p_isCaseSensitiveLeveraging;
    }

    public void setCaseDifferencePenalty(long p_caseDifferencePenalty)
    {
        m_caseDifferencePenalty = p_caseDifferencePenalty;
    }

    public void setIsWhiteSpaceSensitiveLeveraging(
            boolean p_isWhiteSpaceSensitiveLeveraging)
    {
        m_isWhiteSpaceSensitiveLeveraging = p_isWhiteSpaceSensitiveLeveraging;
    }

    public void setWhiteSpaceDifferencePenalty(
            long p_whiteSpaceDifferencePenalty)
    {
        m_whiteSpaceDifferencePenalty = p_whiteSpaceDifferencePenalty;
    }

    public void setIsCodeSensitiveLeveraging(boolean p_isCodeSensitiveLeveraging)
    {
        m_isCodeSensitiveLeveraging = p_isCodeSensitiveLeveraging;
    }

    public void setCodeDifferencePenalty(long p_codeDifferencePenalty)
    {
        m_codeDifferencePenalty = p_codeDifferencePenalty;
    }

    public void setIsMultiLingualLeveraging(boolean p_isMultiLingualLeveraging)
    {
        m_isMultiLingualLeveraging = p_isMultiLingualLeveraging;
    }

    public void setMultipleExactMatches(String p_multipleExactMatches)
    {
        m_multipleExactMatches = p_multipleExactMatches;
    }

    public void setMultipleExactMatchPenalty(long p_multipleExactMatchesPenalty)
    {
        m_multipleExactMatchesPenalty = p_multipleExactMatchesPenalty;
    }

    public void setFuzzyMatchThreshold(long p_fuzzyMatchThreshold)
    {
        m_fuzzyMatchThreshold = p_fuzzyMatchThreshold;
    }

    public void setNumberOfMatchesReturned(long p_numberOfMatchesReturned)
    {
        m_numberOfMatchesReturned = p_numberOfMatchesReturned;
    }

    public void setIsLatestMatchForReimport(boolean p_isLatestMatchForReimport)
    {
        m_isLatestMatchForReimport = p_isLatestMatchForReimport;
    }

    public void setIsTypeSensitiveLeveragingForReimp(
            boolean p_isTypeSensitiveLeveragingForReimp)
    {
        m_isTypeSensitiveLeveragingForReimp = p_isTypeSensitiveLeveragingForReimp;
    }

    public void setTypeDifferencePenaltyForReimp(
            long p_typeDifferencePenaltyForReimp)
    {
        m_typeDifferencePenaltyForReimp = p_typeDifferencePenaltyForReimp;
    }

    public void setIsMultipleMatchesForReimp(boolean p_isMultipleMatchesForReimp)
    {
        m_isMultipleMatchesForReimp = p_isMultipleMatchesForReimp;
    }

    public void setMultipleMatchesPenalty(long p_multipleMatchesPenalty)
    {
        m_multipleMatchesPenalty = p_multipleMatchesPenalty;
    }

    public void setL10nProfile(L10nProfile p_l10nProfile)
    {
        m_l10nProfile = p_l10nProfile;
    }

    public String getName()
    {
        return m_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public long getProjectTmIdForSave()
    {
        return m_projectTmIdForSave;
    }

    public boolean isSaveUnLocSegToPageTM()
    {
        return m_isSaveUnLocSegToPageTM;
    }

    public boolean isSaveUnLocSegToProjectTM()
    {
        return m_isSaveUnLocSegToProjectTM;
    }

    public Vector<String> getJobExcludeTuTypes()
    {
        return m_jobExcludeTuTypes;
    }

    /**
     * Used by TOPLink to populate the database with a string - pipe delimited
     */
    public String getJobExcludeTuTypesAsString()
    {
        StringBuffer result = new StringBuffer();

        for (Enumeration<String> enumeration = m_jobExcludeTuTypes.elements(); enumeration
                .hasMoreElements();)
        {
            result.append((String) enumeration.nextElement());
            result.append(EXCLUDE_TU_TYPE_DELIMITER);
        }

        return result.toString();
    }

    public boolean isLeverageLocalizable()
    {
        return m_isLeverageLocalizable;
    }

    public Vector getProjectTMsToLeverageFrom()
    {
        return m_projectTMsToLeverageFrom;
    }

    public boolean isTypeSensitiveLeveraging()
    {
        return m_isTypeSensitiveLeveraging;
    }

    public long getTypeDifferencePenalty()
    {
        return m_typeDifferencePenalty;
    }

    public boolean isCaseSensitiveLeveraging()
    {
        return m_isCaseSensitiveLeveraging;
    }

    public long getCaseDifferencePenalty()
    {
        return m_caseDifferencePenalty;
    }

    public boolean isWhiteSpaceSensitiveLeveraging()
    {
        return m_isWhiteSpaceSensitiveLeveraging;
    }

    public long getWhiteSpaceDifferencePenalty()
    {
        return m_whiteSpaceDifferencePenalty;
    }

    public boolean isCodeSensitiveLeveraging()
    {
        return m_isCodeSensitiveLeveraging;
    }

    public long getCodeDifferencePenalty()
    {
        return m_codeDifferencePenalty;
    }

    public boolean isMultiLingualLeveraging()
    {
        return m_isMultiLingualLeveraging;
    }

    public String getMultipleExactMatches()
    {
        return m_multipleExactMatches;
    }

    public long getMultipleExactMatchesPenalty()
    {
        return m_multipleExactMatchesPenalty;
    }

    public long getFuzzyMatchThreshold()
    {
        return m_fuzzyMatchThreshold;
    }

    public long getNumberOfMatchesReturned()
    {
        return m_numberOfMatchesReturned;
    }

    public boolean isLatestMatchForReimport()
    {
        return m_isLatestMatchForReimport;
    }

    public boolean isTypeSensitiveLeveragingForReimp()
    {
        return m_isTypeSensitiveLeveragingForReimp;
    }

    public long getTypeDifferencePenaltyForReimp()
    {
        return m_typeDifferencePenaltyForReimp;
    }

    public boolean isMultipleMatchesForReimp()
    {
        return m_isMultipleMatchesForReimp;
    }

    public long getMultipleMatchesPenalty()
    {
        return m_multipleMatchesPenalty;
    }

    public L10nProfile getL10nProfile()
    {
        return m_l10nProfile;
    }

    /**
     * Use "getDynLevFromInProgressTm()" instead.
     * @deprecated
     */
    public boolean dynamicLeveragesFromInProgressTm()
    {
        return m_dynLevFromInProgressTm;
    }
    
    /**
     * Use "setDynLevFromInProgressTm(..)" instead.
     * @deprecated
     */
    public void setDynamicLeverageFromInProgressTm(boolean p_value)
    {
        m_dynLevFromInProgressTm = p_value;
    }
    
    public boolean getDynLevFromInProgressTm()
    {
        return m_dynLevFromInProgressTm;
    }

    public void setDynLevFromInProgressTm(boolean levFromInProgressTm)
    {
        m_dynLevFromInProgressTm = levFromInProgressTm;
    }

    /**
     * Use "getDynLevFromGoldTm(...)" as need not duplicated getter.
     * @deprecated
     */
    public boolean dynamicLeveragesFromGoldTm()
    {
        return m_dynLevFromGoldTm;
    }
    
    /**
     * Use "setDynLevFromGoldTm(...)" as need not duplicated setter.
     * @deprecated
     */
    public void setDynamicLeverageFromGoldTm(boolean p_value)
    {
        m_dynLevFromGoldTm = p_value;
    }
    
    public boolean getDynLevFromGoldTm()
    {
        return m_dynLevFromGoldTm;
    }

    public void setDynLevFromGoldTm(boolean levFromGoldTm)
    {
        m_dynLevFromGoldTm = levFromGoldTm;
    }

    /**
     * Use "setDynLevFromPopulationTm(...)" as need not duplicated getter.
     * @deprecated
     */
    public boolean dynamicLeveragesFromPopulationTm()
    {
        return m_dynLevFromPopulationTm;
    }
    
    /**
     * Use "setDynLevFromPopulationTm(...)" as need not duplicated setter.
     * @deprecated
     */
    public void setDynamicLeverageFromPopulationTm(boolean p_value)
    {
        m_dynLevFromPopulationTm = p_value;
    }
    
    public boolean getDynLevFromPopulationTm()
    {
        return m_dynLevFromPopulationTm;
    }

    public void setDynLevFromPopulationTm(boolean levFromPopulationTm)
    {
        m_dynLevFromPopulationTm = levFromPopulationTm;
    }

    /**
     * Use "getDynLevFromReferenceTm()" as need not duplicated getter.
     * @deprecated 
     */
    public boolean dynamicLeveragesFromReferenceTm()
    {
        return m_dynLevFromReferenceTm;
    }

    /**
     * Use "setDynLevFromReferenceTm(...)" as need not duplicated setter.
     * @deprecated
     */
    public void setDynamicLeverageFromReferenceTm(boolean p_value)
    {
        m_dynLevFromReferenceTm = p_value;
    }
    
    public boolean getDynLevFromReferenceTm()
    {
        return m_dynLevFromReferenceTm;
    }

    public void setDynLevFromReferenceTm(boolean levFromReferenceTm)
    {
        m_dynLevFromReferenceTm = levFromReferenceTm;
    }
    
    // Below method for hibernate
    public void setJobExcludeTuTypesStr(String jobExcludeTuTypes)
    {
        setExcludeTuTypes(jobExcludeTuTypes);
    }

    public String getJobExcludeTuTypesStr()
    {
        return getJobExcludeTuTypesAsString();
    }

    public boolean getIsExactMatchLeveraging()
    {
        return m_isExactMatchLeveraging;
    }

    public void setIsExactMatchLeveraging(boolean exactMatchLeveraging)
    {
        m_isExactMatchLeveraging = exactMatchLeveraging;
    }

    /**
     * @deprecated
     * @see setIsExactMatchLeveraging(boolean exactMatchLeveraging)
     */
    public void setIsLeverageExactMatches(boolean p_isExactMatchLeveraging)
    {
        m_isExactMatchLeveraging = p_isExactMatchLeveraging;
    }

    /**
     * @deprecated
     * @see getIsExactMatchLeveraging()
     */
    public boolean isLeverageExactMatch()
    {
        return m_isExactMatchLeveraging;
    }

    public boolean getIsTypeSensitiveLeveraging()
    {
        return m_isTypeSensitiveLeveraging;
    }

    public boolean getIsCaseSensitiveLeveraging()
    {
        return m_isCaseSensitiveLeveraging;
    }

    public boolean getIsWhiteSpaceSensitiveLeveraging()
    {
        return m_isWhiteSpaceSensitiveLeveraging;
    }

    public boolean getIsCodeSensitiveLeveraging()
    {
        return m_isCodeSensitiveLeveraging;
    }

    public boolean getIsMultiLingualLeveraging()
    {
        return m_isMultiLingualLeveraging;
    }

    public void setMultipleExactMatchesPenalty(long exactMatchesPenalty)
    {
        m_multipleExactMatchesPenalty = exactMatchesPenalty;
    }

    public boolean getIsLatestMatchForReimport()
    {
        return m_isLatestMatchForReimport;
    }

    public boolean getIsTypeSensitiveLeveragingForReimp()
    {
        return m_isTypeSensitiveLeveragingForReimp;
    }

    public boolean getIsMultipleMatchesForReimp()
    {
        return m_isMultipleMatchesForReimp;
    }

    public Set getProjectTMsToLeverageFromSet()
    {
        HashSet set = new HashSet();
        if (m_projectTMsToLeverageFrom != null)
        {
            set = new HashSet(m_projectTMsToLeverageFrom);
        }
        return set;
    }

    public void setProjectTMsToLeverageFromSet(Set projectTMsToLeverageFromSet)
    {
        setAllLeverageProjectTMs(new Vector(projectTMsToLeverageFromSet));
    }

    public boolean getIsContextMatchLeveraging()
    {
        return m_isContextMatchLeveraging;
    }

    public void setIsContextMatchLeveraging(boolean contextMatchLeveraging)
    {
        m_isContextMatchLeveraging = contextMatchLeveraging;
    }

    public boolean isMatchPercentage()
    {
        return isMatchPercentage;
    }

    public void setMatchPercentage(boolean isMatchPercentage)
    {
        this.isMatchPercentage = isMatchPercentage;
    }

    public boolean isTmProcendence()
    {
        return isTmProcendence;
    }

    public void setTmProcendence(boolean isTmProcendence)
    {
        this.isTmProcendence = isTmProcendence;
    }

    public long getRefTmPenalty()
    {
        return m_refTmPenalty;
    }

    public void setRefTmPenalty(long tmPenalty)
    {
        m_refTmPenalty = tmPenalty;
    }

    // added 2

    public String getRefTMsToLeverageFrom()
    {
        return m_refTMsToLeverageFrom;
    }

    public void setRefTMsToLeverageFrom(String msRefToLeverageFrom)
    {
        m_refTMsToLeverageFrom = msRefToLeverageFrom;
    }

    public boolean getSelectRefTm()
    {
        return m_selectRefTm;
    }

    public void setSelectRefTm(boolean refTm)
    {
        m_selectRefTm = refTm;
    }

    public String getMsMTClientID()
    {
        return msMTClientID;
    }

    public void setMsMTClientID(String msMTClientID)
    {
        this.msMTClientID = msMTClientID;
    }

    public String getMsMTClientSecret()
    {
        return msMTClientSecret;
    }

    public void setMsMTClientSecret(String msMTClientSecret)
    {
        if (TMProfileHandlerHelper.checkPassword(msMTClientSecret))
        {
            this.msMTClientSecret = msMTClientSecret;
        }
    }

    public void setMtEngine(String p_value)
    {
        this.m_mtEngine = p_value;
    }

    public String getMtEngine()
    {
        return this.m_mtEngine;
    }

    public void setUseMT(boolean p_value)
    {
        this.m_useMT = p_value;
    }

    public boolean getUseMT()
    {
        return this.m_useMT;
    }

    public void setShowInEditor(boolean p_value)
    {
        this.m_showInEditor = p_value;
    }

    public boolean getShowInEditor()
    {
        return this.m_showInEditor;
    }

    public void setMtConfidenceScore(long p_mtConfidenceScore)
    {
    	this.m_mtConfidenceScore = p_mtConfidenceScore;
    }
    
    public long getMtConfidenceScore()
    {
    	return this.m_mtConfidenceScore;
    }

    /**
     * Utility method
     * @deprecated
     */
    public boolean getAutoCommitToTM()
    {
		return (this.m_mtConfidenceScore == 100 ? true : false);
    }

    public void setPtsurl(String p_ptsUrl)
    {
        this.m_ptsurl = p_ptsUrl;
    }

    public String getPtsurl()
    {
        return this.m_ptsurl;
    }

    public void setPtsUsername(String p_ptsUsername)
    {
        this.m_ptsUsername = p_ptsUsername;
    }

    public String getPtsUsername()
    {
        return this.m_ptsUsername;
    }

    public String getMsMTUrl()
    {
        return m_msMTUrl;
    }

    public void setMsMTUrl(String msMTUrl)
    {
        m_msMTUrl = msMTUrl;
    }

    public void setPtsPassword(String p_ptsPassword)
    {
        if (TMProfileHandlerHelper.checkPassword(p_ptsPassword))
        {
            this.m_ptsPassword = p_ptsPassword;
        }
    }

    public String getPtsPassword()
    {
        return this.m_ptsPassword;
    }
    
    public void setPtsUrlFlag(String p_ptsUrlFlag)
    {
        this.m_ptsUrlFlag = p_ptsUrlFlag;
    }

    public String getPtsUrlFlag()
    {
        return this.m_ptsUrlFlag;
    }
    
    // For Asia Online MT
    public Vector getTmProfileAoInfoVector()
    {
        return m_tmProfileAoInfos;
    }
    
    public void setTmProfileAoInfoVector(Vector p_aoInfoVector)
    {
        m_tmProfileAoInfos = p_aoInfoVector;
    }
    
    public Set getTmProfileAoInfoSet()
    {
        HashSet set = new HashSet();
        if (m_tmProfileAoInfos != null)
        {
            set = new HashSet(m_tmProfileAoInfos);
        }

        return set;
    }

    public void setTmProfileAoInfoSet(Set p_tmProfileAoInfoSet)
    {
        setTmProfileAoInfoVector(new Vector(p_tmProfileAoInfoSet));
    }

    public void addTmProfileAoInfo(AsiaOnlineLP2DomainInfo p_aoInfo)
    {
        m_tmProfileAoInfos.add(p_aoInfo);
    }

    // For PROMT
    public void setTmProfilePromtInfoSet(Set p_tmProfilePromtInfoSet)
    {
        setAllPromtInfo(new Vector(p_tmProfilePromtInfoSet));
    }
    
    public Set getTmProfilePromtInfoSet()
    {
        HashSet set = new HashSet();
        if (m_promtInfos != null)
        {
            set = new HashSet(m_promtInfos);
        }

        return set;
    }

    public void addPromtInfo(ProMTInfo p_promtInfo)
    {
        m_promtInfos.add(p_promtInfo);
    }

    public void setAllPromtInfo(Vector p_promtInfo)
    {
        m_promtInfos = p_promtInfo;
    }

    public Vector getPromtInfos()
    {
        return m_promtInfos;
    }

    public boolean tmOrderChanged()
    {
        if (m_newProjectTMsToLeverageFrom == null
                || m_projectTMsToLeverageFrom == null)
        {
            return false;
        }

        if (m_newProjectTMsToLeverageFrom.size() != m_projectTMsToLeverageFrom
                .size())
        {
            return true;
        }

        for (LeverageProjectTM newTm : (Vector<LeverageProjectTM>) m_newProjectTMsToLeverageFrom)
        {
            boolean found = false;
            for (LeverageProjectTM oldTm : (Vector<LeverageProjectTM>) m_projectTMsToLeverageFrom)
            {
                if (newTm.equals(oldTm))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isAutoRepair()
    {
        return autoRepair;
    }

    public void setAutoRepair(boolean autoRepair)
    {
        this.autoRepair = autoRepair;
    }

    public int getMultipleExactMatcheMode()
    {
        int mode = 0;
        String modeString = this.getMultipleExactMatches();
        if (modeString.equals(TranslationMemoryProfile.LATEST_EXACT_MATCH))
        {
            mode = LeverageOptions.PICK_LATEST;
        }
        else if (modeString.equals(TranslationMemoryProfile.OLDEST_EXACT_MATCH))
        {
            mode = LeverageOptions.PICK_OLDEST;
        }
        else if (modeString
                .equals(TranslationMemoryProfile.DEMOTED_EXACT_MATCH))
        {
            mode = LeverageOptions.DEMOTE;
        }

        return mode;
    }

    public TDATM getTdatm()
    {
        return this.tdatm;
    }

    public void setTdatm(TDATM P_TDATM)
    {
        this.tdatm = P_TDATM;
    }
    
    private Set<TMPAttribute> attributes;
    
    public List<TMPAttribute> getAllTMPAttributes()
    {
        List<TMPAttribute> atts = new ArrayList<TMPAttribute>();
        Set<TMPAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            atts.addAll(tmAtts);
        }

        return atts;
    }
    
    public List<String> getAllTMPAttributenames()
    {
        List<String> atts = new ArrayList<String>();
        Set<TMPAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            Iterator<TMPAttribute> it = tmAtts.iterator();
            while(it.hasNext())
            {
                TMPAttribute tma = it.next();
                atts.add(tma.getAttributename());
            }
        }

        return atts;
    }
    
    public Set<TMPAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Set<TMPAttribute> attributes)
    {
        this.attributes = attributes;
    }

	/**
	 * Get all reference TMs' names in order to display on TM profile main list
	 * UI for "Reference TM(s)" column.
	 */
    public String getProjectTMNamesToLeverageFrom()
    {
		if (this.m_projectTMsToLeverageFrom == null
				|| this.m_projectTMsToLeverageFrom.size() == 0) {
			return "";
		}

		// Get TreeMap<projectIndex, tmName> sorted by projectIndex.
		String tmName = null;
		TreeMap<Integer, String> lmIdTmNameMap = new TreeMap<Integer, String>();
		for (Iterator<LeverageProjectTM> it = this.m_projectTMsToLeverageFrom
				.iterator(); it.hasNext();)
		{
			LeverageProjectTM levTm = it.next();
			try
			{
				tmName = ServerProxy.getProjectHandler()
						.getProjectTMById(levTm.getProjectTmId(), false).getName();
				if (!StringUtil.isEmpty(tmName))
				{
					lmIdTmNameMap.put(levTm.getProjectTmIndex(), tmName);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Per TM name one line
		StringBuilder result = new StringBuilder();
		for(Integer projectIndex : lmIdTmNameMap.keySet())
		{
			tmName = lmIdTmNameMap.get(projectIndex);
	        if (result.length() > 0) {
				result.append("<br/>").append(tmName);
			} else {
				result.append(tmName);
			}			
		}

		return result.toString();
    }

}