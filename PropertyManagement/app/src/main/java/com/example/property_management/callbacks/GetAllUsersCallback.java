package com.example.property_management.callbacks;


import com.example.property_management.data.User;

import java.util.ArrayList;

public interface GetAllUsersCallback {
    void onSuccess(ArrayList<User> users);
    void onError(String msg);
}
