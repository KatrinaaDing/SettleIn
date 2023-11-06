package com.example.property_management.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

import java.util.Locale;

/**
 * This class represents a light sensor listener that tracks light intensity using the device's light sensor.
 * It calculates the average light intensity over a period of time and notifies a callback interface.
 */
public class LightSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorCallback callback;
    private float changedValue;
    private float totalValue = 0;
    private int readingCount = 0;
    private boolean isRecording;

    /**
     * Constructor for LightSensor.
     * @param context  the application context
     * @param callback the callback to notify with sensor data
     */
    public LightSensor(Context context, SensorCallback callback) {
        //super(callback);
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    /**
     * Starts the light sensor test by registering the listener and initializing values.
     */
    public void startTest() {
        // Reset the recording state and values
        totalValue = 0;
        readingCount = 0;
        isRecording = true;

        // Register the light sensor listener
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Start a new thread to track the recording duration
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                // Loop for a specified duration or until recording stops
                while (isRecording && count < 30) {
                    Log.d("isLightRecording", "true");
                    count++;
                    try {
                        // Pause for a short duration before the next reading
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Stop the test when recording is still true after the loop
                if (isRecording) {
                    Log.d("Final light stop", "called");
                    stopTestFull();
                }
            }
        }).start();
    }

    public void stopTestFull() {
        // Mark recording as complete and unregister the sensor listener
        isRecording = false;
        sensorManager.unregisterListener(this);

        // Calculate the average value from the total sum of readings and the count
        float averageValue = totalValue / readingCount;

        // Format the average value to two decimal places
        String formattedAverageValue = String.format(Locale.US, "%.2f", averageValue);

        // Notify the callback with the average light data
        if (callback != null) {
            callback.onSensorDataChanged("Light", Float.valueOf(formattedAverageValue));
            callback.onLightTestCompletedFull();
        }
    }


    /**
     * Stops the light sensor test, calculates the average light value, and notifies the callback.
     */
    public void stopTest() {
        // Mark recording as complete and unregister the sensor listener
        isRecording = false;
        sensorManager.unregisterListener(this);

        // Calculate the average value from the total sum of readings and the count
        float averageValue = totalValue / readingCount;

        // Format the average value to two decimal places
        String formattedAverageValue = String.format(Locale.US, "%.2f", averageValue);

        // Notify the callback with the average light data
        if (callback != null) {
            callback.onSensorDataChanged("Light", Float.valueOf(formattedAverageValue));
            callback.onLightTestCompleted();
        }
    }

    /**
     * Callback method that receives light sensor updates.
     * @param sensorEvent the event containing the sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Read the light value from the sensor event
        float lightValue = sensorEvent.values[0];

        // Accumulate the total light value and increment the reading count
        totalValue += lightValue;
        readingCount++;

        // Notify the callback with the latest light data
        if (callback != null) {
            callback.onSensorDataChanged("Light", lightValue);
        }
    }

    /**
     * Callback method that receives sensor accuracy updates. Not used in this implementation.
     * @param sensor   the sensor whose accuracy has changed
     * @param accuracy the new accuracy of this sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not implemented but can be used to handle sensor accuracy changes
    }

    /**
     * Sets or updates the callback for sensor data changes.
     * @param callback the new callback
     */
    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }

}