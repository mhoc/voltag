package edu.purdue.voltag.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

        // Get current game ID
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

        // If there's no game, exit
        if (gameID.equals("")) {
            return;
        }

        // Query parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
        query.whereEqualTo(ParseConstants.CLASS_ID, gameID);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseObjects, ParseException e) {

            }
        });


    }

}
