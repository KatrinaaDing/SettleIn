package com.example.property_management.sensors;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.property_management.callbacks.SensorCallback;

import java.util.List;

public class LocationSensor {
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    private Activity activity;
    private SensorCallback callback;
    private boolean hasPermission = false;

    public LocationSensor(Activity activity, SensorCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public boolean hasPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requiresPermissions(Activity activity) {
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

}
