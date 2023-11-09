package com.example.property_management.callbacks;

import com.google.firebase.auth.FirebaseUser;

/**
 * Callback for Firebase authentication
 */
public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(Exception e);
}
