package com.jonny.wgsb;

class TimetableWidgetWeekData {
    final String day, nextDay;
    final int weekNo, nextWeekNo;

    TimetableWidgetWeekData(String day, String nextDay, int weekNo, int nextWeekNo) {
        this.day = day;
        this.nextDay = nextDay;
        this.weekNo = weekNo;
        this.nextWeekNo = nextWeekNo;
    }
}