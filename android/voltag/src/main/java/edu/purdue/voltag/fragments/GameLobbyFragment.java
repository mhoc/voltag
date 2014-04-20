package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.app.ListFragment;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.purdue.voltag.PlayerListAdapter;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnAsyncCompletedListener;

import static android.nfc.NdefRecord.createMime;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment implements OnAsyncCompletedListener,NfcAdapter.CreateNdefMessageCallback {

    VoltagDB db;
    ListView theList;
    private NfcAdapter mNfcAdapter;

    public GameLobbyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        db = VoltagDB.getDB(getActivity());
        db.refreshPlayersTable(this);
        //setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[]{"David", "Tylor", "Kyle", "Cartman", "Michael"}));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("GameLobbyFragment", "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_game_lobby, container, false);
        assert v != null;
        theList = (ListView) v.findViewById(android.R.id.list);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState)
    {
        Log.d("GameLobbyFragment", "onViewCreated()");
        final Player it = new Player(null, null, null, "dmtschida1@gmail.com");

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return it.getGravitar(180);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView iv = (ImageView) view.findViewById(R.id.imageView);
                iv.setImageBitmap(bitmap);
            }
        }.execute();
        //done("");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (mNfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }
        mNfcAdapter.setNdefPushMessageCallback(this,this.getActivity());
    }

    @Override
    public void done(String id) {
        // Note that ID is just an empty string during this call

        Log.d("Lobby", "Database has refreshed!!");
        AsyncTask<Void, Void, List<Player>> addAdapter = new AsyncTask<Void, Void, List<Player>>() {

            @Override
            protected List<Player> doInBackground(Void... params) {
                Log.d("PlayerLoader", "doInBackground()");
                //List<Player> players = db.getPlayersInCurrentGame();
                ArrayList<Player> players = new ArrayList<Player>();

                String[] names = { "David", "Gary", "Charles", "Chuck", "Dave", "Kyle", "Madison", "Jordan", "Katie", "Jennifer", "Anthony" };
                String[] emails = {"tylorgarrett@gmail.com", "dmtschida1@gmail.com", "punkkid209@gmail.com", "mike@hockerman.com", "kyle@kptechblog.com"};
                for(String name : names)
                {
                    Random r = new Random();
                    int i = r.nextInt(emails.length);
                    players.add(new Player(null, null, name, emails[i]));
                }
                return players;
            }

            @Override
            protected void onPostExecute(List<Player> players)
            {
                Log.d("PlayerLoader", "onPostExecute()");
                PlayerListAdapter adapt = new PlayerListAdapter(getActivity(), R.layout.player_list_item, R.id.name, players);
                theList.setAdapter(adapt);
            }
        };
        addAdapter.execute();
    }


    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("You're it!!\n\n" +
                "Beam Time: " + System.currentTimeMillis());
        Log.d("debug","sendingNFC");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
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
                });
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getActivity().getIntent().getAction())) {
            processIntent(getActivity().getIntent());
        }
    }

    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        getActivity().setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Log.d("debug","processing");
        /*
        textView = (TextView) findViewById(R.id.textView);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        textView.setText(new String(msg.getRecords()[0].getPayload()));
        */
    }






}
