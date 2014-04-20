package edu.purdue.voltag;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import edu.purdue.voltag.data.Player;

/**
 * Created by david on 4/19/14.
 */
public class PlayerListAdapter extends ArrayAdapter<Player> {

    List<Player> players;
    int listItemId;

    public PlayerListAdapter(Context context, int resource, int textViewResourceId, List<Player> players) {
        super(context, resource, textViewResourceId, players);
        Log.d("PlayerLostAdapter", "New Adapter");
        for(Player p : players)
        {
            Log.d("Adapter", p.getUserName());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public Player getItem(int position) {
        return players.get(position);
    }

    @Override
    public long getItemId(int position) {
        Log.d("PlayerListAdapter", "getItemId()");
        return this.players.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("PlayerListAdapter", "getView()");
        View v;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        v = inflater.inflate(listItemId, (ViewGroup) convertView, false);

        TextView name = (TextView) v.findViewById(R.id.name);

        name.setText(getItem(position).getUserName());

        return v;
    }
}
