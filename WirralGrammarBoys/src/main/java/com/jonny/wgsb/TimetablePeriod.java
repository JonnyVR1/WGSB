package com.jonny.wgsb;

public class TimetablePeriod {
    String startString, endString, teacher, id, name, room, day;
    boolean isBreak;
    int _id, end, start, length;

	static String timeString(int time) {
		int minutes = time%60;
		String stringMinutes;
		if (minutes < 10) stringMinutes = "0" + minutes;
		else stringMinutes = "" + minutes;
		int hours = (int) Math.floor(time/60);
		String stringHours;
		if (hours < 10) stringHours = "0" + hours;
		else stringHours = "" + hours;
        return stringHours + ":" + stringMinutes;
	}

	TimetablePeriod(int _id, String day) {
		this._id = _id;
		this.day = day;
	}

	void setId(String id) {
		this.id = id;
	}

	void setName(String name) {
		this.name = name;
	}

	void setRoom(String room) {
		this.room = room;
	}

	void setTeacher(String teacher) {
		this.teacher = teacher;
	}

	void setTime(int start, int end) {
		this.start = start;
		this.startString = timeString(start);
		this.end = end;
		this.endString = timeString(end);
		this.length = end-start;
	}

	void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}
}