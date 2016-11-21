/**
 * Created by mihai on 7/17/16.
 */
public class FTPCredentials {

    public  String server = "89.45.206.118";
    public  int port = 21;
    public  String user = "fidelia";
    public  String pass = "MMunimulaftp";

    public FTPCredentials(){}

    public FTPCredentials(String server,
                          int port,
                          String user,
                          String password)
    {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = password;
    }

}

