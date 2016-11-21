/**
 * Created by mihai on 4/16/16.
 */

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FTPClientApplication {

    public FTPClient client = null;
    public boolean isConnected = false;

    private String server;
    private String username;
    private String password;
    private int port = 21;

    private final static Logger logger = Logger.getLogger(FTPClientApplication.class);


    public FTPClientApplication(FTPCredentials credentials)
    {
        this.server = credentials.server;
        this.username = credentials.user;
        this.password = credentials.pass;
        this.port = credentials.port;

        client = new FTPClient();

        try {
            client.connect(server);
            showServerReply(client);
            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Operation failed. Server reply code: " + replyCode);
                logger.error("Operation failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = client.login(username, password);
            showServerReply(client);
            if (!success) {
                System.out.println("Could not login to the server");
                logger.warn("Could not login to the server: " + replyCode);
                isConnected = false;
                return;
            } else {
                System.out.println("LOGGED IN SERVER");
                logger.info("LOGGED IN SERVER");
                isConnected = true;

            }
        } catch (IOException ex) {
            System.out.println("Oops! Something wrong happened");
            isConnected = false;
            ex.printStackTrace();
            logger.error("Something wrong happened!",ex);
        }
    }

    public FTPClientApplication(String server, int port, String username, String password)
    {

/*        String server = "ftp.gruen.ro";
        int port = 21;
        String user = "gruen";
        String pass = "1661112066";*/

        this.server = server;
        this.username = username;
        this.password = password;
        this.port = port;


        client = new FTPClient();

        try {
            client.connect(server);
            showServerReply(client);
            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Operation failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = client.login(username, password);
            showServerReply(client);
            if (!success) {
                System.out.println("Could not login to the server");
                isConnected = false;
                return;
            } else {
                System.out.println("LOGGED IN SERVER");
                isConnected = true;

            }
        } catch (IOException ex) {
            System.out.println("Oops! Something wrong happened");
            isConnected = false;
            ex.printStackTrace();
        }

    }

    public boolean uploadFile(String localFilePath, String remoteDirectoryPath, String screenshotName)
    {

        boolean result = false;

        if(client != null)
        {

            try{

                //enter passive mode
                client.enterLocalPassiveMode();

                //change current directory
                //client.changeWorkingDirectory(remoteDirectoryPath);


                //get input stream
                InputStream input;
                input = new FileInputStream(localFilePath);
                client.setFileType(FTPClient.BINARY_FILE_TYPE);

                //store the file in the remote server
                if(client.storeFile(remoteDirectoryPath + "/" + screenshotName, input))
                {
                    System.out.println("File uploaded with success!");
                    //logger.info("File uploaded with success!");

                    result = true;
                }
                else
                {
                    System.out.println("File was not uploaded!");
                    logger.error("Error while uploading the files");

                    result = false;
                }


                //close the stream
                input.close();


            }
            catch(IOException e)
            {
                System.out.println("Error while uploading the files");
                logger.error("Error while uploading the files");

                result = false;
            }
        }
        else
        {
            logger.error("FTPClient is null!");
            result = false;
        }

        return result;
    }

    public boolean makeDirecoryIfNotAlreadyExists(String absolutePath)
    {
        boolean result = false;
        try
        {
            if(!client.changeWorkingDirectory(absolutePath))
            {
                client.makeDirectory(absolutePath);
                result = true;
            }
        }
        catch(IOException e)
        {

        }

        return result;
    }

    public void logoutAndDisconnect()
    {
        try
        {
            client.logout();
            client.disconnect();
            isConnected = false;
        }
        catch (IOException e)
        {
            System.out.println("Client cannot logout or disconnect!");
            logger.error("Client cannot logout or disconnect!");
        }

    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    /**
     * Creates a nested directory structure on a FTP server
     * @param dirPath Path of the directory, i.e /projects/java/ftp/demo
     * @return true if the directory was created successfully, false otherwise
     * @throws java.io.IOException if any error occurred during client-server communication
     */
    public boolean makeDirectories(String dirPath)
            throws IOException {
        String[] pathElements = dirPath.split("/");
        int i = pathElements.length;
        if (pathElements != null && pathElements.length > 0) {
            for (String singleDir : pathElements) {
                boolean existed = client.changeWorkingDirectory(singleDir);
                if (!existed) {
                    boolean created = client.makeDirectory(singleDir);
                    if (created) {
                        System.out.println("CREATED directory: " + singleDir);
                        logger.info("CREATED directory: " + singleDir);
                        client.changeWorkingDirectory(singleDir);
                    } else {
                        System.out.println("COULD NOT create directory: " + singleDir);
                        logger.error("COULD NOT create directory: " + singleDir);
                        return false;
                    }
                }
            }
        }


        while(i > 0)
        {
            client.changeWorkingDirectory("../");
            i--;
        }

        return true;
    }



    public static void main(String[] args) throws IOException
    {

        FTPClientApplication ftpCLient =
                new FTPClientApplication("89.45.204.42",21,"fidelia","MMunimulaftp");


        ftpCLient.makeDirectories("html/ClientSupraveghere/images/computer_100/");

        ftpCLient.uploadFile("screenshot_3.jpg",
                "html/ClientSupraveghere/images/computer_100",
                "screenshot_3.jpg");

        //ftpCLient.makeDirectories("ClientSupraveghere/images/");


        ftpCLient.logoutAndDisconnect();

    }
}