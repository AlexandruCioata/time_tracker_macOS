package oscommons;

/**
 * Created by Mihai on 8/28/2016.
 */

public class OSValidator {

    private String OS = "";

    public OSValidator(String operatingSystem)
    {
        this.OS = operatingSystem.toLowerCase();
    }

    public static void main(String[] args) {

        OSValidator validator = new OSValidator("windows");

        System.out.println(validator.isLinux());
        System.out.println(validator.isWindows());

        String operatingSystem = System.getProperty("os.name");
        System.out.println(operatingSystem);
    }

    public boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public boolean isLinux() {

        return (OS.indexOf("linux") >= 0);

    }

    public boolean isSolaris() {

        return (OS.indexOf("sunos") >= 0);

    }

}
