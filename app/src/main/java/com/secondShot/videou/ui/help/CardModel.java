package com.secondShot.videou.ui.help;

public class CardModel {

    private String title;
    private String content;

    // Constructor
    public CardModel(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Getter and Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}