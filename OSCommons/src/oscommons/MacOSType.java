package oscommons;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by petrica on 21/11/16.
 */
public class MacOSType implements IOSType {

    private final static Logger logger = Logger.getLogger(LinuxOSType.class);

    private String adminPassword = "";

    public MacOSType(String adminPassword)
    {
        this.adminPassword = adminPassword;
    }

    public void getFocusedApplication(String scriptPath, String outputPath,String outputFilename)
    {

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

    public void getVisitedSites(String scriptPath,
                                String outputPath,
                                String outputFilename)
    {



    }



    public String preprocessCurrentWebsiteAppName(String line)
    {
        String result = line;

        //0x04800002  0 15665  mihai-To-be-filled-by-O-E-M Google - Google Chrome
        String[] parts = line.split(" +");
        if(parts.length > 4)
        {
            String lastPart = parts[4];

            result = line.substring(line.indexOf(lastPart));
        }


        return result;
    }

    public boolean validateBrowser(String line)
    {

        /*
        * list with supported browsers
        * */
/*
        List<String> supportedBrowsers = new ArrayList<>();

        supportedBrowsers.add("google chrome");
        supportedBrowsers.add("mozilla firefox");
        supportedBrowsers.add("opera");
        supportedBrowsers.add("chromium");
        supportedBrowsers.add("ubuntu web browser");


        String appName = line.substring(line.lastIndexOf("-"));

        for(String browser:supportedBrowsers)
        {
            if(appName.toLowerCase().contains(browser))
            {
                return true;
            }
        }
*/

        return false;
    }

    /**
     * Starts an application using the script name given
     * as a parameter
     *
     * @param filename full path of the script used to start the application
     * @param params list of strings used as parameters for command line
     */
    public void startApplication(String filename, List<String> params)
    {
        executeCommandsFromScript(filename,params,false,true);
    }


    /**
     * Executes a specific script given as parameter
     * with commands within it
     *
     * @param filename full path of the script to be executed
     * @param params list of strings used as parameters for command line
     * @param waitFor flag used to determine whether the main execution process
     *                waits for the process which executes commands
     * @param isOutputEnabled flag used to display at console the output
     *                        of the process executed
     */
    public boolean executeCommandsFromScript(String filename,
                                             List<String> params,
                                             boolean waitFor,
                                             boolean isOutputEnabled)
    {

        File commandsFile = new File(filename);
        if(commandsFile.exists())
        {

            logger.info("Executing commands script:" + filename);

            List<String> commandWithParams = new ArrayList<>();

            //Windows specific console command line invocation
            commandWithParams.add("sh");

            //add the script name to be executed
            commandWithParams.add(commandsFile.getAbsolutePath());

            //add all parameters for command line
            if(params != null)
            {
                commandWithParams.addAll(params);
            }

            ProcessBuilder pb = new ProcessBuilder(commandWithParams);

            try
            {

                Process proc = pb.start();

                if(waitFor)
                {
                    proc.waitFor();
                }

                if(isOutputEnabled)
                {
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(proc.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(proc.getErrorStream()));

                    // read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }

                    // read any errors from the attempted command
                    System.out.println("Here is the standard error of the command (if any):\n");
                    while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    }
                }

                System.out.println("Commands from " + commandsFile.getName() + " were successfully executed!");

                return true;

            }
            catch(Exception e)
            {
                e.printStackTrace();
                logger.error("Excepion -> ",e);

                return false;
            }

        }
        else
        {
            logger.error("Script not found! -> " + filename);
            return false;
        }
    }

    public String executeCommandsFromScriptAndPrintOutput(String filename,
                                                          List<String> params)
    {

        String output = "";

        File commandsFile = new File(filename);
        if(commandsFile.exists())
        {

            logger.info("Executing commands script:" + filename);

            List<String> commandWithParams = new ArrayList<>();

            //Windows specific console command line invocation
            commandWithParams.add("sh");

            //add the script name to be executed
            commandWithParams.add(commandsFile.getAbsolutePath());

            //add all parameters for command line
            if(params != null)
            {
                commandWithParams.addAll(params);
            }

            ProcessBuilder pb = new ProcessBuilder(commandWithParams);

            try
            {

                Process proc = pb.start();


                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));

                // read the output from the command


                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    output += s;
                }

                // read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    output += s;
                }

                //return true;

            }
            catch(Exception e)
            {
                e.printStackTrace();
                logger.error("Excepion -> ",e);

                //return false;
            }

        }
        else
        {
            logger.error("Script not found! -> " + filename);
            //return false;
        }

        return output;
    }

    /**
     * Shuts down the process responsible for the application
     * identified by the given name as parameter
     *
     * @param stopApplicationScriptPath the path to the stop application script
     * @param mainApplicationName the name of application which will be shut down
     * @return boolean which determines if
     *          the shutdown execution was successfully done
     */
    public boolean shutdownApplication(
            String stopApplicationScriptPath,
            String mainApplicationName)
    {
        boolean result = false;

        List<String> params = new ArrayList<>();

        params.add(mainApplicationName);

        result = executeCommandsFromScript(stopApplicationScriptPath,params,true,false);

        return result;
    }


    public void getUserIdleTime(String scriptPath, String outputPath, String outputFilename)
    {


    }



    public void shutdownSystem()
    {
        /*
        * TODO: shutdown UBUNTU system
        * */

        try
        {
            System.out.println("shutdown...");
            logger.info("shutdown linux computer...");

            Runtime runtime = Runtime.getRuntime();

            String[] cmd = {"/bin/bash","-c","echo " + adminPassword +  " | sudo -S shutdown -h now"};

            Process proc = runtime.exec(cmd);
            System.exit(0);

        }
        catch(Exception e)
        {
            logger.error("Exception in shutdownSystem: ", e);
        }

    }



}
