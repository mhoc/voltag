package edu.purdue.voltag.data;

/**
 * Created by mike on 4/19/14.
 */
public class Player {

    private String parseID;
    private String hardwareID;
    private String userName;
    private String email;
    private boolean isIt = false;


    public Player(String parseID, String hardwareID, String userName, String email) {
        this.parseID = parseID;
        this.hardwareID = hardwareID;
        this.userName = userName;
        this.email = email;
    }

    public String getParseID() {
        return this.parseID;
    }

    public String getHardwareID() {
        return this.hardwareID;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setIsIt(boolean isIt) {
        this.isIt = isIt;
    }

}
