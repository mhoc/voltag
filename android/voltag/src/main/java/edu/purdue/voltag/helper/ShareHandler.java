package edu.purdue.voltag.helper;

import android.content.Context;
import android.content.Intent;

/**
 * Created by david on 5/10/14 for android
 */
public final class ShareHandler {

    public static void shareGame(Context context, String id)
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Come join the revolt! Enter the id " + id + " or click here http://voltag.davidtschida.com/"+ id;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join the revolt");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
