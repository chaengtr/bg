package model;

import java.util.Date;

public class Banner {

    private String bannerId;
    private String bannerName;
    private String imageName;
    private String imageRef;
    private Date bannerStart;
    private Date bannerEnd;

    public Banner(String bannerId, String bannerName, String imageName, String imageRef, Date bannerStart, Date bannerEnd) {
        this.bannerId = bannerId;
        this.bannerName = bannerName;
        this.imageName = imageName;
        this.imageRef = imageRef;
        this.bannerStart = bannerStart;
        this.bannerEnd = bannerEnd;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getBannerName() {
        return bannerName;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageRef() {
        return imageRef;
    }

    public Date getBannerStart() {
        return bannerStart;
    }

    public Date getBannerEnd() {
        return bannerEnd;
    }
}
