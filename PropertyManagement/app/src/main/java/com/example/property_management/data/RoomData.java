package com.example.property_management.data;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class RoomData implements Serializable {
    private float brightness;
    private float noise;
    private String windowOrientation;
    private ArrayList<String> images;

    public RoomData() {};

    public RoomData(float brightness, float noise, String windowOrientation, ArrayList<String> images) {
        this.brightness = brightness;
        this.noise = noise;
        this.windowOrientation = windowOrientation;
        this.images = images;
    }

    public float getBrightness() { return brightness; }

    public void setBrightness(float brightness) { this.brightness = brightness; }

    public float getNoise() { return noise; }

    public void setNoise(float noise) { this.noise = noise; }

    public String getWindowOrientation() { return windowOrientation; }

    public void setWindowOrientation(String windowOrientation) { this.windowOrientation = windowOrientation; }

    public ArrayList<String> getImages() { return images; }

    public void setImage(ArrayList<String> images) { this.images = images; }

    @NonNull
    @Override
    public String toString() {
        return "\n{brightness: " + this.brightness + "\nnoise: " + this.noise + "\nwindowOrientation: " + this.windowOrientation + "\nimages: " + this.images.toString() + "}";
    }


}
