package com.example.myapplication.model;

import java.util.Date;

public class Notification {
    private Date date;
    private String detail, image, title;

    public Notification() {}

    public Notification(Date date, String detail, String image, String title) {
        this.date = date;
        this.detail = detail;
        this.image = image;
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public String getDetail() {
        return detail;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}
