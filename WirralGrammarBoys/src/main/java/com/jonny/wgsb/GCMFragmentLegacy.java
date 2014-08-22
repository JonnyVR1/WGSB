package com.jonny.wgsb;

import static com.jonny.wgsb.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.jonny.wgsb.CommonUtilities.SENDER_ID;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GCMFragmentLegacy extends Fragment {
    private String regId;
    private static String name, email, year7, year8, year9, year10, year11, year12, year13;
    private final static String TAG = "WGSB:GCM";
    TextView tDisplay;
    GoogleCloudMessaging gcm;
    Context context;
    AlertDialogManager alert = new AlertDialogManager();
    DatabaseHandler dbhandler;
    ConnectionDetector cd;
    ListView notificationsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_gcm, container, false);
        setupActionBar();
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        dbhandler = DatabaseHandler.getInstance(getActivity());
        savedInstanceState = getArguments();
        if (savedInstanceState != null) {
            name = savedInstanceState.getString("name");
            email = savedInstanceState.getString("email");
            year7 = savedInstanceState.getString("year7");
            year8 = savedInstanceState.getString("year8");
            year9 = savedInstanceState.getString("year9");
            year10 = savedInstanceState.getString("year10");
            year11 = savedInstanceState.getString("year11");
            year12 = savedInstanceState.getString("year12");
            year13 = savedInstanceState.getString("year13");
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String prefs_name = prefs.getString("name", null);
            final String prefs_email = prefs.getString("email", null);
            if (prefs_name != null) name = prefs_name;
            if (prefs_email != null) email = prefs_email;
        }
        tDisplay = (TextView) view.findViewById(R.id.tDisplay);
        notificationsListView = (ListView) view.findViewById(R.id.notifications_list);
        setInitialText();
        context = getActivity().getApplicationContext();
        checkRegistration();
        getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        setInitialText();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setInitialText() {
        if (dbhandler.getNotificationsCount() > 0) {
            tDisplay.setText("Touch an item to view the message");
            getNotificationsList();
        } else {
            tDisplay.setText("There are no notifications to display");
        }
    }

    private void getNotificationsList() {
        final List<HashMap<String, String>> notificationsListItems = new ArrayList<HashMap<String, String>>();
        List<Notifications> notifications = dbhandler.getAllNotifications();
        for(Notifications n : notifications) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("listID", n.getID().toString());
            map.put("listRead", n.getRead().toString());
            map.put("listTitle", n.getTitle());
            map.put("listDate", n.getDate());
            notificationsListItems.add(map);
        }
        final BaseAdapter notificationsListAdapter = new SimpleAdapter(getActivity(), notificationsListItems, R.layout.list_notifications,
                new String[]{"listID", "listRead", "listTitle", "listDate"},
                new int[]{R.id.notificationId, R.id.readNotification, R.id.titleNotification, R.id.dateNotification}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView title = (TextView) view.findViewById(R.id.titleNotification);
                TextView readText = (TextView) view.findViewById(R.id.readNotification);
                Integer read = Integer.parseInt(readText.getText().toString());
                if (read == 0) title.setTextColor(getResources().getColor(R.color.red));
                return view;
            }
        };
        notificationsListView.setAdapter(notificationsListAdapter);
        notificationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idText = (TextView) view.findViewById(R.id.notificationId);
                Integer notificationId = Integer.parseInt(idText.getText().toString());
                Bundle args = new Bundle();
                args.putInt("id", notificationId);
                GCMFragmentSpecific GCMFragmentSpecific = new GCMFragmentSpecific();
                GCMFragmentSpecific.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT").addToBackStack(null).commit();
            }
        });
        notificationsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                TextView idText = (TextView) view.findViewById(R.id.notificationId);
                final Integer notificationId = Integer.parseInt(idText.getText().toString());
                final TextView readText = (TextView) view.findViewById(R.id.readNotification);
                final Integer read = Integer.parseInt(readText.getText().toString());
                CharSequence[] items;
                if (read == 1) items = new CharSequence[]{"Mark as unread", "Delete"};
                else items = new CharSequence[]{"Mark as read", "Delete"};
                AlertDialog.Builder alertBox = new AlertDialog.Builder(getActivity());
                alertBox.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                if (read == 0) {
                                    dbhandler.updateNotifications(new Notifications(notificationId, 1));
                                } else {
                                    dbhandler.updateNotifications(new Notifications(notificationId, 0));
                                }
                                getNotificationsList();
                                dialog.dismiss();
                                break;
                            case 1:
                                dbhandler.deleteNotificationAtId(notificationId);
                                notificationsListItems.remove(position);
                                notificationsListAdapter.notifyDataSetChanged();
                                if (dbhandler.getNotificationsCount() > 0) {
                                    tDisplay.setText("Touch an item to view the message");
                                } else {
                                    tDisplay.setText("There are no notifications to display");
                                }
                                getNotificationsList();
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                alertBox.show();
                return true;
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 9000).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void checkRegistration() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getActivity());
            regId = getRegistrationId(context);
            if (regId.isEmpty() && name == null) {
                if (cd.isConnectingToInternet()) {
                    if (dbhandler.getNotificationsCount() != 0) dbhandler.deleteAllNotifications();
                    Toast.makeText(getActivity(), "Push notifications need to be on to show alerts", Toast.LENGTH_SHORT).show();
                    SettingsFragment settingsFragment = new SettingsFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                            .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null).commit();
                } else {
                    alert.showAlertDialog(getActivity(), getString(R.string.internet_connection_error), getString(R.string.internet_connection_error_extra), false);
                }
            } else if (regId.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            alert.showAlertDialog(getActivity(), getString(R.string.gcm_play_services_error), getString(R.string.gcm_play_services_error_extra), false);
        }
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getRegistrationId(Context context) {
        String registrationId = dbhandler.getRegId();
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = dbhandler.getRegIdAppVersion();
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    ServerUtilities.register(context, regId, name, email, year7, year8, year9, year10, year11, year12, year13);
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    String msg = "Error :" + ex.getMessage();
                    Log.e(TAG, msg);
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        if (dbhandler.getRegIdCount() > 0) {
            dbhandler.getWritableDatabase();
            dbhandler.updateRegId(regId, appVersion);
        } else {
            dbhandler.getWritableDatabase();
            dbhandler.addRegId(regId, appVersion);
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dbhandler.getWritableDatabase();
            if (dbhandler.getNotificationsCount() > 0) {
                tDisplay.setText("Touch an item to view the message");
                getNotificationsList();
            } else {
                tDisplay.setText("There are no notifications to display");
            }
        }
    };

    private void setupActionBar() {
        final ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle("Notifications");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}