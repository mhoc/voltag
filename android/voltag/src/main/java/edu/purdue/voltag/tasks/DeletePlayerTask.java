package edu.purdue.voltag.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.interfaces.OnPlayerDeletedListener;


public class DeletePlayerTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private OnPlayerDeletedListener listener;

    public DeletePlayerTask(Context c) {
        this.c = c;
    }

    public void setListener(OnPlayerDeletedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Deleting player from parse.");

        // Prepare shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Get player ID
        String playerID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (playerID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: Player is not signed in.");
            return null;
        }

        // Query parse for the player obejct
        ParseQuery<ParseObject> playerQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
        ParseObject player = null;
        try {
            player = playerQuery.get(playerID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Player does not exist on Parse");
            return null;
        }

        // Delete the player
        try {
            player.delete();
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Player could not be deleted");
            return null;
        }

        // Clear out the shared preferences
        prefs.edit().putString(MainActivity.PREF_USER_ID, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_EMAIL, "").commit();
        prefs.edit().putString(MainActivity.PREF_USER_NAME, "").commit();

        // Call the listener
        if (listener != null) {
            listener.onPlayerDeleted();
        }

        return null;
    }
}
