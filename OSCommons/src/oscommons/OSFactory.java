package oscommons;

/**
 * Created by Mihai on 8/28/2016.
 */
public class OSFactory {

    private String password = "";

    public OSFactory(String password)
    {
        this.password = password;
    }

    public IOSType createOSType(String type)
    {
        OSValidator validator = new OSValidator(type);
        IOSType operatingSystemType = null;

        if(validator.isLinux())
        {
            operatingSystemType = new LinuxOSType(password);
        }
        else if(validator.isMac())
        {
            operatingSystemType = new MacOSType(password);
        }

        return operatingSystemType;
    }

}
