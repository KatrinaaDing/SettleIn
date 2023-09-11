package com.example.property_management.api.callbacks;

import com.google.firebase.auth.FirebaseUser;

public interface AuthCallback {
    public void onSuccess(FirebaseUser user);
    public void onFailure(Exception e);
}
