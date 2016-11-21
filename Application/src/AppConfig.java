import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Created by mihai on 9/2/2016.
 */
public class AppConfig{

    private int computerNumber = -1;

    private int maxNoScreenshotsStored = -1;

    private String imagesLocalRootFolder = "";
    private String rootImagesPathOnServer = "";
    private String rootCroppedImagesPathOnServer = "";

    //maximum time (s) accepted for no interaction with the pc
    private long interactionWithPCTimeOut = -1;

    //maximum time (s) accepted for the pc to stay awake with no interaction with it
    private long SHUTDOWN_TIMEOUT = -1;

    public String getVisitedSitesFilename() {
        return visitedSitesFilename;
    }

    public String getAccessedAppsFilename() {
        return accessedAppsFilename;
    }

    public String getGetFocusedApplicationScriptPath() {
        return getFocusedApplicationScriptPath;
    }

    public long getVisitedSitesTimeout() {
        return visitedSitesTimeout;
    }

    public long getAccessedAppsTimeout() {
        return accessedAppsTimeout;
    }

    private String visitedSitesFilename= "";
    private String getFocusedApplicationScriptPath= "";
    private String accessedAppsFilename= "";
    private long visitedSitesTimeout = -1;
    private long accessedAppsTimeout = -1;




    private String adminPassword = "";


    //UpdatesDownloader info
    private String updateInfoPath = "";
    private String remoteUpdatesDirPath = "";
    private String localUpdatesDirPath = "";
    private String updatesManagerPath = "";
    private String updatesManagerStartScript = "";
    private String updatesManagerStopScript = "";


    private String updatesManagerAppName="";

    private long TIMEOUT_UPLOAD_LOG_FILES = 0;


    private String remoteRootLogPath = "";
    private String logFileName = "";


    //pc interaction info
    private String userInteractionIdleScriptPath = "";
    private String userInteractionIdleOutputFilename = "";

    private FTPCredentials credentials;

    private final static Logger logger = Logger.getLogger(AppConfig.class);

    public AppConfig(Properties properties)
    {


        if(properties == null || properties.isEmpty())
        {
            logger.error("There are no properties or properties is empty!");
            return;
        }

        /*
         * set application parameters from these properties
         * */
        try
        {
            computerNumber = Integer.parseInt(
                    properties.getProperty("computerNumber").trim());
        }
        catch(Exception e)
        {
            System.out.println("Please insert a positive integer in configuration file for the computer number");
            e.printStackTrace();

            logger.error("Please insert a positive integer in configuration file for the computer number",e);

            System.exit(0);
        }

        try
        {
            maxNoScreenshotsStored = Integer.parseInt(
                    properties.getProperty("maxNoScreenshotsStored").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("this.properties.getProperty(\"maxNoScreenshotsStored\").trim());\n",e);

            maxNoScreenshotsStored = 5000;
        }

        imagesLocalRootFolder = properties.getProperty("imagesLocalRootFolder");
        rootImagesPathOnServer = properties.getProperty("rootImagesPathOnServer");
        rootCroppedImagesPathOnServer = properties.getProperty("rootCroppedImagesPathOnServer");

        try
        {
            interactionWithPCTimeOut = Integer.parseInt(
                    properties.getProperty("interactionWithPCTimeOut").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception in MainApplication -> ",e);

            interactionWithPCTimeOut = 60;
        }

        try
        {
            SHUTDOWN_TIMEOUT = Integer.parseInt(
                    properties.getProperty("shutdownTimeout").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception in MainApplication -> ",e);

            SHUTDOWN_TIMEOUT = 7200;
        }

        visitedSitesFilename = properties.getProperty("visitedSitesFilename");
        getFocusedApplicationScriptPath = properties.getProperty("getFocusedApplicationScriptPath");
        accessedAppsFilename = properties.getProperty("accessedAppsFilename");

        userInteractionIdleScriptPath = properties.getProperty("userInteractionIdleScriptPath");
        userInteractionIdleOutputFilename = properties.getProperty("userInteractionIdleOutputFilename");

        try
        {
            visitedSitesTimeout = Integer.parseInt(
                    properties.getProperty("visitedSitesTimeout").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception in MainApplication -> ",e);

            visitedSitesTimeout = 3000;
        }


        try
        {
            accessedAppsTimeout = Integer.parseInt(
                    properties.getProperty("accessedAppsTimeout").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception in MainApplication -> ",e);

            accessedAppsTimeout = 3000;
        }





    /*
    * FTP credentials information
    * */
        String host = properties.getProperty("ftpHost");
        String user = properties.getProperty("ftpUser");
        String pass = properties.getProperty("ftpPassword");
        int port = 0;

        try{

            port = Integer.parseInt(properties.getProperty("ftpPort").trim());

        }catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception in MainApplication -> ",e);

            port = 21;
        }

        adminPassword = properties.getProperty("adminPassword");

        credentials = new FTPCredentials(host,port,user,pass);

        remoteUpdatesDirPath = properties.getProperty("remoteUpdatesDirPath");
        localUpdatesDirPath = properties.getProperty("localUpdatesDirPath");
        updatesManagerPath=properties.getProperty("updatesManagerPath");
        updatesManagerStartScript=properties.getProperty("updatesManagerStartScript");
        updatesManagerStopScript=properties.getProperty("updatesManagerStopScript");
        updatesManagerAppName=properties.getProperty("updatesManagerAppName");
        updateInfoPath = properties.getProperty("updateInfoPath");



        /*
        * Uploader configuration
        * */
        try
        {
            TIMEOUT_UPLOAD_LOG_FILES = Integer.parseInt(
                    properties.getProperty("TIMEOUT_UPLOAD_LOG_FILES").trim());
        }
        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Exception -> ",e);

            TIMEOUT_UPLOAD_LOG_FILES = 3600;
        }

        remoteRootLogPath = properties.getProperty("remoteRootLogPath");
        logFileName = properties.getProperty("logFileName");

    }

    public static void main(String[] args) {

        String log4jConfigFilename = "log4j.properties";
        PropertyConfigurator.configure(log4jConfigFilename);

        Properties prop = MainApplication.readProperties("appConfig.properties");

        AppConfig config = new AppConfig(prop);


        config.displayAllConfigs();

    }


    private void displayAllConfigs()
    {
        System.out.println(computerNumber);
        System.out.println(maxNoScreenshotsStored);
        System.out.println(imagesLocalRootFolder);
        System.out.println(rootImagesPathOnServer);
        System.out.println(rootCroppedImagesPathOnServer);
        System.out.println(interactionWithPCTimeOut);
        System.out.println(SHUTDOWN_TIMEOUT);
        System.out.println(adminPassword);

        System.out.println(updateInfoPath);
        System.out.println(remoteUpdatesDirPath);
        System.out.println(localUpdatesDirPath);
        System.out.println(updatesManagerPath);
        System.out.println(updatesManagerStartScript);

        System.out.println(TIMEOUT_UPLOAD_LOG_FILES);
        System.out.println(remoteRootLogPath);
        System.out.println(logFileName);

        System.out.println("credentials");
        System.out.println(credentials.user);
        System.out.println(credentials.pass);
        System.out.println(credentials.server);
        System.out.println(credentials.port);

    }


    public int getComputerNumber() {
        return computerNumber;
    }

    public int getMaxNoScreenshotsStored() {
        return maxNoScreenshotsStored;
    }

    public String getImagesLocalRootFolder() {
        return imagesLocalRootFolder;
    }

    public String getRootImagesPathOnServer() {
        return rootImagesPathOnServer;
    }

    public String getRootCroppedImagesPathOnServer() {
        return rootCroppedImagesPathOnServer;
    }

    public long getInteractionWithPCTimeOut() {
        return interactionWithPCTimeOut;
    }

    public long getSHUTDOWN_TIMEOUT() {
        return SHUTDOWN_TIMEOUT;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getUpdateInfoPath() {
        return updateInfoPath;
    }

    public String getRemoteUpdatesDirPath() {
        return remoteUpdatesDirPath;
    }

    public String getLocalUpdatesDirPath() {
        return localUpdatesDirPath;
    }

    public String getUpdatesManagerPath() {
        return updatesManagerPath;
    }

    public String getUpdatesManagerStartScript() {
        return updatesManagerStartScript;
    }

    public FTPCredentials getCredentials() {
        return credentials;
    }

    public long getTIMEOUT_UPLOAD_LOG_FILES() {
        return TIMEOUT_UPLOAD_LOG_FILES;
    }

    public String getRemoteRootLogPath() {
        return remoteRootLogPath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getUpdatesManagerStopScript() {
        return updatesManagerStopScript;
    }

    public String getUpdatesManagerAppName() {
        return updatesManagerAppName;
    }

    public String getUserInteractionIdleOutputFilename() {
        return userInteractionIdleOutputFilename;
    }

    public String getUserInteractionIdleScriptPath() {
        return userInteractionIdleScriptPath;
    }

}
