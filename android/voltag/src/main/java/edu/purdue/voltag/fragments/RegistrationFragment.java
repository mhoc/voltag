package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;
import edu.purdue.voltag.interfaces.OnPlayerRegisteredListener;
import edu.purdue.voltag.tasks.RegisterPlayerTask;


/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener, OnPlayerRegisteredListener {

    private EditText emailBox;
    private EditText nameBox;
    private Button regButton;
    private VoltagDB db;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_registration, container, false);
        emailBox = (EditText) v.findViewById(R.id.etxt_email);
        nameBox = (EditText) v.findViewById(R.id.etxt_displayName);
        regButton = (Button) v.findViewById(R.id.btn_register);
        regButton.setOnClickListener(this);

        emailBox.addTextChangedListener(new EmailTextWatcher(getActivity(), emailBox, regButton));
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        db = VoltagDB.getDB(getActivity());
    }

    /**
     * Pulls the login data and registers the player.
     * @param view
     */
    @Override
    public void onClick(View view) {

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);

        String email = emailBox.getText().toString();
        String name = nameBox.getText().toString();

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e("Email", "Email is not valid. Using anyway...");
            Toast.makeText(getActivity(), "Email is not valid.", Toast.LENGTH_SHORT);
        } else {
            String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            Player p = new Player(null, android_id, name, email, false);

            RegisterPlayerTask task = new RegisterPlayerTask(getActivity(), p);
            task.setListener(this);
            task.execute();

            Toast.makeText(getActivity(), "You are registered", Toast.LENGTH_LONG);
            regButton.setEnabled(false);

            SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.PREF_ISREGISTERED, true).commit();
            editor.putString(MainActivity.PREF_USER_NAME, name).commit();
            getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(android.R.id.content, new GameChoiceFragment()).commit();

        }
    }

    /**
     * Callback for the register task. Called when the user has been logged in
     * @param p The Player
     */
    @Override
    public void onPlayerRegistered(Player p) {

    }
}
