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
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnDatabaseRefreshListener;

public class RefreshPlayersTask extends AsyncTask<Void, Void, Void> {

    private Context c;
    private OnDatabaseRefreshListener listener;

    public RefreshPlayersTask(Context c) {
        this.c = c;
    }

    public void setListener(OnDatabaseRefreshListener listener) {
        listener.onDatabaseRefresh();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(MainActivity.LOG_TAG, "Refreshing database.");

        // Prepare the shared preferences
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);

        // Get the current game ID
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
        if (gameID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Error: User is not currently in a game.");
            return null;
        }

        // Get the parse object
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        ParseObject game = null;
        try {
            game = query.get(gameID);
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Game is not recognized by parse.");
            return null;
        }

        // Get all the players in the game
        ParseRelation<ParseObject> relationPlayersInGame = game.getRelation(ParseConstants.GAME_PLAYERS);
        List<ParseObject> players = null;
        try {
            players = relationPlayersInGame.getQuery().find();
        } catch (ParseException e) {
            Log.d(MainActivity.LOG_TAG, "Error: Error in parse relation. IDK why this would be here.");
            e.printStackTrace();
            return null;
        }

        // Get the player who is IT
        ParseRelation<ParseObject> relationPlayerIt = game.getRelation(ParseConstants.GAME_TAGGED);
        ParseObject isIt = null;
        try {
            isIt = relationPlayerIt.getQuery().find().get(0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create the database and clear it out
        VoltagDB db = VoltagDB.getDB(c);
        db.dropTablePlayers();

        // Refill it with the new players
        for (ParseObject player : players) {

            // Get player information
            String playerID = player.getObjectId();
            String hardwareID = player.getString(ParseConstants.PLAYER_HARDWARE_ID);
            String playerName = player.getString(ParseConstants.PLAYER_NAME);
            String playerEmail = player.getString(ParseConstants.PLAYER_EMAIL);

            // Determine if they are it or not
            boolean isItBool = false;
            if (playerID.equals(isIt.getObjectId())) {
                isItBool = true;
            }

            // Add it to the database
            Player p = new Player(playerID, hardwareID, playerName, playerEmail, isItBool);
            db.addPlayerToDB(p);

        }

        // Alert listeners
        if (listener != null) {
            listener.onDatabaseRefresh();
        }

        return null;
    }
}
