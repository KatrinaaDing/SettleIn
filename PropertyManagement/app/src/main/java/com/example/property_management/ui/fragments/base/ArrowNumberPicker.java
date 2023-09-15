package com.example.property_management.ui.fragments.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.property_management.R;
import com.example.property_management.callbacks.onIntegerChangeCallback;
import com.google.android.material.button.MaterialButton;

public class ArrowNumberPicker extends LinearLayout {
    private int value;
    private int direction;
    private onIntegerChangeCallback callback;

    public ArrowNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_arrow_number_picker, this, true);

        // receive attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArrowNumberPicker);
        try {
            direction = a.getInteger(R.styleable.ArrowNumberPicker_direction, 0);
            handleAttribute(direction);
        } finally {
            a.recycle();
        }

        // Initialize child views here if needed
        MaterialButton buttonUp = findViewById(R.id.buttonUp);
        TextView textViewNumber = findViewById(R.id.textViewNumber);
        MaterialButton buttonDown = findViewById(R.id.buttonDown);

        // Set values to child views based on `value` and `direction`
        textViewNumber.setText(String.valueOf(value));
        // Do something with direction if needed

        // ======================================== Listeners =====================================
        buttonUp.setOnClickListener(view -> {
            if (this.callback != null && value < 10) {
                value++;
                textViewNumber.setText(String.valueOf(value));
                this.callback.onIncrease(value);
            }
        });
        buttonDown.setOnClickListener(view -> {
            if (this.callback != null && value > 0) {
                value--;
                textViewNumber.setText(String.valueOf(value));
                this.callback.onIncrease(value);
            }
        });
    }

    public void handleAttribute(int direction) {
        LinearLayout layout = findViewById(R.id.layout);
        if (direction == 0) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
        } else if (direction == 1) {
            layout.setOrientation(LinearLayout.VERTICAL);
        } else {
            throw new IllegalArgumentException("Invalid 'direction' attribute value. It must be either 'horizontal' or 'vertical'.");
        }
    }
    public void setOnValueChangeListener(onIntegerChangeCallback callback) {
        this.callback = callback;
    }
}
