package edu.purdue.voltag.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.List;

import edu.purdue.voltag.MainActivity;

public class VoltagDB extends SQLiteOpenHelper{

    /** Database information */
    public static final String DB_NAME = "voltag_db";
    public static final int DB_VERSION = 1;
    private Context c;

    /** Tables */
    public static final String TABLE_PLAYERS = "t_players";

    /** Table - Players */
    public static final String PLAYERS_ID = "player_id";
    public static final String PLAYERS_NAME = "player_name";
    public static final String PLAYERS_EMAIL = "player_email";
    public static final String PLAYERS_ISIT = "player_isit";

    public VoltagDB(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
        this.c = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTablePlayers = "CREATE " + TABLE_PLAYERS + " (" +
                PLAYERS_ID + " TEXT, " +
                PLAYERS_NAME + " TEXT, " +
                PLAYERS_EMAIL + " TEXT, " +
                PLAYERS_ISIT + " INTEGER );";

        if (db != null) {
            db.execSQL(createTablePlayers);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On database upgrade
    }

    /** Destroys the entire database */
    public void destroy() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("DROP TABLE " + TABLE_PLAYERS);
        }
    }

    /** Destroys the players table */
    public void dropTablePlayers() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("DROP TABLE " + TABLE_PLAYERS);
        }
    }

    /** Adds a player to the database */
    public void addPlayer(Player p) {

        ContentValues values = new ContentValues();
        values.put(PLAYERS_ID, p.getHardwareID());
        values.put(PLAYERS_EMAIL, p.getEmail());
        values.put(PLAYERS_NAME, p.getUserName());

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.insert(TABLE_PLAYERS, null, values);
        }

    }

    /** Refreshes the database from parse */
    public void refreshDB() {
        Log.d(MainActivity.LOG_TAG, "Starting database refresh.");

        // Get current game ID
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
        gameID = "wMa6q5KXob";

        // If there's no game, exit
        if (gameID.equals("")) {
            Toast.makeText(c, "No current game.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        query.whereEqualTo(ParseConstants.CLASS_ID, gameID);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseObjects, ParseException e) {

                // Parse should return a list of a single Game which is the game we are currently in
                if (parseObjects.size() > 1) {
                    Toast.makeText(c, "Error in parse query. There should only be 1 game returned.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseObject game = parseObjects.get(0);
                Log.d(MainActivity.LOG_TAG, "Found game " + game.getString(ParseConstants.GAME_NAME));

                // Get relation to current users
                ParseRelation<ParseObject> relation = game.getRelation(ParseConstants.GAME_TAGGED);
                relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        for (ParseObject p : parseObjects) {
                            String playerID = p.getString(ParseConstants.CLASS_ID);
                            String hardwareID = p.getString(ParseConstants.PLAYER_HARDWARE_ID);
                            String playerName = p.getString(ParseConstants.PLAYER_NAME);
                            String playerEmail = p.getString(ParseConstants.PLAYER_EMAIL);
                            Player p = new Player(playerID, hardwareID, playerName, playerEmail);
                            addPlayer(p);
                        }
                    }
                });

            }
        });

    }

}
