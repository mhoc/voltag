package edu.purdue.voltag;

import android.app.Application;

import com.parse.Parse;

import edu.purdue.voltag.data.ParseConstants;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
    }

}
