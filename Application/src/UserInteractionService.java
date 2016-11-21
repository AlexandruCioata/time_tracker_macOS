import org.apache.log4j.Logger;
import oscommons.IOSType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by admin on 11/21/16.
 */
public class UserInteractionService implements Runnable {

    private IOSType osType = null;
    private long timeout = 3000;

    private String scriptPath = "";
    private String outputFolderPath = "";
    private String outputIdleFilename = "";

    AppConfig configuration = null;

    private final static Logger logger = Logger.getLogger(VisitedSitesService.class);

    public UserInteractionService(IOSType type,
                               AppConfig configuration)
    {
        this.osType = type;
        this.configuration = configuration;

        //todo:
        this.scriptPath = this.configuration.getUserInteractionIdleScriptPath();
        this.outputFolderPath = this.configuration.getImagesLocalRootFolder();
        this.outputIdleFilename = this.configuration.getUserInteractionIdleOutputFilename();
    }

    public void run()
    {

        if(osType!=null)
        {

            while(true)
            {

                try
                {
                    getUserIdleTime(scriptPath,outputFolderPath,outputIdleFilename);
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
                    logger.error("Exception in UserInteractionService: ", e);
                }


            }
        }
        else
        {
            logger.error("osTYpe is null!");
        }
    }

    private static void writeDataToFile(String outputFilename, String stringData) throws Exception
    {
        File outputFile = new File(outputFilename);
        BufferedWriter bufferedWriter = null;

        if(outputFile.exists())
        {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile,true));
        }
        else
        {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        }

        bufferedWriter.write(stringData + "\r\n");

        bufferedWriter.close();
    }

    public void getUserIdleTime(String scriptPath, String outputFolderPath, String outputIdleFilename) throws Exception
    {
        String outputLine = this.osType.executeCommandsFromScriptAndPrintOutput(scriptPath, null);

        writeDataToFile(outputFolderPath + "/" + outputIdleFilename, outputLine);

    }

}
