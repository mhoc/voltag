package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import edu.purdue.voltag.bitmap.BitmapCacheHost;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.helper.ShareHandler;
import edu.purdue.voltag.interfaces.OnDatabaseRefreshListener;
import edu.purdue.voltag.tasks.DeletePlayerTask;
import edu.purdue.voltag.tasks.LeaveGameTask;
import edu.purdue.voltag.tasks.RefreshPlayersTask;

/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameLobbyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameLobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GameLobbyFragment extends ListFragment implements OnDatabaseRefreshListener, BitmapCacheHost {

    VoltagDB db;
    ListView theList;
    private NfcAdapter mNfcAdapter;
    private ImageView iv;
    private TextView tv;

    private LruCache<String, Bitmap> mMemoryCache;
    private List<Player> players;
    private Player it;
    private TextView tv_it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initMemoryCache();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.game_lobby_menu, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        db = VoltagDB.getDB(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("GameLobbyFragment", "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_game_lobby, container, false);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d("GameLobbyFragment", "onViewCreated()");
        theList = (ListView) view.findViewById(android.R.id.list);
        iv = (ImageView) view.findViewById(R.id.imageView);
        tv = (TextView) view.findViewById(R.id.gamelobby_tv_lobbyid);
        tv_it = (TextView) view.findViewById(R.id.gamelobby_tv_whosit);
        String gameName;
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        gameName = settings.getString(MainActivity.PREF_CURRENT_GAME_NAME, "");

        tv.setText(gameName);
        RefreshPlayersTask task = new RefreshPlayersTask(getActivity());
        task.setListener(this);
        task.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        int id = item.getItemId();
        switch (id) {

            case R.id.drop_registration:

                // Leave the game
                leaveGame();

                // Delete the player from parse
                new DeletePlayerTask(getActivity()).execute();

                // Switch the fragment back to the registration fragment
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(android.R.id.content, new RegistrationFragment())
                        .commit();

                return true;

            case R.id.exit_game:

                // Leave the game
                leaveGame();

                // Switch fragment to game choosing fragming
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(android.R.id.content, new GameChoiceFragment())
                        .commit();

                return true;

            case R.id.share:
                String gameId = null;
                gameId = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, "");

                ShareHandler.shareGame(this.getActivity(), gameId);

                return true;

            default:
                return false;

        }
    }

    private void leaveGame() {

        // Get preferences
        final SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);

        // Alert other players that we've left the game
        ParsePush pushDrop = new ParsePush();
        pushDrop.setChannel("a"+prefs.getString(MainActivity.PREF_CURRENT_GAME_ID, ""));
        pushDrop.setMessage(prefs.getString(MainActivity.PREF_USER_NAME, "") + " has left the game.");
           final String oldGameID = prefs.getString(MainActivity.PREF_CURRENT_GAME_ID,"");
        // Send the push and unsubscribe them from push notifications
        pushDrop.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("debug", "unsubscriding from channel "+ "a" + oldGameID);
                PushService.unsubscribe(getActivity(), "a"+oldGameID);

            }
        });

        // Execute task
        new LeaveGameTask(getActivity()).execute();


    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        assert (mMemoryCache != null);
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        assert (mMemoryCache != null);
        return mMemoryCache.get(key);
    }

    public void initMemoryCache() {
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

    public void clearCache() {
        mMemoryCache.evictAll();
    }

    public Player getWhoIsIt(List<Player> list) {
        Log.d("debug","getWhoIsIt called");
        Player it = null;
        for (Player p : list) {
            if (p.getIsIt()) {
                Log.d("tylor", "It: " + p.getUserName());
                it = p;
            }
            Log.d("tylor", p.getUserName());
        }
        return it;
    }

    @Override
    public void onDatabaseRefresh() {
        Log.d("Lobby", "Database has refreshed!!");

        // Get the players in the current game
        final List<Player> players = db.getPlayersInCurrentGame();

        // Create the adapter
        final PlayerListAdapter adapt = new PlayerListAdapter(getActivity(), R.layout.player_list_item, R.id.name, players, GameLobbyFragment.this);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                theList.setAdapter(adapt);
            }
        });

        // Get the player's bitmap
        final Player it = getWhoIsIt(players);
        final Bitmap b = it.getGravitar((int) getActivity().getResources().getDimension(R.dimen.itSize));

        // Post to UI
        handler.post(new Runnable() {
            public void run() {
                iv.setImageBitmap(b);
                tv_it.setText(it.getUserName());
            }
        });

    }
}
