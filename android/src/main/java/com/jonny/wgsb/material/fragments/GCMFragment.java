package com.jonny.wgsb.material.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.activities.MainActivity;
import com.jonny.wgsb.material.adapter.NotificationsRecyclerViewAdapter;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.ui.helper.Notifications;
import com.jonny.wgsb.material.util.AlertDialogManager;
import com.jonny.wgsb.material.util.CommonUtilities;
import com.jonny.wgsb.material.util.ConnectionDetector;
import com.jonny.wgsb.material.util.ServerUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.jonny.wgsb.material.util.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.jonny.wgsb.material.util.CommonUtilities.SENDER_ID;

public class GCMFragment extends Fragment {
    private static GCMFragment instance = null;
    private static String name, email, year7, year8, year9, year10, year11, year12, year13;
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
    private final AlertDialogManager alert = new AlertDialogManager();
    private MainActivity mActivity;
    private TextView tDisplay;
    private GoogleCloudMessaging gcm;
    private DatabaseHandler dbhandler;
    private ConnectionDetector cd;
    private RecyclerView notificationsListView;
    private Context mContext;
    private String regId;
    private CharSequence previousTitle;

    public GCMFragment() {
    }

    public static GCMFragment getInstance() {
        if (instance == null) instance = new GCMFragment();
        return instance;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mContext = getActivity();
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        dbhandler = DatabaseHandler.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gcm, container, false);
        mActivity = ((MainActivity) getActivity());
        previousTitle = mActivity.getSupportActionBar().getTitle();
        setupActionBar();
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
        notificationsListView = (RecyclerView) view.findViewById(R.id.notifications_list);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mContext));
        notificationsListView.setItemAnimator(new DefaultItemAnimator());
        setInitialText();
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
        setupActionBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        mActivity.getSupportActionBar().setTitle(previousTitle);
        mActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
        final List<HashMap<String, String>> notificationsListItems = new ArrayList<>();
        List<Notifications> notifications = dbhandler.getAllNotifications();
        for (Notifications n : notifications) {
            HashMap<String, String> map = new HashMap<>();
            map.put("listID", n.id.toString());
            map.put("listRead", n.read.toString());
            map.put("listTitle", n.title);
            map.put("listDate", n.date);
            notificationsListItems.add(map);
        }
        final NotificationsRecyclerViewAdapter adapter;
        notificationsListView.setAdapter(adapter = new NotificationsRecyclerViewAdapter(notificationsListItems, R.layout.list_notifications));
        adapter.setOnItemClickListener(new NotificationsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView idText = (TextView) view.findViewById(R.id.notificationId);
                Integer notificationId = Integer.parseInt(idText.getText().toString());
                Bundle args = new Bundle();
                args.putInt("id", notificationId);
                ((MainActivity) getActivity()).GCMFragmentSpecific.setArguments(args);
                ((MainActivity) getActivity()).selectItem(8);
            }
        });
        adapter.setOnItemLongClickListener(new NotificationsRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                TextView idText = (TextView) view.findViewById(R.id.notificationId);
                final Integer notificationId = Integer.parseInt(idText.getText().toString());
                final TextView readText = (TextView) view.findViewById(R.id.readNotification);
                final Integer read = Integer.parseInt(readText.getText().toString());
                CharSequence[] items;
                if (read == 1) items = new CharSequence[]{"Mark as unread", "Delete"};
                else items = new CharSequence[]{"Mark as read", "Delete"};
                new MaterialDialog.Builder(mContext)
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                switch (i) {
                                    case 0:
                                        if (read == 0) {
                                            dbhandler.updateNotifications(new Notifications(notificationId, 1));
                                        } else {
                                            dbhandler.updateNotifications(new Notifications(notificationId, 0));
                                        }
                                        getNotificationsList();
                                        break;
                                    case 1:
                                        dbhandler.deleteNotificationAtId(notificationId);
                                        notificationsListItems.remove(position);
                                        adapter.notifyDataSetChanged();
                                        if (dbhandler.getNotificationsCount() > 0) {
                                            tDisplay.setText("Touch an item to view the message");
                                        } else {
                                            tDisplay.setText("There are no notifications to display");
                                        }
                                        getNotificationsList();
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 9000).show();
            } else {
                Log.i(CommonUtilities.TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void checkRegistration() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getActivity());
            regId = getRegistrationId(mContext);
            if (regId.isEmpty() && name == null) {
                if (cd.isConnectingToInternet()) {
                    if (dbhandler.getNotificationsCount() != 0) dbhandler.deleteAllNotifications();
                    Toast.makeText(getActivity(), R.string.push_need_on, Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).selectItem(9);
                } else {
                    alert.showAlertDialog(getActivity(), getString(R.string.internet_connection_error), getString(R.string.internet_connection_error_extra));
                }
            } else if (regId.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(CommonUtilities.TAG, "No valid Google Play Services APK found.");
            alert.showAlertDialog(getActivity(), getString(R.string.gcm_play_services_error), getString(R.string.gcm_play_services_error_extra));
        }
    }

    private String getRegistrationId(Context context) {
        String registrationId = dbhandler.getRegId();
        if (registrationId.isEmpty()) {
            Log.i(CommonUtilities.TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = dbhandler.getRegIdAppVersion();
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(CommonUtilities.TAG, "App version changed.");
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
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regId = gcm.register(SENDER_ID);
                    ServerUtilities.register(mContext, regId, name, email, year7, year8, year9, year10, year11, year12, year13);
                    storeRegistrationId(mContext, regId);
                } catch (IOException ex) {
                    String msg = "Error :" + ex.getMessage();
                    Log.e(CommonUtilities.TAG, msg);
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(CommonUtilities.TAG, "Saving regId on app version " + appVersion);
        if (dbhandler.getRegIdCount() > 0) {
            dbhandler.getWritableDatabase();
            dbhandler.updateRegId(regId, appVersion);
        } else {
            dbhandler.getWritableDatabase();
            dbhandler.addRegId(regId, appVersion);
        }
    }

    private void setupActionBar() {
        setHasOptionsMenu(true);
        mActivity.mDrawerToggle.setDrawerIndicatorEnabled(false);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActivity.getSupportActionBar().setTitle(R.string.notifications);
        mActivity.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                mActivity.getSupportActionBar().setTitle(previousTitle);
                mActivity.mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        });
    }
}