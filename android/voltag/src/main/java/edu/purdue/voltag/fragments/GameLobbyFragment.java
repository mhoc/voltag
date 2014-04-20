package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.purdue.voltag.PlayerListAdapter;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.helper.ImageHelper;
import edu.purdue.voltag.interfaces.OnDBRefreshListener;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment implements OnDBRefreshListener {

    VoltagDB db;

    public GameLobbyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        //db = new VoltagDB(getActivity());
        //db.refreshPlayersTable(this);
        onDBRefresh();
        setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[]{"David", "Tylor", "Kyle", "Cartman", "Michael"}));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game_lobby, container, false);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState)
    {
        final Player it = new Player(null, null, null, "dmtschida1@gmail.com");

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return it.getGravitar(180);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView iv = (ImageView) view.findViewById(R.id.imageView);
                iv.setImageBitmap(bitmap);
            }
        }.execute();
    }

    @Override
    public void onDBRefresh() {
        Log.d("Lobby", "Database has refreshed!!");
        AsyncTask<Void, Void, List<Player>> addAdapter = new AsyncTask<Void, Void, List<Player>>() {

            @Override
            protected List<Player> doInBackground(Void... params) {
                Log.d("PlayerLoader", "doInBackground()");
                //List<Player> players = db.getPlayersInCurrentGame();
                ArrayList<Player> players = new ArrayList<Player>();

                String[] names = { "David", "Gary", "Charles", "Chuck", "Dave", "Kyle", "Madison", "Jordan", "Katie", "Jennifer", "Anthony" };
                for(String name : names)
                {
                    players.add(new Player(null, null, name, name + "@email.com"));
                }
                return players;
            }

            @Override
            protected void onPostExecute(List<Player> players)
            {
                Log.d("PlayerLoader", "onPostExecute()");
                PlayerListAdapter adapt = new PlayerListAdapter(getActivity(), R.layout.player_list_item, R.id.name, players);
                GameLobbyFragment.this.setListAdapter(adapt);
            }
        };
        addAdapter.execute();
    }
}
