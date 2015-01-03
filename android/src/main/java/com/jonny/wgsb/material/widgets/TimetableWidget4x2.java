package com.jonny.wgsb.material.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteException;
import android.widget.RemoteViews;

import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.activities.MainActivity;
import com.jonny.wgsb.material.db.TimetableProvider;
import com.jonny.wgsb.material.ui.helper.TimetablePeriod;
import com.jonny.wgsb.material.ui.helper.TimetableWidgetWeekData;

import java.util.Calendar;

public class TimetableWidget4x2 extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_widget4x2_layout);
            views.setOnClickPendingIntent(R.id.widget4x2, pendingIntent);
            TimetableWidgetWeekData weekStuff = weekStuff(context);
            String nowName = "";
            String nowId = "";
            String nowTeacher = "";
            String nowRoom = "";
            String nowStart = "";
            String nowEnd = "";
            String nextName = "";
            String nextId = "";
            String nextTeacher = "";
            String nextRoom = "";
            String nextStart = "";
            String nextEnd = "";
            String thenName = "";
            String thenId = "";
            String thenTeacher = "";
            String thenRoom = "";
            String thenStart = "";
            String thenEnd = "";
            try {
                Calendar cal = Calendar.getInstance();
                int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                ContentResolver cr = context.getContentResolver();
                Cursor c = cr.query(TimetableProvider.PERIODS_URI, new String[]{
                                TimetableProvider.ID,
                                TimetableProvider.NAME,
                                TimetableProvider.ACTIVITY,
                                TimetableProvider.ROOM,
                                TimetableProvider.START,
                                TimetableProvider.END,
                                TimetableProvider.BREAK,
                                TimetableProvider.TEACHER,
                        },
                        (TimetableProvider.DAY + "='" + weekStuff.day + "'"), null, TimetableProvider.START
                );
                if (!weekStuff.day.equals("weekend")) {
                    c.moveToFirst();
                    try {
                        nowName = c.getString(2);
                        nowTeacher = c.getString(7);
                        nowRoom = c.getString(3);
                        nowStart = TimetablePeriod.timeString(c.getInt(4));
                        nowEnd = TimetablePeriod.timeString(c.getInt(5));
                    } catch (CursorIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        nowName = "Nothing";
                        nowTeacher = " - ";
                        nowRoom = " - ";
                        nowStart = "";
                        nowEnd = "";
                        c.moveToLast();
                        break;
                    }
                    if (currentTime >= c.getInt(4) && currentTime <= c.getInt(5)) {
                        try {
                            nowName = c.getString(2);
                            nowTeacher = c.getString(7);
                            nowRoom = c.getString(3);
                            nowStart = TimetablePeriod.timeString(c.getInt(4));
                            nowEnd = TimetablePeriod.timeString(c.getInt(5));
                        } catch (CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            nowName = "Nothing";
                            nowTeacher = " - ";
                            nowRoom = " - ";
                            nowStart = "";
                            nowEnd = "";
                            c.moveToLast();
                            break;
                        }
                    }
                    while (currentTime > c.getInt(5)) {
                        try {
                            c.moveToNext();
                            nowName = c.getString(2);
                            nowTeacher = c.getString(7);
                            nowRoom = c.getString(3);
                            nowStart = TimetablePeriod.timeString(c.getInt(4));
                            nowEnd = TimetablePeriod.timeString(c.getInt(5));
                        } catch (CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            nowName = "Nothing";
                            nowTeacher = " - ";
                            nowRoom = " - ";
                            nowStart = "";
                            nowEnd = "";
                            c.moveToLast();
                            break;
                        }
                    }

                    if (!c.isLast() && !c.isAfterLast()) {
                        c.moveToNext();
                        nextId = c.getString(1);
                        nextName = c.getString(2);
                        nextTeacher = c.getString(7);
                        nextRoom = c.getString(3);
                        nextStart = TimetablePeriod.timeString(c.getInt(4));
                        nextEnd = TimetablePeriod.timeString(c.getInt(5));
                    } else {
                        nextName = "Nothing";
                        nextTeacher = " - ";
                        nextRoom = " - ";
                    }
                    if (!c.isLast() && !c.isAfterLast()) {
                        c.moveToNext();
                        thenId = c.getString(1);
                        thenName = c.getString(2);
                        thenTeacher = c.getString(7);
                        thenRoom = c.getString(3);
                        thenStart = TimetablePeriod.timeString(c.getInt(4));
                        thenEnd = TimetablePeriod.timeString(c.getInt(5));
                    } else {
                        thenName = "Nothing";
                        thenRoom = " - ";
                        thenTeacher = " - ";
                    }
                } else {
                    nextName = "Nothing";
                    nextTeacher = " - ";
                    nextRoom = " - ";
                    nowName = "Nothing";
                    nowTeacher = " - ";
                    nowRoom = " - ";
                    thenName = "Nothing";
                    thenTeacher = " - ";
                    thenRoom = " - ";
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                nextName = "Nothing";
                nextTeacher = " - ";
                nextRoom = " - ";
                nowName = "Nothing";
                nowTeacher = " - ";
                nowRoom = " - ";
                thenName = "Nothing";
                thenTeacher = " - ";
                thenRoom = " - ";
            } catch (NullPointerException e) {
                e.printStackTrace();
                nextName = "Nothing";
                nextTeacher = " - ";
                nextRoom = " - ";
                nowName = "Nothing";
                nowTeacher = " - ";
                nowRoom = " - ";
                thenName = "Nothing";
                thenTeacher = " - ";
                thenRoom = " - ";
            }
            views.setTextViewText(R.id.widget4x2_now_id, nowId);
            views.setTextViewText(R.id.widget4x2_now_name, nowName);
            views.setTextViewText(R.id.widget4x2_now_time, nowStart + " - " + nowEnd);
            views.setTextViewText(R.id.widget4x2_now_teacher, nowTeacher);
            views.setTextViewText(R.id.widget4x2_now_room, nowRoom);
            views.setTextViewText(R.id.widget4x2_next_id, nextId);
            views.setTextViewText(R.id.widget4x2_next_name, nextName);
            views.setTextViewText(R.id.widget4x2_next_time, nextStart + " - " + nextEnd);
            views.setTextViewText(R.id.widget4x2_next_teacher, "" + nextTeacher);
            views.setTextViewText(R.id.widget4x2_next_room, "" + nextRoom);
            views.setTextViewText(R.id.widget4x2_then_id, thenId);
            views.setTextViewText(R.id.widget4x2_then_name, thenName);
            views.setTextViewText(R.id.widget4x2_then_time, thenStart + " - " + thenEnd);
            views.setTextViewText(R.id.widget4x2_then_teacher, "" + thenTeacher);
            views.setTextViewText(R.id.widget4x2_then_room, "" + thenRoom);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private TimetableWidgetWeekData weekStuff(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor w = cr.query(TimetableProvider.WEEK_URI, new String[]{
                        TimetableProvider.ID, TimetableProvider.KEY,
                        TimetableProvider.NUM
                },
                TimetableProvider.KEY + "='weekNo'", null, null);
        w.moveToFirst();
        int weekNo = w.getInt(2);
        int nextWeekNo = 1;
        switch (weekNo) {
            case 1:
                nextWeekNo = 2;
            case 2:
                nextWeekNo = 1;
        }
        Calendar cal = Calendar.getInstance();
        int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 2;
        String day;
        String nextDay;
        switch (weekDay) {
            case 0:
                day = "mon_" + weekNo;
                nextDay = "tues_" + weekNo;
                break;
            case 1:
                day = "tues_" + weekNo;
                nextDay = "wed_" + weekNo;
                break;
            case 2:
                day = "wed_" + weekNo;
                nextDay = "thurs_" + weekNo;
                break;
            case 3:
                day = "thurs_" + weekNo;
                nextDay = "fri_" + weekNo;
                break;
            case 4:
                day = "fri_" + weekNo;
                nextDay = "weekend";
                break;
            default:
                day = "weekend";
                nextDay = "mon_" + weekNo;
        }
        return new TimetableWidgetWeekData(day);
    }
}