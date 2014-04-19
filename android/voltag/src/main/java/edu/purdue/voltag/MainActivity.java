package edu.purdue.voltag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.fragments.CreateGameFragment;
import edu.purdue.voltag.fragments.GameChoiceFragment;
import edu.purdue.voltag.fragments.GameLobbyFragment;
import edu.purdue.voltag.fragments.RegistrationFragment;


public class MainActivity extends Activity {

    public static final String LOG_TAG = "voltag_log";
    public static final String PREFS_NAME = "voltag_prefs";
    public static final String PREF_CURRENT_GAME_ID = "current_game_id";
    public static final String PREF_ISREGISTERED = "is_registered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
