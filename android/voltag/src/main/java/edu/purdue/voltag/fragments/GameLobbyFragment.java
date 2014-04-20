package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
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
        setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[]{"David", "Tylor", "Kyle", "Cartman", "Michael"}));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View v = inflater.inflate(R.layout.fragment_game_lobby, container, false);

        final Player p = new Player(null, null, null, "dmtschida1@gmail.com");

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                final String baseUrl = "http://www.gravatar.com/avatar/";
                final String processedAddress = p.getEmail();

                final String hashCode = p.getGravitarURL();
                final String url = baseUrl + hashCode+"?s=" + "180" + "&d=blank";

                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                    final InputStream is = httpURLConnection.getInputStream();
                    Bitmap gravitar = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
                    return ImageHelper.getRoundedCornerBitmap(gravitar, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpURLConnection.disconnect();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ((ImageView)v.findViewById(R.id.imageView)).setImageBitmap(bitmap);
            }
        }.execute();

        return v;
    }


}
