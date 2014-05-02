package edu.purdue.voltag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MyCustomReceiver extends BroadcastReceiver {
private static final String TAG = "MyCustomReceiver";
 
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      String action = intent.getAction();
      String channel = intent.getExtras().getString("com.parse.Channel");
        if(channel == null){
            return;
        }
      JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
 
      Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
      Iterator itr = json.keys();
      while (itr.hasNext()) {
        String key = (String) itr.next();
        Log.d(TAG, "..." + key + " => " + json.getString(key));
      }
        Intent i = new Intent();
        i.setClassName("edu.purdue.voltag", "edu.purdue.voltag.MainActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    } catch (JSONException e) {
      Log.d(TAG, "JSONException: " + e.getMessage());
    }
  }
}