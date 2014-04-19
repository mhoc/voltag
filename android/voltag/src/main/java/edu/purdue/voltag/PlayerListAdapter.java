package edu.purdue.voltag;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import edu.purdue.voltag.data.Player;

/**
 * Created by david on 4/19/14.
 */
public class PlayerListAdapter extends ArrayAdapter<Player> {


    public PlayerListAdapter(Context context, int resource, int textViewResourceId, Player[] objects) {
        super(context, resource, textViewResourceId, objects);
    }


}
