package com.jonny.wgsb.material;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonny.wgsb.material.activities.TimetableActivity;
import com.jonny.wgsb.material.adapter.DrawerRecyclerViewAdapter;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.db.TimetableProvider;
import com.jonny.wgsb.material.fragments.CalendarFragment;
import com.jonny.wgsb.material.fragments.GCMFragment;
import com.jonny.wgsb.material.fragments.GCMFragmentSpecific;
import com.jonny.wgsb.material.fragments.NewsFragment;
import com.jonny.wgsb.material.fragments.NewsFragmentSpecific;
import com.jonny.wgsb.material.fragments.RegisterFragment;
import com.jonny.wgsb.material.fragments.SettingsFragment;
import com.jonny.wgsb.material.fragments.TopicalFragment;
import com.jonny.wgsb.material.fragments.TopicalFragmentSpecific;
import com.jonny.wgsb.material.ui.helper.Icons;
import com.jonny.wgsb.material.util.CompatUtils;
import com.jonny.wgsb.material.util.TimetableBackupRestore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.jonny.wgsb.material.util.CommonUtilities.DISPLAY_MESSAGE_ACTION;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity {
    final Context mContext = this;
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CompatUtils.isNotLegacyApi11()) invalidateOptionsMenu();
        }
    };
    public NewsFragmentSpecific newsFragmentSpecific = new NewsFragmentSpecific();
    public TopicalFragmentSpecific topicalFragmentSpecific = new TopicalFragmentSpecific();
    public ActionBarDrawerToggle mDrawerToggle;
    public DrawerLayout mDrawerLayout;
    public Toolbar mToolbar;
    NewsFragment newsFragment = new NewsFragment();
    TopicalFragment topicalFragment = new TopicalFragment();
    CalendarFragment calendarFragment = new CalendarFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    GCMFragment GCMFragment = new GCMFragment();
    GCMFragmentSpecific GCMFragmentSpecific = new GCMFragmentSpecific();
    RegisterFragment registerFragment = new RegisterFragment();
    DatabaseHandler dbhandler;
    private Integer id;
    private LinearLayout mDrawerLeftLayout;
    private RecyclerView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbhandler = DatabaseHandler.getInstance(mContext);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLeftLayout = (LinearLayout) findViewById(R.id.left_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.recyclerView);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getDrawerList();
        getOverflowMenu();
        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
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
            GCMFragmentSpecific.setArguments(args);
            selectItem(97);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        if (CompatUtils.isNotLegacyApi11()) invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mHandleMessageReceiver);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        final SettingsFragment settings = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
        if (settings != null && settings.isVisible() && settings.changed == 1) {
            if (!settings.getRegistrationId().isEmpty()) {
                settings.updateDetails();
                settings.changed = 0;
                super.onBackPressed();
            } else {
                selectItem(10);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
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
        View.OnClickListener handler = new View.OnClickListener() {
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.badge:
                selectItem(7);
                break;
            case R.id.settings:
                selectItem(9);
                break;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void getDrawerList() {
        String[] drawerTitles = getResources().getStringArray(R.array.navigation_main_sections);
        TypedArray drawerIcons = getResources().obtainTypedArray(R.array.drawable_ids);
        ArrayList<Icons> icons = new ArrayList<>();
        icons.add(new Icons(drawerTitles[0], drawerIcons.getResourceId(0, -1), 0));
        icons.add(new Icons(drawerTitles[1], drawerIcons.getResourceId(1, -2), 1));
        icons.add(new Icons(drawerTitles[2], drawerIcons.getResourceId(2, -3), 2));
        icons.add(new Icons(drawerTitles[3], drawerIcons.getResourceId(3, -4), 3));
        drawerIcons.recycle();
        mDrawerList.setHasFixedSize(true);
        DrawerRecyclerViewAdapter adapter;
        mDrawerList.setAdapter(adapter = new DrawerRecyclerViewAdapter(icons, R.layout.adapter_main));
        mDrawerList.setLayoutManager(new LinearLayoutManager(mContext));
        mDrawerList.setItemAnimator(new DefaultItemAnimator());
        adapter.setOnItemClickListener(new DrawerRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0:
                        if (!newsFragment.isVisible()) selectItem(1);
                        else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                        break;
                    case 1:
                        if (!topicalFragment.isVisible()) selectItem(3);
                        else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                        break;
                    case 2:
                        if (!calendarFragment.isVisible()) selectItem(5);
                        else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                        break;
                    case 3:
                        mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                        selectItem(6);
                        break;
                    default:
                        mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                }
            }
        });
        /*mDrawerList.addOnItemTouchListener(new RecyclerItemClickListener(mContext, mDrawerList, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    switch (position) {
                        case 0:
                            if (!newsFragment.isVisible()) selectItem(1);
                            else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                            break;
                        case 1:
                            if (!topicalFragment.isVisible()) selectItem(3);
                            else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                            break;
                        case 2:
                            if (!calendarFragment.isVisible()) selectItem(5);
                            else mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                            break;
                        case 3:
                            mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                            selectItem(6);
                            break;
                        default:
                            mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                    }
                }

                @Override public void onItemLongClick(View view, int position) {
                }
            })
        );*/
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(mContext);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setup(Bundle savedInstanceState, Integer id) {
        if (id != 0) {
            Bundle args = new Bundle();
            args.putInt("id", id);
            GCMFragmentSpecific.setArguments(args);
            if (findViewById(R.id.fragment_container) != null) {
                selectItem(97);
            } else {
                selectItem(98);
            }
        } else if (findViewById(R.id.fragment_container) != null && savedInstanceState == null) {
            selectItem(0);
        }
    }

    public void selectItem(int position) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                ft.add(R.id.fragment_container, newsFragment, "NEWS_FRAGMENT");
                break;
            case 1:
                ft.replace(R.id.fragment_container, newsFragment, "NEWS_FRAGMENT").addToBackStack(null);
                break;
            case 2:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, newsFragmentSpecific, "NEWS_FRAGMENT_SPECIFIC").addToBackStack(null);
                break;
            case 3:
                ft.replace(R.id.fragment_container, topicalFragment, "TOPICAL_FRAGMENT").addToBackStack(null);
                break;
            case 4:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, topicalFragmentSpecific, "TOPICAL_FRAGMENT_SPECIFIC").addToBackStack(null);
                break;
            case 5:
                ft.replace(R.id.fragment_container, calendarFragment, "CALENDAR_FRAGMENT").addToBackStack(null);
                break;
            case 6:
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(TimetableProvider.PERIODS_URI, new String[]{TimetableProvider.ID, TimetableProvider.DAY},
                        TimetableProvider.DAY + "='set_up'", null, null);
                File dir = new File(Environment.getExternalStorageDirectory(), "WGSB\backup");
                File file = new File(dir, "backup.txt");
                if (cursor.getCount() == 0 && file.exists()) {
                    cursor.close();
                    onCreateDialog();
                } else {
                    cursor.close();
                    startActivity(new Intent(mContext, TimetableActivity.class));
                }
                break;
            case 7:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragment, "GCM_FRAGMENT").addToBackStack(null);
                break;
            case 8:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT").addToBackStack(null);
                break;
            case 9:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null);
                break;
            case 10:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, registerFragment, "REGISTER_FRAGMENT").addToBackStack(null);
                break;
            case 97:
                ft.setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                        .replace(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT");
                break;
            case 98:
                ft.add(R.id.fragment_container, GCMFragmentSpecific, "GCM_SPECIFIC_FRAGMENT");
                break;
            case 99:
                ft.replace(R.id.fragment_container, newsFragment, "NEWS_FRAGMENT");
                break;
        }
        ft.commit();
        mDrawerLayout.closeDrawer(mDrawerLeftLayout);
    }

    private Dialog onCreateDialog() {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog alert;
        final CharSequence[] bool = {getString(R.string.yes), getString(R.string.no)};
        builder.setTitle(R.string.restore_detected_backup);
        builder.setItems(bool, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    TimetableBackupRestore.restore(mContext);
                }
                startActivity(new Intent(mContext, TimetableActivity.class));
                dialog.cancel();
            }
        });
        alert = builder.create();
        dialog = alert;
        return dialog;
    }
}