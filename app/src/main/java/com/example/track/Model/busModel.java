package com.example.track.Model;

public class busModel {
    private String BusName,BusNumber,BusRoute,BusDocID,BusImage="",CurrentUserID;
    private double BusLongitude,BusLatitude;

    public busModel() {
    }

    public busModel(String busName, String busNumber, String busRoute, double busLongitude,
                    double busLatitude, String busDocID, String busImage, String currentUserID) {
        BusName = busName;
        BusNumber = busNumber;
        BusRoute = busRoute;
        BusLongitude = busLongitude;
        BusLatitude = busLatitude;
        BusDocID = busDocID;
        BusImage = busImage;
        CurrentUserID = currentUserID;
    }

    public String getBusName() {
        return BusName;
    }

    public void setBusName(String busName) {
        BusName = busName;
    }

    public String getBusNumber() {
        return BusNumber;
    }

    public void setBusNumber(String busNumber) {
        BusNumber = busNumber;
    }

    public String getBusRoute() {
        return BusRoute;
    }

    public void setBusRoute(String busRoute) {
        BusRoute = busRoute;
    }

    public double getBusLongitude() {
        return BusLongitude;
    }

    public void setBusLongitude(double busLongitude) {
        BusLongitude = busLongitude;
    }

    public double getBusLatitude() {
        return BusLatitude;
    }

    public void setBusLatitude(double busLatitude) {
        BusLatitude = busLatitude;
    }

    public String getBusDocID() {
        return BusDocID;
    }

    public void setBusDocID(String busDocID) {
        BusDocID = busDocID;
    }

    public String getBusImage() {
        return BusImage;
    }

    public void setBusImage(String busImage) {
        BusImage = busImage;
    }

    public String getCurrentUserID() {
        return CurrentUserID;
    }

    public void setCurrentUserID(String currentUserID) {
        CurrentUserID = currentUserID;
    }
}
