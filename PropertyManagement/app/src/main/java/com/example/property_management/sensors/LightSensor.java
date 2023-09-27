package com.example.property_management.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.property_management.callbacks.SensorCallback;

import java.util.Collections;
import java.util.List;

public class LightSensor extends MeasurableSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorCallback callback;
    private float changedValue;

    public LightSensor(Context context, SensorCallback callback) {
        super(callback);
        this.callback = callback;  // 初始化 callback
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float lightValue = sensorEvent.values[0];
        if (callback != null) {
            callback.onSensorDataChanged("Light", lightValue);  // 调用 callback 方法
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }

    @Override
    public List<Float> onSensorValuesChanged() {
        // Return the latest sensor values
        return Collections.singletonList(changedValue);
    }

    @Override
    public void onSensorAccuracyChanged(int accuracy) {
        // Handle sensor accuracy changes if needed
    }

    @Override
    public boolean hasSensor() {
        return lightSensor != null;
    }

    @Override
    public void start() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public SensorManager requiresPermissions() {
        // Optionally, check and request necessary permissions
        return sensorManager;
    }
}