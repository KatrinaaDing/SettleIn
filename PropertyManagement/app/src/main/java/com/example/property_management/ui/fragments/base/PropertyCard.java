package com.example.property_management.ui.fragments.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.google.android.material.card.MaterialCardView;

public class PropertyCard extends MaterialCardView {
    private String address = "No Address Available";
    private int price;
    private int thumbnail = R.drawable.property_image_placeholder;
    private boolean isInspected;

    public PropertyCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_property_card, this, true);

        // get amentities group
        AmenitiesGroup amenitiesGroup = findViewById(R.id.amenitiesGroup);

        // set attributes and default values
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PropertyCard);
        try {
            // get amenities values
            int bedrooms = a.getInteger(R.styleable.PropertyCard_bedrooms, 0);
            int bathrooms = a.getInteger(R.styleable.PropertyCard_bathrooms, 0);
            int parkings = a.getInteger(R.styleable.PropertyCard_parkings, 0);
            amenitiesGroup.setValues(bedrooms, bathrooms, parkings);
            // get other values
            this.price = a.getInteger(R.styleable.PropertyCard_price, 0);
            this.isInspected = a.getBoolean(R.styleable.PropertyCard_isInspected, false);
            if (a.hasValue(R.styleable.PropertyCard_address)){
                this.address = a.getString(R.styleable.PropertyCard_address);
            }
            // set values to ui
            setValue();
        } finally {
            a.recycle();
        }
    }

    /**
     * Set the property address, price and thumbnail to the card ui
     */
    @SuppressLint("ResourceAsColor")
    private void setValue() {
        // get views
        TextView addressView = findViewById(R.id.addressTextView);
        TextView priceView = findViewById(R.id.priceTextView);
        TextView inspectedView = findViewById(R.id.inspectionStatusTextView);
        ImageView thumbnailView = findViewById(R.id.thumbnailImageView);
        // set values
        thumbnailView.setImageResource(this.thumbnail);
        addressView.setText(this.address);
        if (this.isInspected) {
            inspectedView.setText("Inspected");
            inspectedView.setTextColor(R.color.success);
        } else {
            inspectedView.setText("Not Inspected");
            inspectedView.setTextColor(R.color.grey);
        }
        if (this.price == 0) {
            priceView.setText("Price not available");
        } else {
            priceView.setText("$" + String.valueOf(this.price));
        }
    }

}
