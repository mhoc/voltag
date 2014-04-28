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
import edu.purdue.voltag.interfaces.OnPlayerTaggedListener;

public class TagPlayerTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private OnPlayerTaggedListener listener;

    public TagPlayerTask(Context c) {
        this.c = c;
    }

    public void setListener(OnPlayerTaggedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Tagging player on Parse.");

        // Prepare shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Determine if they are already it. If so, fail.
        boolean isIt = prefs.getBoolean(MainActivity.PREF_ISIT, false);
        if (isIt) {
            Log.d(MainActivity.LOG_TAG, "Error: Cannot tag a player who is already it.");
            return null;
        }

        // Get the GameID
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
        if (gameID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: Player is not currently in a game.");
            return null;
        }

        // Get the player id
        String playerID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (playerID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: Player is not logged in.");
            return null;
        }

        // Query parse to get the game
        ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        ParseObject game = null;
        try {
            game = gameQuery.get(gameID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Game does not exist on Parse.");
            return null;
        }

        // Query parse to get the user
        ParseQuery<ParseObject> playerQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
        ParseObject player = null;
        try {
            player = playerQuery.get(playerID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Player does not exist on Parse.");
            return null;
        }

        // Query parse to get the currently tagged player
        ParseObject playerIt = null;
        try {
            playerIt = game.getRelation(ParseConstants.GAME_TAGGED).getQuery().find().get(0);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Could not find tagged player relation on Parse.");
        }

        // Remove the player who is currently IT
        game.getRelation(ParseConstants.GAME_TAGGED).remove(playerIt);

        // Add the currently player as IT
        game.getRelation(ParseConstants.GAME_TAGGED).add(player);

        // Set the shared preferences
        prefs.edit().putBoolean(MainActivity.PREF_ISIT, true).commit();

        // Create a tag object on Parse
        ParseObject tag = new ParseObject(ParseConstants.PARSE_CLASS_TAG);
        tag.getRelation(ParseConstants.TAG_GAME).add(game);
        tag.getRelation(ParseConstants.TAG_PLAYER_IT).add(player);
        tag.getRelation(ParseConstants.TAG_PLAYER_TAGGED).add(playerIt);

        // Save the game and tag
        try {
            Log.d("debug", "before tag save");
            tag.save();
            Log.d("debug", "after tag save,before game save");
            game.save();
            Log.d("debug", "after game save");
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Could not save either the Tag or the Game");
            e.printStackTrace();
            return null;
        }

        // Alert the listener
        if (listener != null) {
            listener.onPlayerTagged();
        }

        return null;
    }
}
