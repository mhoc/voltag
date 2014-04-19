package edu.purdue.voltag.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

import edu.purdue.voltag.R;
import edu.purdue.voltag.data.ParseConstants;
import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.data.VoltagDB;


/*
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener {
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
        Parse.initialize(getActivity(), ParseConstants.PARSE_APPLICATION_KEY, ParseConstants.PARSE_CLIENT_KEY);
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
        db = new VoltagDB(getActivity());
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onClick(View view) {
        String name = emailBox.getText().toString();
        String email = nameBox.getText().toString();
        String android_id = Settings.Secure.getString(getActivity().getContentResolver(),Settings.Secure.ANDROID_ID);
        Player p = new Player(null,android_id,name,email);
        db.createPlayerOnParse(p);
        Toast.makeText(getActivity(), "You are registered", Toast.LENGTH_LONG);
        regButton.setEnabled(false);

    }
}
