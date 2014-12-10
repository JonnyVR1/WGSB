package com.jonny.wgsb;

class Calendar {
    Integer id;
    String event, date, dateString;

    Calendar() {
    }

    Calendar(Integer id, String event, String date, String dateString) {
        this.id = id;
        this.event = event;
        this.date = date;
        this.dateString = dateString;
    }
}