package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameChoiceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameChoiceFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button newGameButton;
    private Button exisitngGameButton;

    public GameChoiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameChoiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameChoiceFragment newInstance(String param1, String param2) {
        GameChoiceFragment fragment = new GameChoiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_game_choice, container, false);
        newGameButton = (Button) v.findViewById(R.id.btn_new_game);
        exisitngGameButton = (Button) v.findViewById(R.id.btn_existing_game);
        newGameButton.setOnClickListener(this);
        exisitngGameButton.setOnClickListener(this);

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        String email = settings.getString(MainActivity.PREF_USER_EMAIL, "");
        String name = settings.getString(MainActivity.PREF_USER_NAME, "");

        Player p = new Player(null, null, name, email, false);
        AsyncTask<Player, Void, Bitmap> loadPlayerTask = new AsyncTask<Player, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Player... params) {
                return params[0].getGravitar(MainActivity.PROFILE_PICTURE_LARGE_SIZE);
            }

            @Override
            protected void onPostExecute(Bitmap img)
            {
                ImageView face = (ImageView) v.findViewById(R.id.userFace);
                face.setImageBitmap(img);
            }
        };
        loadPlayerTask.execute(p);

        return v;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_new_game:
                getFragmentManager().beginTransaction().addToBackStack("CreateGameFragment").setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new CreateGameFragment()).commit();
                break;
            case R.id.btn_existing_game:
                getFragmentManager().beginTransaction().addToBackStack("JoinGameFragment").setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new JoinGameFragment()).commit();
                break;
        }

    }
}
