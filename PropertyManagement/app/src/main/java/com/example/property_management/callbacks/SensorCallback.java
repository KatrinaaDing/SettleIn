package com.example.property_management.callbacks;

public interface SensorCallback {
    void onSensorDataChanged(String sensorType, float value);
    void onCurrentDbCalculated(double currentDb);
    void onAverageDbCalculated(double averageDb);
    void onAudioTestCompleted ();
    void onLightTestCompleted ();
    void onCompassTestCompleted ();




}
