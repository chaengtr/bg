package model;

import java.text.SimpleDateFormat;

public class BookingTable {
    private String bookingCode;
    private String userName;
    private String gameName;
    private String bookingStart;
    private String bookingEnd;
    private String usageStatus;
    private String receiptStatus;
    private String proName;

    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    public BookingTable(Booking booking, String userName, String gameName, String proName) {
        this.bookingCode = booking.getBookingCode();
        this.userName = userName;
        this.gameName = gameName;
        this.bookingStart = df.format(booking.getBookingStart());
        if (booking.getBookingEnd() == null) {
            this.bookingEnd = "";
        } else {
            this.bookingEnd = df.format(booking.getBookingEnd());
        }
        this.usageStatus = booking.getUsageStatus();
        this.receiptStatus = booking.getReceiptStatus();
        this.proName = proName;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getGameName() {
        return gameName;
    }

    public String getBookingStart() {
        return bookingStart;
    }

    public String getBookingEnd() {
        return bookingEnd;
    }

    public String getUsageStatus() {
        return usageStatus;
    }

    public String getReceiptStatus() {
        return receiptStatus;
    }

    public String getProName() {
        return proName;
    }
}
