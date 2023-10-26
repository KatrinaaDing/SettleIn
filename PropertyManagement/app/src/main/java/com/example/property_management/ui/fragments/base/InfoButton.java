package com.example.property_management.ui.fragments.base;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.google.android.material.button.MaterialButton;

public class InfoButton extends FrameLayout {

    private String title;
    private String content;

    public InfoButton(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_info_button, this, true);
        MaterialButton button = findViewById(R.id.infoBtn);

        button.setOnClickListener(v -> {
            Log.i("info-button", "clicked");
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                InfoDialog dialog = new InfoDialog(title, content);
                dialog.show(activity.getSupportFragmentManager(), "Hint dialog");
            }
        });
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
