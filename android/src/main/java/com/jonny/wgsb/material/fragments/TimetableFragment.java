package com.jonny.wgsb.material.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.db.TimetableProvider;
import com.jonny.wgsb.material.ui.helper.TimetablePeriod;
import com.jonny.wgsb.material.util.CompatUtils;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
@SuppressLint({"NewApi", "ValidFragment"})
public class TimetableFragment extends Fragment {
    static final String[] SUBJECTS = new String[]{
            "Registration", "Lunch", "Break", "Free", "Art", "Astronomy", "Biology",
            "Business Studies", "Chemistry", "Computing", "Design & Technology",
            "Drama", "Economics", "English", "English Language", "English Literature",
            "EPQ", "Financial Studies", "Food Technology", "French", "Games", "General Studies",
            "Geography", "Geology", "History", "ICT", "Law", "Maths", "Maths - Applied",
            "Maths - Core", "Maths - Decision", "Maths - Statistics", "Maths - Mechanics",
            "Media Studies", "Music", "Psychology", "PHSE", "Physics", "Physical Education",
            "Politics", "Science", "Sociology", "Spanish", "Religious Studies"
    };
    private boolean isCurrent, stopTimer, tablet;
    private int titleColour = 0xFF555555, mMinute, dayInt, mHour;
    private String day;
    private EditDialogFragment editor;
    private TimePickerDialog.OnTimeSetListener mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            editor.updateStartTime(hourOfDay, minute);
        }
    };
    private TimePickerDialog.OnTimeSetListener mEndTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            editor.updateEndTime(hourOfDay, minute);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateData();
        stopTimer = false;
        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            public void run() {
                if (!stopTimer) {
                    updateData();
                }
            }
        };
        timer.scheduleAtFixedRate(tt, 0, 300000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout all = new LinearLayout(getActivity());
        all.setOrientation(LinearLayout.VERTICAL);
        all.setGravity(Gravity.CENTER_HORIZONTAL);
        buildTimetable(all);
        all.setTag(day);
        return all;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        stopTimer = false;
        updateData();
        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            public void run() {
                if (!stopTimer) {
                    updateData();
                }
            }
        };
        timer.scheduleAtFixedRate(tt, 0, 300000);
    }

    public void refreshView() {
        LinearLayout currentView;
        View v = getView();
        assert v != null;
        currentView = (LinearLayout) v.findViewWithTag(day);
        currentView.removeAllViews();
        updateData();
        buildTimetable(currentView);
    }

    private void updateData() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor t = cr.query(TimetableProvider.WEEK_URI, new String[]{TimetableProvider.ID, TimetableProvider.NUM},
                TimetableProvider.KEY + "='theme'", null, null);
        t.moveToFirst();
        if (t.getInt(1) == 1) {
            titleColour = 0xFF555555;
            getActivity().setTheme(R.style.Light);
        } else if (t.getInt(1) == 2) {
            titleColour = 0x00FFFFFF;
            getActivity().setTheme(R.style.Dark);
        }
        t.close();
        Bundle args = getArguments();
        if (args == null) {
            day = "mon_1";
        } else {
            day = args.getString("day");
        }
        dayInt = 0;
        if (day.contains("tues")) {
            dayInt = 1;
        } else if (day.contains("wed")) {
            dayInt = 2;
        } else if (day.contains("thurs")) {
            dayInt = 3;
        } else if (day.contains("fri")) {
            dayInt = 4;
        }
        isCurrent = dayInt == weekDay();
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7) {
            isCurrent = false;
        }
        int width;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
    }

    private void buildTimetable(LinearLayout all) {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(TimetableProvider.PERIODS_URI,
                new String[]{TimetableProvider.ID, TimetableProvider.NAME,
                        TimetableProvider.ACTIVITY, TimetableProvider.TEACHER, TimetableProvider.ROOM, TimetableProvider.START,
                        TimetableProvider.END, TimetableProvider.BREAK},
                "day='" + day + "'", null, TimetableProvider.START);
        c.moveToFirst();
        LinearLayout main = new LinearLayout(getActivity());
        main.setOrientation(LinearLayout.VERTICAL);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        int size;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        if (CompatUtils.isNotLegacyApi13()) {
            Point metrics = new Point();
            display.getSize(metrics);
            size = metrics.x;
        } else {
            size = display.getWidth();
        }
        if (tablet) {
            LinearLayout title = new LinearLayout(getActivity());
            title.setOrientation(LinearLayout.VERTICAL);
            title.setGravity(Gravity.CENTER_HORIZONTAL);
            title.setBackgroundColor(titleColour);
            LayoutParams breakParams = new LayoutParams(dp(110), dp(4));
            View topBreak = new View(getActivity());
            topBreak.setLayoutParams(breakParams);
            title.addView(topBreak);
            TextView titleText = new TextView(getActivity());
            switch (dayInt) {
                case 0:
                    titleText.setText("Monday");
                    break;
                case 1:
                    titleText.setText("Tuesday");
                    break;
                case 2:
                    titleText.setText("Wednesday");
                    break;
                case 3:
                    titleText.setText("Thursday");
                    break;
                case 4:
                    titleText.setText("Friday");
                    break;
                default:
                    titleText.setText("BrokenDay");
            }
            titleText.setGravity(Gravity.CENTER_HORIZONTAL);
            titleText.setTextSize(14);
            if (isCurrent) {
                titleText.setTypeface(null, Typeface.BOLD);
                titleText.setTextColor(0xFFFFFFFF);
            } else {
                titleText.setTextColor(0xFF909090);
            }
            titleText.setPadding(0, dp(2), 0, dp(2));
            title.addView(titleText);
            View bottomBreak = new View(getActivity());
            if (isCurrent) {
                bottomBreak.setBackgroundColor(0xFF33B5E5);
            }
            bottomBreak.setLayoutParams(breakParams);
            title.addView(bottomBreak);
            View line = new View(getActivity());
            line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp(2)));
            line.setBackgroundColor(0xFF33B5E5);
            title.addView(line);
            all.addView(title);
        }
        if (size > dp(540)) {
            main.setPadding(dp(8), 0, dp(8), 0);
        } else {
            main.setPadding(dp(16), 0, dp(16), 0);
        }

        ScrollView sv = new ScrollView(getActivity());
        sv.addView(main);
        all.addView(sv);
        for (int i = 0; i < c.getCount(); i++) {
            TimetablePeriod currentPeriod = new TimetablePeriod(c.getInt(0), day);
            currentPeriod.setId(c.getString(1));
            if (c.getString(1).equals("")) currentPeriod.setId(" ");
            currentPeriod.setName(c.getString(2));
            currentPeriod.setTeacher(c.getString(3));
            currentPeriod.setRoom(c.getString(4));
            currentPeriod.setTime(c.getInt(5), c.getInt(6));
            currentPeriod.setBreak(c.getInt(7) == 1);
            LinearLayout item = null;
            Cursor r = cr.query(TimetableProvider.WEEK_URI, new String[]{TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM},
                    TimetableProvider.KEY + "='style'", null, null);
            r.moveToFirst();
            if (r.getInt(2) == 1) {
                item = expandedItem(currentPeriod);
            } else if (r.getInt(2) == 2) {
                item = compactItem(currentPeriod);
            }
            if (item != null) {
                main.addView(item);
            }
            View horLine = new View(getActivity());
            horLine.setBackgroundColor(0xFF909090);
            horLine.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.line_thickness)));
            main.addView(horLine);
            c.moveToNext();
        }
        c.close();
    }

    private LinearLayout expandedItem(TimetablePeriod currentPeriod) {
        Calendar cal = Calendar.getInstance();
        int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        LinearLayout item = new LinearLayout(getActivity());
        item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp(48)));
        item.setPadding(0, dp(4), 0, dp(4));
        TextView idView = new TextView(getActivity());
        idView.setText(currentPeriod.id);
        idView.setMaxLines(1);
        idView.setGravity(Gravity.CENTER);
        idView.setLayoutParams(new LayoutParams((int) getResources().getDimension(R.dimen.id_width), LayoutParams.MATCH_PARENT));
        idView.setPadding(dp(4), 0, dp(4), 0);
        if (currentTime >= currentPeriod.start && isCurrent) {
            if (currentTime < currentPeriod.end) {
                idView.setBackgroundColor(0x9033B5E5);
            } else {
                idView.setBackgroundColor(0x90909090);
            }
        }
        item.addView(idView);
        View verLine = new View(getActivity());
        verLine.setBackgroundColor(0xFF909090);
        verLine.setLayoutParams(new LayoutParams(getResources().getDimensionPixelOffset(R.dimen.line_thickness), LayoutParams.MATCH_PARENT));
        item.addView(verLine);
        RelativeLayout rel = new RelativeLayout(getActivity());
        TextView nameView = new TextView(getActivity());
        nameView.setText(currentPeriod.name);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setId(R.id.timetable_name_view);
        RelativeLayout.LayoutParams nameLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        nameLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        nameLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        nameView.setPadding(dp(4), 0, dp(4), 0);
        rel.addView(nameView);

        TextView roomView = new TextView(getActivity());
        roomView.setText(currentPeriod.room);
        roomView.setId(R.id.timetable_room_view);
        RelativeLayout.LayoutParams roomLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        roomLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        roomLp.addRule(RelativeLayout.ALIGN_BOTTOM, nameView.getId());
        roomView.setPadding(dp(4), 0, dp(4), 0);
        rel.addView(roomView, roomLp);

        TextView timeView = new TextView(getActivity());
        timeView.setText(currentPeriod.startString + " - " + currentPeriod.endString);
        timeView.setId(R.id.timetable_time_view);
        RelativeLayout.LayoutParams timeLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        timeLp.addRule(RelativeLayout.BELOW, roomView.getId());
        timeView.setPadding(dp(4), 0, dp(4), 0);
        rel.addView(timeView, timeLp);

        TextView teacherView = new TextView(getActivity());
        teacherView.setText(currentPeriod.teacher);
        teacherView.setId(R.id.timetable_teacher_view);
        RelativeLayout.LayoutParams teacherLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        teacherLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        teacherLp.addRule(RelativeLayout.BELOW, nameView.getId());
        teacherView.setPadding(dp(4), 0, dp(4), 0);
        rel.addView(teacherView, teacherLp);

        if (currentPeriod.isBreak) rel.setBackgroundColor(0x90909090);

        View clickable = new View(getActivity());
        clickable.setTag(currentPeriod);
        clickable.setClickable(true);
        clickable.setBackgroundResource(R.drawable.layout_selector);
        clickable.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TimetablePeriod data = ((TimetablePeriod) (v.getTag()));
                Bundle args = new Bundle();
                args.putInt("_id", data._id);
                args.putString("id", data.id);
                args.putString("name", data.name);
                args.putString("teacher", data.teacher);
                args.putString("room", data.room);
                args.putString("start", data.startString);
                args.putString("end", data.endString);
                args.putBoolean("break", data.isBreak);
                DialogFragment optionsFragment = new OptionsDialogFragment();
                optionsFragment.setArguments(args);
                optionsFragment.show(getFragmentManager(), "options");
                return false;
            }
        });
        rel.addView(clickable);
        item.addView(rel);
        return item;
    }

    private LinearLayout compactItem(TimetablePeriod currentPeriod) {
        Calendar cal = Calendar.getInstance();
        int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        LinearLayout item = new LinearLayout(getActivity());
        item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        item.setPadding(0, dp(4), 0, dp(4));
        TextView idView = new TextView(getActivity());
        idView.setText(currentPeriod.id);
        idView.setMaxLines(1);
        idView.setGravity(Gravity.CENTER);
        idView.setLayoutParams(new LayoutParams((int) getResources().getDimension(R.dimen.id_width), LayoutParams.MATCH_PARENT));
        idView.setPadding(dp(4), 0, dp(4), 0);
        if (currentTime >= currentPeriod.start && isCurrent) {
            if (currentTime < currentPeriod.end) {
                idView.setBackgroundColor(0x9033B5E5);
            } else {
                idView.setBackgroundColor(0x90909090);
            }
        }
        item.addView(idView);
        View verLine = new View(getActivity());
        verLine.setBackgroundColor(0xFF909090);
        verLine.setLayoutParams(new LayoutParams(getResources().getDimensionPixelOffset(R.dimen.line_thickness), LayoutParams.MATCH_PARENT));
        item.addView(verLine);
        RelativeLayout rel = new RelativeLayout(getActivity());
        rel.setPadding(dp(4), dp(4), dp(4), dp(4));
        TextView nameView = new TextView(getActivity());
        nameView.setText(currentPeriod.name);
        nameView.setTypeface(null, Typeface.BOLD);
        rel.addView(nameView);
        TextView secondaryView = new TextView(getActivity());
        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
        Cursor c = cr.query(TimetableProvider.WEEK_URI, new String[]{TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM},
                TimetableProvider.KEY + "='comp'", null, null);
        c.moveToFirst();
        if (c.getInt(2) == 1) {
            secondaryView.setText(currentPeriod.room);
        } else if (c.getInt(2) == 2) {
            secondaryView.setText(currentPeriod.startString + " - " + currentPeriod.endString);
        } else if (c.getInt(2) == 3) {
            secondaryView.setText(currentPeriod.teacher + " - " + currentPeriod.room);
        }
        c.close();
        RelativeLayout.LayoutParams roomLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        roomLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        roomLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rel.addView(secondaryView, roomLp);
        if (currentPeriod.isBreak) {
            rel.setBackgroundColor(0x90909090);
        }
        item.addView(rel);
        return item;
    }

    private int weekDay() {
        Calendar cal = Calendar.getInstance();
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1 || weekDay == 7) {
            weekDay = 0;
        } else {
            weekDay -= 2;
        }
        return weekDay;
    }

    private int dp(int amount) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (amount * scale + 0.5f);
    }

    public class EditDialogFragment extends DialogFragment {
        EditDialogFragment mContext = this;
        ArrayAdapter<String> startAdapter;
        ArrayAdapter<String> endAdapter;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            final int _id = args.getInt("_id");
            String formerId = args.getString("id");
            String formerName = args.getString("name");
            String formerTeacher = args.getString("teacher");
            String formerRoom = args.getString("room");
            String formerStart = args.getString("start");
            String formerEnd = args.getString("end");
            boolean formerBreak = args.getBoolean("break");

            AlertDialog.Builder builder = new AlertDialog.Builder(TimetableFragment.this.getActivity());

            LinearLayout editView = new LinearLayout(TimetableFragment.this.getActivity());
            editView.setPadding(dp(16), dp(8), dp(16), dp(8));
            editView.setOrientation(LinearLayout.VERTICAL);

            LinearLayout idRoom = new LinearLayout(TimetableFragment.this.getActivity());
            LinearLayout idTeacher = new LinearLayout(TimetableFragment.this.getActivity());
            final EditText id = new EditText(TimetableFragment.this.getActivity());
            id.setHint("ID");
            id.setMaxEms(2);
            id.setMaxLines(1);
            id.setText(formerId);
            idRoom.addView(id);
            final EditText teacher = new EditText(TimetableFragment.this.getActivity());
            teacher.setHint("Teacher");
            teacher.setEms(6);
            teacher.setMaxLines(1);
            teacher.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            teacher.setText(formerTeacher);
            idRoom.addView(teacher);
            editView.addView(idTeacher);
            final EditText room = new EditText(TimetableFragment.this.getActivity());
            room.setHint("Room");
            room.setEms(6);
            room.setMaxLines(1);
            room.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            room.setText(formerRoom);
            idRoom.addView(room);
            editView.addView(idRoom);
            final AutoCompleteTextView name = new AutoCompleteTextView(TimetableFragment.this.getActivity());
            name.setHint("Subject Name");
            name.setEms(10);
            name.setMaxLines(1);
            name.setText(formerName);
            name.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(TimetableFragment.this.getActivity(), R.layout.timetable_list_item, SUBJECTS);
            name.setAdapter(adapter);
            editView.addView(name);
            RelativeLayout timeBreak = new RelativeLayout(TimetableFragment.this.getActivity());
            LinearLayout timesLayout = new LinearLayout(TimetableFragment.this.getActivity());
            timesLayout.setOrientation(LinearLayout.HORIZONTAL);
            timesLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            RelativeLayout startRelative = new RelativeLayout(TimetableFragment.this.getActivity());
            startRelative.setPadding(dp(8), 0, dp(8), 0);
            Spinner startSpinner = new Spinner(TimetableFragment.this.getActivity());
            startAdapter = new ArrayAdapter<>(TimetableFragment.this.getActivity(), R.layout.timetable_spinner_text, R.id.spinner_text);
            startAdapter.add("09:00");
            if (formerStart != null) {
                startAdapter.clear();
                startAdapter.add(formerStart);
                mHour = Integer.parseInt(formerStart.split(":")[0]);
                mMinute = Integer.parseInt(formerStart.split(":")[1]);
            } else {
                mHour = 9;
                mMinute = 0;
            }
            startSpinner.setAdapter(startAdapter);
            startSpinner.setClickable(false);
            startRelative.addView(startSpinner);

            View startSet = new View(TimetableFragment.this.getActivity());
            startSet.setClickable(true);
            startSet.setBackgroundResource(R.drawable.layout_selector);
            startSet.setLayoutParams(new LayoutParams(dp(64), dp(48)));
            startSet.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putBoolean("start", true);
                    DialogFragment startSet = new TimeSetDialog();
                    startSet.setArguments(args);
                    startSet.show(getFragmentManager(), "startSet");
                }
            });
            startRelative.addView(startSet);
            timesLayout.addView(startRelative);

            TextView dash = new TextView(TimetableFragment.this.getActivity());
            dash.setText(" - ");
            timesLayout.addView(dash);
            RelativeLayout endRelative = new RelativeLayout(TimetableFragment.this.getActivity());
            startRelative.setPadding(dp(8), 0, dp(8), 0);
            Spinner endSpinner = new Spinner(TimetableFragment.this.getActivity());
            endAdapter = new ArrayAdapter<>(TimetableFragment.this.getActivity(), R.layout.timetable_spinner_text, R.id.spinner_text);
            endAdapter.add("10:00");
            if (formerEnd != null) {
                endAdapter.clear();
                endAdapter.add(formerEnd);
            }
            endSpinner.setAdapter(endAdapter);
            endSpinner.setClickable(false);
            endRelative.addView(endSpinner);

            View endSet = new View(TimetableFragment.this.getActivity());
            endSet.setClickable(true);
            endSet.setBackgroundResource(R.drawable.layout_selector);
            endSet.setLayoutParams(new LayoutParams(dp(64), dp(48)));
            endSet.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putBoolean("start", false);
                    DialogFragment endSet = new TimeSetDialog();
                    endSet.setArguments(args);
                    endSet.show(getFragmentManager(), "endSet");
                }
            });
            endRelative.addView(endSet);
            timesLayout.addView(endRelative);

            RelativeLayout.LayoutParams timeRules = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            timeRules.addRule(RelativeLayout.CENTER_VERTICAL);
            timeRules.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            timeBreak.addView(timesLayout, timeRules);

            final CheckBox breakCheck = new CheckBox(TimetableFragment.this.getActivity());
            breakCheck.setText("Break");
            breakCheck.setChecked(formerBreak);
            RelativeLayout.LayoutParams breakRules = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            breakRules.addRule(RelativeLayout.CENTER_VERTICAL);
            breakRules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            timeBreak.addView(breakCheck, breakRules);
            editView.addView(timeBreak);
            builder.setView(editView);
            builder.setTitle("Edit Period");
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    int startInt = Integer.parseInt(startAdapter.getItem(0).split(":")[0]) * 60 +
                            Integer.parseInt(startAdapter.getItem(0).split(":")[1]);
                    int endInt = Integer.parseInt(endAdapter.getItem(0).split(":")[0]) * 60 +
                            Integer.parseInt(endAdapter.getItem(0).split(":")[1]);
                    ContentResolver cr = TimetableFragment.this.getActivity().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(TimetableProvider.NAME, id.getText().toString());
                    values.put(TimetableProvider.ACTIVITY, name.getText().toString());
                    values.put(TimetableProvider.TEACHER, teacher.getText().toString());
                    values.put(TimetableProvider.ROOM, room.getText().toString());
                    values.put(TimetableProvider.START, startInt);
                    values.put(TimetableProvider.END, endInt);
                    values.put(TimetableProvider.BREAK, breakCheck.isChecked());
                    cr.update(TimetableProvider.PERIODS_URI, values, TimetableProvider.ID + "=" + _id, null);
                    refreshView();
                    mContext.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mContext.dismiss();
                }
            });
            return builder.create();
        }

        private void updateStartTime(int hour, int minute) {
            startAdapter.clear();
            startAdapter.add(TimetablePeriod.timeString((hour * 60) + minute));
        }

        private void updateEndTime(int hour, int minute) {
            endAdapter.clear();
            endAdapter.add(TimetablePeriod.timeString((hour * 60) + minute));
        }
    }

    public class RemoveDialogFragment extends DialogFragment {
        RemoveDialogFragment mContext = this;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            final int _id = args.getInt("_id");
            String name = args.getString("name");
            AlertDialog.Builder builder = new AlertDialog.Builder(TimetableFragment.this.getActivity());
            TextView confirmRemove = new TextView(TimetableFragment.this.getActivity());
            confirmRemove.setPadding(dp(16), dp(8), dp(16), dp(8));
            confirmRemove.setText("There is no way to undo this unless you've backed up.");
            builder.setView(confirmRemove);
            builder.setTitle("Remove " + name + "?");
            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ContentResolver cr = TimetableFragment.this.getActivity().getContentResolver();
                    cr.delete(TimetableProvider.PERIODS_URI, TimetableProvider.ID + "=" + _id, null);
                    refreshView();
                    (mContext).dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    (mContext).dismiss();
                }
            });
            return builder.create();
        }
    }

    public class OptionsDialogFragment extends DialogFragment {
        OptionsDialogFragment mContext = this;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            final CharSequence[] items = {"Edit Period", "Remove Period"};
            AlertDialog.Builder builder = new AlertDialog.Builder(TimetableFragment.this.getActivity());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            EditDialogFragment editFragment = new EditDialogFragment();
                            editor = editFragment;
                            editFragment.setArguments(args);
                            editFragment.show(getFragmentManager(), "edit");
                            (mContext).dismiss();
                            break;
                        case 1:
                            DialogFragment removeFragment = new RemoveDialogFragment();
                            removeFragment.setArguments(args);
                            removeFragment.show(getFragmentManager(), "remove");
                            (mContext).dismiss();
                            break;
                    }
                }
            });
            return builder.create();
        }
    }

    public class TimeSetDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");
            if (start) {
                mHour = 9;
                return new TimePickerDialog(getActivity(), mStartTimeSetListener, mHour, mMinute, true);
            } else {
                mHour = 10;
                return new TimePickerDialog(getActivity(), mEndTimeSetListener, mHour, mMinute, true);
            }
        }
    }
}