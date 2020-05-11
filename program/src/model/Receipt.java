package model;

import java.util.Date;

public class Receipt {
    private String id;
    private Date date, start, end;
    private String user;
    private String game;
    private String promotion;
    private double price;
    private double discount;
    private double amount;
    private int hour;
    private int point;
    private String bookingCode;

    public Receipt(String id, Date date, String user, String game, String promotion, double price, double discount, double amount, int hour, int point, String bookingCode, Date start, Date end) {
        this.id = id;
        this.date = date;
        this.user = user;
        this.game = game;
        this.promotion = promotion;
        this.price = price;
        this.discount = discount;
        this.amount = amount;
        this.hour = hour;
        this.point = point;
        this.bookingCode = bookingCode;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public String getGame() {
        return game;
    }

    public String getPromotion() {
        return promotion;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscount() {
        return discount;
    }

    public double getAmount() {
        return amount;
    }

    public int getHour() {
        return hour;
    }

    public int getPoint() {
        return point;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
