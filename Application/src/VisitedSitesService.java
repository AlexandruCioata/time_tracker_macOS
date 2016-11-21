import org.apache.log4j.Logger;
import oscommons.IOSType;


/**
 * Created by admin on 9/14/16.
 */
public class VisitedSitesService implements Runnable {

    private IOSType osType = null;
    private long timeout = 3000;

    private String scriptPath = "";
    private String outputFolderPath = "";
    //private String outputAppsFilename = "";
    private String outputSitesFilename = "";

    AppConfig configuration = null;

    private final static Logger logger = Logger.getLogger(VisitedSitesService.class);

    public VisitedSitesService(IOSType type,
                               AppConfig configuration)
    {
        this.osType = type;
        this.configuration = configuration;

        //todo:
        this.scriptPath = this.configuration.getGetFocusedApplicationScriptPath();
        this.outputFolderPath = this.configuration.getImagesLocalRootFolder();
        this.outputSitesFilename = this.configuration.getVisitedSitesFilename();
    }

    public void run()
    {

        if(osType!=null)
        {

            while(true)
            {

                try
                {

                    /*
                    * get current application which is focused by the user
                    * */
                    //this.osType.getFocusedApplication(scriptPath,outputFolderPath,outputAppsFilename);

                    /*
                    * get the website name user is surfing
                    * */
                    this.osType.getVisitedSites(scriptPath,outputFolderPath,outputSitesFilename);


                    Thread.sleep(timeout);
                }
                catch(Exception e)
                {
                    logger.error("Exception in AppAndSites: ", e);
                }


            }
        }
        else
        {
            logger.error("osTYpe is null!");
        }

    }

}
