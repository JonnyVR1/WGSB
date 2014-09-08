package com.jonny.wgsb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    static DatabaseHandler getInstance(Context context) {
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

    void addCalendar(Calendar calendar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, calendar.getID());
        values.put(KEY_EVENT, calendar.getEvent());
        values.put(KEY_DATE, calendar.getDate());
        values.put(KEY_DATESTRING, calendar.getDateString());
        db.insert(TABLE_CALENDAR, null, values);
        db.close();
    }


    void addNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, news.getID());
        values.put(KEY_TITLE, news.getTitle());
        values.put(KEY_STORY, news.getStory());
        values.put(KEY_DATE, news.getDate());
        values.put(KEY_IMAGE_SRC, news.getImageSrc());
        db.insert(TABLE_NEWS, null, values);
        db.close();
    }

    void addNotification(Notifications Notifications) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, Notifications.getID());
        values.put(KEY_TITLE, Notifications.getTitle());
        values.put(KEY_NOTIFICATION_MESSAGE, Notifications.getMessage());
        values.put(KEY_DATE, Notifications.getDate());
        values.put(KEY_NOTIFICATION_READ, Notifications.getRead());
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    void addRegId(String regId, Integer appVersion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, 1);
        values.put(KEY_REGID, regId);
        values.put(KEY_APP_VERSION, appVersion);
        db.insert(TABLE_REGISTRATIONS, null, values);
    }

    void addTopical(Topical topical) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, topical.getID());
        values.put(KEY_TITLE, topical.getTitle());
        values.put(KEY_STORY, topical.getStory());
        values.put(KEY_RED, topical.getRed());
        db.insert(TABLE_TOPICAL, null, values);
        db.close();
    }

    void deleteAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATIONS);
        db.close();
    }

    void deleteNotificationAtId(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATIONS + " where " + KEY_ID + "=" + id);
        db.close();
    }

    void deleteAllRegId() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_REGISTRATIONS);
        db.close();
    }

    void deleteAllTopical() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_TOPICAL);
        db.close();
    }

    News getNews(int id) {
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

    Notifications getNotification(int id) {
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

    String getRegId() {
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

    Integer getRegIdAppVersion() {
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

    Topical getTopical(int id) {
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

    List<Calendar> getAllCalendar() {
        List<Calendar> calendarList = new ArrayList<Calendar>();
        String selectQuery = "SELECT  * FROM " + TABLE_CALENDAR;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Calendar calendar = new Calendar();
                calendar.setID(cursor.getInt(0));
                calendar.setEvent(cursor.getString(1));
                calendar.setDate(cursor.getString(2));
                calendar.setDateString(cursor.getString(3));
                calendarList.add(calendar);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return calendarList;
    }

    List<Calendar> getAllCalendarAtDate(String date) {
        List<Calendar> calendarList = new ArrayList<Calendar>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_CALENDAR, new String[]{KEY_ID,
                        KEY_EVENT, KEY_DATE, KEY_DATESTRING}, KEY_DATE + "=?",
                new String[]{date}, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Calendar calendar = new Calendar();
                calendar.setID(cursor.getInt(0));
                calendar.setEvent(cursor.getString(1));
                calendar.setDate(cursor.getString(2));
                calendar.setDateString(cursor.getString(3));
                calendarList.add(calendar);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return calendarList;
    }

    List<News> getAllNews() {
        List<News> newsList = new ArrayList<News>();
        String selectQuery = "SELECT  * FROM " + TABLE_NEWS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                News news = new News();
                news.setID(cursor.getInt(0));
                news.setTitle(cursor.getString(1));
                news.setStory(cursor.getString(2));
                news.setImageSrc(cursor.getString(3));
                news.setDate(cursor.getString(4));
                newsList.add(news);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return newsList;
    }

    List<Notifications> getAllNotifications() {
        List<Notifications> notificationsList = new ArrayList<Notifications>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS + " order by id desc";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Notifications notifications = new Notifications();
                notifications.setID(cursor.getInt(0));
                notifications.setTitle(cursor.getString(1));
                notifications.setDate(cursor.getString(2));
                notifications.setMessage(cursor.getString(3));
                notifications.setRead(cursor.getInt(4));
                notificationsList.add(notifications);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notificationsList;
    }

    List<Topical> getAllTopical() {
        List<Topical> topicalList = new ArrayList<Topical>();
        String selectQuery = "SELECT  * FROM " + TABLE_TOPICAL;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Topical topical = new Topical();
                topical.setID(cursor.getInt(0));
                topical.setTitle(cursor.getString(1));
                topical.setStory(cursor.getString(2));
                topical.setRed(cursor.getInt(3));
                topicalList.add(topical);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topicalList;
    }

    int getCalendarCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_CALENDAR;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    int getNewsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NEWS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    int getNotificationsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS;
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    int getUnreadNotificationsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_NOTIFICATION_READ + " = 0";
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    int getRegIdCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_REGISTRATIONS;
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    int getTopicalCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_TOPICAL;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    void updateCalendar(Calendar calendar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT, calendar.getEvent());
        values.put(KEY_DATE, calendar.getDate());
        values.put(KEY_DATESTRING, calendar.getDateString());
        db.update(TABLE_CALENDAR, values, KEY_ID + " = ?", new String[]{String.valueOf(calendar.getID())});
        db.close();
    }

    void updateNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, news.getTitle());
        values.put(KEY_STORY, news.getStory());
        values.put(KEY_IMAGE_SRC, news.getImageSrc());
        values.put(KEY_DATE, news.getDate());
        db.update(TABLE_NEWS, values, KEY_ID + " = ?", new String[]{String.valueOf(news.getID())});
        db.close();
    }

    void updateNotifications(Notifications notifications) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIFICATION_READ, notifications.getRead());
        db.update(TABLE_NOTIFICATIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(notifications.getID())});
        db.close();
    }

    void updateRegId(String regId, Integer appVersion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REGID, regId);
        values.put(KEY_APP_VERSION, appVersion);
        db.update(TABLE_REGISTRATIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(1)});
        db.close();
    }

    void updateTopical(Topical topical) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, topical.getTitle());
        values.put(KEY_STORY, topical.getStory());
        values.put(KEY_RED, topical.getRed());
        db.update(TABLE_TOPICAL, values, KEY_ID + " = ?", new String[]{String.valueOf(topical.getID())});
        db.close();
    }
}