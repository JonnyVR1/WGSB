package com.jonny.wgsb;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private static final String AllCalendarItemsURL = "http://app.wirralgrammarboys.com/get_calendar.php";
    private static final String TAG_SUCCESS = "success";
    private static final String CALENDAR = "calendar";
    private static final String EVENT = "event";
    private static final String DATE = "date";
    static int height;
    JSONParser jParser = new JSONParser();
    JSONArray calendarItems = null;
    DatabaseHandler dbhandler;
    ProgressDialog mProgress;
    RelativeLayout previous;
    RelativeLayout next;
    ConnectionDetector cd;
    GridView gridview;
    TextView title;
    AsyncTask<Void, Integer, Void> mLoadCalendarTask;
    private ActionBarDrawerToggle mDrawerToggle;
    private Integer contentAvailable = 1;
    private Boolean FlagCancelled = false;
    private GregorianCalendar month, itemMonth;
    private CalendarAdapter adapter;
    private Handler handler;
    private ArrayList<String> items;
    private Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            items.clear();
            for (int i = 0; i < 7; i++) {
                itemMonth.add(GregorianCalendar.DATE, 1);
                List<Calendar> calendar = dbhandler.getAllCalendar();
                for (Calendar c : calendar) {
                    items.add(c.getDate());
                }
            }
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };
    private Boolean taskSuccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        cd = new ConnectionDetector(this.getActivity().getApplicationContext());
        dbhandler = DatabaseHandler.getInstance(getActivity());
        setupActionBar();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        setHasOptionsMenu(true);
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.banner);
        actionBar.setTitle(R.string.calendar);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        previous = (RelativeLayout) view.findViewById(R.id.previousMonth);
        next = (RelativeLayout) view.findViewById(R.id.nextMonth);
        title = (TextView) view.findViewById(R.id.title);
        gridview = (GridView) view.findViewById(R.id.gridview);
        month = (GregorianCalendar) GregorianCalendar.getInstance();
        Locale.setDefault(Locale.UK);
        itemMonth = (GregorianCalendar) month.clone();
        items = new ArrayList<String>();
        adapter = new CalendarAdapter(getActivity(), month);
        gridview.setAdapter(adapter);
        handler = new Handler();
        if (dbhandler.getCalendarCount() == 0) {
            if (cd.isConnectingToInternet()) {
                loadCalendar();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        } else {
            refreshCalendar();
        }
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
        previous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousMonth();
                refreshCalendar();
            }
        });
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextMonth();
                refreshCalendar();
            }
        });
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                String selectedGridDate = CalendarAdapter.dayString.get(position);
                String[] separatedTime = selectedGridDate.split("-");
                String gridValueString = separatedTime[0].replaceFirst("^0*", "");
                int gridValue = Integer.parseInt(gridValueString);
                if ((gridValue > 10) && (position < 8)) {
                    setPreviousMonth();
                    refreshCalendar();
                } else if ((gridValue < 12) && (position > 28)) {
                    setNextMonth();
                    refreshCalendar();
                }
                if (items.contains(selectedGridDate)) {
                    String events = null;
                    String dateString = null;
                    List<Calendar> calendar = dbhandler.getAllCalendarAtDate(selectedGridDate);
                    for (Calendar c : calendar) {
                        if (events == null) events = "- " + c.getEvent();
                        else events = events + "\n\n- " + c.getEvent();
                        dateString = c.getDateString();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Events on " + dateString);
                    alertDialog.setMessage(events);
                    alertDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contentAvailable == 0) {
            if (cd.isConnectingToInternet()) {
                loadCalendar();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dbhandler.getCalendarCount() == 0) {
            contentAvailable = 0;
        } else {
            contentAvailable = 1;
        }
        if (mLoadCalendarTask != null && mLoadCalendarTask.getStatus() == AsyncTask.Status.PENDING) {
            FlagCancelled = true;
            mLoadCalendarTask.cancel(true);
            mLoadCalendarTask = null;
        } else if (mLoadCalendarTask != null && mLoadCalendarTask.getStatus() == AsyncTask.Status.RUNNING) {
            FlagCancelled = true;
            mLoadCalendarTask.cancel(true);
            mLoadCalendarTask = null;
        } else if (mLoadCalendarTask != null && mLoadCalendarTask.getStatus() == AsyncTask.Status.FINISHED) {
            FlagCancelled = true;
            mLoadCalendarTask = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.calendar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.calendarRefresh) {
            if (cd.isConnectingToInternet()) {
                loadCalendar();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet_refresh));
            }
        } else if (item.getItemId() == R.id.settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                    .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null).commit();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void internetDialogue(String string) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(this.getActivity());
        alertBox.setIcon(R.drawable.fail);
        alertBox.setMessage(string);
        alertBox.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            }
        });
        alertBox.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        alertBox.show();
    }

    private void loadCalendar() {
        mLoadCalendarTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected synchronized void onPreExecute() {
                super.onPreExecute();
                mProgress = new ProgressDialog(getActivity());
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.setMessage("Loading Calendar, Please wait...");
                mProgress.setIndeterminate(false);
                mProgress.setCanceledOnTouchOutside(true);
                mProgress.show();
            }

            @Override
            protected Void doInBackground(Void... args) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                try {
                    JSONObject json = jParser.makeHttpRequest(AllCalendarItemsURL, params);
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        calendarItems = json.getJSONArray(CALENDAR);
                        int progressCount = 0;
                        mProgress.setMax(calendarItems.length());
                        for (int i = 0; i < calendarItems.length(); i++) {
                            JSONObject obj = calendarItems.getJSONObject(i);
                            Integer id = i + 1;
                            String event = obj.getString(EVENT);
                            String date = obj.getString(DATE).replaceAll(" ", "");
                            String[] splitDate = date.split("-");
                            Integer dayDateInt = Integer.parseInt(splitDate[0]);
                            String dayDateString = (new StringBuilder(dayDateInt + getDayNumberSuffix(Integer.parseInt(splitDate[0])))).toString();
                            String monthDateString = getMonthName(Integer.parseInt(splitDate[1]));
                            String yearDateString = splitDate[2];
                            String dateString = dayDateString + " " + monthDateString + " " + yearDateString;
                            if (date.length() != 10) {
                                if (splitDate[0].length() != 2) {
                                    splitDate[0] = "0" + splitDate[0];
                                }
                                if (splitDate[1].length() != 2) {
                                    splitDate[1] = "0" + splitDate[1];
                                }
                                date = splitDate[0] + "-" + splitDate[1] + "-" + yearDateString;
                            }
                            if (id > dbhandler.getCalendarCount()) {
                                dbhandler.addCalendar(new Calendar(id, event, date, dateString));
                            } else {
                                dbhandler.updateCalendar(new Calendar(id, event, date, dateString));
                            }
                            if (isCancelled() || FlagCancelled) break;
                            progressCount++;
                            publishProgress((int) (progressCount * 100 / calendarItems.length()));
                        }
                        taskSuccess = true;
                    } else {
                        Log.e("JSON Response", "success == 0");
                        taskSuccess = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    taskSuccess = false;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                mProgress.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                mLoadCalendarTask = null;
                mProgress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        refreshCalendar();
                        if (!taskSuccess)
                            internetDialogue(getResources().getString(R.string.no_working_connection));
                    }
                });
            }
        };
        mLoadCalendarTask.execute(null, null, null);
    }

    private void setNextMonth() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMaximum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) + 1), month.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) + 1);
        }
    }

    private void setPreviousMonth() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMinimum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) - 1), month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
        }
    }

    private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private String getMonthName(int day) {
        switch (day) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            default:
                return "December";
        }
    }

    private void refreshCalendar() {
        adapter.refreshDays();
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater);
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }
}