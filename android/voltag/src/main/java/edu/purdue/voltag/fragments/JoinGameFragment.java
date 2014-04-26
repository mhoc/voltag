package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
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

import com.parse.PushService;

import java.nio.charset.Charset;
import static android.nfc.NdefRecord.createMime;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Game;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnAsyncCompletedListener;
import edu.purdue.voltag.interfaces.OnEnterLobbyListener;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JoinGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoinGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class JoinGameFragment extends Fragment implements View.OnClickListener {
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
        db = VoltagDB.getDB(getActivity());
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
                SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME,0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(MainActivity.PREF_CURRENT_GAME_ID,gameName);
                editor.commit();
                PushService.subscribe(getActivity(), gameName, MainActivity.class);
                db.addPlayerToGameOnParse(gameName, new OnEnterLobbyListener() {
                    public void onLobbyEnter(Game g) {
                        if (g == null) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();
                            }
                        });
                    }
                });

        }
    }


}
