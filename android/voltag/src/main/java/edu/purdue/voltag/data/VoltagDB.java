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
import edu.purdue.voltag.interfaces.OnDBRefreshListener;
import edu.purdue.voltag.interfaces.OnGameCreatedListener;
import edu.purdue.voltag.interfaces.OnUserCreatedListener;

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
    public void createPlayerOnParse(final Player p, final OnUserCreatedListener listener) {

        if (p.getParseID() != null) {
            Log.d(MainActivity.LOG_TAG, "Error in createPlayer(): ParseID field SHOULD be null.");
            return;
        }

        new Thread(new Runnable() {
            public void run() {

                // Check to see if the user has already logged in
                ParseQuery<ParseObject> userQuery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
                userQuery.whereEqualTo(ParseConstants.PLAYER_EMAIL, p.getEmail());

                List<ParseObject> users = null;
                try {
                    users = userQuery.find();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (users.size() >= 1) {
                    // Don't add the user
                    // TODO: Add the ID if they are logged in already
                    Toast.makeText(c, "You're already logged in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create the player
                ParseObject player = new ParseObject(ParseConstants.PARSE_CLASS_PLAYER);
                player.put(ParseConstants.PLAYER_HARDWARE_ID, p.getHardwareID());
                player.put(ParseConstants.PLAYER_NAME, p.getUserName());
                player.put(ParseConstants.PLAYER_EMAIL, p.getEmail());

                // Save to parse
                try {
                    player.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // When complete, query to get the new ID
                ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
                query.whereEqualTo(ParseConstants.PLAYER_HARDWARE_ID, p.getHardwareID());

                ParseObject user = null;
                try {
                    user = query.find().get(0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Update their ID in shared preferences
                SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
                prefs.edit().putString(MainActivity.PREF_USER_ID, user.getObjectId()).commit();
                prefs.edit().putBoolean(MainActivity.PREF_ISREGISTERED, true).commit();

                // Alert listeners
                if (listener != null) {
                    listener.onUserCreated(user.getObjectId());
                }

            }
        }).start();

    }

    /** Creates a new game on parse.
     *  When complete, the game ID is stored in the shared preferences. */
    public void createGameOnParse(final String gameName, final OnGameCreatedListener listener) {

        // Get the user's userID
        final SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final String userID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (userID.equals("")) {
            Log.d(MainActivity.LOG_TAG, "Cannot create map. User is not logged in.");
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
                String id = game.getObjectId();
                prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, id).commit();

                // Call the listener
                if (listener != null) {
                    listener.onGameCreated(id);
                }
            }
        }).start();


    }

    /** Adds a player to a given game on parse */
    public void addPlayerToGameOnParse(final String gameID) {

        // Get user ID
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final String userID = prefs.getString(MainActivity.PREF_USER_ID, "");
        if (userID.equals("")) {
            Toast.makeText(c, "User is not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Spawn off a thread
        new Thread(new Runnable() {
            public void run() {

                // Query parse for the game
                ParseQuery<ParseObject> gamequery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_GAME);
                gamequery.whereEqualTo(ParseConstants.CLASS_ID, gameID);

                ParseObject game = null;
                try {
                    game = gamequery.find().get(0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Query parse for the player
                ParseQuery<ParseObject> userquery = ParseQuery.getQuery(ParseConstants.PARSE_CLASS_PLAYER);
                userquery.whereEqualTo(ParseConstants.CLASS_ID, userID);

                ParseObject user = null;
                try {
                    user = userquery.find().get(0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Update game's relational reference to its player objects
                game.getRelation(ParseConstants.GAME_PLAYERS).add(user);

                // Save the game to parse
                try {
                    game.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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
    public void refreshPlayersTable(final OnDBRefreshListener listener) {
        Log.d(MainActivity.LOG_TAG, "Starting database refresh.");

        // Get current game ID
        SharedPreferences prefs = c.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

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

                        // Alert listeners
                        if (listener != null) {
                            listener.onDBRefresh();
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
