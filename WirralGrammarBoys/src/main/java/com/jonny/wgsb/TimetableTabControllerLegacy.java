package com.jonny.wgsb;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

@SuppressLint({"ValidFragment", "NewAPI"})
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class TimetableTabControllerLegacy extends ActionBarActivity implements ViewPager.OnPageChangeListener {
	private TimetableFragmentLegacy[] days = new TimetableFragmentLegacy[5];
	private TimetableFragmentLegacy monday = new TimetableFragmentLegacy();
	private TimetableFragmentLegacy tuesday = new TimetableFragmentLegacy();
	private TimetableFragmentLegacy wednesday = new TimetableFragmentLegacy();
	private TimetableFragmentLegacy thursday = new TimetableFragmentLegacy();
	private TimetableFragmentLegacy friday = new TimetableFragmentLegacy();
    private TitlePageIndicator titleIndicator;
    private final static int CONFIRM_DIALOG_ID = 0, RESTORE_DIALOG_ID = 1;
    private boolean tablet = false, weekend;
    private int weekNo,  theme, weekDay;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(TimetableProvider.WEEK_URI, new String[] {
			TimetableProvider.ID, TimetableProvider.NUM
		},
		TimetableProvider.KEY+"='theme'", null, null);
        cursor.moveToFirst();
		if (cursor.getInt(1) == 1) {
			setTheme(R.style.Light);
			theme = 1;
		} else if (cursor.getInt(1) == 2) {
			setTheme(R.style.Dark);
			theme = 2;
		}
        cursor = cr.query(TimetableProvider.WEEK_URI, new String[] {
			TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM
		},
		TimetableProvider.KEY+"='weekNo'", null, null);
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
        final ActionBar actionBar = getSupportActionBar();
        setupActionBar(actionBar, weekString);
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
		if (android.os.Build.VERSION.SDK_INT >= 13) {
	        Point metrics = new Point();
	        display.getSize(metrics);
	        width = metrics.x;
		} else {
			width = display.getWidth();
		}
		if (width >= dp(540)) {
			tablet = true;
		}
        Bundle mon = new Bundle();
        mon.putString("day", monWeek);
		monday.setArguments(mon);

		Bundle tues = new Bundle();
        tues.putString("day", tuesWeek);
		tuesday.setArguments(tues);

		Bundle wed = new Bundle();
		wed.putString("day", wedWeek);
		wednesday.setArguments(wed);

		Bundle thurs = new Bundle();
		thurs.putString("day", thursWeek);
		thursday.setArguments(thurs);

		Bundle fri = new Bundle();
		fri.putString("day", friWeek);
		friday.setArguments(fri);

		if (tablet) {
	    	setContentView(R.layout.timetable_tablet_fragment_layout);
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
			LinearLayout container = (LinearLayout)findViewById(R.id.pager_container);
			View viewPager = findViewById(R.id.pager);
			if (theme == 1) {
				viewPager.setBackgroundColor(0xFFE8E8E8);
			} else if (theme == 2 && android.os.Build.VERSION.SDK_INT < 11) {
				viewPager.setBackgroundColor(0xFF000000);
			}
            initialiseViewPager();
			mViewPager.setPageMargin(dp(8));
			titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
			if (extras != null) {
				titleIndicator.setViewPager(mViewPager, extras.getInt("tab"));
			} else {
				titleIndicator.setViewPager(mViewPager, weekDay);
			}
			titleIndicator.setSelectedBold(true);
			titleIndicator.setFooterColor(0xFF33B5E5);
			titleIndicator.setTextSize(dp(14));
			if (theme == 1) {
				container.setBackgroundColor(0xFF004890);
				if (android.os.Build.VERSION.SDK_INT >= 11) {
					mViewPager.setPageMarginDrawable(R.color.blue_logo);
				}
			} else if (theme == 2 && android.os.Build.VERSION.SDK_INT < 11) {
				container.setBackgroundColor(0xFF000000);
			}
	    }
	}

    private void setupActionBar(ActionBar actionBar, String weekString) {
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle("Timetable - Week " + weekString);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_wgsb));
        actionBar.setSplitBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bottom_solid_wgsb));
    }

    protected void onSaveInstanceState(Bundle outState) {
        try {
        	super.onSaveInstanceState(outState);
        } catch (NullPointerException e) {
        	e.printStackTrace();
		}
    }

    private void initialiseViewPager() {
		List<Fragment> fragments = new Vector<Fragment>();
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
            Cursor e = cr.query(TimetableProvider.WEEK_URI, new String[] {
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
	    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case(R.id.menu_switch_week):
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
			case(R.id.menu_refresh):
				refresh();				
				return true;
			case(R.id.menu_add_period):
	            startActivity(new Intent(this, TimetableAddSubjectActivityLegacy.class));
	            return true;
			case(R.id.menu_today):
	            this.titleIndicator.setCurrentItem(weekDay);
	            return true;
			case(R.id.menu_compact):
	            DialogFragment compactDialog = new CompactDialogFragment();
	            compactDialog.show(getSupportFragmentManager(), "compact");
	            return true;
			case(R.id.menu_preferences):
				Intent nextSetup = new Intent(this, TimetableSettingsActivityLegacy.class);
	            startActivity(nextSetup);
	            return true;
			case(android.R.id.home):
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

    public class CompactDialogFragment extends DialogFragment {
        CompactDialogFragment mContext = this;

		@NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(TimetableTabControllerLegacy.this);
			builder.setTitle(getString(R.string.compact_detail));
			builder.setItems(R.array.secondary, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	ContentResolver cr = getContentResolver();
					ContentValues values = new ContentValues();
					values.put(TimetableProvider.KEY, "comp");
					switch(item) {
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
                        startActivity(new Intent(TimetableTabControllerLegacy.this, TimetableTabControllerLegacy.class));
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
                        progressDialog = new ProgressDialog(TimetableTabControllerLegacy.this);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage("Restoring...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Intent backToHome = new Intent(TimetableTabControllerLegacy.this, TimetableTabControllerLegacy.class);
                        backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ContentResolver cr = getContentResolver();
                        cr.delete(TimetableProvider.PERIODS_URI, null, null);
                        cr.delete(TimetableProvider.WEEK_URI, null, null);
                        TimetableBackupRestore.restore(TimetableTabControllerLegacy.this);
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
            for (TimetableFragmentLegacy day : days) {
                day.refreshView();
            }
		} else {
			Intent refresh = new Intent(TimetableTabControllerLegacy.this, TimetableTabControllerLegacy.class);
			refresh.putExtra("tab", mViewPager.getCurrentItem());
			startActivity(refresh);
			overridePendingTransition(0, 0);
			finish();
		}
	}
	
	private int dp (int amount) {
		final float scale = getResources().getDisplayMetrics().density;
        return (int)(amount*scale+0.5f);
	}
}