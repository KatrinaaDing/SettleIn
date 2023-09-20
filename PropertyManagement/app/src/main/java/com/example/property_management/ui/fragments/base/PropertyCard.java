package com.example.property_management.ui.fragments.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.ui.activities.PropertyDetailActivity;
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

        // set attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PropertyCard);
        try {
            this.price = a.getInteger(R.styleable.PropertyCard_price, 203);
            int bedrooms = a.getInteger(R.styleable.PropertyCard_bedrooms, 0);
            int bathrooms = a.getInteger(R.styleable.PropertyCard_bathrooms, 0);
            int parkings = a.getInteger(R.styleable.PropertyCard_parkings, 0);
            amenitiesGroup.setValues(bedrooms, bathrooms, parkings);
            this.isInspected = a.getBoolean(R.styleable.PropertyCard_isInspected, false);
            if (a.hasValue(R.styleable.PropertyCard_address)){
                this.address = a.getString(R.styleable.PropertyCard_address);
            }
            setValue();
        } finally {
            a.recycle();
        }

    }
    private void setValue() {
        TextView addressView = findViewById(R.id.addressTextView);
        TextView priceView = findViewById(R.id.priceTextView);
        ImageView thumbnailView = findViewById(R.id.thumbnailImageView);
        thumbnailView.setImageResource(this.thumbnail);
        addressView.setText(this.address);
        if (this.price == -1)
            priceView.setText("Price not available");
        else
            priceView.setText("$" + String.valueOf(this.price));
    }

}
