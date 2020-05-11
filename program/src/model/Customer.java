package model;

import java.util.Date;

public class Customer {
    private String cusId;
    private String cusUsername;
    private String email;
    private int point;
    private Date lastActivePoint;

    public Customer(String cusId, String cusUsername, String email, int point, Date lastActivePoint) {
        this.cusId = cusId;
        this.cusUsername = cusUsername;
        this.email = email;
        this.point = point;
        this.lastActivePoint = lastActivePoint;
    }

    public String getCusId() {
        return cusId;
    }

    public String getCusUsername() {
        return cusUsername;
    }

    public String getEmail() {
        return email;
    }

    public int getPoint() {
        return point;
    }

    public Date getLastActivePoint() {
        return lastActivePoint;
    }
}
