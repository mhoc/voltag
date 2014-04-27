package edu.purdue.voltag.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.interfaces.OnPlayerRegisteredListener;

/** Registers a new player on Parse.
 *  This class will send their information to Parse then set their ParseID into the shared preferences.
 *  You may register a listener which is called when the task is complete, should you desire.
 *  If a user attempts to register with an already recognized email, it gives them the same ID
 *  that Parse already has. */
public class RegisterPlayerTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private Player player;
    private OnPlayerRegisteredListener listener;

    public RegisterPlayerTask(Context c, Player player) {
        this.c = c;
        this.player = player;
    }

    public void setListener(OnPlayerRegisteredListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Set the player's name and email to the shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        prefs.edit().putString(MainActivity.PREF_USER_NAME, player.getUserName()).commit();
        prefs.edit().putString(MainActivity.PREF_USER_EMAIL, player.getEmail()).commit();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Creating a new player on parse.");

        // Prepare shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);

        // Check to see if the player is currently logged in by email
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
        userQuery.whereEqualTo(ParseConstants.PLAYER_EMAIL, player.getEmail());

        // Scan over the results
        try {
            List<ParseObject> results = userQuery.find();

            // If size is 1, we just set the ID to what it already is on Parse
            if (results.size() == 1) {
                Log.d(MainActivity.LOG_TAG, "User's email is already registered. Getting ID.");
                String id = results.get(0).getObjectId();
                prefs.edit().putString(MainActivity.PREF_USER_ID, id).commit();
                return null;
            }

            // If the size is more than 1, a large error has occured.
            if (results.size() > 1) {
                Log.d(MainActivity.LOG_TAG, "Warning: More than one player with that email exists. This is an error.");
                return null;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create the player
        ParseObject parsePlayer = new ParseObject(ParseConstants.PARSE_CLASS_PLAYER);
        parsePlayer.put(ParseConstants.PLAYER_HARDWARE_ID, player.getHardwareID());
        parsePlayer.put(ParseConstants.PLAYER_NAME, player.getUserName());
        parsePlayer.put(ParseConstants.PLAYER_EMAIL, player.getEmail());

        // Save to parse
        try {
            parsePlayer.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the new ID
        String id = parsePlayer.getObjectId();

        // Update the ID of the player in the preferences
        prefs.edit().putString(MainActivity.PREF_USER_ID, id).commit();
        prefs.edit().putBoolean(MainActivity.PREF_ISREGISTERED, true).commit();

        // Call listeners
        if (listener != null) {
            listener.onPlayerRegistered(new Player(id, player.getHardwareID(), player.getUserName(), player.getEmail(), false));
        }

        return null;

    }
}
