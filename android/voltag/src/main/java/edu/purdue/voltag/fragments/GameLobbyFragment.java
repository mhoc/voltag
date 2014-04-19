package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import edu.purdue.voltag.R;
import edu.purdue.voltag.helper.ImageHelper;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment {

    public GameLobbyFragment() {

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[] {"David", "Tylor", "Kyle", "Cartman", "Michael"} ));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.david);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
        imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 500));
        return inflater.inflate(R.layout.fragment_game_lobby, container, false);
    }

}
