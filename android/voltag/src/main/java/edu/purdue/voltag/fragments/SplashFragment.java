package edu.purdue.voltag.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.purdue.voltag.MainActivity;
import edu.purdue.voltag.R;

/**
 * A fragment for the splash screen of the app.
 */
public class SplashFragment extends Fragment implements View.OnClickListener {

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_splash, container, false);

        Button begin = (Button) v.findViewById(R.id.btn_beginButton);
        begin.setOnClickListener(this);

        return v;
    }
    @Override
    public void onClick(View view) {

        // Get the shared preferences and whether they are registered
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, 0);
        boolean isRegistered = settings.getBoolean(MainActivity.PREF_ISREGISTERED, false);

        switch (view.getId()) {

            case R.id.btn_beginButton:

                // Choose which fragment to inflate
                Fragment toInflate = isRegistered ? new GameChoiceFragment() : new RegistrationFragment();

                // Inflate the fragment
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(android.R.id.content, toInflate)
                        .commit();

                break;

        }

    }

}
