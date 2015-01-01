package com.jonny.wgsb.material.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonny.wgsb.material.MainActivity;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.adapter.TimetablePagerAdapter;
import com.jonny.wgsb.material.db.TimetableProvider;
import com.jonny.wgsb.material.fragments.TimetableFragment;
import com.jonny.wgsb.material.util.CompatUtils;
import com.jonny.wgsb.material.util.TimetableBackupRestore;
import com.melnykov.fab.FloatingActionButton;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

@SuppressLint({"NewApi", "ValidFragment", "Deprecation"})
@SuppressWarnings("deprecation")
public class TimetableActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {
    private final static int CONFIRM_DIALOG_ID = 0, RESTORE_DIALOG_ID = 1;
    private final TimetableFragment[] days = new TimetableFragment[5];
    private final TimetableFragment monday = new TimetableFragment();
    private final TimetableFragment tuesday = new TimetableFragment();
    private final TimetableFragment wednesday = new TimetableFragment();
    private final TimetableFragment thursday = new TimetableFragment();
    private final TimetableFragment friday = new TimetableFragment();
    private ViewPager mViewPager;
    private TitlePageIndicator titleIndicator;
    private boolean tablet = false, weekend;
    private int weekNo, theme, weekDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(TimetableProvider.WEEK_URI, new String[]{
                        TimetableProvider.ID, TimetableProvider.NUM
                },
                TimetableProvider.KEY + "='theme'", null, null);
        cursor.moveToFirst();
        if (cursor.getInt(1) == 1) {
            setTheme(R.style.Light);
            theme = 1;
        } else if (cursor.getInt(1) == 2) {
            setTheme(R.style.Dark);
            theme = 2;
        }
        cursor = cr.query(TimetableProvider.WEEK_URI, new String[]{
                        TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM
                },
                TimetableProvider.KEY + "='weekNo'", null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            weekNo = 0;
        } else {
            weekNo = cursor.getInt(2);
        }
        cursor.close();
        String weekString;
        if (weekNo == 1) {
            weekString = ("A");
        } else {
            weekString = ("B");
        }
        Calendar cal = Calendar.getInstance();
        weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1 || weekDay == 7) {
            weekDay = 0;
            weekend = true;
        } else {
            weekDay -= 2;
        }
        String monWeek = "mon_" + weekNo;
        String tuesWeek = "tues_" + weekNo;
        String wedWeek = "wed_" + weekNo;
        String thursWeek = "thurs_" + weekNo;
        String friWeek = "fri_" + weekNo;
        int width;
        Display display = getWindowManager().getDefaultDisplay();
        if (CompatUtils.isNotLegacyApi13()) {
            Point metrics = new Point();
            display.getSize(metrics);
            width = metrics.x;
        } else {
            width = display.getWidth();
        }
        if (width >= dp(540)) {
            tablet = true;
        }

        Bundle mon = new Bundle(), tues = new Bundle(), wed = new Bundle(), thurs = new Bundle(), fri = new Bundle();
        mon.putString("day", monWeek);
        monday.setArguments(mon);
        tues.putString("day", tuesWeek);
        tuesday.setArguments(tues);
        wed.putString("day", wedWeek);
        wednesday.setArguments(wed);
        thurs.putString("day", thursWeek);
        thursday.setArguments(thurs);
        fri.putString("day", friWeek);
        friday.setArguments(fri);

        if (tablet) {
            setContentView(R.layout.timetable_tablet_fragment_layout);
            setupActionBar(weekString);
            getSupportFragmentManager().beginTransaction().add(R.id.monday_fragment_container, monday).commit();
            days[0] = monday;
            getSupportFragmentManager().beginTransaction().add(R.id.tuesday_fragment_container, tuesday).commit();
            days[1] = tuesday;
            getSupportFragmentManager().beginTransaction().add(R.id.wednesday_fragment_container, wednesday).commit();
            days[2] = wednesday;
            getSupportFragmentManager().beginTransaction().add(R.id.thursday_fragment_container, thursday).commit();
            days[3] = thursday;
            getSupportFragmentManager().beginTransaction().add(R.id.friday_fragment_container, friday).commit();
            days[4] = friday;
        } else {
            setContentView(R.layout.timetable_tabs_viewpager_layout);
            setupActionBar(weekString);
            LinearLayout container = (LinearLayout) findViewById(R.id.pager_container);
            View viewPager = findViewById(R.id.pager);
            initialiseViewPager();
            mViewPager.setPageMargin(dp(8));
            titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
            if (theme == 1) {
                viewPager.setBackgroundColor(0xFFE8E8E8);
                container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (CompatUtils.isNotLegacyApi11()) {
                    mViewPager.setPageMarginDrawable(R.color.colorPrimary);
                }
            } else if (theme == 2 && !CompatUtils.isNotLegacyApi11()) {
                viewPager.setBackgroundColor(0xFF000000);
                container.setBackgroundColor(0xFF000000);
            }
            if (extras != null) {
                titleIndicator.setViewPager(mViewPager, extras.getInt("tab"));
            } else {
                titleIndicator.setViewPager(mViewPager, weekDay);
            }
            titleIndicator.setSelectedBold(true);
            titleIndicator.setFooterColor(0xFF33B5E5);
            titleIndicator.setTextSize(dp(14));
        }
    }

    private void setupActionBar(String weekString) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Timetable - Week " + weekString);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TimetableActivity.this, TimetableAddSubjectActivity.class));
            }
        });
    }

    private void initialiseViewPager() {
        List<Fragment> fragments = new Vector<>();
        fragments.add(monday);
        fragments.add(tuesday);
        fragments.add(wednesday);
        fragments.add(thursday);
        fragments.add(friday);
        TimetablePagerAdapter mPagerAdapter = new TimetablePagerAdapter(super.getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager) super.findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timetable, menu);
        try {
            ContentResolver cr = getContentResolver();
            if (tablet || weekend) {
                menu.removeItem(R.id.menu_today);
            }
            Cursor e = cr.query(TimetableProvider.WEEK_URI, new String[]{
                            TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM
                    },
                    TimetableProvider.KEY + "='style'", null, null);
            e.moveToFirst();
            if (e.getInt(2) == 1) {
                menu.removeItem(R.id.menu_compact);
            }
        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case (R.id.menu_switch_week):
                ContentResolver cr = getContentResolver();
                if (weekNo == 1) {
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.NUM, 2);
                    cr.update(TimetableProvider.WEEK_URI, values, TimetableProvider.KEY + "='weekNo'", null);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.NUM, 1);
                    cr.update(TimetableProvider.WEEK_URI, values, TimetableProvider.KEY + "='weekNo'", null);
                }
                refresh();
                return true;
            case (R.id.menu_refresh):
                refresh();
                return true;
            case (R.id.menu_add_period):
                startActivity(new Intent(this, TimetableAddSubjectActivity.class));
                return true;
            case (R.id.menu_today):
                this.titleIndicator.setCurrentItem(weekDay);
                return true;
            case (R.id.menu_compact):
                DialogFragment compactDialog = new CompactDialogFragment();
                compactDialog.show(getSupportFragmentManager(), "compact");
                return true;
            case (R.id.menu_preferences):
                Intent nextSetup = new Intent(this, TimetableSettingsActivity.class);
                startActivity(nextSetup);
                return true;
            case (android.R.id.home):
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
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
                confirm.setText(getString(R.string.clear_data_confirm));
                confirm.setPadding(15, 15, 15, 15);
                builder.setTitle(getString(R.string.clear_data));
                builder.setView(confirm);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ContentResolver cr = getContentResolver();
                        cr.delete(TimetableProvider.PERIODS_URI, null, null);
                        cr.delete(TimetableProvider.WEEK_URI, null, null);
                        startActivity(new Intent(TimetableActivity.this, TimetableActivity.class));
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
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ProgressDialog progressDialog;
                        progressDialog = new ProgressDialog(TimetableActivity.this);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage("Restoring...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Intent backToHome = new Intent(TimetableActivity.this, TimetableActivity.class);
                        backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ContentResolver cr = getContentResolver();
                        cr.delete(TimetableProvider.PERIODS_URI, null, null);
                        cr.delete(TimetableProvider.WEEK_URI, null, null);
                        TimetableBackupRestore.restore(TimetableActivity.this);
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

    private void refresh() {
        if (tablet) {
            for (TimetableFragment day : days) {
                day.refreshView();
            }
        } else {
            Intent refresh = new Intent(TimetableActivity.this, TimetableActivity.class);
            refresh.putExtra("tab", mViewPager.getCurrentItem());
            startActivity(refresh);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    private int dp(int amount) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (amount * scale + 0.5f);
    }

    public class CompactDialogFragment extends DialogFragment {
        final CompactDialogFragment mContext = this;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TimetableActivity.this);
            builder.setTitle(getString(R.string.select_compact_detail));
            builder.setItems(R.array.secondary, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ContentResolver cr = getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.KEY, "comp");
                    switch (item) {
                        case 0:
                            values.put(TimetableProvider.NUM, 1);
                            break;
                        case 1:
                            values.put(TimetableProvider.NUM, 2);
                            break;
                        case 2:
                            values.put(TimetableProvider.NUM, 3);
                            break;
                    }
                    cr.update(TimetableProvider.WEEK_URI, values, TimetableProvider.KEY + "='comp'", null);
                    refresh();
                    (mContext).dismiss();
                }
            });
            return builder.create();
        }
    }
}