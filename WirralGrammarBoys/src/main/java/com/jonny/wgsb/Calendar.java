package com.jonny.wgsb;

public class Calendar {
	Integer _id;
	String _event, _date, _dateString;

    Calendar() {
    }

    Calendar(Integer id, String event, String date, String dateString) {
	    this._id = id;
	    this._event = event;
	    this._date = date;
	    this._dateString = dateString;
	}

    Calendar(String event, String date, String dateString) {
	    this._event = event;
	    this._date = date;
	    this._dateString = dateString;
	}

    Integer getID() {
	    return this._id;
	}

    void setID(Integer id) {
	    this._id = id;
	}

    String getEvent() {
	    return this._event;
	}

    void setEvent(String event) {
	    this._event = event;
	}

    String getDate() {
	    return this._date;
	}

    void setDate(String date) {
	    this._date = date;
	}

    String getDateString() {
	    return this._dateString;
	}

    void setDateString(String dateString) {
	    this._dateString = dateString;
	}
}