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

    //trace the light value got in 3 seconds
    private float totalValue = 0;
    private int readingCount = 0;

    public LightSensor(Context context, SensorCallback callback) {
        //super(callback);
        this.callback = callback;  // 初始化 callback
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

    }


    public void startTest() {
        totalValue = 0;
        readingCount = 0;
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);  // 开始传感器读取
        // 创建一个新线程来处理3秒后的停止测试
        new Thread(() -> {
            try {
                Thread.sleep(3000);  // 等待3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopTest();  // 停止测试
        }).start();
    }

    public void stopTest() {
        sensorManager.unregisterListener(this);  // 停止传感器读取
        float averageValue = totalValue / readingCount;
        String formattedAverageValue = String.format(Locale.US, "%.2f", averageValue);  //format the averageValue
        if (callback != null) {
            callback.onSensorDataChanged("Light", Float.valueOf(formattedAverageValue));  // 发送平均值
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float lightValue = sensorEvent.values[0];
        totalValue += lightValue;
        readingCount++;
        if (callback != null) {
            callback.onSensorDataChanged("Light", lightValue);  // 发送当前值
        }
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }


    /**
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
    **/
}