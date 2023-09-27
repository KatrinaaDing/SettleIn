package com.example.property_management.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.property_management.callbacks.SensorCallback;

import java.util.List;

public class SensorManagerClass {

    private LightSensor lightSensor;
    private SensorCallback callback;
    private CompassSensor compassSensor;

    public SensorManagerClass(Context context, SensorCallback callback) {
        this.callback = callback;  // 初始化 callback
        lightSensor = new LightSensor(context, callback);  // 传递 callback 到 LightSensor
        compassSensor = new CompassSensor(context, callback);
    }

    public void startAllSensors() {

        lightSensor.start();
        compassSensor.start();
    }

    public void stopAllSensors() {

        lightSensor.stop();
        compassSensor.stop();

    }

    public List<Float> getLightSensorValues() {

        return lightSensor.onSensorValuesChanged();
    }
}