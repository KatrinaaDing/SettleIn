package com.example.property_management.data;

public class NewProperty {
    private String url;
    private int bedroomNum;
    private int bathroomNum;
    private int parkingNum;
    private String address;
    private float lat;
    private float lng;

    public NewProperty(String url, int bedroomNum, int bathroomNum, int parkingNum, String address, float lat, float lng) {
        this.url = url;
        this.bedroomNum = bedroomNum;
        this.bathroomNum = bathroomNum;
        this.parkingNum = parkingNum;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public Property castToProperty() {
        Property newPropertyObj = new Property();
        newPropertyObj.setHref(this.getUrl());
        newPropertyObj.setNumBedrooms(this.getBedroomNum());
        newPropertyObj.setNumBathrooms(this.getBathroomNum());
        newPropertyObj.setNumParking(this.getParkingNum());
        newPropertyObj.setAddress(this.getAddress());
        newPropertyObj.setLat(this.getLat());
        newPropertyObj.setLng(this.getLng());
        return newPropertyObj;
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
    public float getLat() {
        return lat;
    }
    public float getLng() {
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
    public void setLat(float lat) {
        this.lat = lat;
    }
    public void setLng(float lng) {
        this.lng = lng;
    }

}
