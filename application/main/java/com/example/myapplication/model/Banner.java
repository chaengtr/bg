package com.example.myapplication.model;

import java.util.Date;

public class Banner {

    private String bannerId;
    private String bannerName;
    private String bannerImage;
    private Date bannerStart;
    private Date bannerEnd;

    public Banner(String bannerId, String bannerName, String bannerImage, Date bannerStart, Date bannerEnd) {
        this.bannerId = bannerId;
        this.bannerName = bannerName;
        this.bannerImage = bannerImage;
        this.bannerStart = bannerStart;
        this.bannerEnd = bannerEnd;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getBannerName() {
        return bannerName;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public Date getBannerStart() {
        return bannerStart;
    }

    public Date getBannerEnd() {
        return bannerEnd;
    }
}
