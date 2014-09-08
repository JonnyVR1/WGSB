package com.jonny.wgsb;

public class Notifications {
    Integer _id, _read;
    String _title, _date, _message;

    Notifications() {
    }

    Notifications(Integer id, String title, String date, String message, Integer read) {
        this._id = id;
        this._title = title;
        this._date = date;
        this._message = message;
        this._read = read;
    }

    Notifications(Integer id, Integer read) {
        this._id = id;
        this._read = read;
    }

    Integer getID() {
        return this._id;
    }

    void setID(Integer id) {
        this._id = id;
    }

    String getTitle() {
        return this._title;
    }

    void setTitle(String title) {
        this._title = title;
    }

    String getDate() {
        return this._date;
    }

    void setDate(String date) {
        this._date = date;
    }

    String getMessage() {
        return this._message;
    }

    void setMessage(String message) {
        this._message = message;
    }

    Integer getRead() {
        return this._read;
    }

    void setRead(Integer read) {
        this._read = read;
    }
}