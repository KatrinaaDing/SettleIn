package com.example.property_management.callbacks;

import com.example.property_management.data.Property;

import java.util.ArrayList;

public interface GetAllPropertiesCallback {
    void onSuccess(ArrayList<Property> properties);
    void onError(Exception e);
}
