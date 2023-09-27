package com.example.property_management.utils;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Helpers {

    /**
     * Close the keyboard
     * @param context the related context
     * @param view the view that is currently focused (usually the input layout)
     * @example Helpers.closeKeyboard(this, editTextLayout);
     */
    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
