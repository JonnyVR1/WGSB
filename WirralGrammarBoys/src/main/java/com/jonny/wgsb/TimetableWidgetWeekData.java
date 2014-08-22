package com.jonny.wgsb;

public class TimetableWidgetWeekData {
	String day, nextDay;
	int weekNo, nextWeekNo;

	TimetableWidgetWeekData(String day, String nextDay, int weekNo, int nextWeekNo) {
		this.day = day;
		this.nextDay = nextDay;
		this.weekNo = weekNo;
		this.nextWeekNo = nextWeekNo;
	}
}