package com.example.property_management.sensors;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

public class CompassSensor implements SensorEventListener {

    private SensorCallback callback;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;
    long lastUpdatedTime = 0;

    private boolean isRecording;

    public CompassSensor(Context context, SensorCallback callback) {
        //super(callback);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.callback = callback;
    }

    private Handler handler = new Handler();
    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopTest();
        }
    };

    // sensor rotate
    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        if(sensorEvent.sensor == accelerometerSensor){
            System.arraycopy(sensorEvent.values, 0, lastAccelerometer, 0, sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        } else if(sensorEvent.sensor == magnetometerSensor) {
            System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }
        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthRadians = orientation[0];
            float azimuthInDegree = (float) Math.toDegrees(azimuthRadians);
            if (azimuthInDegree < 0.0f) {
                azimuthInDegree += 360.0f;
            }

            int x = (int) azimuthInDegree;
            String direction = getDirection(azimuthInDegree);
            if (callback != null) {
                float combinedValue = x + getDirectionDecimal(direction);
                callback.onSensorDataChanged("Compass", combinedValue);
            }

            lastUpdatedTime = System.currentTimeMillis();
        }
    }

    public void startTest() {
        isRecording = true;
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
        handler.postDelayed(stopRunnable, 3000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (isRecording && count < 30) {
                    Log.d("isCompassRecording","true");
                    count++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isRecording) { // 只有当仍在记录时才更新平均值
                    Log.d("Final compass stop","called");
                    stopTest();
                }
            }
        }).start();

    }

    public void stopTest() {
        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, magnetometerSensor);
        handler.removeCallbacks(stopRunnable);
        callback.onCompassTestCompleted();
    }

    private String getDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "E";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "SW";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }

    private float getDirectionDecimal(String direction) {
        switch (direction) {
            case "N": return 0.01f;
            case "NE": return 0.02f;
            case "E": return 0.03f;
            case "SE": return 0.04f;
            case "S": return 0.05f;
            case "SW": return 0.06f;
            case "W": return 0.07f;
            case "NW": return 0.08f;
            default: return 0f;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
        // Handle sensor accuracy changes if needed
    }

    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }
}
