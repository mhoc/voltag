package edu.purdue.voltag.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.purdue.voltag.helper.ImageHelper;
import edu.purdue.voltag.lobby.ImageRenderer;

/**
 * Created by mike on 4/19/14.
 */
public class Player implements ImageRenderer {

    private String parseID;
    private String hardwareID;
    private String userName;
    private String email;
    private boolean isIt = false;


    public Player(String parseID, String hardwareID, String userName, String email, boolean isIt) {
        this.parseID = parseID;
        this.hardwareID = hardwareID;
        this.userName = userName;
        this.email = email;
        this.isIt = isIt;
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

    public boolean getIsIt() {
        return this.isIt;
    }

    private String getEmailMD5() {

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

    public Bitmap getGravitar(int size) {
        final String baseUrl = "http://www.gravatar.com/avatar/";
        final String processedAddress = getEmail();

        final String hashCode = getEmailMD5();
        final String url = baseUrl + hashCode + "?s=" + size + "&d=blank";

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            final InputStream is = httpURLConnection.getInputStream();
            Bitmap gravitar = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
            return ImageHelper.getRoundedCornerBitmap(gravitar, 500);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }
        return null;
    }

    public void setIsIt(boolean isIt) {
        this.isIt = isIt;
    }

    @Override
    public Bitmap renderBitmap(int sizePX) {
        return getGravitar(sizePX);
    }

    @Override
    public String getUniqueImageId() {
        return getEmail();
    }
}
