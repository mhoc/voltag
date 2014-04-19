package edu.purdue.voltag.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VoltagDB extends SQLiteOpenHelper{

    /** Database information */
    public static final String DB_NAME = "voltag_db";
    public static final int DB_VERSION = 1;

    /** Tables */
    public static final String TABLE_PLAYERS = "t_players";

    /** Table - Players */
    public static final String PLAYERS_ID = "player_id";
    public static final String PLAYERS_EMAIL = "player_email";
    public static final String PLAYERS_ISIT = "player_isit";

    public VoltagDB(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTablePlayers = "CREATE " + TABLE_PLAYERS + " (" +
                PLAYERS_ID + " TEXT, " +
                PLAYERS_EMAIL + " TEXT, " +
                PLAYERS_ISIT + " INTEGER );";

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL(createTablePlayers);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
