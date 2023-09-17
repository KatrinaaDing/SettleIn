package com.example.property_management.callbacks;

import com.example.property_management.data.User;

public interface GetUserInfoByIdCallback {
    void onSuccess(User user);
    void onError(Exception e);
}
