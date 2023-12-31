package com.example.property_management.ui.fragments.base;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.content.ContextCompat;
import com.example.property_management.R;
import com.google.android.material.snackbar.Snackbar;

public class BasicSnackbar {

    private int duration = Snackbar.LENGTH_SHORT;
    private View view;
    private String message;
    private String type = "";

    public BasicSnackbar(View view, String message) {
        this.view = view;
        this.message = message;
        showSnackbar();
    }
    public BasicSnackbar(View view, String message, String type) {
        this.view = view;
        this.message = message;
        this.type = type;
        showSnackbar();
    }

    public BasicSnackbar(View view, String message, String type, int duration) {
        this.view = view;
        this.message = message;
        this.type = type;
        this.duration = duration;
        showSnackbar();
    }

    private void showSnackbar() {
        try {
            Snackbar snackbar = Snackbar.make(view, message, duration);
            View snackbarView = snackbar.getView();

            ViewGroup.LayoutParams layoutParams = snackbarView.getLayoutParams();
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                // appears at the bottom by default
                params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                // set margin
                params.setMargins(20, 0, 20, 200); // Last parameter is the distance from the bottom edge
                snackbarView.setLayoutParams(params);
                // set color
                if (type.equals("success")) {
                    snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.success));
                } else if (type.equals("error")) {
                    snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.error));
                } else if (type.equals("warning")) {
                    snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.warning));
                }

            } else if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbarView.getLayoutParams();
                // Set the margins if needed
                params.setMargins(20, 0, 20, 300); // Example margins
                if ("success".equals(type)) {
                    snackbarView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.success));
                } else if ("error".equals(type)) {
                    snackbarView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.error));
                } else if ("info".equals(type)) {
                    snackbarView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.warning));
                }
                snackbarView.setLayoutParams(params);
            }


            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
