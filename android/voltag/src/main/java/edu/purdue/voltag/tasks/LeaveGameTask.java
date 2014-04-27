package edu.purdue.voltag.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.List;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.data.Game;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.interfaces.OnLeaveGameListener;

public class LeaveGameTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private OnLeaveGameListener listener;

    public LeaveGameTask(Context c) {
        this.c = c;
    }

    public void setListener(OnLeaveGameListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Set shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, "").commit();
        prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_NAME, "").commit();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Dropping player from game on Parse.");

        // Set up shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Get current game ID
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
        if (gameID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: Player is not currently in any game.");
            return null;
        }

        // Get the player ID
        String playerID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (playerID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: Player is not logged in.");
            return null;
        }

        // Get whether they are currently IT
        boolean playerIsIt = prefs.getBoolean(MainActivity.PREF_ISIT, false);

        // Query parse for the game object
        ParseQuery<ParseObject> gameQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        ParseObject game = null;
        try {
            game = gameQuery.get(gameID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Game does not exist with this id.");
            return null;
        }

        // Query parse for the player object
        ParseQuery<ParseObject> playerQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
        ParseObject player = null;
        try {
            player = playerQuery.get(playerID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Player does not exist on Parse.");
            return null;
        }

        // Get the players relation on the game
        ParseRelation<ParseObject> players = game.getRelation(ParseConstants.GAME_PLAYERS);
        try {
            List<ParseObject> playersInGame = players.getQuery().find();

            if (playersInGame.size() == 1) {
                // Just delete the game, because they are the last player and they left.
                game.delete();

            } else {
                // Remove the player
                players.remove(player);

                // If they were it, assign the next player in the game to be it.
                if (playerIsIt) {
                    ParseRelation<ParseObject> itRelation = game.getRelation(ParseConstants.GAME_TAGGED);
                    itRelation.remove(player);
                    itRelation.add(playersInGame.get(0));
                }

                game.save();
            }
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: IDFK what would go wrong here.");
        }

        // Alert listeners
        String gameName = game.getString(ParseConstants.GAME_NAME);
        if (listener != null) {
            listener.onLeaveGame(new Game(gameID, gameName));
        }

        return null;
    }
}
