package com.jonny.wgsb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.widget.RemoteViews;

import java.util.Calendar;

public class TimetableWidget4x1 extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Intent intent;
        for (int appWidgetId : appWidgetIds) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                intent = new Intent(context, TimetableTabController.class);
            } else {
                intent = new Intent(context, TimetableTabControllerLegacy.class);
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_widget4x1_layout);
            views.setOnClickPendingIntent(R.id.widget4x1, pendingIntent);
            TimetableWidgetWeekData weekStuff = weekStuff(context);
            String nowName = "";
            String nowTeacher = "";
            String nowRoom = "";
            String nowStart = "";
            String nowEnd = "";
            String nextName = "";
            String nextTeacher = "";
            String nextRoom = "";
            String nextStart = "";
            String nextEnd = "";
            try {
                Calendar cal = Calendar.getInstance();
                int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                ContentResolver cr = context.getContentResolver();
                Cursor c = cr.query(TimetableProvider.PERIODS_URI, new String[]{
                                TimetableProvider.ID,
                                TimetableProvider.ACTIVITY,
                                TimetableProvider.ROOM,
                                TimetableProvider.START,
                                TimetableProvider.END,
                                TimetableProvider.TEACHER,
                        },
                        (TimetableProvider.DAY + "='" + weekStuff.day + "'"), null, TimetableProvider.START
                );
                if (!weekStuff.day.equals("weekend")) {
                    c.moveToFirst();
                    try {
                        nowName = c.getString(1);
                        nowTeacher = c.getString(5);
                        nowRoom = " - " + c.getString(2);
                        nowStart = TimetablePeriod.timeString(c.getInt(3));
                        nowEnd = TimetablePeriod.timeString(c.getInt(4));
                    } catch (CursorIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        nowName = "Nothing";
                        nowTeacher = " - ";
                        nowRoom = "";
                        nowStart = "";
                        nowEnd = "";
                        c.moveToLast();
                        break;
                    }
                    while (currentTime > c.getInt(4)) {
                        try {
                            c.moveToNext();
                            nowName = c.getString(1);
                            nowTeacher = c.getString(5);
                            nowRoom = " - " + c.getString(2);
                            nowStart = TimetablePeriod.timeString(c.getInt(3));
                            nowEnd = TimetablePeriod.timeString(c.getInt(4));
                        } catch (CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            nowName = "Nothing";
                            nowTeacher = " - ";
                            nowRoom = "";
                            nowStart = "";
                            nowEnd = "";
                            c.moveToLast();
                            break;
                        }
                    }
                    if (!c.isLast() && !c.isAfterLast()) {
                        c.moveToNext();
                        nextName = c.getString(1);
                        nextTeacher = c.getString(5);
                        nextRoom = " - " + c.getString(2);
                        nextStart = TimetablePeriod.timeString(c.getInt(3));
                        nextEnd = TimetablePeriod.timeString(c.getInt(4));
                    } else {
                        nextName = "Nothing";
                        nextTeacher = " - ";
                        nextRoom = "";
                    }
                } else {
                    nextName = "Nothing";
                    nextTeacher = " - ";
                    nextRoom = "";
                    nowName = "Nothing";
                    nowTeacher = " - ";
                    nowRoom = " - ";
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                nextName = "Nothing";
                nextTeacher = " - ";
                nextRoom = "";
                nowName = "Nothing";
                nowTeacher = " - ";
                nowRoom = "";
            } catch (NullPointerException e) {
                e.printStackTrace();
                nextName = "Nothing";
                nextTeacher = " - ";
                ;
                nextRoom = "";
                nowName = "Nothing";
                nowTeacher = " - ";
                nowRoom = "";
            }
            TimetableWidgetContent data = new TimetableWidgetContent(nextName, nextTeacher, nextRoom, nextStart, nextEnd, nowName, nowTeacher, nowRoom, nowStart, nowEnd);
            views.setTextViewText(R.id.widget4x1_now_name, "Now: " + data.nowName);
            views.setTextViewText(R.id.widget4x1_now_time, data.nowStart + " - " + data.nowEnd);
            views.setTextViewText(R.id.widget4x1_now_teacher, data.nowTeacher + data.nowRoom);
            views.setTextViewText(R.id.widget4x1_next_name, "Next: " + data.nextName);
            views.setTextViewText(R.id.widget4x1_next_time, data.nextStart + " - " + data.nextEnd);
            views.setTextViewText(R.id.widget4x1_next_teacher, "" + data.nextTeacher + data.nextRoom);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}

	private TimetableWidgetWeekData weekStuff(Context context) {
		ContentResolver cr = context.getContentResolver();
		Cursor w = cr.query(TimetableProvider.WEEK_URI, new String[] {
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
        return new TimetableWidgetWeekData(day, nextDay, weekNo, nextWeekNo);
	}
}