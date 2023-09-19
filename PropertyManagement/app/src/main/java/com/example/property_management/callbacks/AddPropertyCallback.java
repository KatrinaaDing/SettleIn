package com.example.property_management.callbacks;

import com.example.property_management.data.Property;

public interface AddPropertyCallback {
    void onSuccess(String documentId);
    void onError(String msg);
}
