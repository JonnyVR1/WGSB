package com.jonny.wgsb.material.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.db.TimetableProvider;
import com.jonny.wgsb.material.util.TimetableBackupRestore;

@SuppressWarnings("deprecation")
public class TimetableSettingsActivity extends ActionBarActivity {
    private final static int CONFIRM_DIALOG_ID = 0, RESTORE_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(R.id.fragment_container_timetable);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (savedInstanceState == null) {
            TimetableSettingsFragment TimetableSettingsFragment = new TimetableSettingsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_timetable, TimetableSettingsFragment, "TIMETABLE_SETTINGS_FRAGMENT").commit();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        MaterialDialog dialog;
        MaterialDialog.Builder builder;
        switch (id) {
            case CONFIRM_DIALOG_ID:
                builder = new MaterialDialog.Builder(this);
                TextView confirm = new TextView(this);
                confirm.setText(R.string.clear_data);
                confirm.setPadding(15, 15, 15, 15);
                builder.title(R.string.clear_data_confirm)
                        .customView(confirm, false)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                Intent backToHome = new Intent(TimetableSettingsActivity.this, TimetableActivity.class);
                                backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                ContentResolver cr = getContentResolver();
                                cr.delete(TimetableProvider.PERIODS_URI, null, null);
                                cr.delete(TimetableProvider.WEEK_URI, null, null);
                                startActivity(backToHome);
                                overridePendingTransition(0, R.anim.push_down_out);
                            }
                        });
                dialog = builder.show();
                break;
            case RESTORE_DIALOG_ID:
                builder = new MaterialDialog.Builder(this);
                TextView restoreTv = new TextView(this);
                restoreTv.setText(R.string.restore_confirm);
                restoreTv.setPadding(15, 15, 15, 15);
                builder.title(R.string.restore)
                        .customView(restoreTv, false)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog materialDialog) {
                                ProgressDialog progressDialog;
                                progressDialog = new ProgressDialog(TimetableSettingsActivity.this);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setMessage(getString(R.string.restoring));
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                Intent backToHome = new Intent(TimetableSettingsActivity.this, TimetableActivity.class);
                                backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                ContentResolver cr = getContentResolver();
                                cr.delete(TimetableProvider.PERIODS_URI, null, null);
                                cr.delete(TimetableProvider.WEEK_URI, null, null);
                                TimetableBackupRestore.restore(TimetableSettingsActivity.this);
                                progressDialog.dismiss();
                                startActivity(backToHome);
                                overridePendingTransition(0, R.anim.push_down_out);
                            }
                        });
                dialog = builder.show();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    public static class TimetableSettingsFragment extends PreferenceFragment {
        TimetableSettingsActivity mActivity;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_timetable_settings, container, false);
            setupToolbar(view);
            return view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.timetable_settings);
            setPrefs();
            setStaticPrefs();
        }

        private void setupToolbar(View view) {
            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            toolbar.setTitle(R.string.timetable_settings);
            mActivity = ((TimetableSettingsActivity) getActivity());
            mActivity.setSupportActionBar(toolbar);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, R.anim.push_down_out);
                }
            });
        }

        private void setPrefs() {
            Preference backup = findPreference("backup");
            Preference clearData = findPreference("clearData");
            Preference restore = findPreference("restore");
            ListPreference theme = (ListPreference) findPreference("theme");
            backup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    TimetableBackupRestore.backup(getActivity());
                    return true;
                }
            });
            clearData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().showDialog(CONFIRM_DIALOG_ID);
                    return true;
                }
            });
            restore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().showDialog(RESTORE_DIALOG_ID);
                    return true;
                }
            });
            theme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference pref, Object key) {
                    ContentResolver cr = getActivity().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.KEY, "theme");
                    if (key.equals("light")) {
                        values.put(TimetableProvider.NUM, 1);
                    } else {
                        values.put(TimetableProvider.NUM, 2);
                    }
                    cr.update(TimetableProvider.WEEK_URI, values, TimetableProvider.KEY + "='theme'", null);
                    return true;
                }
            });
        }

        private void setStaticPrefs() {
            ListPreference viewStyle = (ListPreference) findPreference("viewStyle");
            viewStyle.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference pref, Object key) {
                    ContentResolver cr = getActivity().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.KEY, "style");
                    if (key.equals("exp")) {
                        values.put(TimetableProvider.NUM, 1);
                    } else if (key.equals("comp")) {
                        values.put(TimetableProvider.NUM, 2);
                    }
                    cr.update(TimetableProvider.WEEK_URI, values, TimetableProvider.KEY + "='style'", null);
                    return true;
                }
            });
        }
    }
}