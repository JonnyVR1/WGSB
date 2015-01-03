package com.jonny.wgsb.material.ui.helper;

public class News {
    public Integer id;
    public String title, imageSrc, story, date;

    public News() {
    }

    public News(Integer id, String title, String story, String imageSrc, String date) {
        this.id = id;
        this.title = title;
        this.story = story;
        this.imageSrc = imageSrc;
        this.date = date;
    }
}