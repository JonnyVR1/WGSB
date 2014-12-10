package com.jonny.wgsb.material.ui.helper;

public class TimetablePeriod {
    public String startString;
    public String endString;
    public String teacher;
    public String id;
    public String name;
    public String room;
    public boolean isBreak;
    public int _id;
    public int end;
    public int start;
    String day;
    int length;

    public TimetablePeriod(int _id, String day) {
        this._id = _id;
        this.day = day;
    }

    public static String timeString(int time) {
        int minutes = time % 60;
        String stringMinutes;
        if (minutes < 10) stringMinutes = "0" + minutes;
        else stringMinutes = "" + minutes;
        int hours = (int) Math.floor(time / 60);
        String stringHours;
        if (hours < 10) stringHours = "0" + hours;
        else stringHours = "" + hours;
        return stringHours + ":" + stringMinutes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setTime(int start, int end) {
        this.start = start;
        this.startString = timeString(start);
        this.end = end;
        this.endString = timeString(end);
        this.length = end - start;
    }

    public void setBreak(boolean isBreak) {
        this.isBreak = isBreak;
    }
}