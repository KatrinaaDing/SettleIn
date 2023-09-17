package com.example.property_management.callbacks;


import com.example.property_management.data.Property;

public interface GetPropertyByIdCallback {
    void onSuccess(Property property);
    void onError(Exception e);
}
