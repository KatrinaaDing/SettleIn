package com.example.property_management.callbacks;

public interface AddUserCallback {
    void onSuccess(String documentId);
    void onError(String msg);
}
