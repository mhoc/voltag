package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.purdue.voltag.R;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CreateGameFragment extends Fragment implements View.OnClickListener {
    private Button shareButton;

    public CreateGameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_game, container, false);
        shareButton = (Button)v.findViewById(R.id.creategame_bu_share);
        shareButton.setOnClickListener(this);
        return v;

    }

    @Override
    public void onClick(View view) {
        return;
    }
}
