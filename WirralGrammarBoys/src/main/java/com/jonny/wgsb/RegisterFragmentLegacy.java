package com.jonny.wgsb;

import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.security.NoSuchAlgorithmException;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class RegisterFragmentLegacy extends Fragment {
    AlertDialogManager alert = new AlertDialogManager();
    private String year7, year8, year9, year10, year11, year12, year13;
    String yes = "yes", no = "no";
    ConnectionDetector cd;
    EditText txtName, txtEmail;
    Button btnRegister;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_register, container, false);
		setupActionBar();
		cd = new ConnectionDetector(getActivity().getApplicationContext());
		if (!cd.isConnectingToInternet()) {
			alert.showAlertDialog(getActivity(), "Internet Connection Error", "Please connect to a working Internet connection", false);
            getActivity().getSupportFragmentManager().popBackStack();
		}
		txtName = (EditText) view.findViewById(R.id.txtName);
		txtEmail = (EditText) view.findViewById(R.id.txtEmail);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		getPrefs(preferences);
		btnRegister = (Button) view.findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String name = txtName.getText().toString();
				String email = txtEmail.getText().toString();
				if (name.trim().length() > 0 && email.trim().length() > 0 && email.contains("@")) {
                    name = crypto(name, getActivity());
                    email = crypto(email, getActivity());
                    preferences.edit().putString("name", name).putString("email", email).apply();
                    GCMFragmentLegacy GCMFragmentLegacy = new GCMFragmentLegacy();
                    Bundle args = new Bundle();
                    args.putString("name", name);
                    args.putString("email", email);
                    args.putString("year7", year7);
                    args.putString("year8", year8);
                    args.putString("year9", year9);
                    args.putString("year10", year10);
                    args.putString("year11", year11);
                    args.putString("year12", year12);
                    args.putString("year13", year13);
                    GCMFragmentLegacy.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                            .replace(R.id.fragment_container, GCMFragmentLegacy, "GCM_FRAGMENT").addToBackStack(null).commit();
				} else {
					alert.showAlertDialog(getActivity(), "Registration Error!", "Please enter your details", false);
				}
			}
		});
        return view;
	}

    private void getPrefs(SharedPreferences preferences) {
        Boolean pref_year7 = preferences.getBoolean("pref_year7", false);
        Boolean pref_year8 = preferences.getBoolean("pref_year8", false);
        Boolean pref_year9 = preferences.getBoolean("pref_year9", false);
        Boolean pref_year10 = preferences.getBoolean("pref_year10", false);
        Boolean pref_year11 = preferences.getBoolean("pref_year11", false);
        Boolean pref_year12 = preferences.getBoolean("pref_year12", false);
        Boolean pref_year13 = preferences.getBoolean("pref_year13", false);
        if(pref_year7)  year7 = yes;
        else year7 = no;
        if(pref_year8) year8 = yes;
        else year8 = no;
        if(pref_year9) year9 = yes;
        else year9 = no;
        if(pref_year10) year10 = yes;
        else year10 = no;
        if(pref_year11) year11 = yes;
        else year11 = no;
        if(pref_year12) year12 = yes;
        else year12 = no;
        if(pref_year13) year13 = yes;
        else year13 = no;
    }

    private String crypto(String string, Context context) {
        try {
            string = Crypto.SHA512(string, context);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return string;
    }
	
	private void setupActionBar() {
		final ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    	actionBar.setIcon(R.drawable.banner);
    	actionBar.setTitle(R.string.register);
    	actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home){
            getActivity().getSupportFragmentManager().popBackStack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}