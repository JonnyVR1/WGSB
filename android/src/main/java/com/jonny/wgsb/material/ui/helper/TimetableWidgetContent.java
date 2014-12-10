package com.jonny.wgsb.material.ui.helper;

public class TimetableWidgetContent {
    public String nextName;
    public String nextTeacher;
    public String nextRoom;
    public String nextStart;
    public String nextEnd;
    public String nowName;
    public String nowTeacher;
    public String nowRoom;
    public String nowStart;
    public String nowEnd;

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