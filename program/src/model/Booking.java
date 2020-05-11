package model;

import java.util.Date;

public class Booking {
    private String bookingCode;
    private Date bookingEnd;
    private Date bookingStart;
    private String gameId;
    private String group;
    private String userId;
    private String proId;
    private String receiptStatus;
    private String usageStatus;

    public Booking(String bookingCode, Date bookingEnd, Date bookingStart, String gameId, String group, String userId, String proId, String receiptStatus, String usageStatus) {
        this.bookingCode = bookingCode;
        this.bookingEnd = bookingEnd;
        this.bookingStart = bookingStart;
        this.gameId = gameId;
        this.group = group;
        this.userId = userId;
        this.proId = proId;
        this.receiptStatus = receiptStatus;
        this.usageStatus = usageStatus;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Date getBookingEnd() {
        return bookingEnd;
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

    public String getUserId() {
        return userId;
    }

    public String getProId() {
        return proId;
    }

    public String getReceiptStatus() {
        return receiptStatus;
    }

    public String getUsageStatus() {
        return usageStatus;
    }

    public void setBookingEnd(Date bookingEnd) {
        this.bookingEnd = bookingEnd;
    }

    public void setBookingStart(Date bookingStart) {
        this.bookingStart = bookingStart;
    }

    public void setReceiptStatus(String receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public void setUsageStatus(String usageStatus) {
        this.usageStatus = usageStatus;
    }
}


