package com.example.property_management.sensors;

import android.hardware.SensorManager;

import java.util.List;

abstract public class MeasurableSensor {

    abstract public List<Float> onSensorValuesChanged();
    abstract public void onSensorAccuracyChanged(int accuracy);
    abstract public boolean hasSensor();
    abstract public SensorManager requiresPermissions();
    abstract public void start();
    abstract public void stop();

}
