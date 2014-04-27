package edu.purdue.voltag.tasks;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.data.Game;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.interfaces.OnGameCreatedListener;

public class CreateGameTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private String gameName;
    private OnGameCreatedListener listener;

    public CreateGameTask(Context c, String gameName) {
        this.c = c;
        this.gameName = gameName;
    }

    public void setListener(OnGameCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Set the information in the shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_NAME, gameName).commit();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Creating a new game on Parse.");

        // Prepare the shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);

        // Get the user's ID
        String userID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (userID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: User is not signed in.");
            return null;
        }

        // Get the user object from parse
        ParseObject user = null;
        try {
            ParseQuery<ParseObject> queryUser = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
            user = queryUser.get(userID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: User creating game does not exist on Parse.");
            e.printStackTrace();
            return null;
        }

        // Create the game
        ParseObject game = new ParseObject(ParseConstants.PARSE_CLASS_GAME);
        game.put(ParseConstants.GAME_NAME, gameName);

        // Add the current user as a player and as the tagged player
        game.getRelation(ParseConstants.GAME_PLAYERS).add(user);
        game.getRelation(ParseConstants.GAME_TAGGED).add(user);

        // Save the game
        try {
            game.save();
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Saving new game to Parse has failed.");
            e.printStackTrace();
        }

        // Set the shared preferences ID and name
        String id = game.getObjectId();
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_NAME, gameName).commit();
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, id).commit();

        // Set the player as tagged locally
        prefs.edit().putBoolean(MainActivity.PREF_ISIT, true).commit();

        // Call listeners
        if (listener != null) {
            listener.onGameCreated(new Game(id, gameName));
        }

        return null;
    }
}
