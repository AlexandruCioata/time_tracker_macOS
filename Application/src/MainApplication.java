import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import oscommons.IOSType;
import oscommons.OSFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by mihai on 4/12/16.
 */
public class MainApplication {

    //private Properties properties = null;

    public static int i = 0;
    public static String uniqueFileName ="";

    //measure the time when no click was received
    public static long timeWhenNoClickWasReceived = 0;

    private static int currentImageSize = 0;
    private static int noOfIdenticalImages = 0;
    private static String rootImagesPathOnServer = "";

    private AppConfig configuration = null;

    private IOSType OSType = null;

    private final static Logger logger = Logger.getLogger(MainApplication.class);

    public MainApplication()
    {

    }

    public MainApplication(Properties properties)
    {
        /*
        * Build AppConfig class from loaded properties
        * */
        this.configuration = new AppConfig(properties);

        /*
        * Choosing local operating system type
        * */
        String operatingSystem = System.getProperty("os.name");
        OSFactory osFactory = new OSFactory(this.configuration.getAdminPassword());

        this.OSType = osFactory.createOSType(operatingSystem);
    }

    public static void main(String args[]) throws Exception
    {

        if(args.length == 0)
        {
            System.out.println("Incorrect arguments! " +
                    "Please give the configuration file path " +
                    "as the first parameter at command line");

            return;
        }


        /* Get application configuration properties
         from configuration file provided in command line arguments
        */
        String configFile = args[0];
        Properties prop = readProperties(configFile);

        /*
        * Load log4j configuration
        * from the same config properties of application
        * */
        PropertyConfigurator.configure(configFile);

        if(prop == null || prop.isEmpty())
        {
            System.out.println("There is no properties in the configuration file provided");
            logger.warn("There is no properties in the configuration file provided");

            return;
        }

        MainApplication application = new MainApplication(prop);

        application.initApplication();

        application.run();

    }

    public void run()
    {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        /*
        * function responsible for taking any keystrokes of user and
        * upload them to the server
        * */
        startKeyLoggerService(rootImagesPathOnServer,executor);

        /*
        * function responsible for listening to the mouse events
        * like click or scroll
        * */
        startMouseListenerService(executor);

        /*
        * function responsible for tracking the current application
        * running on the computer
        * */
        startApplicationsTrackerService(executor);

        /*
        * function responsible for checking the user interaction time
        * with the pc
        * */
        startUserInteractionService(executor);

        executor.shutdown();

        /*
        * take screenshots and save them locally
        * */
        takeScreenshots();
    }

    public void startKeyLoggerService(String rootImagesPathOnServer, ThreadPoolExecutor executor)
    {
        KeyLogger keyLogger = new KeyLogger(
                this.configuration.getCredentials(),
                this.configuration.getImagesLocalRootFolder(),
                rootImagesPathOnServer);

        logger.info("execute.keyLogger..");

        executor.execute(keyLogger);
    }

    public void startMouseListenerService(ThreadPoolExecutor executor)
    {
        GlobalMouseListener mouseListener = new GlobalMouseListener();

        logger.info("execute.mouseListener..");
        executor.execute(mouseListener);
    }

    public void startApplicationsTrackerService(ThreadPoolExecutor executor)
    {
        ApplicationsTracker applicationsTracker = new ApplicationsTracker(this.OSType,configuration);

        logger.info("execute.applicationsTracker..");
        executor.execute(applicationsTracker);

    }

    public void startUserInteractionService(ThreadPoolExecutor executor)
    {
        UserInteractionService userInteractionService = new UserInteractionService(this.OSType,configuration);

        logger.info("execute.userInteractionService..");
        executor.execute(userInteractionService);
    }

    public static Properties readProperties(String configFile)
    {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(configFile);

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();

            logger.error("Exception in MainApplication->readProperties -> ",ex);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return prop;
    }

    public void takeScreenshotAndCompress(String rootPath,
                                           String screenshotName)
    {

        try
        {
            //take screenshot
            BufferedImage image = new Robot().createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            if(timeWhenNoClickWasReceived <
                    this.configuration.getInteractionWithPCTimeOut())
            {
                //compress taken screenshot
                CompressJPEGFIle.compress(image,rootPath,screenshotName);

                //crop image and write it on disk
                BufferedImage bufferedImage = ImageIO.read(
                        Files.newInputStream(Paths.get(rootPath + "/" + screenshotName)));

                String[] tokens = screenshotName.split("\\.(?=[^\\.]+$)");
                CropImage.cropImage(bufferedImage,
                        new Rectangle(0, 0, bufferedImage.getWidth(), 130),
                        rootPath, tokens[0] + "_cropped.jpg");

                i++;
            }
            else
            {
                logger.info("Inactivity! -> no screenshots taken any more: " +
                        "timeWhenNoClickWasReceived= " +
                        timeWhenNoClickWasReceived);

                if(timeWhenNoClickWasReceived >= this.configuration.getSHUTDOWN_TIMEOUT())
                {
                    logger.info("Inactivity -> more than timeout: " +
                            "timeWhenNoClickWasReceived= " + timeWhenNoClickWasReceived);

                    shutDownComputer();
                }
            }
        }
        catch(Exception ex)
        {
            logger.error("Exception in MainApplication->takeScreenshotAndCompress -> ",ex);
        }

    }


    public void shutDownComputer()
    {
        this.OSType.shutdownSystem();
    }

    private int sizeOfImage(BufferedImage image)
    {
        try
        {
            int size = 0;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( image, "jpg", baos );
            baos.flush();
            byte[] imageInByte1 = baos.toByteArray();
            baos.close();

            size = imageInByte1.length;

            return size;
        }
        catch(Exception ex)
        {
            logger.error("Exception in MainApplication->sizeOfImage -> ",ex);
        }

        return 0;
    }

    private void generateUniqueComputerName()
    {
        if(this.configuration.getComputerNumber()>0)
        {
            uniqueFileName = "computer_" + this.configuration.getComputerNumber();
        }
        else
        {
            System.out.println("Incorrect computer number! This number should be a positive Integer");
            logger.error("MainApplication -> generateUniqueComputerName: " +
                    "Incorrect computer number! This number should be a positive Integer!");

            System.exit(0);
        }

    }

    private void takeScreenshots()
    {
        List<String> localFiles = new ArrayList<String>();

        int index = 1;

        while(true)
        {

            long startTime = System.currentTimeMillis();
            try{

                //make if not already exists the root images folder
                File current_date_folder =
                        new File(this.configuration.getImagesLocalRootFolder());

                if(!current_date_folder.exists())
                {
                    current_date_folder.mkdir();
                }

                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String screenshotName = "screenshot" + "_" + index + "_" +
                        (dateFormat.format(date)).toString() + ".jpg";


                String localFilePath = current_date_folder.getAbsolutePath() + "/" + screenshotName;
                localFiles.add(localFilePath);

                //make a screenshot and save it at the local path with screenshot name
                takeScreenshotAndCompress(current_date_folder.getAbsolutePath(),screenshotName);

                index++;
                Thread.sleep(3000);
            }
            catch(Exception e)
            {
                System.out.println("Exceptie: "  + e);
                logger.error("Exception in MainApplication->run->(While(true)) -> ",e);
            }


            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            System.out.println(elapsedTime);

            //compute the time with no interaction with the pc
            timeWhenNoClickWasReceived = timeWhenNoClickWasReceived *1000 + elapsedTime;
            timeWhenNoClickWasReceived = timeWhenNoClickWasReceived/1000;

        }
    }

    private void initApplication()
    {
        logger.info("MainApplication.generateUniqueComputerName()..");
        generateUniqueComputerName();

        //make if not already exists a root cache folder
        File imagesRootFolder = new File(this.configuration.getImagesLocalRootFolder());
        if(!imagesRootFolder.exists())
        {
            imagesRootFolder.mkdir();
        }

        rootImagesPathOnServer = this.configuration.getRootImagesPathOnServer() + "/" +
                uniqueFileName + "/";
    }



}
