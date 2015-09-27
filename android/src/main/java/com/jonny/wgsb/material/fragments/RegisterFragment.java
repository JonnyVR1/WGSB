package com.jonny.wgsb.material.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.activities.MainActivity;
import com.jonny.wgsb.material.security.Crypto;
import com.jonny.wgsb.material.util.AlertDialogManager;
import com.jonny.wgsb.material.util.ConnectionDetector;

import java.security.NoSuchAlgorithmException;

public class RegisterFragment extends Fragment implements View.OnClickListener {
    private static RegisterFragment instance = null;
    private final AlertDialogManager alert = new AlertDialogManager();
    private SharedPreferences preferences;
    private MainActivity mActivity;
    private EditText txtName, txtEmail;
    private String year7, year8, year9, year10, year11, year12, year13;

    public RegisterFragment() {
    }

    public static RegisterFragment getInstance() {
        if (instance == null) instance = new RegisterFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        setRetainInstance(true);
        setupActionBar();
        ConnectionDetector cd = new ConnectionDetector(getActivity().getApplicationContext());
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(getActivity(), "Internet Connection Error", "Please connect to a working Internet connection");
            getActivity().getSupportFragmentManager().popBackStack();
        }
        txtName = (EditText) view.findViewById(R.id.txtName);
        txtEmail = (EditText) view.findViewById(R.id.txtEmail);
        Button btnRegister = (Button) view.findViewById(R.id.btnRegister);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getPrefs(preferences);
        btnRegister.setOnClickListener(this);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                String name = txtName.getText().toString();
                String email = txtEmail.getText().toString();
                if (name.trim().length() > 0 && email.trim().length() > 0 && email.contains("@")) {
                    name = crypto(name, getActivity());
                    email = crypto(email, getActivity());
                    preferences.edit().putString("name", name).putString("email", email).apply();
                    GCMFragment GCMFragment = new GCMFragment();
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
                    GCMFragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.push_up_in, 0, 0, R.anim.push_down_out)
                            .replace(R.id.fragment_container, GCMFragment, "GCM_FRAGMENT").addToBackStack(null).commit();
                } else {
                    alert.showAlertDialog(getActivity(), "Registration Error!", "Please enter your details");
                }
                break;
        }
    }

    private void getPrefs(SharedPreferences preferences) {
        Boolean pref_year7 = preferences.getBoolean("pref_year7", false);
        Boolean pref_year8 = preferences.getBoolean("pref_year8", false);
        Boolean pref_year9 = preferences.getBoolean("pref_year9", false);
        Boolean pref_year10 = preferences.getBoolean("pref_year10", false);
        Boolean pref_year11 = preferences.getBoolean("pref_year11", false);
        Boolean pref_year12 = preferences.getBoolean("pref_year12", false);
        Boolean pref_year13 = preferences.getBoolean("pref_year13", false);
        String yes = "yes";
        String no = "no";
        if (pref_year7) year7 = yes;
        else year7 = no;
        if (pref_year8) year8 = yes;
        else year8 = no;
        if (pref_year9) year9 = yes;
        else year9 = no;
        if (pref_year10) year10 = yes;
        else year10 = no;
        if (pref_year11) year11 = yes;
        else year11 = no;
        if (pref_year12) year12 = yes;
        else year12 = no;
        if (pref_year13) year13 = yes;
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
        setHasOptionsMenu(true);
        mActivity = ((MainActivity) getActivity());
        mActivity.setupActionBar(getString(R.string.register));
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
        mActivity.getDelegate().getSupportActionBar().setHomeButtonEnabled(true);
        mActivity.getDelegate().getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActivity.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mActivity.getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mActivity.getDelegate().getSupportActionBar().setDisplayShowTitleEnabled(true);
                mActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        });
    }
}