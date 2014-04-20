package edu.purdue.voltag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.Parse;

import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.fragments.CreateGameFragment;
import edu.purdue.voltag.fragments.GameChoiceFragment;
import edu.purdue.voltag.fragments.GameLobbyFragment;
import edu.purdue.voltag.fragments.RegistrationFragment;


public class MainActivity extends Activity {

    public static final String LOG_TAG = "voltag_log";
    public static final String PREFS_NAME = "voltag_prefs";
    public static final String PREF_CURRENT_GAME_ID = "current_game_id";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_EMAIL = "user_email";
    public static final String PREF_ISREGISTERED = "is_registered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Parse.initialize(this, ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
        new Thread(new Runnable() {
            public void run() {
                Parse.initialize(MainActivity.this, ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
            }
        }).start();

        String gameId;
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
        gameId = settings.getString(MainActivity.PREF_CURRENT_GAME_ID,"");
        if(!(gameId.equals(""))){
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();

        }
        setContentView(R.layout.activity_main);
    }

    public void testClick(View view)
    {
        View v = (View) findViewById(R.id.splash);
        v.setVisibility(View.GONE);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();
    }

    public void beginButton(View view)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        boolean isRegistered = settings.getBoolean(PREF_ISREGISTERED,false);
        Log.d("debug","isRegistered="+isRegistered);

        View v = (View) findViewById(R.id.splash);
        v.setVisibility(View.GONE);

        if(!isRegistered) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new RegistrationFragment()).commit();
        }
        else{
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GameChoiceFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
