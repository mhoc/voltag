package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.app.ListFragment;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.PlayerListAdapter;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnAsyncCompletedListener;
import edu.purdue.voltag.lobby.BitmapCacheHost;

import static android.nfc.NdefRecord.createMime;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment implements OnAsyncCompletedListener {

    VoltagDB db;
    ListView theList;
    private NfcAdapter mNfcAdapter;

    public GameLobbyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.game_lobby_menu, menu);

    }

    public boolean onContextItemSelected(MenuItem item){
        if(item.getItemId() == R.id.exit_game){
            Log.d("debug","exit game!");

        }
        return true;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);


        //db = VoltagDB.getDB(getActivity());
        //db.refreshPlayersTable(this);
        //setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[]{"David", "Tylor", "Kyle", "Cartman", "Michael"}));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("GameLobbyFragment", "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_game_lobby, container, false);
        assert v != null;
        theList = (ListView) v.findViewById(android.R.id.list);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState)
    {
        Log.d("GameLobbyFragment", "onViewCreated()");
        final Player it = new Player(null, null, null, "dmtschida1@gmail.com");

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return it.getGravitar(220);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView iv = (ImageView) view.findViewById(R.id.imageView);
                iv.setImageBitmap(bitmap);
            }
        }.execute();
        done("");
    }

    @Override
    public void done(String id) {
        // Note that ID is just an empty string during this call

        Log.d("Lobby", "Database has refreshed!!");
        AsyncTask<Void, Void, List<Player>> addAdapter = new AsyncTask<Void, Void, List<Player>>() {

            @Override
            protected List<Player> doInBackground(Void... params) {
                Log.d("PlayerLoader", "doInBackground()");
                //List<Player> players = db.getPlayersInCurrentGame();
                ArrayList<Player> players = new ArrayList<Player>();

                String[] names = { "David", "Gary", "Charles", "Chuck", "Dave", "Kyle", "Madison", "Jordan", "Katie", "Jennifer", "Anthony" };
                String[] emails = {"tylorgarrett@gmail.com", "punkkid209@gmail.com", "mike@hockerman.com", "kyle@kptechblog.com"};
                Random r = new Random();
                for(String name : names)
                {
                    int i = r.nextInt(emails.length);
                    players.add(new Player(null, null, name, emails[i]));
                }
                return players;
            }

            @Override
            protected void onPostExecute(List<Player> players)
            {
                Log.d("PlayerLoader", "onPostExecute()");
                PlayerListAdapter adapt = new PlayerListAdapter(getActivity(),
                        R.layout.player_list_item, R.id.name, players, (BitmapCacheHost) getActivity());
                theList.setAdapter(adapt);
            }
        };
        addAdapter.execute();
    }

}
