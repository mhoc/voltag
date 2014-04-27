package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.app.ListFragment;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SendCallback;

import java.util.List;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.PlayerListAdapter;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnAsyncCompletedListener;
import edu.purdue.voltag.lobby.BitmapCacheHost;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment implements OnAsyncCompletedListener, BitmapCacheHost {

    VoltagDB db;
    ListView theList;
    private NfcAdapter mNfcAdapter;

    private LruCache<String, Bitmap> mMemoryCache;

    public GameLobbyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initMemoryCache();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.game_lobby_menu, menu);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        db = VoltagDB.getDB(getActivity());
        //setListAdapter(new ArrayAdapter<String>(activity, R.layout.player_list_item, R.id.name, new String[]{"David", "Tylor", "Kyle", "Cartman", "Michael"}));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        clearCache();
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
        db.refreshPlayersTable(this);
        final List<Player> whichOne = db.getPlayersInCurrentGame();
        String gameName;
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        gameName = settings.getString(MainActivity.PREF_CURRENT_GAME_NAME,"");

        TextView id = (TextView) view.findViewById(R.id.gamelobby_tv_lobbyid);
        id.setText(gameName);

        new AsyncTask<Void, Void, Bitmap>() {

            Player it;

            @Override
            protected Bitmap doInBackground(Void... params) {
                it = getWhoIsIt(whichOne);
                return it.getGravitar(MainActivity.IT_SIZE);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView iv = (ImageView) view.findViewById(R.id.imageView);
                iv.setImageBitmap(bitmap);

                TextView t = (TextView) view.findViewById(R.id.gamelobby_tv_whosit);
                t.setText(it.getUserName());
            }
        }.execute();

        db.refreshPlayersTable(this);

    }

    public Player getWhoIsIt(List<Player> list){
        Player it = null;
        for ( Player p : list){
            if ( p.getIsIt() ){
                Log.d("tylor", "It: " + p.getUserName());
                it = p;
            }
            Log.d("tylor", p.getUserName());
        }
        return it;
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
                List<Player> players = db.getPlayersInCurrentGame();

                /*String[] names = { "David", "Gary", "Charles", "Chuck", "Dave", "Kyle", "Madison", "Jordan", "Katie", "Jennifer", "Anthony" };
                String[] emails = {"tylorgarrett@gmail.com", "dmtschida1@gmail.com", "mike@hockerman.com", "kyle@kptechblog.com"};
                Random r = new Random();
                for(String name : names)
                {
                    int i = r.nextInt(emails.length);
                    players.add(new Player(null, null, name, emails[i]));
                }*/
                return players;
            }

            @Override
            protected void onPostExecute(List<Player> players)
            {
                Log.d("PlayerLoader", "onPostExecute()");
                PlayerListAdapter adapt = new PlayerListAdapter(getActivity(),
                        R.layout.player_list_item, R.id.name, players, (BitmapCacheHost) GameLobbyFragment.this);
                theList.setAdapter(adapt);
            }
        };
        addAdapter.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final VoltagDB db = VoltagDB.getDB(getActivity());

        int id = item.getItemId();
        switch (id) {

            case R.id.drop_registration:
                db.dropPlayerRegistrationOnParse(null);
                prefs.edit().putString(MainActivity.PREF_USER_ID, "").commit();
                prefs.edit().putString(MainActivity.PREF_USER_EMAIL, "").commit();
                prefs.edit().putBoolean(MainActivity.PREF_ISREGISTERED, false).commit();

                String nameA = prefs.getString(MainActivity.PREFS_NAME,"");
                ParsePush pushA = new ParsePush();
                pushA.setChannel(prefs.getString(MainActivity.PREF_CURRENT_GAME_ID,""));
                pushA.setMessage(nameA + " has left the game");
                Activity a = getActivity();
                pushA.sendInBackground(new SendCallback() {
                    @Override
                    public void done(ParseException e) {
                        PushService.unsubscribe(getActivity(), prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));
                    }
                });
                getFragmentManager().beginTransaction().replace(android.R.id.content, new GameChoiceFragment()).commit();
                db.removePlayerFromGameOnParse(null);

                prefs.edit().putString(MainActivity.PREF_CURRENT_GAME_ID, "").commit();

                return true;

            case R.id.exit_game:

                final SharedPreferences _settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME,0);
                String name = _settings.getString(MainActivity.PREFS_NAME,"");
                ParsePush push = new ParsePush();
                push.setChannel(_settings.getString(MainActivity.PREF_CURRENT_GAME_ID,""));
                push.setMessage(name + " has left the game");
                final Activity aA = getActivity();

                // Save the game ID so we can remove it later
                final String gameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

                push.sendInBackground(new SendCallback() {
                    public void done(ParseException e) {
                        PushService.unsubscribe(aA, gameID);
                    }
                });
                getFragmentManager().beginTransaction().replace(android.R.id.content, new GameChoiceFragment()).commit();
                db.removePlayerFromGameOnParse(null);

                SharedPreferences.Editor editor = _settings.edit();
                editor.putString(MainActivity.PREF_CURRENT_GAME_ID, "");
                editor.commit();

                return true;

            case R.id.share:
                String gameId = null;
                SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
                gameId = settings.getString(MainActivity.PREF_CURRENT_GAME_ID,"");

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Come join the revolt! Enter the id " + gameId;
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join the revolt");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

                return true;

            default:
                return false;

        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        assert(mMemoryCache != null);
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        assert(mMemoryCache != null);
        return mMemoryCache.get(key);
    }

    public void initMemoryCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 15;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    public void clearCache()
    {
        mMemoryCache.evictAll();
    }
}
