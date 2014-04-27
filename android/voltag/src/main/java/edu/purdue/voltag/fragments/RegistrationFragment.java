package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;
import edu.purdue.voltag.data.ParseConstants;
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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText emailBox;
    private EditText nameBox;
    private  Button  regButton;
    private VoltagDB  db;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistrationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //Parse.initialize(getActivity(), ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_registration, container, false);
        nameBox = (EditText)v.findViewById(R.id.etxt_email);
        emailBox = (EditText)v.findViewById(R.id.etxt_displayName);
        regButton = (Button)v.findViewById(R.id.btn_register);
        regButton.setOnClickListener(this);


        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        db = VoltagDB.getDB(getActivity());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onClick(View view) {

        /*InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);*/

        String name = emailBox.getText().toString();
        String email = nameBox.getText().toString();
        String android_id = Settings.Secure.getString(getActivity().getContentResolver(),Settings.Secure.ANDROID_ID);
        Player p = new Player(null,android_id,name,email,false);

        RegisterPlayerTask task = new RegisterPlayerTask(getActivity(), p);
        task.setListener(this);
        task.execute();

        Toast.makeText(getActivity(), "You are registered", Toast.LENGTH_LONG);
        regButton.setEnabled(false);

        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.PREF_ISREGISTERED, true).commit();
        editor.putString(MainActivity.PREFS_NAME, name).commit();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GameChoiceFragment()).commit();

    }

    @Override
    public void onPlayerRegistered(Player p) {

    }
}
