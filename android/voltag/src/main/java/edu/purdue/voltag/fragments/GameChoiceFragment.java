package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.purdue.voltag.R;

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

    public GameChoiceFragment() {
        // Required empty public constructor
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
        View v = inflater.inflate(R.layout.fragment_game_choice, container, false);
        newGameButton = (Button) v.findViewById(R.id.btn_new_game);
        exisitngGameButton = (Button) v.findViewById(R.id.btn_existing_game);
        newGameButton.setOnClickListener(this);
        exisitngGameButton.setOnClickListener(this);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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
                getFragmentManager().beginTransaction().addToBackStack("CreateGameFragment").replace(android.R.id.content, new CreateGameFragment()).commit();
                break;
            case R.id.btn_existing_game:
                getFragmentManager().beginTransaction().addToBackStack("JoinGameFragment").replace(android.R.id.content, new JoinGameFragment()).commit();
                break;
        }

    }
}
