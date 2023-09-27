package com.example.property_management.callbacks;

public interface SensorCallback {
    void onSensorDataChanged(String sensorType, float value);
}
