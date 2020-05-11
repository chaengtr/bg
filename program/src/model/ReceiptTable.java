package model;

public class ReceiptTable {
    private String id;
    private String date;
    private String price;
    private String discount;
    private String amount;

    public ReceiptTable(String id, String date, String price, String discount, String amount) {
        this.id = id;
        this.date = date;
        this.price = price;
        this.discount = discount;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscount() {
        return discount;
    }

    public String getAmount() {
        return amount;
    }
}