package com.example.property_management.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.property_management.callbacks.SensorCallback;

import java.util.List;

abstract public class MeasurableSensor implements SensorEventListener {
    protected SensorCallback callback;

    public MeasurableSensor(SensorCallback callback) {
        this.callback = callback;
    }

    public abstract void onSensorChanged(SensorEvent sensorEvent);

    public abstract void onAccuracyChanged(Sensor sensor, int accuracy);

    abstract public List<Float> onSensorValuesChanged();
    abstract public void onSensorAccuracyChanged(int accuracy);
    abstract public boolean hasSensor();
    abstract public SensorManager requiresPermissions();
    abstract public void start();
    abstract public void stop();

}
