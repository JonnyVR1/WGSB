package com.jonny.wgsb.material.ui.helper;

public class Topical {
    public Integer id, red;
    public String title, story;

    public Topical() {
    }

    public Topical(Integer id, String title, String story, Integer red) {
        this.id = id;
        this.title = title;
        this.story = story;
        this.red = red;
    }
}