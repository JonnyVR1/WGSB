package com.jonny.wgsb;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GCMFragmentSpecificLegacy extends Fragment {
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gcm_specific, container, false);
        setupActionBar();
        final Integer id = getArguments().getInt("id", 1);
        DatabaseHandler dbhandler = DatabaseHandler.getInstance(getActivity());
        Notifications notifications = dbhandler.getNotification(id);
        final String title = notifications.getTitle();
        final String date = notifications.getDate();
        final String message = notifications.getMessage();
        TextView tDisplay = (TextView) view.findViewById(R.id.tDisplay);
        TextView dDisplay = (TextView) view.findViewById(R.id.dDisplay);
        TextView mDisplay = (TextView) view.findViewById(R.id.mDisplay);
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

    private void setupActionBar() {
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle("Notifications");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}