package edu.purdue.voltag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import edu.purdue.voltag.fragments.GameLobbyFragment;
import edu.purdue.voltag.fragments.RegistrationFragment;
import edu.purdue.voltag.fragments.SplashFragment;
import edu.purdue.voltag.interfaces.OnPlayerTaggedListener;
import edu.purdue.voltag.tasks.DeletePlayerTask;
import edu.purdue.voltag.tasks.LeaveGameTask;
import edu.purdue.voltag.tasks.TagPlayerTask;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {

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
    private MyCustomReceiver customReceiver;

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
        customReceiver = new MyCustomReceiver();

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

        // Get the current game ID
        SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        String gameId = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

        if (!(gameId.equals(""))) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(android.R.id.content, new GameLobbyFragment())
                    .commit();
        } else
        {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(android.R.id.content, new SplashFragment())
                    .commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d("debug-intent",getIntent().getAction());
        LocalBroadcastManager.getInstance(this).registerReceiver(customReceiver, new IntentFilter("edu.purdue.voltag.PARSE_IT_CHANGE"));
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
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
            Log.d("ndefMessage","sending it");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MainActivity.PREF_ISIT,false);
            editor.commit();
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
            Log.d("ndefMessage", "sending ignore");
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
        Log.d("new Intent", intent.getAction());
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.drop_registration_main:
                SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
                String gameId = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
                if (!(gameId.equals(""))) {
                    leaveGame();
                }

                // Delete the player from parse
                new DeletePlayerTask(this).execute();

                getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                // Switch the fragment back to the registration fragment
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(android.R.id.content, new RegistrationFragment())
                        .commit();

                return true;
        }
        return false;
    }

    private void leaveGame() {

        // Get preferences
        final SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Alert other players that we've left the game
        ParsePush pushDrop = new ParsePush();
        pushDrop.setChannel("a"+prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));
        pushDrop.setMessage(prefs.getString(MainActivity.PREF_USER_NAME, "") + " has left the game.");

        // Send the push and unsubscribe them from push notifications
        pushDrop.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                PushService.unsubscribe(MainActivity.this, "a"+prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));
            }
        });

        // Execute task
        new LeaveGameTask(this).execute();

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

                    String test = "a"+prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");
                    Log.d("debug", "sending push to channels " + test);
                    JSONObject data = null;
                    String name = prefs.getString(MainActivity.PREF_USER_NAME, "")+" is it ";
                    try {
                        data = new JSONObject("{\"action\": \"com.example.UPDATE_STATUS\",\"alert\":\""+name+"\"}");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    push.setChannel(test);
                    push.setMessage(name + " is now it!");
                    push.setData(data);
                    push.sendInBackground();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new GameLobbyFragment()).commit();
                        }
                    });


                }
            });
            task.execute();

        } else  if(!(message.equals("ignore"))){
            Log.d("settings","setting is it to false");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MainActivity.PREF_ISIT,false);
            editor.commit();
        }

    }


}
