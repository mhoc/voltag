package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.nfc.NfcAdapter;
import android.widget.TextView;

import com.parse.ParsePush;
import com.parse.PushService;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Game;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnJoinedGameListener;
import edu.purdue.voltag.tasks.AddPlayerToGameTask;

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


        gameNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    joinGameButton.performClick();
                }
                return false;
            }
        });
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.joingame_bu_join:

                final String gameName = gameNameEditText.getText().toString();
                final SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME,0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(MainActivity.PREF_CURRENT_GAME_ID,gameName);
                editor.commit();


                final Activity a = getActivity();
                AddPlayerToGameTask task = new AddPlayerToGameTask(getActivity(), gameName);
                task.setListener(new OnJoinedGameListener() {
                    public void onJoinedGame(Game g) {
                        PushService.subscribe(getActivity(), gameName, MainActivity.class);
                        ParsePush push = new ParsePush();
                        push.setChannel(settings.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));
                        String name = settings.getString(MainActivity.PREF_USER_NAME, "");
                        push.setMessage(name + " has joined the game");
                        push.sendInBackground();

                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                getFragmentManager().beginTransaction().replace(android.R.id.content, new GameLobbyFragment()).commit();
                            }
                        });
                    }
                });
                task.execute();

                break;
        }
    }
}
