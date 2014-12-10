package com.jonny.wgsb;

public class Notifications {
    Integer id, read;
    String title, date, message;

    Notifications() {
    }

    Notifications(Integer id, String title, String date, String message, Integer read) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.message = message;
        this.read = read;
    }

    Notifications(Integer id, Integer read) {
        this.id = id;
        this.read = read;
    }
}