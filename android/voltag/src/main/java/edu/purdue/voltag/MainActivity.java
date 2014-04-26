package edu.purdue.voltag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.PushService;

import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.fragments.CreateGameFragment;
import edu.purdue.voltag.fragments.GameChoiceFragment;
import edu.purdue.voltag.fragments.GameLobbyFragment;
import edu.purdue.voltag.fragments.RegistrationFragment;
import edu.purdue.voltag.lobby.BitmapCacheHost;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {

    public static final String LOG_TAG = "voltag_log";
    public static final String PREFS_NAME = "voltag_prefs";
    public static final String PREF_ISIT = "player_is_it";
    public static final String PREF_CURRENT_GAME_ID = "current_game_id";
    public static final String PREF_CURRENT_GAME_NAME = "current_game_name";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_EMAIL = "user_email";
    public static final String PREF_ISREGISTERED = "is_registered";
    private NfcAdapter mNfcAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            public void run() {
                Parse.initialize(MainActivity.this, ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
                PushService.setDefaultPushCallback(getApplicationContext(),MainActivity.class);
                Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

            }
        }).start();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mNfcAdapter.setNdefPushMessageCallback(this,this);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();
        String gameId;
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
        gameId = settings.getString(MainActivity.PREF_CURRENT_GAME_ID,"");
        if(!(gameId.equals(""))){
            closeSplash();
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();

        }
    }


    public void testClick(View view)
    {
        closeSplash();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();
    }

    public void beginButton(View view)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        boolean isRegistered = settings.getBoolean(PREF_ISREGISTERED,false);
        Log.d("debug", "isRegistered=" + isRegistered);

        closeSplash();

        if(!isRegistered) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new RegistrationFragment()).commit();
        }
        else{
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GameChoiceFragment()).commit();
        }
    }

    private void closeSplash() {
        View v = (View) findViewById(R.id.splash);
        v.setVisibility(View.GONE);
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
        } else if (id == R.id.action_about) {
            showAboutDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_message)
                .setCancelable(true)
                .setTitle(R.string.dialog_title);

        builder.setPositiveButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }


    public NdefMessage createNdefMessage(NfcEvent event) {
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        Boolean isIt = settings.getBoolean(MainActivity.PREF_ISIT,false);
        if(isIt) {
            String text = ("it");
            Log.d("debug", "sendingNFC You are it.");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMime(
                            "application/edu.purdue.voltag", text.getBytes()),
                            /**
                             * The Android Application Record (AAR) is commented out. When a device
                             * receives a push with an AAR in it, the application specified in the AAR
                             * is guaranteed to run. The AAR overrides the tag dispatch system.
                             * You can add it back in to guarantee that this
                             * activity starts when receiving a beamed message. For now, this code
                             * uses the tag dispatch system.
                             */
                            NdefRecord.createApplicationRecord("edu.purdue.voltag")
                    }
            );
      
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.PREF_ISIT,false);
            editor.commit();
            return msg;
        }

        else{
            String text = ("ignore");
            Log.d("debug", "sendingNFC ignore.");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMime(
                            "application/edu.purdue.voltag", text.getBytes()),
                            /**
                             * The Android Application Record (AAR) is commented out. When a device
                             * receives a push with an AAR in it, the application specified in the AAR
                             * is guaranteed to run. The AAR overrides the tag dispatch system.
                             * You can add it back in to guarantee that this
                             * activity starts when receiving a beamed message. For now, this code
                             * uses the tag dispatch system.
                             */
                            NdefRecord.createApplicationRecord("edu.purdue.voltag")
                    }
            );
            return msg;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Log.d("debug","processing sending that I am now it to server");
        Toast.makeText(this, "You are it!", Toast.LENGTH_LONG).show();
        VoltagDB db = VoltagDB.getDB(this);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String message = new String(msg.getRecords()[0].getPayload());
        Log.d("debug","message="+message);
        if(message.equals("it")){
            db.tagThisPlayerOnParse(null);
            SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME,0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.PREF_ISIT,true);
            editor.commit();
            ParsePush push = new ParsePush();
            String test = settings.getString(MainActivity.PREF_CURRENT_GAME_ID,"");
            Log.d("debug","sending push to channels " + test);
            push.setChannel(test);
            String name = settings.getString(MainActivity.PREFS_NAME,"");
            push.setMessage("is now it!");
            Log.d("debug",push.);
            push.sendInBackground();
        }
        // record 0 contains the MIME type, record 1 is the AAR, if present
    }

}
