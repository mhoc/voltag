package edu.purdue.voltag.data;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public String getGravitarURL() {

        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = email.toLowerCase().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] thedigest = md.digest(bytesOfMessage);

        String hash =  new String(thedigest);

        return "http://www.gravatar.com/avatar/" + hash;
    }

    public void setIsIt(boolean isIt) {
        this.isIt = isIt;
    }

}
