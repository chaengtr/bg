package com.example.myapplication.model;

import java.sql.Timestamp;

public class Promotion {
    private String proDetail, proName;
    private Timestamp proEnd, proStart;
    private int proPoint;

    public Promotion(){

    }

    public Promotion(String proDetail, String proName, Timestamp proEnd, Timestamp proStart, int proPoint) {
        this.proDetail = proDetail;
        this.proName = proName;
        this.proEnd = proEnd;
        this.proStart = proStart;
        this.proPoint = proPoint;
    }

    public String getProDetail() {
        return proDetail;
    }

    public String getProName() {
        return proName;
    }

    public Timestamp getProEnd() {
        return proEnd;
    }

    public Timestamp getProStart() {
        return proStart;
    }

    public int getProPoint() {
        return proPoint;
    }
}
