package com.example.property_management.ui.fragments.property;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.property_management.R;

public class AmenitiesGroup extends LinearLayout {
    private int bedrooms = 0;
    private int bathrooms = 0;
    private int parkings = 0;

    public AmenitiesGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_amenities_group, this, true);
    }
    public void setValues(int bedrooms, int bathrooms, int parkings) {
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.parkings = parkings;
        setTexts();
    }
    private void setTexts() {
        // set texts
         TextView bathroomsText = findViewById(R.id.bathroomTextView);
         bathroomsText.setText(String.valueOf(this.bathrooms));
         TextView bedroomsText = findViewById(R.id.bedroomTextView);
         bedroomsText.setText(String.valueOf(this.bedrooms));
         TextView parkingsText = findViewById(R.id.parkingTextView);
         parkingsText.setText(String.valueOf(this.parkings));
    }

}
