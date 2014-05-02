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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.PushService;

import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.fragments.GameChoiceFragment;
import edu.purdue.voltag.fragments.GameLobbyFragment;
import edu.purdue.voltag.fragments.RegistrationFragment;
import edu.purdue.voltag.interfaces.OnPlayerTaggedListener;
import edu.purdue.voltag.tasks.DeletePlayerTask;
import edu.purdue.voltag.tasks.TagPlayerTask;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, View.OnClickListener {

    public static final String LOG_TAG = "voltag_log";
    public static final String SHARED_PREFS_NAME = "voltag_prefs";
    public static final String PREF_ISIT = "player_is_it";
    public static final String PREF_CURRENT_GAME_ID = "current_game_id";
    public static final String PREF_CURRENT_GAME_NAME = "current_game_name";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_ISREGISTERED = "is_registered";
    public static int ITEM_SIZE;
    public static int IT_SIZE;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            public void run() {
                PushService.setDefaultPushCallback(getApplicationContext(), MainActivity.class);
                Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

            }
        }).start();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Button buBegin = (Button) findViewById(R.id.btn_beginButton);
        buBegin.setOnClickListener(this);

        mNfcAdapter.setNdefPushMessageCallback(this, this);

        IT_SIZE = (int) getResources().getDimension(R.dimen.itSize);
        ITEM_SIZE = (int) getResources().getDimension(R.dimen.itemSize);
    }

    @Override
    public void onStart() {

        super.onStart();
        Button button = (Button) this.findViewById(R.id.btn_beginButton);
        button.setOnClickListener(this);
        String gameId;
        SharedPreferences settings = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        gameId = settings.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
        if (!(gameId.equals(""))) {
            closeSplash();
            getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new GameLobbyFragment()).commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d("debug-intent",getIntent().getAction());
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }

    }


    @Override
    public void onClick(View view) {

        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);
        boolean isRegistered = settings.getBoolean(PREF_ISREGISTERED, false);

        closeSplash();

        if (!isRegistered) {
            getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new RegistrationFragment()).commit();
        } else {
            getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new GameChoiceFragment()).commit();
        }

    }

    private void closeSplash() {
        View v = findViewById(R.id.splash);
        v.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.drop_registration_main:

                SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
                VoltagDB db = VoltagDB.getDB(this);

                new DeletePlayerTask(this).execute();

                getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new RegistrationFragment()).commit();

                break;

            case R.id.action_settings:
                return true;

            case R.id.action_about:
                showAboutDialog();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {

        // Create builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set message, cancelable, and title
        builder.setMessage(R.string.dialog_message)
                .setCancelable(true)
                .setTitle(R.string.dialog_title);

        // Set a positive button to dismiss
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        builder.create().show();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        SharedPreferences settings = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        Boolean isIt = settings.getBoolean(MainActivity.PREF_ISIT, false);
        if (isIt) {
            String text = ("it");
            Log.d("debug", "sendingNFC You are it.");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMime(
                            "application/edu.purdue.voltag", text.getBytes()),
                            NdefRecord.createApplicationRecord("edu.purdue.voltag")
                    }
            );

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.PREF_ISIT, false);
            editor.commit();
            return msg;
        } else {
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
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    public void processIntent(Intent intent) {
        Log.d("debug", "processing sending that I am now it to server");
        Toast.makeText(this, "You are it!", Toast.LENGTH_LONG).show();
        VoltagDB db = VoltagDB.getDB(this);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String message = new String(msg.getRecords()[0].getPayload());
        Log.d("debug", "message=" + message);
        if (message.equals("it")) {
            Log.d("debug", "is tagged");
            TagPlayerTask task = new TagPlayerTask(this);
            task.setListener(new OnPlayerTaggedListener() {
                @Override
                public void onPlayerTagged() {
                    SharedPreferences settings = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(MainActivity.PREF_ISIT, true);
                    editor.commit();
                    ParsePush push = new ParsePush();
                    String test = settings.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
                    Log.d("debug", "sending push to channels " + test);
                    push.setChannel(test);
                    String name = settings.getString(MainActivity.PREF_USER_NAME, "");
                    push.setMessage(name + " is now it!");
                    push.sendInBackground();

                }
            });
            task.execute();

        }
        // record 0 contains the MIME type, record 1 is the AAR, if present
    }

}
