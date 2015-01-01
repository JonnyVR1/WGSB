package com.jonny.wgsb.material.ui.helper;

public class TimetableWidgetContent {
    public final String nextName;
    public final String nextTeacher;
    public final String nextRoom;
    public final String nextStart;
    public final String nextEnd;
    public final String nowName;
    public final String nowTeacher;
    public final String nowRoom;
    public final String nowStart;
    public final String nowEnd;

    public TimetableWidgetContent(String nextName, String nextTeacher, String nextRoom, String nextStart, String nextEnd, String nowName, String nowTeacher, String nowRoom, String nowStart, String nowEnd) {
        this.nextName = nextName;
        this.nextTeacher = nextTeacher;
        this.nextRoom = nextRoom;
        this.nextStart = nextStart;
        this.nextEnd = nextEnd;
        this.nowName = nowName;
        this.nowTeacher = nowTeacher;
        this.nowRoom = nowRoom;
        this.nowStart = nowStart;
        this.nowEnd = nowEnd;
    }
}