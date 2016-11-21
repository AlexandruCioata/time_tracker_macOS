import org.apache.log4j.Logger;
import oscommons.IOSType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by admin on 11/21/16.
 */
public class ApplicationsTracker implements Runnable {

    private IOSType osType = null;
    private long timeout = 3000;

    private String scriptPath = "";
    private String outputFolderPath = "";
    private String outputAppsFilename = "";

    AppConfig configuration = null;

    private final static Logger logger = Logger.getLogger(VisitedSitesService.class);

    public ApplicationsTracker(IOSType type,
                               AppConfig configuration)
    {
        this.osType = type;
        this.configuration = configuration;

        //todo:
        this.scriptPath = this.configuration.getGetFocusedApplicationScriptPath();
        this.outputFolderPath = this.configuration.getImagesLocalRootFolder();
        this.outputAppsFilename = this.configuration.getAccessedAppsFilename();
    }

    public void run()
    {

        if(osType!=null)
        {
            while(true)
            {

                try
                {
                    getFocusedApplication(scriptPath, outputFolderPath, outputAppsFilename);
                }
                catch(Exception e)
                {
                    logger.error("Exception in UserInteractionService: ", e);
                }

                try
                {
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

    public void getFocusedApplication(String scriptPath, String outputFolderPath, String outputAppsFilename)
    {
        String outputLine = this.osType.executeCommandsFromScriptAndPrintOutput(scriptPath,null);

        System.out.println(outputLine);


        //preprocess the read line in order to take the site name and browser name
        outputLine = preprocessCurrentAppName(outputLine);


        File outputFile = new File(outputFolderPath + "/" + outputAppsFilename);

        try{

            BufferedWriter bufferedWriter = null;

            if(outputFile.exists())
            {
                bufferedWriter = new BufferedWriter(new FileWriter(outputFile,true));
            }
            else
            {
                bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            }



            bufferedWriter.write(outputLine + "\r\n");

            bufferedWriter.close();

        }
        catch(Exception e)
        {
            logger.error(e);
        }
    }


    public String preprocessCurrentAppName(String line)
    {
        String result = line;

        //0x04800002  0 15665  mihai-To-be-filled-by-O-E-M Google - Google Chrome
        String[] parts = line.split(" +");
        if(parts.length > 4)
        {
            String lastPart = parts[4];

            result = line.substring(line.indexOf(lastPart));
        }

        //System Settings
        //java - Splitting a string with multiple spaces - Stack Overflow - Google Chrome
        parts = result.split("-");
        if(parts.length > 1)
        {
            result = result.substring(result.indexOf(parts[parts.length-1]));
        }

        result = result.trim();

        return result;
    }

}
