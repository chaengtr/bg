package com.example.myapplication.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;

public class TimeDisplay {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private Date date;
    private long time;
    private int milliSecInHour, milliSecInDay;

    public TimeDisplay(Date date) {
        this.date = date;
        this.time = new Date().getTime() - date.getTime();
        milliSecInHour = 1000 * 60 * 60;
        milliSecInDay = milliSecInHour * 24;
    }

    public String getDisplayDate() {
        String output;
        if (time < TimeUnit.MILLISECONDS.convert(1, HOURS)) {
            output = "เมื่อสักครู่";
        } else if (time < (milliSecInHour * 24)) {
            int hour = (int) time / milliSecInHour;
            output = hour + " ชั่วโมงที่แล้ว";
        } else if (time < (milliSecInDay * 7)) {
            int day = (int) time / milliSecInDay;
            output = day + " วันที่แล้ว";
        } else {
            output = dateFormat.format(date);
        }
        return output;
    }
}
