package com.example.property_management.ui.fragments.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.property_management.R;
import com.example.property_management.callbacks.onValueChangeCallback;
import com.google.android.material.button.MaterialButton;

public class ArrowNumberPicker extends LinearLayout {
    private int value;
    private int direction;
    private onValueChangeCallback callback;

    public ArrowNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_arrow_number_picker, this, true);

        // receive attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArrowNumberPicker);
        try {
            value = a.getInteger(R.styleable.ArrowNumberPicker_value, 0);
            direction = a.getInteger(R.styleable.ArrowNumberPicker_direction, 0);
            handleAttribute(value, direction);
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
            // max value is 9
            if (this.callback != null && this.value < 9) {
                this.value++;
                textViewNumber.setText(String.valueOf(this.value));
                this.callback.onChange(this.value);
            }
        });
        buttonDown.setOnClickListener(view -> {
            if (this.callback != null && this.value > 0) {
                this.value--;
                textViewNumber.setText(String.valueOf(this.value));
                this.callback.onChange(this.value);
            }
        });
    }

    public void handleAttribute(int value, int direction) {
        // set initial value
        TextView textViewNumber = findViewById(R.id.textViewNumber);
        textViewNumber.setText(String.valueOf(value));

        // set direction
        LinearLayout layout = findViewById(R.id.layout);
        if (direction == 0) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
        } else if (direction == 1) {
            layout.setOrientation(LinearLayout.VERTICAL);
        } else {
            throw new IllegalArgumentException("Invalid 'direction' attribute value. It must be either 'horizontal' or 'vertical'.");
        }
    }
    public void setOnValueChangeListener(onValueChangeCallback callback) {
        this.callback = callback;
    }
    public void setValue(int value) {
        this.value = value;
        TextView textViewNumber = findViewById(R.id.textViewNumber);
        textViewNumber.setText(String.valueOf(this.value));
    }
}
