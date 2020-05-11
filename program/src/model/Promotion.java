package model;

import java.util.Date;

public class Promotion {

    private String proId;
    private String proName;
    private String proDetail;
    private Date proStart;
    private Date proEnd;
    private int proPoint;
    private String proType;

    public Promotion(String proId, String proName, String proDetail, Date proStart, Date proEnd, int proPoint, String proType) {
        this.proId = proId;
        this.proName = proName;
        this.proDetail = proDetail;
        this.proStart = proStart;
        this.proEnd = proEnd;
        this.proPoint = proPoint;
        this.proType = proType;
    }

    public String getProId() {
        return proId;
    }

    public String getProName() {
        return proName;
    }

    public String getProDetail() {
        return proDetail;
    }

    public Date getProStart() {
        return proStart;
    }

    public Date getProEnd() {
        return proEnd;
    }

    public int getProPoint() {
        return proPoint;
    }

    public String getProType() {
        return proType;
    }
}
