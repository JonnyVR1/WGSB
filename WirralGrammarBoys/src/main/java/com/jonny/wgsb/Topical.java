package com.jonny.wgsb;

class Topical {
    Integer id, red;
    String title, story;

    Topical() {
    }

    Topical(Integer id, String title, String story, Integer red) {
        this.id = id;
        this.title = title;
        this.story = story;
        this.red = red;
    }
}