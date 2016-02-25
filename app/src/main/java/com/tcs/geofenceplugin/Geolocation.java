package com.tcs.geofenceplugin;

import java.io.Serializable;

/**
 * Created by kaustav on 10/15/2015.
 */
public class Geolocation implements Serializable {

    int triggerID;
    int userID;

    String tokenID;

    String place;
    double latitude;
    double longitude;
int expires;



    int radius;

    String notificationText;

    String startDate;
    String endDate;

    String status;

    public Geolocation(int triggerID, int userID, String tokenID, String place, double latitude, double longitude, int radius, String notificationText, String startDate, String endDate, String status,int expires) {
        this.triggerID = triggerID;
        this.userID = userID;
        this.tokenID = tokenID;
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.notificationText = notificationText;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.expires=expires;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public int getTriggerID() {
        return triggerID;
    }

    public void setTriggerID(int triggerID) {
        this.triggerID = triggerID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
