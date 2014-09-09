package com.jonny.wgsb;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

public class TimetableProvider extends ContentProvider {
    static final String ACTIVITY = "activity";
    static final String TEACHER = "teacher";
    static final String START = "start";
    static final String BREAK = "break";
    static final String ROOM = "room";
    static final String NAME = "name";
    static final String ID = "id";
    static final String DAY = "day";
    static final String END = "end";
    static final String KEY = "key";
    static final String NUM = "num";
    private static final String DATABASE_NAME = "Timetable.db";
    private static final String PERIODS_TABLE_NAME = "periods";
    private static final String WEEK_TABLE_NAME = "week";
    private static final UriMatcher sUriMatcher;
    private static final int DATABASE_VERSION = 1, PERIODS = 1, WEEK = 3;
    private static final String PROVIDER_NAME = "com.jonny.wgsb.TimetableProvider";
    static final Uri PERIODS_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + PERIODS_TABLE_NAME);
    static final Uri WEEK_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + WEEK_TABLE_NAME);
    private static final String PERIODS_TYPE = "com.jonny.wgsb/periods";
    private static final String WEEK_TYPE = "com.jonny.wgsb/week";
    private static HashMap<String, String> projectionMap;
    private static DatabaseHelper dbHelper;

    private static void setDefaults(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TimetableProvider.KEY, "style");
        values.put(TimetableProvider.NUM, 1);
        db.insert(WEEK_TABLE_NAME, KEY, values);
        values.put(TimetableProvider.KEY, "weekNo");
        values.put(TimetableProvider.NUM, 1);
        db.insert(WEEK_TABLE_NAME, KEY, values);
        values.put(TimetableProvider.KEY, "amount");
        values.put(TimetableProvider.NUM, 1);
        db.insert(WEEK_TABLE_NAME, KEY, values);
        values.put(TimetableProvider.KEY, "comp");
        values.put(TimetableProvider.NUM, 1);
        db.insert(WEEK_TABLE_NAME, KEY, values);
        values.put(TimetableProvider.KEY, "theme");
        values.put(TimetableProvider.NUM, 1);
        db.insert(WEEK_TABLE_NAME, KEY, values);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case PERIODS:
                count = db.delete(PERIODS_TABLE_NAME, where, whereArgs);
                break;
            case WEEK:
                count = db.delete(WEEK_TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PERIODS:
                return PERIODS_TYPE;
            case WEEK:
                return WEEK_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri whichURI;
        long rowId;
        switch (sUriMatcher.match(uri)) {
            case PERIODS:
                whichURI = PERIODS_URI;
                rowId = db.insert(PERIODS_TABLE_NAME, ACTIVITY, values);
                break;
            case WEEK:
                whichURI = WEEK_URI;
                rowId = db.insert(WEEK_TABLE_NAME, KEY, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowId >= 0) {
            Uri noteUri = ContentUris.withAppendedId(whichURI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case PERIODS:
                qb.setTables(PERIODS_TABLE_NAME);
                qb.setProjectionMap(projectionMap);
                break;
            case WEEK:
                qb.setTables(WEEK_TABLE_NAME);
                qb.setProjectionMap(projectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case PERIODS:
                count = db.update(PERIODS_TABLE_NAME, values, where, whereArgs);
                break;
            case WEEK:
                count = db.update(WEEK_TABLE_NAME, values, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PROVIDER_NAME, PERIODS_TABLE_NAME, PERIODS);
        sUriMatcher.addURI(PROVIDER_NAME, WEEK_TABLE_NAME, WEEK);
        projectionMap = new HashMap<String, String>();
        projectionMap.put(ID, ID);
        projectionMap.put(DAY, DAY);
        projectionMap.put(START, START);
        projectionMap.put(END, END);
        projectionMap.put(ACTIVITY, ACTIVITY);
        projectionMap.put(TEACHER, TEACHER);
        projectionMap.put(ROOM, ROOM);
        projectionMap.put(NAME, NAME);
        projectionMap.put(BREAK, BREAK);
        projectionMap.put(KEY, KEY);
        projectionMap.put(NUM, NUM);
    }

    public class DatabaseHelper extends SQLiteOpenHelper {
        Context context;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + PERIODS_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            NAME + " VARCHAR(2)," + DAY + " VARCHAR(10)," + START + " INTEGER," + END + " INTEGER," +
                            ACTIVITY + " VARCHAR(50)," + TEACHER + " VARCHAR(50)," + ROOM + " VARCHAR(50)," +
                            BREAK + " INTEGER" + ");"
            );
            db.execSQL("CREATE TABLE " + WEEK_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            KEY + " VARCHAR(10)," + NUM + " INTEGER" + ");"
            );
            setDefaults(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + PERIODS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + WEEK_TABLE_NAME);
            onCreate(db);
        }
    }
}