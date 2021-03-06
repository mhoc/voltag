package edu.purdue.voltag.fragments;

import android.animation.ArgbEvaluator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParsePush;
import com.parse.PushService;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Game;
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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PREP_GAME_ID = "preparedGameId";


    // TODO: Rename and change types of parameters
    private String preparedGameId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment GameChoiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JoinGameFragment newInstance(String preparedGameId) {
        JoinGameFragment fragment = new JoinGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PREP_GAME_ID, preparedGameId);
        fragment.setArguments(args);
        return fragment;
    }
    public JoinGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            preparedGameId = getArguments().getString(ARG_PREP_GAME_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_join_game, container, false);

        // Set up widgets
        joinGameButton = (Button) v.findViewById(R.id.joingame_bu_join);
        gameNameEditText = (EditText) v.findViewById(R.id.joingame_et_lobbyid);

        if(preparedGameId != null)
        {
            gameNameEditText.setText(preparedGameId);
        }

        joinGameButton.setOnClickListener(this);

        // Set a listener for when the user clicks the enter button to submit.
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
        switch (view.getId()) {

            case R.id.joingame_bu_join:

                // Create the preferences
                final SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

                // Get the game name the user entered in
                final String gameID = gameNameEditText.getText().toString();

                // Create an add player task
                AddPlayerToGameTask task = new AddPlayerToGameTask(getActivity(), gameID);

                // Set the listener by saving the context of the click
                final Context c = getActivity();
                task.setListener(new OnJoinedGameListener() {
                    public void onJoinedGame(Game g) {

                        // Subscribe the user to the channel by this name and create the push
                        PushService.subscribe(c, "a" + gameID, MainActivity.class);
                        ParsePush push = new ParsePush();
                        push.setChannel("a" + prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));

                        // Get the user's name
                        String name = prefs.getString(MainActivity.PREF_USER_NAME, "");
                        push.setMessage(name + " has joined the game");
                        push.sendInBackground();

                        // Execute a fragment transaction on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                getFragmentManager().beginTransaction()
                                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                        .replace(android.R.id.content, new GameLobbyFragment())
                                        .commit();
                            }
                        });
                    }
                });

                // Execute the task
                task.execute();

                break;
        }
    }
}
