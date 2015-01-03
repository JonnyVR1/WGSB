package com.jonny.wgsb.material.ui.helper;

public class Calendar {
    public Integer id;
    public String event, date, dateString;

    public Calendar() {
    }

    public Calendar(Integer id, String event, String date, String dateString) {
        this.id = id;
        this.event = event;
        this.date = date;
        this.dateString = dateString;
    }
}