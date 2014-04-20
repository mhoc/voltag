package edu.purdue.voltag.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.purdue.voltag.R;
import edu.purdue.voltag.data.VoltagDB;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CreateGameFragment extends Fragment implements View.OnClickListener, OnGameCreatedListener {
    private Button shareButton;
    private EditText gameNameEditText;
    private VoltagDB db;

    public CreateGameFragment() {
        // Required empty public constructor

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new VoltagDB(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_game, container, false);
        gameNameEditText = (EditText)v.findViewById(R.id.creategame_et_lobbyid);
        shareButton = (Button)v.findViewById(R.id.creategame_bu_share);
        shareButton.setOnClickListener(this);
        return v;

    }

    @Override
    public void onClick(View view) {
        String gameName = gameNameEditText.getText().toString();
        db.createGameOnParse(gameName,this);
        Log.d("debug","test");

    }

    @Override
    public void onGameCreated(String id) {
        Log.d("debug","gameId="+id);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Come join the revolt! Enter the id " + id;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join the revolt");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }
}
