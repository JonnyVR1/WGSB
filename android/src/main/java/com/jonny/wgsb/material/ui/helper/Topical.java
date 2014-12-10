package com.jonny.wgsb.material.ui.helper;

public class Topical {
    public Integer id;
    public Integer red;
    public String title;
    public String story;

    public Topical() {
    }

    public Topical(Integer id, String title, String story, Integer red) {
        this.id = id;
        this.title = title;
        this.story = story;
        this.red = red;
    }
}