package com.example.property_management.data;

import java.util.ArrayList;

public class RoomData {
    private float brightness;
    private float noise;
    private String windowOrientation;
    private ArrayList<String> image;

    public RoomData() {};

    public RoomData(float brightness, float noise, String windowOrientation, ArrayList<String> image) {
        this.brightness = brightness;
        this.noise = noise;
        this.windowOrientation = windowOrientation;
        this.image = image;
    }

    public float getBrightness() { return brightness; }

    public void setBrightness(float brightness) { this.brightness = brightness; }

    public float getNoise() { return noise; }

    public void setNoise(float noise) { this.noise = noise; }

    public String getWindowOrientation() { return windowOrientation; }

    public void setWindowOrientation(String windowOrientation) { this.windowOrientation = windowOrientation; }

    public ArrayList<String> getImage() { return image; }

    public void setImage(ArrayList<String> image) { this.image = image; }
}
