package com.example.myapplication.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Booking {
    private String bookingCode;
    private Date bookingStart;
    private String gameId;
    private String group;
    private int memberMax;
    private List<String> customers;

    public Booking() {
    }

    public Booking(String bookingCode, Date bookingStart, String gameId, String group, int memberMax, List<String> customers) {
        this.bookingCode = bookingCode;
        this.bookingStart = bookingStart;
        this.gameId = gameId;
        this.group = group;
        this.memberMax = memberMax;
        this.customers = customers;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Date getBookingStart() {
        return bookingStart;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGroup() {
        return group;
    }

    public int getMemberMax() {
        return memberMax;
    }

    public List<String>  getCustomers() {
        return customers;
    }

    public int getMember() {
        return customers.size();
    }
}
