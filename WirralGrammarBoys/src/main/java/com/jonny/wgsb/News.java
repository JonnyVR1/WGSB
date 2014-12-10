package com.jonny.wgsb;

class News {
    Integer id;
    String title, imageSrc, story, date;

    News() {
    }

    News(Integer id, String title, String story, String imageSrc, String date) {
        this.id = id;
        this.title = title;
        this.story = story;
        this.imageSrc = imageSrc;
        this.date = date;
    }
}