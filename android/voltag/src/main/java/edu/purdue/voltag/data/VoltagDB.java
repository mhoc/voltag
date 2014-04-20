package edu.purdue.voltag.data;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.interfaces.OnGameCreatedListener;

public class VoltagDB extends SQLiteOpenHelper{

    /** Database information */
    public static final String DB_NAME = "voltag_db";
    public static final int DB_VERSION = 1;
    private Context c;

    /** Tables */
    public static final String TABLE_PLAYERS = "t_players";

    /** Table - Players */
    public static final String PLAYERS_PARSE_ID = "player_parse_id";
    public static final String PLAYERS_HARDWARE_ID = "player_hardware_id";
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
                PLAYERS_HARDWARE_ID + " TEXT, " +
                PLAYERS_PARSE_ID + " TEXT, " +
                PLAYERS_NAME + " TEXT, " +
                PLAYERS_EMAIL + " TEXT, " +
                PLAYERS_ISIT + " INTEGER);";

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
            onCreate(db);
        }
    }

    /** Destroys the players table */
    public void dropTablePlayers() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("DROP TABLE " + TABLE_PLAYERS);
            String createTablePlayers = "CREATE " + TABLE_PLAYERS + " (" +
                    PLAYERS_HARDWARE_ID + " TEXT, " +
                    PLAYERS_PARSE_ID + " TEXT, " +
                    PLAYERS_NAME + " TEXT, " +
                    PLAYERS_EMAIL + " TEXT, " +
                    PLAYERS_ISIT + " INTEGER);";
            db.execSQL(createTablePlayers);
        }
    }

    /** Creates a new player on parse.
     *  The ParseID field in the player object should be null. This call will fail if it isn't. */
    public void createPlayerOnParse(final Player p) {

        if (p.getParseID() != null) {
            Log.d(MainActivity.LOG_TAG, "Error in createPlayer(): ParseID field SHOULD be null.");
            return;
        }

        ParseObject player = new ParseObject(ParseConstants.PARSE_CLASS_PLAYER);
        player.put(ParseConstants.PLAYER_HARDWARE_ID, p.getHardwareID());
        player.put(ParseConstants.PLAYER_NAME, p.getUserName());
        player.put(ParseConstants.PLAYER_EMAIL, p.getEmail());

        // Save it to parse
        player.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {

                // Query to get the new ID created
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
                query.whereEqualTo(ParseConstants.PLAYER_HARDWARE_ID, p.getHardwareID());
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        ParseObject user = parseObjects.get(0);
                        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
                        prefs.edit().putString(MainActivity.PREF_USER_ID, user.getString(ParseConstants.CLASS_ID)).commit();
                    }
                });
            }
        });

    }

    /** Creates a new game on parse.
     *  When complete, the game ID is stored in the shared preferences. */
    public void createGameOnParse(final String gameName, final OnGameCreatedListener listener) {

        // Get the user's userID
        final SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final String userID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (userID.equals("")) {
            return;
        }

        // Get the user from parse
        new Thread(new Runnable() {
            public void run() {

                ParseQuery<ParseObject> queryUser = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
                queryUser.whereEqualTo(ParseConstants.CLASS_ID, userID);

                ParseObject currentUser = null;
                try {
                    currentUser = queryUser.find().get(0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Create the game
                ParseObject game = new ParseObject(ParseConstants.PARSE_CLASS_GAME);
                game.put(ParseConstants.GAME_NAME, gameName);
                game.getRelation(ParseConstants.GAME_PLAYERS).add(currentUser);
                game.getRelation(ParseConstants.GAME_TAGGED).add(currentUser);

                // Send the game to the server
                try {
                    game.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Save the ID in the shared preferences
                String id = game.getString(ParseConstants.CLASS_ID);
                prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, id).commit();

                // Call the listener
                listener.onGameCreated(id);
            }
        }).start();


    }


    /** Adds a player to the local database */
    private void addPlayerToDB(Player p) {

        ContentValues values = new ContentValues();
        values.put(PLAYERS_PARSE_ID, p.getParseID());
        values.put(PLAYERS_HARDWARE_ID, p.getHardwareID());
        values.put(PLAYERS_EMAIL, p.getEmail());
        values.put(PLAYERS_NAME, p.getUserName());

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.insert(TABLE_PLAYERS, null, values);
        }

    }

    /** Refreshes the database from parse */
    public void refreshPlayersTable() {
        Log.d(MainActivity.LOG_TAG, "Starting database refresh.");

        // Get current game ID
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

        // TODO: REMOVE THIS AND GET FROM SHARED PREFERENCES
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
                        // Clear out the database
                        dropTablePlayers();

                        // Re-fill it with new players
                        for (ParseObject p : parseObjects) {
                            String playerID = p.getString(ParseConstants.CLASS_ID);
                            String hardwareID = p.getString(ParseConstants.PLAYER_HARDWARE_ID);
                            String playerName = p.getString(ParseConstants.PLAYER_NAME);
                            String playerEmail = p.getString(ParseConstants.PLAYER_EMAIL);
                            Player player = new Player(playerID, hardwareID, playerName, playerEmail);
                            addPlayerToDB(player);
                        }
                    }
                });

            }
        });

    }

    public List<Player> getPlayersInCurrentGame() {

        SQLiteDatabase db = getReadableDatabase();

        if (db != null) {
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PLAYERS + ";", null);
            List<Player> players = new ArrayList<Player>();
            if (c.moveToFirst()) {
                do {
                    String parseID = c.getString(c.getColumnIndex(PLAYERS_PARSE_ID));
                    String hwID = c.getString(c.getColumnIndex(PLAYERS_HARDWARE_ID));
                    String name = c.getString(c.getColumnIndex(PLAYERS_NAME));
                    String email = c.getString(c.getColumnIndex(PLAYERS_EMAIL));
                    Player p = new Player(parseID, hwID, name, email);
                    players.add(p);
                } while (c.moveToNext());
            }
            return players;
        }

        return null;
    }

}
