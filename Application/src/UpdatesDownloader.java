import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import oscommons.IOSType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mihai on 7/17/16.
 */
public class UpdatesDownloader implements Runnable{


    private AppConfig appConfig = null;

    private FTPCredentials credentials = null;

    private IOSType osType = null;

    private final static Logger logger = Logger.getLogger(UpdatesDownloader.class);


    public UpdatesDownloader(IOSType osType,
                             AppConfig appConfig)
    {
        this.osType = osType;
        this.appConfig = appConfig;
        this.credentials = appConfig.getCredentials();
    }

    public UpdatesDownloader(){}

    public void run()
    {
        while(true)
        {
            try
            {
                verifyAndDownloadUpdates();
            }
            catch (Exception e)
            {
                System.out.println("Exception in UpdatesDownloader! -> " + e);
                logger.error("Exception in UpdatesDownloader! -> ",e);
            }


            try
            {
                Thread.sleep(3600000);
            }
            catch(Exception e)
            {
                System.out.println("Exception in UpdatesDownloader! -> " + e);
                logger.error("Exception in UpdatesDownloader! -> ",e);
            }

        }

    }


    public void moveFilesFromTo(String source, String dest)
    {

        File sourceDir = new File(source);
        File destDir = new File(dest);

        try{

            if(sourceDir.exists() && destDir.exists())
            {
                File[] files = sourceDir.listFiles();
                if(null != files)
                {
                    for(File file:files)
                    {

                        if(file.isDirectory())
                        {
                            File newDir = new File(dest + "/" + file.getName());
                            if(!newDir.exists())
                            {
                                newDir.mkdir();
                            }

                            moveFilesFromTo(source + "/" + newDir.getName(), dest + "/" + newDir.getName());

                            //Files.delete(Paths.get(source + "/" + newDir.getName()));
                            File directory = new File(source + "/" + newDir.getName());
                            directory.delete();
                        }
                        else
                        {

                            Files.move(Paths.get(file.getAbsolutePath()),
                                    Paths.get(dest + "/" + file.getName()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception: Cannot move updates from " + source +" to " + dest);
            e.printStackTrace();

            logger.error("Exception: Cannot move updates from " + source +" to " + dest, e);
        }

    }

    public boolean downloadFromServer(String remotePath, String localPath)
    {
        boolean result = true;

        String server = credentials.server;
        int port = credentials.port;
        String user = credentials.user;
        String pass = credentials.pass;

        String tmpPath = localPath + "/tmp";
        File saveDirFileFile = new File(tmpPath);
        if(!saveDirFileFile.exists())
        {
            saveDirFileFile.mkdir();
        }

        FTPClient ftpClient = new FTPClient();

        try {
            // connect and login to the server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();

            System.out.println("Connected");

            if(!downloadRecursivelyFilesFrom(ftpClient, remotePath, tmpPath))
            {
                result = false;
            }
            else
            {
                result = true;
            }

            // log out and disconnect from the server
            ftpClient.logout();
            ftpClient.disconnect();

            System.out.println("Disconnected");


        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Exception in UpdatesDownloader -> downloadFromServer",ex);

            return false;
        }

        return result;

    }

    public boolean downloadRecursivelyFilesFrom(FTPClient ftpClient,
                                                String parentDir,
                                                String saveDir)
    {

        try
        {
            String dirToList = parentDir;

            FTPFile[] subFiles = ftpClient.listFiles(dirToList);

            if(subFiles != null && subFiles.length > 0)
            {
                for(FTPFile file:subFiles)
                {

                    if(file.isDirectory())
                    {
                        File newDir = new File(saveDir + "/" + file.getName());
                        if(!newDir.exists())
                        {
                            newDir.mkdir();
                        }

                        downloadRecursivelyFilesFrom(ftpClient,parentDir + "/" + file.getName(),
                                saveDir + "/" + file.getName());
                    }
                    else
                    {

                        boolean success = downloadSingleFile(ftpClient, parentDir + "/" + file.getName(),
                                saveDir + "/" + file.getName());
                        if (success) {
                            System.out.println("DOWNLOADED the file: " + saveDir + "/" + file.getName());
                            logger.info("DOWNLOADED the file: " + saveDir + "/" + file.getName());

                        } else
                        {
                            System.out.println("COULD NOT download the file: "
                                    + saveDir + "/" + file.getName());
                            logger.info("COULD NOT download the file: "
                                    + saveDir + "/" + file.getName());
                        }
                    }

                }
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("Exception in UpdatesDownloader -> downloadRecursivelyFilesFrom",e);
        }



      /*  FTPFile[] directories = ftpClient.listDirectories();
        if(directories != null && directories.length > 0)
        {
            for(FTPFile directory:directories)
            {
                File newDir = new File(saveDir + "/" + directory.getName());
                if(!newDir.exists())
                {
                    newDir.mkdir();
                }

                downloadFrom(ftpClient,parentDir + "/" + directory.getName(),
                        saveDir + "/" + directory.getName());
            }
        }*/


/*        if(downloadSingleFile(ftpClient,parentDir + "/update_info.txt",
                saveDir + "/update_info.txt"))
        {
            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile aFile : subFiles)
                {
                    if(!aFile.getName().equals("update_info.txt"))
                    {
                        boolean success = downloadSingleFile(ftpClient, parentDir + "/" + aFile.getName(),
                                saveDir + "/" + aFile.getName());
                        if (success) {
                            System.out.println("DOWNLOADED the file: " + saveDir + "/" + aFile.getName());
                        } else
                        {
                            System.out.println("COULD NOT download the file: "
                                    + saveDir + "/" + aFile.getName());
                        }
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }*/
        return true;
    }

    public void verifyAndDownloadUpdates()
    {
        File file = new File(appConfig.getLocalUpdatesDirPath() + "/confirmed_updates");
        if(!file.exists())
        {
            file.mkdir();
        }

        if(!downloadFromServer(appConfig.getRemoteUpdatesDirPath(),
                appConfig.getLocalUpdatesDirPath()))
        {
            deleteFolderContent(appConfig.getLocalUpdatesDirPath() + "/tmp");
        }
        else
        {
            confirmUpdates(appConfig.getLocalUpdatesDirPath());
        }

    }

    public int noFilesAndSubfiles(String directory)
    {
        int result = 0;

        File folder = new File(directory);
        if(folder.exists())
        {
            File[] files = folder.listFiles();
            if(files != null)
            {
                for(File f:files)
                {
                    if(f.isDirectory())
                    {
                        result += noFilesAndSubfiles(directory + "/" + f.getName());
                    }
                    else
                    {
                        result++;
                    }
                }
            }
        }

        return result;
    }

    public void confirmUpdates(String basePath)
    {
        String tmpFolderPath = basePath + "/tmp";
        String confirmedUpdatesPath = basePath + "/confirmed_updates";

        int no_files_downloaded = 0;

        no_files_downloaded = noFilesAndSubfiles(tmpFolderPath);


        try
        {
            File verificationFile = new File(tmpFolderPath + "/application/update_info.txt");
            if(verificationFile.exists())
            {
                BufferedReader br = new BufferedReader(new FileReader(verificationFile));

                String versionLine = br.readLine();
                String no_filesLine = br.readLine();

                br.close();

                String[] versionLineParts = versionLine.split(":");
                String[] no_filesLineParts = no_filesLine.split(":");
                if(versionLineParts.length > 1 && no_filesLineParts.length > 1)
                {
                    if(Integer.parseInt(no_filesLineParts[1]) == no_files_downloaded)
                    {
                        System.out.println("deleting existing data from confirmed_updates...");
                        deleteFolderContent(confirmedUpdatesPath);

                        System.out.println("moving existing data from tmp to confirmed_updates...");
                        moveFilesFromTo(tmpFolderPath,confirmedUpdatesPath);

                        System.out.println("deleting existing data from tmp...");
                        deleteFolderContent(tmpFolderPath);

                        /*
                        * Move confirmed updates manager files to the updates manager folder
                        * */
                        File f = new File(appConfig.getUpdatesManagerPath());
                        if(!f.exists())
                        {
                            f.mkdir();
                        }

                        /*
                        * Stop any process that runs the updatesManager jar file
                        * to prevent the exception that appear when moving the new file
                        *
                        * */
                        //TODO:
                        String shutdownUpdatesAppPath = appConfig.getUpdatesManagerStopScript();
                        String updatesConfigAppName = appConfig.getUpdatesManagerAppName();

                        if(osType.shutdownApplication(shutdownUpdatesAppPath,updatesConfigAppName))
                        {
                            logger.info("updates manager stopped correctly");
                        }
                        else
                        {
                            logger.error("updates manager did not stop corectly");
                        }

                        moveFilesFromTo(confirmedUpdatesPath + "/updates_manager",
                                appConfig.getUpdatesManagerPath());

                        System.out.println("Applying downloaded updates...");
                        startUpdateService();
                    }
                    else
                    {
                        System.out.println("deleting existing data from tmp...");
                        deleteFolderContent(tmpFolderPath);
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Could not confirm updates!");
            System.out.println("deleting existing data from tmp (for verification)...");

            logger.info("Could not confirm updates!");
            logger.info("deleting existing data from tmp (for verification)...");
            logger.error("Exception: ",e);

            deleteFolderContent(tmpFolderPath);
        }
    }

    /**
    *   This method starts a new thread responsible to apply updates
    *   and restart the application at the latest version
    * */
    private void startUpdateService()
    {

        System.out.println("Starting updates manager...");
        logger.info("Starting updates manager...");


        List<String> commandLineParams = new ArrayList<>();

        commandLineParams.add(appConfig.getUpdatesManagerPath());
        commandLineParams.add(appConfig.getUpdatesManagerPath() +
                "/updatesConfig.properties");

        this.osType.startApplication(appConfig.getUpdatesManagerStartScript(),
                commandLineParams);

        //startApplication(updatesManagerStartScript);

    }


    public static void deleteFolderContent(String path)
    {
        File directory = new File(path);
        if(directory.exists())
        {
            File[] files = directory.listFiles();
            if(null != files)
            {
                for(File file:files)
                {
                    if(file.isDirectory())
                    {
                        deleteFolderContent(path + "/" + file.getName());
                    }
                    else
                    {
                        file.delete();
                    }

                }
            }
        }
    }


    /**
     * Download a single file from the FTP server
     * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param remoteFilePath path of the file on the server
     * @param savePath path of directory where the file will be stored
     * @return true if the file was downloaded successfully, false otherwise
     * @throws java.io.IOException if any network or IO error occurred.
     */
    public static boolean downloadSingleFile(FTPClient ftpClient,
                                             String remoteFilePath, String savePath) throws IOException {
        File downloadFile = new File(savePath);

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
