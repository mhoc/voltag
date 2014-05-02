package edu.purdue.voltag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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
    public static int PROFILE_PICTURE_SMALL_SIZE;
    public static int PROFILE_PICTURE_LARGE_SIZE;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the content view
        setContentView(R.layout.activity_main);

        // Spawn a thread to set some parse variables.
        new Thread(new Runnable() {
            public void run() {
                PushService.setDefaultPushCallback(getApplicationContext(), MainActivity.class);
                Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

            }
        }).start();

        // Set up the NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        // Get the dimensions for the picture
        PROFILE_PICTURE_LARGE_SIZE = (int) getResources().getDimension(R.dimen.itSize);
        PROFILE_PICTURE_SMALL_SIZE = (int) getResources().getDimension(R.dimen.itemSize);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set up the button
        Button button = (Button) this.findViewById(R.id.btn_beginButton);
        button.setOnClickListener(this);

        // Get the current game ID
        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        String gameId = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

        if (!(gameId.equals(""))) {
            closeSplash();
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(android.R.id.content, new GameLobbyFragment())
                    .commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }


    @Override
    public void onClick(View view) {

        // Get the shared preferences and whether they are registered
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);
        boolean isRegistered = settings.getBoolean(PREF_ISREGISTERED, false);

        switch (view.getId()) {

            case R.id.btn_beginButton:

                // Close the splash screen
                closeSplash();

                // Choose which fragment to inflate
                Fragment toInflate = isRegistered ? new GameChoiceFragment() : new RegistrationFragment();

                // Inflate the fragment
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(android.R.id.content, toInflate)
                        .commit();

                break;

        }

    }

    private void closeSplash() {
        View v = findViewById(R.id.splash);
        v.setVisibility(View.GONE);
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

        // Get the preferences and whether they are it
        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        Boolean isIt = prefs.getBoolean(MainActivity.PREF_ISIT, false);

        // If they are it, we are tagging the other person. Send the message "it" to the other person.
        if (isIt) {
            String text = ("it");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMime(
                            "application/edu.purdue.voltag", text.getBytes()),
                            NdefRecord.createApplicationRecord("edu.purdue.voltag")
                    }
            );
            return msg;

        // Otherwise we just send ignore.
        } else {
            String text = ("ignore");
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{createMime(
                            "application/edu.purdue.voltag", text.getBytes()),
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

    public void processIntent(Intent intent) {

        // Get the shared preferences
        final SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Get the message from the intent
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String message = new String(msg.getRecords()[0].getPayload());
        Log.d(MainActivity.LOG_TAG, "NFC tap registered. Message = " + message);

        // If the message contains it, we are on the tagee's phone who just got tagged
        if (message.equals("it")) {

            // Tag the player on parse
            TagPlayerTask task = new TagPlayerTask(this);
            task.setListener(new OnPlayerTaggedListener() {
                public void onPlayerTagged() {

                    // Push to the channel that the user has been tagged
                    ParsePush push = new ParsePush();
                    push.setChannel("a" + prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));

                    // Get the user's name and include it
                    String name = prefs.getString(MainActivity.PREF_USER_NAME, "");
                    push.setMessage(name + " is now it!");
                    push.sendInBackground();

                }
            });
            task.execute();

        }

    }

}
