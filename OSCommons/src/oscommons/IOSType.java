package oscommons;

import java.util.List;

/**
 * Created by mihai on 8/27/2016.
 */
public interface IOSType {

    void startApplication(String filename, List<String> params);

    boolean executeCommandsFromScript(String filename,
                                      List<String> params,
                                      boolean waitFor,
                                      boolean isOutputEnabled);

    boolean shutdownApplication(String filename, String mainApplicationName);

    void getFocusedApplication(String scriptPath, String outputPath, String outputFilename);

    void getVisitedSites(String scriptPath, String outputPath, String outputFilename);

    String executeCommandsFromScriptAndPrintOutput(String filename,
                                                          List<String> params);

    //void getUserIdleTime(String scriptPath, String outputPath, String outputFilename);

    void shutdownSystem();


}
