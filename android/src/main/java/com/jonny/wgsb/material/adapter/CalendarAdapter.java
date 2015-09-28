package com.jonny.wgsb.material.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonny.wgsb.material.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends BaseAdapter {
    public static int mSelectedPosition = 0;
    public static List<String> dayString;
    private final String currentDateString;
    private final Context mContext;
    private final Calendar month;
    private final DateFormat df;
    private ArrayList<String> items;
    private View previousView;
    private GregorianCalendar pMonth;
    private int firstDay;

    public CalendarAdapter(Context context, GregorianCalendar monthCalendar) {
        CalendarAdapter.dayString = new ArrayList<>();
        Locale.setDefault(Locale.UK);
        month = monthCalendar;
        GregorianCalendar selectedDate = (GregorianCalendar) monthCalendar.clone();
        mContext = context;
        month.set(GregorianCalendar.DAY_OF_MONTH, 1);
        items = new ArrayList<>();
        df = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        currentDateString = df.format(selectedDate.getTime());
        refreshDays();
    }

    public void setItems(ArrayList<String> items) {
        for (int i = 0; i != items.size(); i++) {
            if (items.get(i).length() == 1) {
                items.set(i, "0" + items.get(i));
            }
        }
        this.items = items;
    }

    public int getCount() {
        return dayString.size();
    }

    public Object getItem(int position) {
        return dayString.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.calendar_item, parent, false);
        }
        TextView dayView = (TextView) view.findViewById(R.id.date);
        ImageView dot = (ImageView) view.findViewById(R.id.date_icon);
        String date = dayString.get(position);
        String[] separatedTime = date.split("-");
        String gridValue = separatedTime[0].replaceFirst("^0*", "");
        Integer gridValueInt = Integer.parseInt(gridValue);
        if (date.length() == 1) {
            date = "0" + date;
        }
        if (date.length() > 0 && items != null && items.contains(date) && dayView.getVisibility() == View.VISIBLE) {
            dot.setVisibility(View.VISIBLE);
        } else {
            dot.setVisibility(View.GONE);
        }
        if (gridValueInt > 1 && position < firstDay) {
            dayView.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextSecondary));
            dayView.setClickable(false);
            dayView.setFocusable(false);
            dot.setVisibility(View.GONE);
        } else if (gridValueInt <= 12 && position > 28) {
            dayView.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextSecondary));
            dayView.setClickable(false);
            dayView.setFocusable(false);
            dot.setVisibility(View.GONE);
        } else {
            dayView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        }
        if (date.equals(currentDateString)) {
            setSelected(view, position);
            previousView = view;
        } else {
            view.setBackgroundResource(R.drawable.calendar_date_number_background);
        }
        dayView.setText(gridValue);
        return view;
    }

    public void setSelected(View view, int position) {
        if (previousView != null) {
            previousView.setBackgroundResource(R.drawable.calendar_date_number_background);
        }
        previousView = view;
        mSelectedPosition = position;
        view.setBackgroundResource(R.drawable.calendar_day_selected);
    }

    public void refreshDays() {
        items.clear();
        dayString.clear();
        Locale.setDefault(Locale.UK);
        pMonth = (GregorianCalendar) month.clone();
        firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
        int maxWeekNumber = month.getActualMaximum(GregorianCalendar.MONTH);
        if (maxWeekNumber >= 7) maxWeekNumber = 6;
        if (firstDay == 2) maxWeekNumber = 5;
        int monthLength = maxWeekNumber * 7;
        int maxP = getMaxP();
        int calMaxP = maxP - (firstDay - 1);
        GregorianCalendar pMonthMaxSet = (GregorianCalendar) pMonth.clone();
        pMonthMaxSet.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);
        for (int n = 0; n < monthLength; n++) {
            String itemValue = df.format(pMonthMaxSet.getTime());
            pMonthMaxSet.add(GregorianCalendar.DATE, 1);
            dayString.add(itemValue);
        }
    }

    private int getMaxP() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMinimum(GregorianCalendar.MONTH)) {
            pMonth.set((month.get(GregorianCalendar.YEAR) - 1), month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pMonth.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
        }
        return pMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    }
}