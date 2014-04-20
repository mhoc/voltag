package edu.purdue.voltag.data;

import android.util.Log;

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
        return this.email.toLowerCase();
    }

    public String getGravitarURL() {

        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(getEmail().getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setIsIt(boolean isIt) {
        this.isIt = isIt;
    }

}
