package com.jonny.wgsb.material.ui.helper;

public class Notifications {
    public Integer id;
    public Integer read;
    public String title;
    public String date;
    public String message;

    public Notifications() {
    }

    public Notifications(Integer id, String title, String date, String message, Integer read) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.message = message;
        this.read = read;
    }

    public Notifications(Integer id, Integer read) {
        this.id = id;
        this.read = read;
    }
}