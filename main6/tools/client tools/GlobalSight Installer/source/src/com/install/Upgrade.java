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
package com.install;

import java.io.File;
import java.io.FileFilter;
//import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.JbossUpdater;
import com.util.ServerUtil;
import com.util.UpgradeUtil;
import com.util.db.DbUtil;
//import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

/**
 * The main class that manage upgrade a server to a new server.
 * <p>
 * 
 * You can get more information from doUpdate() method.
 * 
 * @see #doUpgrade()
 */
public class Upgrade
{
    private static Logger log = Logger.getLogger(Upgrade.class);

    public static final String BACKSLASH = "\\";
    public static final String DOUBLEBACKSLASH = "\\\\";
    public static final String FORWARDSLASH = "/";
    public static final String DOUBLEBACKSLASH_REG = "\\\\\\\\";
    public static final String DOUBLEBACKSLASH_REP = "\\\\\\\\\\\\\\\\";

    private static UpgradeUtil UPGRADE_UTIL = UpgradeUtil.newInstance();

    private static final String BACKUP_FILE = "backup";
//    private static final String EAR_PATH = "/jboss/server/standalone/deployments/globalsight.ear";
    private UI ui = UIFactory.getUI();

    private static final String VALIEDATE = "validate";
    private static final String BACKUP = "backup";
    private static final String DELETE = "delete";
    private static final String DATABASE = "database";
    private static final String COPY = "copy";
    
    private Boolean isUpgradeJboss = null;

    private List<String> ignoreFiles;
    private List<String> ignoreEndPaths;

    private static Map<String, Integer> RATES = new HashMap<String, Integer>();
    static
    {
        RATES.put(VALIEDATE, 100000);
        RATES.put(BACKUP, 350000);
        RATES.put(DELETE, 100000);
        RATES.put(COPY, 350000);
        RATES.put(DATABASE, 100000);
    }

    private static List<String> IGNORE_PROPERTIES = new ArrayList<String>();
    static
    {
        IGNORE_PROPERTIES.add("db_connection.properties");
        IGNORE_PROPERTIES.add("db_connection.properties.template");

        IGNORE_PROPERTIES.add("envoy_generated.properties");
        IGNORE_PROPERTIES.add("envoy_generated.properties.template");

        IGNORE_PROPERTIES.add("Logger.properties");
        IGNORE_PROPERTIES.add("Logger.properties.template");
    }
    
    private static List<String> BACKUP_FILES = new ArrayList<String>();
    static
    {
    	BACKUP_FILES.add("/install");
    	BACKUP_FILES.add("/jboss");
    	BACKUP_FILES.add("/logs");
    }
    
    private static List<String> REMOVE_FILES = new ArrayList<String>();
    static
    {
    	REMOVE_FILES.add("/install");
    	REMOVE_FILES.add("/jboss");
    }
    
    private boolean isUpdateJboss()
    {
    	if (isUpgradeJboss == null)
    	{
    		File f = new File(ServerUtil.getPath() + "/install/JavaServiceWrapper/conf/wrapper.conf");
    		isUpgradeJboss = f.exists();
    	}
    	
    	return isUpgradeJboss;
    }
    
    private void removeFilesForUpdateJboss() throws Exception
    {
    	if (!isUpdateJboss())
    		return;
    	
    	ui.addProgress(0, Resource.get("process.deleteFiles"));
    	log.info("Removing files for update jboss.");
    	
        for (String f : REMOVE_FILES)
        {
        	FileUtil.deleteFile(new File(ServerUtil.getPath() + f));
        }
        log.info("Removing files finished.");
    }
    
    private void backImages() throws Exception
    {
    	String imagePath = "/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/images";
    	File root = new File(ServerUtil.getPath() + imagePath);
    	if (!root.exists())
    		return;
    	
    	List<File> files = FileUtil.getAllFiles(root, new FileFilter() 
    	{
			@Override
			public boolean accept(File pathname) 
			{
//				List<String> notBackupFiles = new ArrayList<String>();
//				notBackupFiles.add("server.properties");
//				
//				return !notBackupFiles.contains(pathname.getName());
				return true;
			}
		});
    	
    	for (File f : files)
    	{
    		String path = f.getCanonicalPath().replace("\\", "/");
            String serverPath = ServerUtil.getPath().replace("\\", "/");
            String upgradePath = UPGRADE_UTIL.getPath().replace("\\", "/");
            
            path = path.replace(serverPath, upgradePath);
            
            FileUtil.copyFile(f, new File(path));
    	}
    }
    
    private void backupForUpdateJboss() throws Exception
    {
    	File f1 = new File(ServerUtil.getPath() + "/jboss/jboss_server/server/default/conf/globalsight.keystore");
        if (f1.exists())
        {
            String path = UPGRADE_UTIL.getPath() + "/jboss/server/standalone/configuration/globalsight.keystore";
            FileUtil.copyFile(f1, new File(path));
        }
        
        f1 = new File(ServerUtil.getPath() + "/jboss/server/standalone/configuration/globalsight.keystore");
        if (f1.exists())
        {
            String path = UPGRADE_UTIL.getPath() + "/jboss/server/standalone/configuration/globalsight.keystore";
            FileUtil.copyFile(f1, new File(path));
        }
        
    	if (!isUpdateJboss())
    		return;
    	
    	Map<String, String> paths = new HashMap<String, String>();
		paths.put(
				"/jboss/jboss_server/server/default/deploy/globalsight.ear/lib/classes/properties",
				"/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties");
		paths.put(
				"/jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/reports/messages",
				"/jboss/server/standalone/deployments/globalsight.ear/lib/classes/com/globalsight/resources/messages");
		paths.put(
				"/jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/images",
				"/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/images");
    	
        for (String bfile : paths.keySet())
        {
        	File root = new File(ServerUtil.getPath() + bfile);
        	if (!root.exists())
        		continue;
        	
        	List<File> files = FileUtil.getAllFiles(root, new FileFilter() 
        	{
				@Override
				public boolean accept(File pathname) 
				{
					List<String> notBackupFiles = new ArrayList<String>();
					notBackupFiles.add("server.properties");
					notBackupFiles.add("AdobeAdapter.properties");
					notBackupFiles.add("db_connection.properties.template");
					notBackupFiles.add("envoy.properties");
					notBackupFiles.add("Wordcounter.properties");
					notBackupFiles.add("idmlrule.properties");
					notBackupFiles.add("MSDocxXmlRule.properties");
					notBackupFiles.add("WhitespaceForExport.properties");
					
					return !notBackupFiles.contains(pathname.getName());
				}
			});
        	
        	for (File f : files)
        	{
        		String path = f.getCanonicalPath().replace("\\", "/");
                String serverPath = ServerUtil.getPath().replace("\\", "/");
                String upgradePath = UPGRADE_UTIL.getPath().replace("\\", "/");
                
                path = path.replace(serverPath, upgradePath);
                path = path.replace(bfile, paths.get(bfile));
                
                FileUtil.copyFile(f, new File(path));
        	}
        }
        
        File f = new File(ServerUtil.getPath() + "/install/data/installValues.properties");
        if (f.exists())
        {
        	String path = f.getCanonicalPath().replace("\\", "/");
            String serverPath = ServerUtil.getPath().replace("\\", "/");
            String upgradePath = UPGRADE_UTIL.getPath().replace("\\", "/");
            
            path = path.replace(serverPath, upgradePath);
            
            FileUtil.copyFile(f, new File(path));
        }
        else
        {
        	log.error("Can not find the file: " + f.getAbsolutePath());
        }
        
        JbossUpdater.readOptionsFromWrapper();
    }
    
    private void backup() throws Exception
    {
        log.info("Backuping files.");

        String backupName = ServerUtil.getServerName() + "("
                + ServerUtil.getVersion() + ")";
        File root = new File(BACKUP_FILE + File.separator + backupName);        
        if (root.exists())
        {
            String rootPath = root.getCanonicalPath().replace("\\", "/");
            log.info("The folder (" + rootPath + ") already exist");
            if (!ui.confirmRewrite(rootPath))
            {
                ui.addProgress(RATES.get(BACKUP), "");
                return;
            }
        }
        
        List<File> files = new ArrayList<File>();
        for (String bfile : BACKUP_FILES)
        {
        	File f = new File(ServerUtil.getPath()  + bfile);
        	if (f.exists())
        	    files.addAll(FileUtil.getAllFiles(f));
        }
        
        int size = files.size();
        log.info("Size: " + size);
        int processTotle = RATES.get(BACKUP);
        if (size == 0)
        {
            ui.addProgress(processTotle, "");
            return;
        }
        int rate = processTotle / size;
        int lose = processTotle - rate * size;

        int i = 0;
        for (File f : files)
        {
            ui.addProgress(0, MessageFormat.format(Resource
                    .get("process.backup"), f.getName(), i + 1, size));
            String path = f.getCanonicalPath().replace("\\", "/");
            path = path.replace(ServerUtil.getPath().replace("\\", "/"), "");

            try
            {
        		FileUtil.copyFile(f, new File(BACKUP_FILE + File.separator
                        + backupName + File.separator + path));
            }
            catch (Exception e)
            {
                String fPath = f.getPath().replace("\\", "/");
                ui.confirmContinue(MessageFormat.format(Resource
                        .get("msg.backupFile"), fPath));
            }
            ui.addProgress(rate, MessageFormat.format(Resource
                    .get("process.backup"), f.getName(), i + 1, size));
            i++;
        }
        ui.addProgress(lose, "");
        log.info("Backuping files finished");
    }

//    private FileFilter getClassFilter()
//    {
//        return new FileFilter()
//        {
//            @Override
//            public boolean accept(File pathname)
//            {
//                String name = pathname.getName();
//                return name.endsWith(".class");
//            }
//        };
//    }
//    
//    private FileFilter getJarFilter()
//    {
//        return new FileFilter()
//        {
//            @Override
//            public boolean accept(File pathname)
//            {
//                String name = pathname.getName();
//                return name.endsWith(".class");
//            }
//        };
//    }
    
//    private void removeClassFiles()
//    {
//        log.info("Removing class files");
//        List<File> files = FileUtil.getAllFiles(new File(ServerUtil.getPath()
//                + EAR_PATH), getClassFilter());
//        
//        List<File> jares = FileUtil.getAllFiles(new File(UPGRADE_UTIL.getPath()
//                + EAR_PATH), getJarFilter());        
//        if (jares.size() > 10)
//        {
//            log.info("Removing jar files");
//            files.addAll(FileUtil.getAllFiles(new File(ServerUtil.getPath()
//                + EAR_PATH), getJarFilter()));
//        }
//
//        int size = files.size();
//        log.info("Size: " + size);
//        int processTotle = RATES.get(DELETE);
//        if (size == 0)
//        {
//            ui.addProgress(processTotle, "");
//            return;
//        }
//        int rate = processTotle / size;
//        int lose = processTotle - rate * size;
//
//        int i = 0;
//        for (File f : files)
//        {
//            ui.addProgress(0, MessageFormat.format(Resource
//                    .get("process.delete"), f.getName(), i + 1, size));
//            
//            String path = f.getPath().replace("\\", "/");
//            while(f.exists() && !f.delete())
//            {
//                ui.tryAgain(MessageFormat.format(Resource
//                        .get("msg.deleteFile"), path));
//            }
//            ui.addProgress(rate, MessageFormat.format(Resource
//                    .get("process.delete"), f.getName(), i + 1, size));
//            i++;
//        }
//
//        ui.addProgress(lose, "");
//        log.info("Removing class files finished");
//    }

    /**
     * Upgrades a server to a new server.
     */
    public void doUpgrade()
    {
        log.info("\n\n == Start upgrading =================================\n");
        try
        {
            ui.showWelcomePage();
            validate();
            UPGRADE_UTIL.removeHotfix();
            UPGRADE_UTIL.runPrePlug();
            backupForUpdateJboss();
            backImages();
            backup();
            removeFilesForUpdateJboss();
            copyFiles();
            UPGRADE_UTIL.parseAllTemplates();
            
            if (isUpdateJboss())
            {
            	JbossUpdater.updateJavaOptions();
            }
            
            UPGRADE_UTIL.upgradeVerion(RATES.get(DATABASE));           
            UPGRADE_UTIL.saveSystemInfo();
            
            DbUtil util = DbUtilFactory.getDbUtil();
            util.closeExistConn();
            
            ui.finish();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement msg : trace)
            {
                log.error("\tat " + msg);
            }

            ui.error(e.getMessage());
        }
    }

    private FileFilter getFileFilter()
    {
        return new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
            	if (getIgnoreFiles().contains(pathname.getName()))
            		return false;
            	
            	String path = pathname.getPath();
            	path = path.replace("\\", "/");
            	
            	List<String> ps = getIgnoreEndPath();
            	for (String p : ps)
            	{
            		if (path.endsWith(p))
            			return false;
            	}
            	
            	return true;
            }
        };
    }

    private void copyFiles() throws Exception
    {
        log.info("Copying files");

        ui.addProgress(0, Resource.get("process.count"));
        String serverPath = ServerUtil.getPath().replace("\\", "/");
        String upgradePath = UPGRADE_UTIL.getPath().replace("\\", "/");
        List<File> files = new ArrayList<File>();
        FileFilter filter = getFileFilter();
        for (String root : getCopyRoots())
        {
            files.addAll(FileUtil.getAllFiles(new File(root), filter));
        }

        int size = files.size();
        int processTotle = RATES.get(COPY);
        if (size == 0)
        {
            ui.addProgress(processTotle, "");
            return;
        }

        int rate = processTotle / size;
        int lose = processTotle - rate * size;

        log.info("Size: " + size);

        int i = 0;
        for (File f : files)
        {
            ui.addProgress(0, MessageFormat.format(
                    Resource.get("process.copy"), f.getName(), i + 1, size));

            i++;
            String path = f.getCanonicalPath().replace("\\", "/");
            String serverFilePath = path.replace(upgradePath, serverPath);
            FileUtil.copyFile(f, new File(serverFilePath));
            ui.addProgress(rate, MessageFormat.format(Resource
                    .get("process.copy"), f.getName(), i + 1, size));
        }

        ui.addProgress(lose, "");
        log.info("Copying files finished");
    }
    
    private List<String> getCopyRoots()
    {
        List<String> roots = new ArrayList<String>();
        roots.add(UPGRADE_UTIL.getPath());
        return roots;
    }
    
    private List<String> getIgnoreEndPath()
    {
    	if (ignoreEndPaths == null)
    	{
    		ignoreEndPaths = new ArrayList<String>();
    		
    		ignoreEndPaths.add("/jmx-console.war/WEB-INF/jboss-web.xml");
    		ignoreEndPaths.add("/jmx-console.war/WEB-INF/web.xml");
    		
    		ignoreEndPaths.add("/web-console.war/WEB-INF/jboss-web.xml");
    		ignoreEndPaths.add("/web-console.war/WEB-INF/web.xml");
    	}
    	
    	return ignoreEndPaths;
    }

    private List<String> getIgnoreFiles()
    {
        if (ignoreFiles == null)
        {
            ignoreFiles = new ArrayList<String>();

//            // Ignore propertie files.
//            String sourcePath = ServerUtil.getPath() + PROPERTIES_PATH;
//            List<File> files = FileUtil.getAllFiles(new File(sourcePath),
//                    new FileFilter()
//                    {
//                        @Override
//                        public boolean accept(File pathname)
//                        {
//                            String path = pathname.getName();
//                            return path.endsWith(".properties")
//                                    && !Constants.COPY_PROPERTITES
//                                            .contains(path);
//                        }
//                    });
//            
//            for (File f : files)
//            {
//                ignoreFiles.add(f.getName());
//            }
            ignoreFiles.add("jmx-console-users.properties");
            ignoreFiles.add("jmx-console-roles.properties");
            ignoreFiles.add("web-console-users.properties");
            ignoreFiles.add("web-console-roles.properties");
            
            ignoreFiles.add("system.xml");
            ignoreFiles.add("jboss-service.xml");
            ignoreFiles.add("GlobalSight-JMS-service.xml");
            ignoreFiles.add("ear-deployer.xml");
            ignoreFiles.add("server.xml");
            
            ignoreFiles.add("run.sh");
            ignoreFiles.add("run.bat");
            ignoreFiles.add("run.conf");
            
            if (!isUpdateJboss())
            {
            	ignoreFiles.add("standalone.xml");
                ignoreFiles.add("standalone.conf");
                ignoreFiles.add("standalone.conf.bat");
            }
        }

        return ignoreFiles;
    }

    private void validatePath()
    {
        log.debug("Validating path");
        ui.addProgress(0, Resource.get("process.validatePath"));
        try
        {
            UPGRADE_UTIL.validatePath();
        }
        catch (Exception e)
        {
            log.info(e);
            ui.infoError(e.getMessage());
        }
        ui.addProgress(RATES.get(VALIEDATE) / 3, Resource
                .get("process.validatePath"));
        log.debug("Validating path finished");
    }

    private void validateVersion()
    {
        log.debug("Validating version");
        ui.addProgress(0, Resource.get("process.validateVersion"));
        try
        {
            UPGRADE_UTIL.validateVersion();
        }
        catch (Exception e)
        {
            ui.infoError(e.getMessage());
        }
        ui.addProgress(RATES.get(VALIEDATE) / 3, Resource
                .get("process.validateVersion"));
        log.debug("Validating version finished");
    }

    private void validateDatabase() throws Exception
    {
        log.debug("Validating database");
        int rate = RATES.get(VALIEDATE);
        ui.addProgress(0, Resource.get("process.validateDatabase"));
        DbUtilFactory.getDbUtil().testConnection();
        ui.addProgress(rate - (rate / 3) * 2, Resource
                .get("process.validateDatabase"));
        log.debug("Validating database finished");
    }

    private void validate() throws Exception
    {
        validatePath();
        validateVersion();
        validateDatabase();
    }
}