package com.cyberflow.sparkle.main.widget.calendar;

import androidx.annotation.NonNull;

public class DateBean {
    int year;
    int month;
    int day;

    boolean isThisMonth = true;

    public DateBean(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public boolean isThisMonth() {
        return isThisMonth;
    }

    public void setThisMonth(boolean isThisMonth) {
        this.isThisMonth = isThisMonth;
    }

    public static String getMonthEngStr(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "";
        }
    }

    @NonNull
    @Override
    public String toString() {
        return year + "-" + month + " : " + day;
    }
}
