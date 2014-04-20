package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;
import static android.nfc.NdefRecord.createMime;

import edu.purdue.voltag.R;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnAsyncCompletedListener;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JoinGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoinGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class JoinGameFragment extends Fragment implements View.OnClickListener, CreateNdefMessageCallback {
    private Button joinGameButton;
    private EditText gameNameEditText;
    private VoltagDB db;
    private NfcAdapter mNfcAdapter;

    public JoinGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new VoltagDB(getActivity());
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (mNfcAdapter == null) {
            Toast.makeText(getActivity(), "NFC is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }
        mNfcAdapter.setNdefPushMessageCallback(this,this.getActivity());
    }


    @Override
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_join_game, container, false);
        joinGameButton = (Button) v.findViewById(R.id.joingame_bu_join);
        gameNameEditText = (EditText) v.findViewById(R.id.joingame_et_lobbyid);
        joinGameButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.joingame_bu_join:
                String gameName = gameNameEditText.getText().toString();
                db.addPlayerToGameOnParse(gameName, new OnAsyncCompletedListener() {
                    public void done(String id) {

                    }
                });
                getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();
        }
    }


}
