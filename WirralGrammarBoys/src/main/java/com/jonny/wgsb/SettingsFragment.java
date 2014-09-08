package com.jonny.wgsb;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.MailTo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    static final String TAG = "WGSB:GCM";
    Integer changed = 0;
    GoogleCloudMessaging gcm;
    DatabaseHandler dbhandler;
    AsyncTask<Void, Void, Void> mUpdateTask, mUnregisterTask;
    private String email, year7, year8, year9, year10, year11, year12, year13, regId;
    private SwitchPreference mPush;
    private Preference mYear, appVersion, bugReport, jonny;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setupActionBar();
        dbhandler = DatabaseHandler.getInstance(mContext);
        addPreferencesFromResource(R.xml.pref_settings);
        PreferenceScreen preferencescreen = getPreferenceScreen();
        mPush = (SwitchPreference) preferencescreen.findPreference("pref_push");
        mYear = preferencescreen.findPreference("pref_year");
        appVersion = preferencescreen.findPreference("appVersion");
        bugReport = preferencescreen.findPreference("bugReport");
        jonny = preferencescreen.findPreference("jonny");
        setPrefs();
        setStaticPrefs();
        savedInstanceState = getArguments();
        if (savedInstanceState != null) {
            String startScreen = savedInstanceState.getString("PREFERENCE_SCREEN_KEY");
            if (startScreen != null) {
                savedInstanceState.remove("PREFERENCE_SCREEN_KEY");
                final Preference preference = findPreference(startScreen);
                final PreferenceScreen preferenceScreen = getPreferenceScreen();
                final ListAdapter listAdapter = preferenceScreen.getRootAdapter();
                final int itemsCount = listAdapter.getCount();
                int itemNumber;
                for (itemNumber = 0; itemNumber < itemsCount; ++itemNumber) {
                    if (listAdapter.getItem(itemNumber).equals(preference)) {
                        preferenceScreen.onItemClick(null, null, itemNumber, 0);
                        break;
                    }
                }
                initializeActionBar((PreferenceScreen) preference);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setupActionBar() {
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle(R.string.settings);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Color.parseColor("#FF004890"));
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getActivity().getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void setPrefs() {
        if (mPush.isChecked()) {
            mYear.setEnabled(true);
            mYear.setSelectable(true);
        } else {
            mYear.setEnabled(false);
            mYear.setSelectable(false);
        }
    }

    private void setStaticPrefs() {
        String versionName;
        try {
            versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "IS BROKEN WHAT HAVE YOU DONE";
            e.printStackTrace();
        }
        appVersion.setTitle(getResources().getString(R.string.app_name) + " " + versionName);
        appVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.jonny.wgsb")));
                return true;
            }
        });
        bugReport.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String versionName;
                try {
                    versionName = mContext.getApplicationContext().getPackageManager().getPackageInfo(
                            mContext.getApplicationContext().getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                    versionName = "Unknown";
                    e.printStackTrace();
                }
                MailTo mt = MailTo.parse("mailto:14FitchJ@wirralgrammarboys.com");
                Intent reportEmail = new Intent(Intent.ACTION_SEND);
                reportEmail.setType("message/rfc822");
                reportEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{mt.getTo()});
                reportEmail.putExtra(Intent.EXTRA_SUBJECT, "WGSB bug report");
                reportEmail.putExtra(Intent.EXTRA_TEXT, "WGSB Version: " + versionName +
                        "<br></br>Android Version: " + android.os.Build.VERSION.RELEASE
                        + "<br></br>Device: " + android.os.Build.MODEL
                        + "<br></br>How I found the bug: "
                        + "<br></br>Description of bug: "
                        + "<br></br>If the bug is visual, attached screenshots would be brilliant! :)");
                try {
                    startActivity(Intent.createChooser(reportEmail, "Send email..."));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        jonny.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Jonathon+Fitch")));
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_push")) {
            setPrefs();
            if (mPush.isChecked()) {
                changed = 1;
                SettingsFragment settingsFragment = new SettingsFragment();
                Bundle args = new Bundle();
                args.putString("PREFERENCE_SCREEN_KEY", "pref_year");
                settingsFragment.setArguments(args);
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null).commit();
            } else {
                changed = 0;
                unRegister();
            }
        } else if (key.equals("pref_year7") || key.equals("pref_year8") || key.equals("pref_year9")
                || key.equals("pref_year10") || key.equals("pref_year11") || key.equals("pref_year12")
                || key.equals("pref_year11")) {
            changed = 1;
        }
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    String getRegistrationId() {
        String registrationId = dbhandler.getRegId();
        if (registrationId.equals("")) {
            Log.i(TAG, "Registration not found.");
            return registrationId;
        }
        int registeredVersion = dbhandler.getRegIdAppVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    void updateDetails() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String yes = mContext.getString(R.string.gcm_yes);
        String no = mContext.getString(R.string.gcm_no);
        Boolean pref_year7 = preferences.getBoolean("pref_year7", false);
        Boolean pref_year8 = preferences.getBoolean("pref_year8", false);
        Boolean pref_year9 = preferences.getBoolean("pref_year9", false);
        Boolean pref_year10 = preferences.getBoolean("pref_year10", false);
        Boolean pref_year11 = preferences.getBoolean("pref_year11", false);
        Boolean pref_year12 = preferences.getBoolean("pref_year12", false);
        Boolean pref_year13 = preferences.getBoolean("pref_year13", false);
        email = preferences.getString("email", null);
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
        if (gcm == null) gcm = GoogleCloudMessaging.getInstance(mContext);
        if (!getRegistrationId().isEmpty()) update();
    }

    private void update() {
        mUpdateTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ServerUtilities.update(regId, email, year7, year8, year9, year10, year11, year12, year13);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mUpdateTask = null;
            }
        };
        mUpdateTask.execute(null, null, null);
    }

    private void unRegister() {
        mUnregisterTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                regId = getRegistrationId();
                ServerUtilities.unregister(regId);
                dbhandler.deleteAllRegId();
                gcm = GoogleCloudMessaging.getInstance(mContext);
                try {
                    gcm.unregister();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mUnregisterTask.execute(null, null, null);
                Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
                unregIntent.putExtra("app", PendingIntent.getBroadcast(mContext, 0, new Intent(), 0));
                mContext.startService(unregIntent);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mUnregisterTask = null;
            }
        };
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference);
        }
        return false;
    }

    private void initializeActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();
        if (dialog != null) {
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
            dialog.getActionBar().setIcon(R.drawable.banner);
            dialog.getActionBar().setTitle(R.string.year);
            dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
            dialog.findViewById(android.R.id.list).setBackgroundColor(Color.parseColor("#FFF3F3F3"));
            View homeBtn = dialog.findViewById(android.R.id.home);
            if (homeBtn != null) {
                OnClickListener dismissDialogClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };
                ViewParent homeBtnContainer = homeBtn.getParent();
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();
                    if (containerParent instanceof LinearLayout) {
                        (containerParent).setOnClickListener(dismissDialogClickListener);
                    } else {
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }
}