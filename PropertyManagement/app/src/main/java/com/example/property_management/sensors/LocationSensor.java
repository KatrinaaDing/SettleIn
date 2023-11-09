package com.example.property_management.sensors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.example.property_management.callbacks.SensorCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

/**
 * LocationSensor class to get the current location
 */
public class LocationSensor {
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    private Activity activity;
    private SensorCallback callback;
    private boolean hasPermission = false;
    private FusedLocationProviderClient fusedLocationClient;

    public LocationSensor(Activity activity, SensorCallback callback) {
        this.activity = activity;
        this.callback = callback;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
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

    @SuppressLint("MissingPermission")
    public Task<Location> getCurrentLocation() {
        if (hasPermission(activity)) {
            return fusedLocationClient.getLastLocation();
        }
        return null;
    }

}
