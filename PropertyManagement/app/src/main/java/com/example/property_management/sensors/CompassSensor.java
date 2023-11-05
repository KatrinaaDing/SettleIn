package com.example.property_management.sensors;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

/**
 * This class represents a compass sensor listener that utilizes the device's accelerometer and magnetometer
 * to determine the device's orientation relative to the Earth's magnetic field.
 */
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

    /**
     * Constructor for CompassSensor.
     * @param context  the application context
     * @param callback the callback to notify with sensor data
     */
    public CompassSensor(Context context, SensorCallback callback) {
        //super(callback);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.callback = callback;
    }

    // Handler to schedule tasks on the main thread
    private Handler handler = new Handler();
    // Runnable to stop the compass test after a delay
    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopTest();
        }
    };

    /**
     * Callback method that receives sensor updates.
     * @param sensorEvent the event containing the sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        // Check for accelerometer or magnetometer data
        if(sensorEvent.sensor == accelerometerSensor){
            // Copy accelerometer data for later use
            System.arraycopy(sensorEvent.values, 0, lastAccelerometer, 0, sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        } else if(sensorEvent.sensor == magnetometerSensor) {
            // Copy magnetometer data for later use
            System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }
        // Check if both accelerometer and magnetometer data are ready and it's time to update
        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 250){
            // Calculate the rotation matrix and orientation
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Convert the azimuth from radians to degrees and normalize it
            float azimuthRadians = orientation[0];
            float azimuthInDegree = (float) Math.toDegrees(azimuthRadians);
            if (azimuthInDegree < 0.0f) {
                azimuthInDegree += 360.0f;
            }

            // Determine the compass direction
            int x = (int) azimuthInDegree;
            String direction = getDirection(azimuthInDegree);
            // Notify the callback with the direction and the numeric degree value
            if (callback != null) {
                float combinedValue = x + getDirectionDecimal(direction);
                callback.onSensorDataChanged("Compass", combinedValue);
            }

            // Update the timestamp of the last sensor update
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
