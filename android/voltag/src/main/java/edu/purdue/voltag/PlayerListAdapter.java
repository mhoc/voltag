package edu.purdue.voltag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.helper.ImageHelper;
import edu.purdue.voltag.lobby.BitmapCacheHost;

/**
 * Created by david on 4/19/14.
 */
public class PlayerListAdapter extends ArrayAdapter<Player> {

    int listItemId;
    BitmapCacheHost host;

    public PlayerListAdapter(Context context, int resource, int textViewResourceId, List<Player> players, BitmapCacheHost host) {
        super(context, resource, textViewResourceId, players);
        // Log.d("PlayerLostAdapter", "New Adapter");
        for (Player p : players) {
            //Log.d("Adapter", p.getUserName() + "\t: " + p.getEmail());
        }
        notifyDataSetChanged();
        this.host = host;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Log.d("PlayerListAdapter", "getView()");
        View v;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.player_list_item, parent, false);

        TextView name = (TextView) v.findViewById(R.id.name);

        name.setText(getItem(position).getUserName());

        ImageView iv = (ImageView) v.findViewById(R.id.face);

        ImageHelper.loadBitmapAsAsyncTask(getItem(position), iv, host);

        return v;
    }
}
