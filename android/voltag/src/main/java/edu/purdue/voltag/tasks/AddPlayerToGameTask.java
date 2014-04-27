package edu.purdue.voltag.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.data.Game;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.interfaces.OnJoinedGameListener;

/** Adds a player to a game on Parse
 *  Updates all of the local database and shared preferences
 *  Pass in a listener to be alerted when it fails. */
public class AddPlayerToGameTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private String gameID;
    private OnJoinedGameListener listener;

    public AddPlayerToGameTask(Context c, String gameID) {
        this.c = c;
        this.gameID = gameID;
    }

    public void setListener(OnJoinedGameListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Set shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, gameID).commit();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Adding player to existing game on Parse.");

        // Prepare shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);

        // Get the user's ID
        String userID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (userID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: User is not signed in.");
            return null;
        }

        // Query parse for the game
        ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        ParseObject game = null;
        try {
            game = gameQuery.get(gameID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Game ID does not exist on Parse.");
            listener.onJoinedGame(null);
            return null;
        }

        // Query parse for the player
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
        ParseObject player = null;
        try {
            player = userQuery.get(userID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: User ID is not recognized by Parse.");
            return null;
        }

        // Get game's relation to players
        ParseRelation<ParseObject> gamePlayersRelation = game.getRelation(ParseConstants.GAME_PLAYERS);

        // Add the user to the players
        gamePlayersRelation.add(player);

        // Save to parse
        try {
            game.save();
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Saving game to parse failed.");
            e.printStackTrace();
        }

        // Get game name
        String gameName = game.getString(ParseConstants.GAME_NAME);

        // Save information to local shared preferences
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_NAME, gameName).commit();
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, gameID).commit();
        prefs.edit().putBoolean(MainActivity.PREF_ISIT, false).commit();

        // Call listeners
        if (listener != null) {
            listener.onJoinedGame(new Game(gameID, gameName));
        }

        return null;
    }
}
