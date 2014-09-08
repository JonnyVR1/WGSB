package com.jonny.wgsb;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GCMFragmentSpecific extends Fragment {
    DatabaseHandler dbhandler;
    TextView tDisplay, dDisplay, mDisplay;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gcm_specific, container, false);
        setupActionBar();
        final Integer id = getArguments().getInt("id", 1);
        dbhandler = DatabaseHandler.getInstance(getActivity());
        Notifications notifications = dbhandler.getNotification(id);
        final String title = notifications.getTitle();
        final String date = notifications.getDate();
        final String message = notifications.getMessage();
        tDisplay = (TextView) view.findViewById(R.id.tDisplay);
        dDisplay = (TextView) view.findViewById(R.id.dDisplay);
        mDisplay = (TextView) view.findViewById(R.id.mDisplay);
        if (title == null) tDisplay.setVisibility(View.INVISIBLE);
        else tDisplay.setText(title);
        if (date == null) dDisplay.setVisibility(View.INVISIBLE);
        else dDisplay.setText(date);
        if (message != null) mDisplay.append(message + "\n");
        dbhandler.updateNotifications(new Notifications(id, 1));
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle("Notifications");
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
}