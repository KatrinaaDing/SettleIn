package com.example.property_management.data;

public class NewProperty {
    private String url;
    private int bedroomNum;
    private int bathroomNum;
    private int parkingNum;
    private String address;
    private double lat;
    private double lng;

    public NewProperty(String url, int bedroomNum, int bathroomNum, int parkingNum, String address, double lat, double lng) {
        this.url = url;
        this.bedroomNum = bedroomNum;
        this.bathroomNum = bathroomNum;
        this.parkingNum = parkingNum;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    // getters
    public String getUrl() {
        return url;
    }
    public int getBedroomNum() {
        return bedroomNum;
    }
    public int getBathroomNum() {
        return bathroomNum;
    }
    public int getParkingNum() {
        return parkingNum;
    }
    public String getAddress() {
        return address;
    }
    public double getLat() {
        return lat;
    }
    public double getLng() {
        return lng;
    }
    // setters
    public void setUrl(String url) {
        this.url = url;
    }
    public void setBedroomNum(int bedroomNum) {
        this.bedroomNum = bedroomNum;
    }
    public void setBathroomNum(int bathroomNum) {
        this.bathroomNum = bathroomNum;
    }
    public void setParkingNum(int parkingNum) {
        this.parkingNum = parkingNum;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
}
