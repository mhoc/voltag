package edu.purdue.voltag.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class VoltagDB extends SQLiteOpenHelper {

    /**
     * Database information
     */
    private static VoltagDB db_instance;
    public static final String DB_NAME = "voltag_db";
    public static final int DB_VERSION = 1;
    private Context c;

    /**
     * Tables
     */
    public static final String TABLE_PLAYERS = "t_players";

    /**
     * Table - Players
     */
    public static final String PLAYERS_PARSE_ID = "player_parse_id";
    public static final String PLAYERS_HARDWARE_ID = "player_hardware_id";
    public static final String PLAYERS_NAME = "player_name";
    public static final String PLAYERS_EMAIL = "player_email";
    public static final String PLAYERS_ISIT = "player_isit";

    public static VoltagDB getDB(Context c) {
        if (db_instance == null) {
            db_instance = new VoltagDB(c);
        }
        return db_instance;
    }

    private VoltagDB(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
        this.c = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTablePlayers = "CREATE TABLE " + TABLE_PLAYERS + " (" +
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

    /**
     * Destroys the entire database
     */
    public void destroy() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("DROP TABLE " + TABLE_PLAYERS + ";");
        }
        db.close();
    }

    /**
     * Destroys the players table
     */
    public void dropTablePlayers() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("DELETE FROM " + TABLE_PLAYERS + ";");
        }
        db.close();
    }

    /**
     * Adds a player to the local database
     */
    public void addPlayerToDB(Player p) {

        ContentValues values = new ContentValues();
        values.put(PLAYERS_PARSE_ID, p.getParseID());
        values.put(PLAYERS_HARDWARE_ID, p.getHardwareID());
        values.put(PLAYERS_EMAIL, p.getEmail());
        values.put(PLAYERS_NAME, p.getUserName());
        values.put(PLAYERS_ISIT, p.getIsIt() ? 1 : 0);

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.insert(TABLE_PLAYERS, null, values);
        }
        db.close();

    }

    /**
     * Returns a list of all the players in the game based upon what is currently stored in the database.
     * Note that you can call refreshPlayersTable() before calling this. And actually, because refreshPlayerTable()
     * is asynchronous, utilize the OnAsyncCompletedListener to only call getPlayersInCurrentGame after the async
     * call is complete.
     */
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
                    int isIt = c.getInt(c.getColumnIndex(PLAYERS_ISIT));

                    Player p = new Player(parseID, hwID, name, email, isIt == 1 ? true : false);
                    players.add(p);
                } while (c.moveToNext());
            }
            c.close();
            db.close();
            return players;
        }

        return null;
    }

}
