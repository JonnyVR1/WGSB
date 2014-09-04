package com.jonny.wgsb;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class TimetableAddSubjectActivity extends ActionBarActivity {
	List<ArrayAdapter<String>> startAdapterList = new ArrayList<ArrayAdapter<String>>();
	List<ArrayAdapter<String>> endAdapterList = new ArrayList<ArrayAdapter<String>>();
	List<Integer> startMinuteList = new ArrayList<Integer>();
	List<Integer> startHourList = new ArrayList<Integer>();
	List<Integer> endMinuteList = new ArrayList<Integer>();
	List<EditText> roomList = new ArrayList<EditText>();
	List<EditText> teacherList = new ArrayList<EditText>();
	List<Integer> endHourList = new ArrayList<Integer>();
	List<EditText> idList = new ArrayList<EditText>();
	List<Spinner> weekList = new ArrayList<Spinner>();
	List<Integer> periods = new ArrayList<Integer>();
	List<String> dayList = new ArrayList<String>();
	private static final int START_DIALOG_ID = 0, END_DIALOG_ID = 1;
	int lastStartClicked, lastEndClicked, periodId = 0, mMinute, theme, mHour;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		ContentResolver cr = getContentResolver();
		Cursor t = cr.query(TimetableProvider.WEEK_URI, new String[] {
				TimetableProvider.ID, TimetableProvider.NUM }, TimetableProvider.KEY + "='theme'", null, null);
		t.moveToFirst();
		if (t.getInt(1) == 1) {
			setTheme(R.style.Light);
			theme = 1;
		} else if (t.getInt(1) == 2) {
			setTheme(R.style.Dark);
			theme = 2;
		}
		setContentView(R.layout.timetable_add_subject_layout);
		RelativeLayout main = (RelativeLayout) findViewById(R.id.add_subject_layout_container);

		if (theme == 1 && android.os.Build.VERSION.SDK_INT < 11) {
			main.setBackgroundColor(0xFFFFFFFF);
		} else if (theme == 2 && android.os.Build.VERSION.SDK_INT < 11) {
			main.setBackgroundColor(0xFF000000);
		}

		final LinearLayout mondayLayout = (LinearLayout) findViewById(R.id.monday_layout);
		final LinearLayout tuesdayLayout = (LinearLayout) findViewById(R.id.tuesday_layout);
		final LinearLayout wednesdayLayout = (LinearLayout) findViewById(R.id.wednesday_layout);
		final LinearLayout thursdayLayout = (LinearLayout) findViewById(R.id.thursday_layout);
		final LinearLayout fridayLayout = (LinearLayout) findViewById(R.id.friday_layout);

		final AutoCompleteTextView nameEdit = (AutoCompleteTextView) findViewById(R.id.name_autocomplete);
		if (t.getInt(1) == 1 && android.os.Build.VERSION.SDK_INT < 11) {
			nameEdit.setTextColor(0xFF000000);
		}
		if (t.getInt(1) == 2) {
			findViewById(R.id.bottom_bar).setBackgroundColor(0x00000000);
		}
		t.close();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.timetable_list_item, SUBJECTS);
		nameEdit.setAdapter(adapter);
		final CheckBox breakCheck = (CheckBox) findViewById(R.id.break_check);
		ImageView mondayAdd = (ImageView) findViewById(R.id.monday_add);
		mondayAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mondayLayout.addView(periodLayout("mon_"));
				periodId++;
			}
		});
		ImageView tuesdayAdd = (ImageView) findViewById(R.id.tuesday_add);
		tuesdayAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tuesdayLayout.addView(periodLayout("tues_"));
				periodId++;
			}
		});
		ImageView wednesdayAdd = (ImageView) findViewById(R.id.wednesday_add);
		wednesdayAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wednesdayLayout.addView(periodLayout("wed_"));
				periodId++;
			}
		});
		ImageView thursdayAdd = (ImageView) findViewById(R.id.thursday_add);
		thursdayAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thursdayLayout.addView(periodLayout("thurs_"));
				periodId++;
			}
		});
		ImageView fridayAdd = (ImageView) findViewById(R.id.friday_add);
		fridayAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fridayLayout.addView(periodLayout("fri_"));
				periodId++;
			}
		});
		Button done = (Button) findViewById(R.id.done_button);
		done.setBackgroundResource(R.drawable.layout_selector);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContentValues values = new ContentValues();
				String name = nameEdit.getText().toString();
				values.put(TimetableProvider.ACTIVITY, name);
				boolean isBreak = breakCheck.isChecked();
				values.put(TimetableProvider.BREAK, isBreak);
				for (int i = 0; i < periods.size(); i++) {
					if (periods.get(i) != null) {
						String id = idList.get(i).getText().toString();
						values.put(TimetableProvider.NAME, id);
						String weekInt = "" + (weekList.get(i).getSelectedItemPosition() + 1);
						String room = roomList.get(i).getText().toString();
						values.put(TimetableProvider.ROOM, room);
						String teacher = teacherList.get(i).getText().toString();
						values.put(TimetableProvider.TEACHER, teacher);
						String day = dayList.get(i) + weekInt;
						values.put(TimetableProvider.DAY, day);
						int start = startHourList.get(i) * 60 + startMinuteList.get(i);
						values.put(TimetableProvider.START, start);
						int end = endHourList.get(i) * 60 + endMinuteList.get(i);
						values.put(TimetableProvider.END, end);

						ContentResolver cr = getContentResolver();
						cr.insert(TimetableProvider.PERIODS_URI, values);
					}
				}
				String refresh = "To see changes, press refresh";
				if (android.os.Build.VERSION.SDK_INT < 11) {
					refresh = "Press Menu > Refresh to see changes";
				}
				Toast.makeText(TimetableAddSubjectActivity.this, refresh, Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		Button cancel = (Button) findViewById(R.id.cancel_button);
		cancel.setBackgroundResource(R.drawable.layout_selector);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void setupActionBar() {
		final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    	actionBar.setIcon(R.drawable.banner);
    	actionBar.setTitle("Add Subject");
    	actionBar.setDisplayHomeAsUpEnabled(true);
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Color.parseColor("#FF004890"));
        }
	}
	
	@TargetApi(19) 
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home){
			Intent intent = new Intent(this, TimetableTabController.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private RelativeLayout periodLayout(String day) {
		final int currentPeriodId = periodId;
        dayList.add(day);
		periods.add(currentPeriodId);
		RelativeLayout periodLayout = new RelativeLayout(this);
		periodLayout.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout rows = new LinearLayout(this);
		rows.setOrientation(LinearLayout.VERTICAL);
		LinearLayout topRow = new LinearLayout(this);
		topRow.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout bottomRow = new LinearLayout(this);
		bottomRow.setGravity(Gravity.CENTER_VERTICAL);
		final EditText idTextBox = new EditText(this);
		idTextBox.setTag(currentPeriodId);
		idTextBox.setEms(2);
		idTextBox.setHint("ID");
		idList.add(idTextBox);
		topRow.addView(idTextBox);
		final EditText teacherTextBox = new EditText(this);
		teacherTextBox.setTag(currentPeriodId);
        teacherTextBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		teacherTextBox.setHint("Teacher");
		teacherList.add(teacherTextBox);
		topRow.addView(teacherTextBox);
		final EditText roomTextBox = new EditText(this);
		roomTextBox.setTag(currentPeriodId);
		roomTextBox.setHint("Room");
        roomTextBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		roomList.add(roomTextBox);
		topRow.addView(roomTextBox);
		final Spinner setWeek = new Spinner(this);
		final ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, R.layout.timetable_spinner_text, R.id.spinner_text);
		spinAdapter.add("Week A");
		spinAdapter.add("Week B");
		setWeek.setAdapter(spinAdapter);
		weekList.add(setWeek);
		topRow.addView(setWeek);
		final RelativeLayout startRelative = new RelativeLayout(this);
		startRelative.setPadding(dp(8), 0, dp(8), 0);
		final Spinner startSpinner = new Spinner(this);
		final ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(this, R.layout.timetable_spinner_text, R.id.spinner_text);
		startAdapter.add("09:00");
		startAdapterList.add(startAdapter);
		startHourList.add(0);
		startMinuteList.add(0);
		startSpinner.setAdapter(startAdapter);
		startSpinner.setTag(currentPeriodId);
		startSpinner.setClickable(false);
		startRelative.addView(startSpinner);
		final View startSet = new View(this);
		startSet.setClickable(true);
		startSet.setBackgroundResource(R.drawable.layout_selector);
		startSet.setLayoutParams(new LayoutParams(dp(64), dp(48)));
		startSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(START_DIALOG_ID);
				lastStartClicked = currentPeriodId;
			}
		});
		startRelative.addView(startSet);
		bottomRow.addView(startRelative);
		TextView dash = new TextView(this);
		dash.setText(" - ");
		dash.setGravity(Gravity.CENTER_VERTICAL);
		bottomRow.addView(dash);
		final RelativeLayout endRelative = new RelativeLayout(this);
		endRelative.setPadding(dp(8), 0, dp(8), 0);
		final Spinner endSpinner = new Spinner(this);
		final ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(this,  R.layout.timetable_spinner_text, R.id.spinner_text);
		endAdapter.add("10:00");
		endAdapterList.add(endAdapter);
		endHourList.add(0);
		endMinuteList.add(0);
		endSpinner.setAdapter(endAdapter);
		endSpinner.setTag(currentPeriodId);
		endSpinner.setClickable(false);
		endRelative.addView(endSpinner);
		final View endSet = new View(this);
		endSet.setClickable(true);
		endSet.setBackgroundResource(R.drawable.layout_selector);
		endSet.setLayoutParams(new LayoutParams(dp(64), dp(48)));
		endSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(END_DIALOG_ID);
				lastEndClicked = currentPeriodId;
			}
		});
		endRelative.addView(endSet);
		bottomRow.addView(endRelative);
		rows.addView(topRow);
		rows.addView(bottomRow);
		periodLayout.addView(rows);
		ImageView remove = new ImageView(this);
		remove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
		remove.setClickable(true);
		remove.setBackgroundResource(R.drawable.layout_selector);
		remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((LinearLayout) v.getParent().getParent()).removeView((View) v.getParent());
				idList.set(currentPeriodId, null);
				dayList.set(currentPeriodId, null);
				weekList.set(currentPeriodId, null);
				teacherList.set(currentPeriodId, null);
				roomList.set(currentPeriodId, null);
				startHourList.set(currentPeriodId, null);
				startMinuteList.set(currentPeriodId, null);
				startAdapterList.set(currentPeriodId, null);
				endHourList.set(currentPeriodId, null);
				endMinuteList.set(currentPeriodId, null);
				endAdapterList.set(currentPeriodId, null);
				periods.set(currentPeriodId, null);
			}
		});
		RelativeLayout.LayoutParams removeRules = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		removeRules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		removeRules.addRule(RelativeLayout.CENTER_VERTICAL);
		periodLayout.addView(remove, removeRules);
		return periodLayout;
	}

	private TimePickerDialog.OnTimeSetListener mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateStartTime(hourOfDay, minute, lastStartClicked);
		}
	};

	private TimePickerDialog.OnTimeSetListener mEndTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateEndTime(hourOfDay, minute, lastEndClicked);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case START_DIALOG_ID:
				if (mHour == 0) {
					mHour = 9;
				}
				return new TimePickerDialog(this, mStartTimeSetListener, mHour, mMinute, true);
			case END_DIALOG_ID:
				if (mHour == 0) {
					mHour = 10;
				}
				return new TimePickerDialog(this, mEndTimeSetListener, mHour, mMinute, true);
		}
		return null;
	}

	private void updateStartTime(int hour, int minute, int period) {
		startHourList.set(period, hour);
		startMinuteList.set(period, minute);
		int time = hour * 60 + minute;
		String timeString = TimetablePeriod.timeString(time);
		startAdapterList.get(period).clear();
		startAdapterList.get(period).add(timeString);
	}

	private void updateEndTime(int hour, int minute, int period) {
		endHourList.set(period, hour);
		endMinuteList.set(period, minute);
		int time = hour * 60 + minute;
		String timeString = TimetablePeriod.timeString(time);
		endAdapterList.get(period).clear();
		endAdapterList.get(period).add(timeString);
	}

	private int dp(int amount) {
		final float scale = getResources().getDisplayMetrics().density;
        return (int) (amount * scale + 0.5f);
	}

    static final String[] SUBJECTS = new String[] {
        "Registration", "Lunch", "Break", "Free", "Art", "Astronomy", "Biology",
        "Business Studies", "Chemistry", "Computing", "Design & Technology",
        "Drama", "Economics", "English", "English Language", "English Literature",
        "EPQ", "Financial Studies", "Food Technology", "French", "Games", "General Studies",
        "Geography", "Geology", "History", "ICT", "Law", "Maths", "Maths - Applied",
        "Maths - Core", "Maths - Decision", "Maths - Statistics", "Maths - Mechanics",
        "Media Studies", "Music", "Psychology", "PHSE", "Physics", "Physical Education",
        "Politics", "Science", "Sociology", "Spanish", "Religious Studies"
    };
}