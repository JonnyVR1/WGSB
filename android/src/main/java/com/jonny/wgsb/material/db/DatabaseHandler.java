package com.jonny.wgsb.material.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jonny.wgsb.material.ui.helper.Calendar;
import com.jonny.wgsb.material.ui.helper.News;
import com.jonny.wgsb.material.ui.helper.Notifications;
import com.jonny.wgsb.material.ui.helper.Topical;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "WGSB_local";
    private static final String TABLE_CALENDAR = "CALENDAR";
    private static final String TABLE_NEWS = "NEWS";
    private static final String TABLE_NOTIFICATIONS = "NOTIFICATIONS";
    private static final String TABLE_REGISTRATIONS = "REGISTRATIONS";
    private static final String TABLE_TOPICAL = "TOPICAL";
    private static final String KEY_APP_VERSION = "appVersion";
    private static final String KEY_DATE = "date";
    private static final String KEY_DATESTRING = "dateString";
    private static final String KEY_EVENT = "event";
    private static final String KEY_ID = "id";
    private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE " + TABLE_CALENDAR + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENT + " TEXT," + KEY_DATE + " TEXT," + KEY_DATESTRING + " TEXT" + ")";
    private static final String KEY_IMAGE_SRC = "imageSrc";
    private static final String KEY_NOTIFICATION_MESSAGE = "message";
    private static final String KEY_NOTIFICATION_READ = "read";
    private static final String KEY_RED = "red";
    private static final String KEY_REGID = "regId";
    private static final String CREATE_REGISTRATION_TABLE = "CREATE TABLE " + TABLE_REGISTRATIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_REGID + " TEXT," + KEY_APP_VERSION + " INTEGER" + ")";
    private static final String KEY_STORY = "story";
    private static final String KEY_TITLE = "title";
    private static final String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
            + KEY_STORY + " TEXT," + KEY_IMAGE_SRC + " TEXT," + KEY_DATE + " TEXT" + ")";
    private static final String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," + KEY_DATE + " TEXT,"
            + KEY_NOTIFICATION_MESSAGE + " TEXT," + KEY_NOTIFICATION_READ + " INTEGER" + ")";
    private static final String CREATE_TOPICAL_TABLE = "CREATE TABLE " + TABLE_TOPICAL + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
            + KEY_STORY + " TEXT," + KEY_RED + " INTEGER" + ")";
    private static DatabaseHandler sInstance = null;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHandler getInstance(Context context) {
        if (sInstance == null) sInstance = new DatabaseHandler(context.getApplicationContext());
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CALENDAR_TABLE);
        db.execSQL(CREATE_NEWS_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        db.execSQL(CREATE_REGISTRATION_TABLE);
        db.execSQL(CREATE_TOPICAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPICAL);
        onCreate(db);
    }

    public void addCalendar(Calendar calendar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, calendar.id);
        values.put(KEY_EVENT, calendar.event);
        values.put(KEY_DATE, calendar.date);
        values.put(KEY_DATESTRING, calendar.dateString);
        db.insert(TABLE_CALENDAR, null, values);
        db.close();
    }


    public void addNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, news.id);
        values.put(KEY_TITLE, news.title);
        values.put(KEY_STORY, news.story);
        values.put(KEY_DATE, news.date);
        values.put(KEY_IMAGE_SRC, news.imageSrc);
        db.insert(TABLE_NEWS, null, values);
        db.close();
    }

    public void addNotification(Notifications Notifications) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, Notifications.id);
        values.put(KEY_TITLE, Notifications.title);
        values.put(KEY_NOTIFICATION_MESSAGE, Notifications.message);
        values.put(KEY_DATE, Notifications.date);
        values.put(KEY_NOTIFICATION_READ, Notifications.read);
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    public void addRegId(String regId, Integer appVersion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, 1);
        values.put(KEY_REGID, regId);
        values.put(KEY_APP_VERSION, appVersion);
        db.insert(TABLE_REGISTRATIONS, null, values);
    }

    public void addTopical(Topical topical) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, topical.id);
        values.put(KEY_TITLE, topical.title);
        values.put(KEY_STORY, topical.story);
        values.put(KEY_RED, topical.red);
        db.insert(TABLE_TOPICAL, null, values);
        db.close();
    }

    public void deleteAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATIONS);
        db.close();
    }

    public void deleteNotificationAtId(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATIONS + " where " + KEY_ID + "=" + id);
        db.close();
    }

    public void deleteAllRegId() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_REGISTRATIONS);
        db.close();
    }

    public void deleteAllTopical() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_TOPICAL);
        db.close();
    }

    public News getNews(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NEWS, new String[]{KEY_ID,
                        KEY_TITLE, KEY_STORY, KEY_IMAGE_SRC, KEY_DATE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        News news = new News(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4));
        cursor.close();
        return news;
    }

    public Notifications getNotification(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTIFICATIONS, new String[]{KEY_ID, KEY_TITLE, KEY_DATE,
                        KEY_NOTIFICATION_MESSAGE, KEY_NOTIFICATION_READ}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        Notifications notification = new Notifications(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), Integer.parseInt(cursor.getString(4)));
        cursor.close();
        return notification;
    }

    public String getRegId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REGISTRATIONS, new String[]{KEY_ID, KEY_REGID, KEY_APP_VERSION}, KEY_ID + "=?",
                new String[]{String.valueOf(1)}, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String regId = cursor.getString(1);
            cursor.close();
            return regId;
        } else {
            cursor.close();
            return "";
        }
    }

    public Integer getRegIdAppVersion() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REGISTRATIONS, new String[]{KEY_ID, KEY_REGID, KEY_APP_VERSION}, KEY_ID + "=?",
                new String[]{String.valueOf(1)}, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Integer appVersion = cursor.getInt(2);
            cursor.close();
            return appVersion;
        } else {
            cursor.close();
            return Integer.MIN_VALUE;
        }
    }

    public Topical getTopical(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TOPICAL, new String[]{KEY_ID,
                        KEY_TITLE, KEY_STORY, KEY_RED}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Topical topical = new Topical(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                    Integer.parseInt(cursor.getString(3)));
            cursor.close();
            return topical;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<Calendar> getAllCalendar() {
        List<Calendar> calendarList = new ArrayList<Calendar>();
        String selectQuery = "SELECT  * FROM " + TABLE_CALENDAR;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Calendar calendar = new Calendar();
                calendar.id = cursor.getInt(0);
                calendar.event = cursor.getString(1);
                calendar.date = cursor.getString(2);
                calendar.dateString = cursor.getString(3);
                calendarList.add(calendar);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return calendarList;
    }

    public List<Calendar> getAllCalendarAtDate(String date) {
        List<Calendar> calendarList = new ArrayList<Calendar>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_CALENDAR, new String[]{KEY_ID,
                        KEY_EVENT, KEY_DATE, KEY_DATESTRING}, KEY_DATE + "=?",
                new String[]{date}, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Calendar calendar = new Calendar();
                calendar.id = cursor.getInt(0);
                calendar.event = cursor.getString(1);
                calendar.date = cursor.getString(2);
                calendar.dateString = cursor.getString(3);
                calendarList.add(calendar);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return calendarList;
    }

    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<News>();
        String selectQuery = "SELECT  * FROM " + TABLE_NEWS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                News news = new News();
                news.id = cursor.getInt(0);
                news.title = cursor.getString(1);
                news.story = cursor.getString(2);
                news.imageSrc = cursor.getString(3);
                news.date = cursor.getString(4);
                newsList.add(news);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return newsList;
    }

    public List<Notifications> getAllNotifications() {
        List<Notifications> notificationsList = new ArrayList<Notifications>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS + " order by id desc";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Notifications notifications = new Notifications();
                notifications.id = cursor.getInt(0);
                notifications.title = cursor.getString(1);
                notifications.date = cursor.getString(2);
                notifications.message = cursor.getString(3);
                notifications.read = cursor.getInt(4);
                notificationsList.add(notifications);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notificationsList;
    }

    public List<Topical> getAllTopical() {
        List<Topical> topicalList = new ArrayList<Topical>();
        String selectQuery = "SELECT  * FROM " + TABLE_TOPICAL;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Topical topical = new Topical();
                topical.id = cursor.getInt(0);
                topical.title = cursor.getString(1);
                topical.story = cursor.getString(2);
                topical.red = cursor.getInt(3);
                topicalList.add(topical);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topicalList;
    }

    public int getCalendarCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_CALENDAR;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getNewsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NEWS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getNotificationsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS;
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public int getUnreadNotificationsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_NOTIFICATION_READ + " = 0";
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public int getRegIdCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_REGISTRATIONS;
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public int getTopicalCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_TOPICAL;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void updateCalendar(Calendar calendar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT, calendar.event);
        values.put(KEY_DATE, calendar.date);
        values.put(KEY_DATESTRING, calendar.dateString);
        db.update(TABLE_CALENDAR, values, KEY_ID + " = ?", new String[]{String.valueOf(calendar.id)});
        db.close();
    }

    public void updateNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, news.title);
        values.put(KEY_STORY, news.story);
        values.put(KEY_IMAGE_SRC, news.imageSrc);
        values.put(KEY_DATE, news.date);
        db.update(TABLE_NEWS, values, KEY_ID + " = ?", new String[]{String.valueOf(news.id)});
        db.close();
    }

    public void updateNotifications(Notifications notifications) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIFICATION_READ, notifications.read);
        db.update(TABLE_NOTIFICATIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(notifications.id)});
        db.close();
    }

    public void updateRegId(String regId, Integer appVersion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REGID, regId);
        values.put(KEY_APP_VERSION, appVersion);
        db.update(TABLE_REGISTRATIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(1)});
        db.close();
    }

    public void updateTopical(Topical topical) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, topical.title);
        values.put(KEY_STORY, topical.story);
        values.put(KEY_RED, topical.red);
        db.update(TABLE_TOPICAL, values, KEY_ID + " = ?", new String[]{String.valueOf(topical.id)});
        db.close();
    }
}