package com.example.property_management.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

import java.util.Locale;

public class LightSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorCallback callback;
    private float changedValue;

    //trace the light value got in 3 seconds
    private float totalValue = 0;
    private int readingCount = 0;
    private boolean isRecording;

    public LightSensor(Context context, SensorCallback callback) {
        //super(callback);
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

    }

    public void startTest() {
        totalValue = 0;
        readingCount = 0;
        isRecording = true;

        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (isRecording && count < 30) {
                    Log.d("isLightRecording","true");
                    count++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isRecording) { // 只有当仍在记录时才更新平均值
                    Log.d("Final light stop","called");
                    stopTest();
                }
            }
        }).start();

    }

    public void stopTest() {
        isRecording = false;
        sensorManager.unregisterListener(this);
        float averageValue = totalValue / readingCount;
        String formattedAverageValue = String.format(Locale.US, "%.2f", averageValue);
        if (callback != null) {
            callback.onSensorDataChanged("Light", Float.valueOf(formattedAverageValue));
            callback.onLightTestCompleted();
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