package com.example.myapplication.model;

import java.sql.Timestamp;

public class Customer {
    private String cusUsername, email, cusPassword, cusImage;
    private int point;
    private Timestamp lastActivePoint;

    public Customer(){}

    public Customer(String cusUsername, String email, String cusPassword, int point, Timestamp lastActivePoint, String cusImage) {
        this.cusUsername = cusUsername;
        this.email = email;
        this.cusPassword = cusPassword;
        this.point = point;
        this.lastActivePoint = lastActivePoint;
        this.cusImage = cusImage;
    }

    public String getCusUsername() {
        return cusUsername;
    }

    public String getEmail() {
        return email;
    }

    public String getCusPassword() {
        return cusPassword;
    }

    public int getPoint() {
        return point;
    }

    public Timestamp getLastActivePoint() {
        return lastActivePoint;
    }

    public String getCusImage() {
        return cusImage;
    }
}
