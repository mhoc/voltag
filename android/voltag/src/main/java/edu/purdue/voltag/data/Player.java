package edu.purdue.voltag.data;

/**
 * Created by mike on 4/19/14.
 */
public class Player {

    private String hardwareID;
    private String userName;
    private String email;
    private boolean isIt = false;


    public Player(String hardwareID, String userName, String email) {
        this.hardwareID = hardwareID;
        this.userName = userName;
        this.email = email;
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
