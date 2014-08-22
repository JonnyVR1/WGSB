package com.jonny.wgsb;

public class News {
	Integer _id;
	String _title, _imageSrc, _story, _date;

    News() {
    }

    News(Integer id, String title, String story, String imageSrc, String date) {
	    this._id = id;
	    this._title = title;
	    this._story = story;
	    this._imageSrc = imageSrc;
	    this._date = date;
	}

    Integer getID() {
	    return this._id;
	}

    void setID(Integer id) {
	    this._id = id;
	}

    String getTitle() {
	    return this._title;
	}

    void setTitle(String title) {
	    this._title = title;
	}

    String getImageSrc() {
	    return this._imageSrc;
	}

    void setImageSrc(String imageSrc) {
	    this._imageSrc = imageSrc;
	}

    String getStory() {
	    return this._story;
	}

    void setStory(String story) {
	    this._story = story;
	}

    String getDate() {
	    return this._date;
	}

    void setDate(String date) {
	    this._date = date;
	}
}