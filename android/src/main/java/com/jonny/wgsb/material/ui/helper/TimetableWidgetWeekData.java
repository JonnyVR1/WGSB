package com.jonny.wgsb.material.ui.helper;

public class TimetableWidgetWeekData {
    public final String day;
    public final String nextDay;
    public final int weekNo, nextWeekNo;

    public TimetableWidgetWeekData(String day, String nextDay, int weekNo, int nextWeekNo) {
        this.day = day;
        this.nextDay = nextDay;
        this.weekNo = weekNo;
        this.nextWeekNo = nextWeekNo;
    }
}