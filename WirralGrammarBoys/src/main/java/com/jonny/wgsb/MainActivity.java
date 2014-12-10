package com.jonny.wgsb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.jonny.wgsb.CommonUtilities.DISPLAY_MESSAGE_ACTION;

public class MainActivity extends ActionBarActivity {
    private Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverflowMenu();
        supportInvalidateOptionsMenu();
        id = getIntent().getIntExtra("id", 0);
        setup(savedInstanceState, id);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        id = i.getIntExtra("id", 0);
        if (id != 0) {
            Bundle args = new Bundle();
            args.putInt("id", id);
            GCMFragmentSpecific GCMFragmentSpecific = new GCMFragmentSpecific();
            GCMFragmentSpecific.setArguments(args);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                    .replace(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT").commit();
        }
    }

    @Override
    public void onBackPressed() {
        final NewsFragment news = (NewsFragment) getSupportFragmentManager().findFragmentByTag("NEWS_FRAGMENT");
        final TopicalFragment topical = (TopicalFragment) getSupportFragmentManager().findFragmentByTag("TOPICAL_FRAGMENT");
        final SettingsFragment settings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
        if (news != null && news.isVisible()) {
            if (news.onBackPressed()) {
                super.onBackPressed();
            }
        } else if (topical != null && topical.isVisible()) {
            if (topical.onBackPressed()) {
                super.onBackPressed();
            }
        } else if (settings != null && settings.isVisible()) {
            if (settings.changed == 1) {
                if (!settings.getRegistrationId().isEmpty()) {
                    settings.updateDetails();
                    settings.changed = 0;
                    super.onBackPressed();
                } else {
                    RegisterFragment registerFragment = new RegisterFragment();
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                            .replace(R.id.fragment_container, registerFragment, "REGISTER_FRAGMENT").addToBackStack(null).commit();
                }
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void setup(Bundle savedInstanceState, Integer id) {
        if (id != 0) {
            Bundle args = new Bundle();
            args.putInt("id", id);
            GCMFragmentSpecific GCMFragmentSpecific = new GCMFragmentSpecific();
            GCMFragmentSpecific.setArguments(args);
            if (findViewById(R.id.fragment_container) != null) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT").commit();
            } else {
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT").commit();
            }
        } else if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            MainFragment mainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainFragment, "MAIN_FRAGMENT").commit();
        }
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static class MainFragment extends Fragment {
        final OnClickListener handler = new OnClickListener() {
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.website_btn:
                        Uri website = Uri.parse("http://wirralgrammarboys.com/");
                        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, website);
                        startActivity(websiteIntent);
                        break;
                    case R.id.facebook_btn:
                        Uri facebook = Uri.parse("https://www.facebook.com/WirralGSB");
                        Intent facebookIntent = new Intent(Intent.ACTION_VIEW, facebook);
                        startActivity(facebookIntent);
                        break;
                    case R.id.twitter_btn:
                        Uri twitter = Uri.parse("https://twitter.com/WGSB");
                        Intent twitterIntent = new Intent(Intent.ACTION_VIEW, twitter);
                        startActivity(twitterIntent);
                        break;
                }
            }
        };
        private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (CompatUtils.isNotLegacyApi11()) getActivity().invalidateOptionsMenu();
            }
        };
        DatabaseHandler dbhandler;
        ListView listView;
        Button websiteButton, facebookButton, twitterButton;
        Toolbar toolbar;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_main, container, false);
            toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            setupActionBar();
            dbhandler = DatabaseHandler.getInstance(getActivity());
            getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
            ArrayList<MainDetails> arrayList = GetSearchResults();
            listView = (ListView) view.findViewById(R.id.main_list);
            websiteButton = (Button) view.findViewById(R.id.website_btn);
            facebookButton = (Button) view.findViewById(R.id.facebook_btn);
            twitterButton = (Button) view.findViewById(R.id.twitter_btn);
            websiteButton.setOnClickListener(handler);
            facebookButton.setOnClickListener(handler);
            twitterButton.setOnClickListener(handler);
            listView.setAdapter(new MainListBaseAdapter(this.getActivity(), arrayList));
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    switch (position) {
                        case 0:
                            NewsFragment newsFragment = new NewsFragment();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                                    .replace(R.id.fragment_container, newsFragment, "NEWS_FRAGMENT").addToBackStack(null).commit();
                            break;
                        case 1:
                            TopicalFragment topicalFragment = new TopicalFragment();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                                    .replace(R.id.fragment_container, topicalFragment, "TOPICAL_FRAGMENT").addToBackStack(null).commit();
                            break;
                        case 2:
                            ContentResolver cr = getActivity().getContentResolver();
                            Cursor cursor = cr.query(TimetableProvider.PERIODS_URI, new String[]{TimetableProvider.ID, TimetableProvider.DAY},
                                    TimetableProvider.DAY + "='set_up'", null, null);
                            File dir = new File(Environment.getExternalStorageDirectory(), "WGSB\backup");
                            File file = new File(dir, "backup.txt");
                            if (cursor.getCount() == 0 && file.exists()) {
                                cursor.close();
                                onCreateDialog();
                            } else {
                                cursor.close();
                                startActivity(new Intent(getActivity(), TimetableTabController.class));
                            }
                            break;
                        case 3:
                            CalendarFragment calendarFragment = new CalendarFragment();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                                    .replace(R.id.fragment_container, calendarFragment, "CALENDAR_FRAGMENT").addToBackStack(null).commit();
                            break;
                    }
                }
            });
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
            if (CompatUtils.isNotLegacyApi11()) getActivity().invalidateOptionsMenu();
            setupActionBar();
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
        public void onDetach() {
            super.onDestroy();
            try {
                getActivity().unregisterReceiver(mHandleMessageReceiver);
            } catch (Exception e) {
                Log.e("UnRegister Receiver Error", "> " + e.getMessage());
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.main, menu);
            final MenuItem item = menu.findItem(R.id.badge);
            MenuItemCompat.setActionView(item, R.layout.actionbar_badge_layout);
            View view = MenuItemCompat.getActionView(item);
            TextView notificationText = (TextView) view.findViewById(R.id.actionbar_notifcation_textview);
            Integer i = dbhandler.getUnreadNotificationsCount();
            if (i > 0) {
                notificationText.setText(i.toString());
            } else {
                notificationText.setVisibility((View.INVISIBLE));
            }
            final ImageView item2 = (ImageView) view.findViewById(R.id.badge_circle);
            OnClickListener handler = new OnClickListener() {
                public void onClick(View view) {
                    onOptionsItemSelected(item);
                }
            };
            if (CompatUtils.isNotLegacyApi11()) item.getActionView().setOnClickListener(handler);
            else {
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onOptionsItemSelected(item);
                        return false;
                    }
                });
            }
            item2.setOnClickListener(handler);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.settings) {
                SettingsFragment settingsFragment = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null).commit();
                return true;
            } else if (item.getItemId() == R.id.badge) {
                GCMFragment GCMFragment = new GCMFragment();
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragment, "GCM_FRAGMENT").addToBackStack(null).commit();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void setupActionBar() {
            setHasOptionsMenu(true);
            toolbar.setTitle(R.string.app_name);
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }

        private Dialog onCreateDialog() {
            Dialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialog alert;
            final CharSequence[] bool = {getString(R.string.yes), getString(R.string.no)};
            builder.setTitle(R.string.restore_detected_backup);
            builder.setItems(bool, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        TimetableBackupRestore.restore(getActivity());
                    }
                    startActivity(new Intent(getActivity(), TimetableTabController.class));
                    dialog.cancel();
                }
            });
            alert = builder.create();
            dialog = alert;
            return dialog;
        }

        private ArrayList<MainDetails> GetSearchResults() {
            ArrayList<MainDetails> arrayList = new ArrayList<MainDetails>();

            MainDetails mainDetails = new MainDetails();
            mainDetails.setName(R.string.news);
            mainDetails.setImageNumber(1);
            arrayList.add(mainDetails);

            MainDetails mainDetails2 = new MainDetails();
            mainDetails2.setName(R.string.topical_information);
            mainDetails2.setImageNumber(2);
            arrayList.add(mainDetails2);

            MainDetails mainDetails3 = new MainDetails();
            mainDetails3.setName(R.string.timetable_name);
            mainDetails3.setImageNumber(3);
            arrayList.add(mainDetails3);

            MainDetails mainDetails4 = new MainDetails();
            mainDetails4.setName(R.string.calendar);
            mainDetails4.setImageNumber(4);
            arrayList.add(mainDetails4);

            return arrayList;
        }
    }
}