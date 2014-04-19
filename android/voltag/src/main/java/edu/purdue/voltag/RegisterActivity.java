package edu.purdue.voltag;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.data.Player;


public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Parse.initialize(this, ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
        ParseObject player = new ParseObject("Player");
        player.put("name","Test");
         String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        player.put("hardwareID",android_id);
        player.saveInBackground();

        Log.d("debug", "after");

        VoltagDB db = new VoltagDB(this);
        db.refreshDB();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRegisterClick(View v){
        EditText emailBox = (EditText) findViewById(R.id.userNameEditText);
        EditText nameBox = (EditText) findViewById(R.id.emailEditText);

        String name = emailBox.getText().toString();
        String email = nameBox.getText().toString();
        String android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);


        ParseObject player = new ParseObject("Player");
        player.put("name",name);
        player.put("email",email);
        player.put("hardwareID",android_id);
        player.saveInBackground();
        Toast.makeText(this,"You are registered", Toast.LENGTH_LONG);
        Button b = (Button)findViewById(R.id.registerButton);
        b.setEnabled(false);



    }

}
