package com.jonny.wgsb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

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
        Dialog dialog;
        AlertDialog.Builder builder;
        AlertDialog alert;
        switch (id) {
            case CONFIRM_DIALOG_ID:
                builder = new AlertDialog.Builder(this);
                TextView confirm = new TextView(this);
                confirm.setText(R.string.clear_data);
                confirm.setPadding(15, 15, 15, 15);
                builder.setTitle(R.string.clear_data_confirm);
                builder.setView(confirm);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent backToHome = new Intent(TimetableSettingsActivity.this, TimetableTabController.class);
                        backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ContentResolver cr = getContentResolver();
                        cr.delete(TimetableProvider.PERIODS_URI, null, null);
                        cr.delete(TimetableProvider.WEEK_URI, null, null);
                        startActivity(backToHome);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert = builder.create();
                dialog = alert;
                break;
            case RESTORE_DIALOG_ID:
                builder = new AlertDialog.Builder(this);
                TextView restoreTv = new TextView(this);
                restoreTv.setText(R.string.restore_confirm);
                restoreTv.setPadding(15, 15, 15, 15);
                builder.setTitle(R.string.restore);
                builder.setView(restoreTv);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ProgressDialog progressDialog;
                        progressDialog = new ProgressDialog(TimetableSettingsActivity.this);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage(getString(R.string.restoring));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Intent backToHome = new Intent(TimetableSettingsActivity.this, TimetableTabController.class);
                        backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ContentResolver cr = getContentResolver();
                        cr.delete(TimetableProvider.PERIODS_URI, null, null);
                        cr.delete(TimetableProvider.WEEK_URI, null, null);
                        TimetableBackupRestore.restore(TimetableSettingsActivity.this);
                        progressDialog.dismiss();
                        startActivity(backToHome);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert = builder.create();
                dialog = alert;
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    public static class TimetableSettingsFragment extends PreferenceFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_timetable_settings, container, false);
            setupActionBar(view);
            return view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.timetable_settings);
            setPrefs();
            setStaticPrefs();
        }

        private void setupActionBar(View view) {
            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            toolbar.setTitle(R.string.timetable_settings);
            ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
			ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
			mActionBar.setDisplayHomeAsUpEnabled(true);
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

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                Intent intent = new Intent(getActivity(), TimetableTabController.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}