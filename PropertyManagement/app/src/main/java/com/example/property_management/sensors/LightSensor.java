package com.example.property_management.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.property_management.callbacks.SensorCallback;

import java.util.Locale;

public class LightSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorCallback callback;
    private float changedValue;
    private float totalValue = 0;
    private int readingCount = 0;

    public LightSensor(Context context, SensorCallback callback) {
        //super(callback);
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void startTest() {
        totalValue = 0;
        readingCount = 0;
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);  // 开始传感器读取

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopTest();
        }).start();
    }

    public void stopTest() {
        sensorManager.unregisterListener(this);
        float averageValue = totalValue / readingCount;
        String formattedAverageValue = String.format(Locale.US, "%.2f", averageValue);
        if (callback != null) {
            callback.onSensorDataChanged("Light", Float.valueOf(formattedAverageValue));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float lightValue = sensorEvent.values[0];
        totalValue += lightValue;
        readingCount++;
        if (callback != null) {
            callback.onSensorDataChanged("Light", lightValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }

}