package com.globalsight.selenium.testcases.testmatrix.datasource;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.ExportWorkflowFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.XMLRules;
import com.globalsight.selenium.pages.PermissionGroups;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.PropertyFileConfiguration;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.thoughtworks.selenium.Selenium;

public class Plain_Text_Filter extends BaseTestCase {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();
	private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
	private XMLRulesFuncs iXMLRuleConfig = new XMLRulesFuncs();
	private static String testMatrixFile = PropertyFileConfiguration.TestMatrix_PROPERTIES;
	private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
	private CreateJobsFuncs tmp = new CreateJobsFuncs();
	private ExportWorkflowFuncs export = new ExportWorkflowFuncs();
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	@Test
	public void testPlain_Text_Filter() throws Exception {
		// Initiate filters
        iSelniumUtils.openMenuItemAndWait(selenium,
                MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);
  
		 
		// Read all test cases to "testcases", and create word job with
		// corresponding filter.
		ArrayList<String[]> testCases = new ArrayList<String[]>();
		String filePath = ConfigUtil.getConfigData("Base_Path")
				+ getProperty(testMatrixFile,"Plain_Text_FilterTCPath");
		File file = new File(filePath);
		AssertJUnit.assertTrue(file.exists());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] testCase = line.split("\t");
			testCases.add(testCase);
		}
		br.close();
		
		
		
		String File_Profile_name;;
		String Filter_Name;
		String Base_Text_post_filter;
		String Base_Text_Filter_Internal_Text;
		String Base_Text_Filter_Escaping;
		String Element_post_filter;
		String Custom_Text_Rule;
		String Custom_SID_Rule;
		String Source_File;
		String Empty_File_Profile_Name = "";
		
		
		String filterJob = getStringToday();
//		String filterJob = "201511-04-115231";

    	
		int ljobNames = testCases.size();
		String[][] jobNames = new String[ljobNames][];
		jobNames[0] = new String[1];
		jobNames[0][0] = "Case title";

		BasicFuncs basic = new BasicFuncs();
		//Create Filter
		for (int i = 1; i < testCases.size(); i++) {
			File_Profile_name = testCases.get(i)[1];
			Filter_Name = testCases.get(i)[2];
			Base_Text_post_filter = testCases.get(i)[3];
			Base_Text_Filter_Internal_Text = testCases.get(i)[4];
			Base_Text_Filter_Escaping = testCases.get(i)[5];
			Element_post_filter = testCases.get(i)[6];
			Custom_Text_Rule = testCases.get(i)[7];
			Custom_SID_Rule = testCases.get(i)[8];
			Source_File = testCases.get(i)[9];
			
			
			selenium.click(MainFrame.DATA_SOURCES_MENU);
			selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			
			iFilterConfig.plainTextFilterOperation(selenium, Filter_Name,Base_Text_post_filter,
					Base_Text_Filter_Internal_Text,
					Base_Text_Filter_Escaping,
					Element_post_filter,
					Custom_Text_Rule,
					Custom_SID_Rule);
	
			
			
			// create File Profile
//	        SeleniumUtils.
			
	        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
	                MainFrame.FILE_PROFILES_SUBMENU);

	        ArrayList<String> array = new ArrayList<String>();
	        array.add(File_Profile_name);
	        array.add("Description: Plain Text filter test");
	        array.add(getProperty(testMatrixFile,"Plain_Text_Fitler.localizationProfiles"));
	        array.add("PlainText");
	        array.add(Filter_Name);
	        array.add("x");
	        array.add("txt");

	        fileProfileFuncs.setup(array);
	        fileProfileFuncs.create(selenium);
	        
	        if (i==1) Empty_File_Profile_Name = File_Profile_name;
	        jobNames[i] = new String[2];
	        jobNames[i][0] = i + "_" + Filter_Name + "_" + filterJob;
        	jobNames[i][1] = i + "_" + Filter_Name + "_Empty_Filter"+ "_" + filterJob;

        	if ((!(Source_File.isEmpty())) && (!(Source_File.equalsIgnoreCase("x")))){
        		
	        	tmp.createJob(jobNames[i][0], Source_File, 
									File_Profile_name, getProperty(testMatrixFile,"Plain_Text_targetLocales"));
				tmp.createJob(jobNames[i][1], Source_File, 
										 Empty_File_Profile_Name, getProperty(testMatrixFile,"Plain_Text_targetLocales"));
				}
					
        	}
				
		for (int i =1; i < testCases.size(); i++) {
			for (int j=0; j<jobNames[i].length; j++){
				export.exportWorkflow(jobNames[i][j], getProperty(testMatrixFile,"Plain_Text_targetLocales"));
			}
}				
			

}
    @BeforeTest
    private void beforeTest() {
    	CommonFuncs.loginSystemWithAdmin(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
    }
	}
