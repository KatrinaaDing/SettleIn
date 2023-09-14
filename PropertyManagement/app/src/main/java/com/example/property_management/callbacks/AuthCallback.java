package com.example.property_management.callbacks;

import com.google.firebase.auth.FirebaseUser;

public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(Exception e);
}
