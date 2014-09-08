package com.jonny.wgsb;

public class Topical {
    Integer _id, _red;
    String _title, _story;

    Topical() {
    }

    Topical(Integer id, String title, String story, Integer red) {
        this._id = id;
        this._title = title;
        this._story = story;
        this._red = red;
    }

    Integer getRed() {
        return this._id;
    }

    void setRed(Integer red) {
        this._red = red;
    }

    Integer getID() {
        return this._id;
    }

    void setID(Integer id) {
        this._id = id;
    }

    String getStory() {
        return this._story;
    }

    void setStory(String story) {
        this._story = story;
    }

    String getTitle() {
        return this._title;
    }

    void setTitle(String title) {
        this._title = title;
    }
}