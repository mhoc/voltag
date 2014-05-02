package edu.purdue.voltag.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.helper.ImageHelper;
import edu.purdue.voltag.interfaces.OnPlayerBitmapLoadedListener;

public class LoadPlayerBitmapTask extends AsyncTask<Void, Void, Bitmap> {

    private Context c;
    private Player toLoad;
    private int size = -1;
    private OnPlayerBitmapLoadedListener listener;

    public LoadPlayerBitmapTask(Context c, Player toLoad, int size) {
        this.c = c;
        this.toLoad = toLoad;
        this.size = size;
    }

    public void setListener(OnPlayerBitmapLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        // Generate the URL
        final String baseUrl = "http://www.gravatar.com/avatar/";
        final String processedAddress = toLoad.getEmail().toLowerCase();

        // Get the hash code and generate the URL
        final String hashCode = getEmailMD5();
        final String url = baseUrl + hashCode + "?s=" + size;

        // Create the http url connection
        HttpURLConnection httpURLConnection = null;
        Bitmap b = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            final InputStream is = httpURLConnection.getInputStream();
            Bitmap gravitar = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
            b = ImageHelper.getRoundedCornerBitmap(gravitar, 500);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }

        // Call the listener
        if (listener != null) {
            listener.onPlayerBitmapLoaded(toLoad, b);
        }

        return b;
    }

    private String getEmailMD5() {

        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(toLoad.getEmail().getBytes());
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

}
