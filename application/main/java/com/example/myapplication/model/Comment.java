package com.example.myapplication.model;

import java.util.Date;

public class Comment {

    private String cusId;
    private String comment;
    private double rating;
    private Date time;

    public Comment(String cusId, String comment, double rating, Date time) {
        this.cusId = cusId;
        this.comment = comment;
        this.rating = rating;
        this.time = time;
    }

    public String getCusId() {
        return cusId;
    }

    public String getComment() {
        return comment;
    }

    public double getRating() {
        return rating;
    }

    public Date getTime() {
        return time;
    }
}
