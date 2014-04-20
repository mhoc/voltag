package edu.purdue.voltag;

import android.content.Context;
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
public class PlayerListAdapter extends BaseAdapter {

    List<Player> players;
    int listItemId;
    int nameBox;
    Context context;


    public PlayerListAdapter(Context context, int resource, int textViewResourceId, List<Player> players) {
        this.context = context;
        this.listItemId = resource;
        this.players = players;
        this.nameBox = textViewResourceId;
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
        return this.players.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if(convertView != null)
        {
            v = convertView;
        }
        else
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            v = inflater.inflate(listItemId, parent, false);
        }
        TextView name = (TextView) v.findViewById(R.id.name);

        name.setText(getItem(position).getUserName());

        return v;
    }
}
