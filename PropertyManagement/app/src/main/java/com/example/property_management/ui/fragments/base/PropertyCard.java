package com.example.property_management.ui.fragments.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.callbacks.DeletePropertyByIdCallback;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.google.android.material.card.MaterialCardView;

/**
 * Custom property card to display property information in HomeFragment
 */
public class PropertyCard extends MaterialCardView {
    private String address = "No Address Available";
    private String propertyId = null;
    private int price;
    private String thumbnail;
    private int DEFAULT_THUMBNAIL = R.drawable.property_image_placeholder;
    private boolean isInspected;
    Context context;

    public PropertyCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.custom_property_card, this, true);

        // set attributes and default values
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PropertyCard);
        if (a.hasValue(R.styleable.PropertyCard_address)){
            this.address = a.getString(R.styleable.PropertyCard_address);
        }
        try{
            setValues(
                null,
                this.address,
                a.getInteger(R.styleable.PropertyCard_price, 0),
                a.getString(R.styleable.PropertyCard_thumbnail),
                a.getBoolean(R.styleable.PropertyCard_isInspected,false),
                a.getInteger(R.styleable.PropertyCard_bedrooms, 0),
                a.getInteger(R.styleable.PropertyCard_bathrooms, 0),
                a.getInteger(R.styleable.PropertyCard_parkings, 0)
            );
        } finally {
            // release TypedArray object
            a.recycle();
        }

        // configure delete button
        Button menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), menuBtn);
            popup.inflate(R.menu.property_card_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.property_option_delete) {
                        // handle delete property
                        confirmDeleteProperty(propertyId, context);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            // display menu
            popup.show();
        });

        View propertyCard = findViewById(R.id.propertyCard);
        propertyCard.setOnClickListener(v-> {
            if (propertyId == null) {
                return;
            }
            Intent intent = new Intent(getContext(), PropertyDetailActivity.class);
            intent.putExtra ("property_id", propertyId);
            context.startActivity(intent);
        });
    }

    public void setValues(String id, String address, int price, String thumbnail, boolean isInspected,
                          int bedrooms, int bathrooms, int parkings) {
        // set amenities group
        AmenitiesGroup amenitiesGroup = findViewById(R.id.amenitiesGroup);
        amenitiesGroup.setValues(bedrooms, bathrooms, parkings);

        // get other values
        this.propertyId = id;
        this.price = price;
        this.isInspected = isInspected;
        this.address = address;
        this.thumbnail = thumbnail;

        // set values to ui
        setUiValue();
    }

    /**
     * Set the property address, price and thumbnail to the card ui
     */
    @SuppressLint("ResourceAsColor")
    private void setUiValue() {
        // get views
        TextView addressView = findViewById(R.id.addressTextView);
        TextView priceView = findViewById(R.id.priceTextView);
        TextView inspectedView = findViewById(R.id.inspectionStatusTextView);
        ImageView thumbnailView = findViewById(R.id.thumbnailImageView);
        // set values
        if (this.thumbnail == null) {
            thumbnailView.setImageResource(DEFAULT_THUMBNAIL);
        } else {
            Glide.with(thumbnailView.getContext())
                    .load(this.thumbnail)
                    .into(thumbnailView);
        }

        addressView.setText(this.address);
        if (this.isInspected) {
            inspectedView.setText("Inspected");
            inspectedView.setTextColor(R.color.success);
        } else {
            inspectedView.setText("Not Inspected");
        }
        if (this.price == 0) {
            priceView.setText("Price not available");
        } else {
            priceView.setText("$" + String.valueOf(this.price));
        }
    }

    /**
     * Show a dialog to confirm delete property
     *
     * @param propertyId the property propertyId to be deleted
     */
    public static void confirmDeleteProperty(String propertyId, Context context) {
        BasicDialog dialog = new BasicDialog(true,
                "Are you sure you want to delete this property?",
                "This action cannot be undone.",
                "Cancel", "Delete");
        dialog.setCallback(new BasicDialogCallback() {
            @Override
            public void onLeftBtnClick() {
                dialog.dismiss();
            }
            @Override
            public void onRightBtnClick() {
                FirebaseUserRepository firebaseUserRepository = new FirebaseUserRepository();
                firebaseUserRepository.deleteUserProperty(propertyId, new DeletePropertyByIdCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        Activity activity = (Activity) context;
                        new BasicSnackbar(activity.findViewById(android.R.id.content), msg, "success");
                        Log.d("property-card-adapter", "delete property success");
                        // refresh the activity after showing the snackbar
                        new Handler().postDelayed(() -> {
                            activity.recreate();
                        }, 1000);
                    }
                    @Override
                    public void onError(String msg) {
                        Activity activity = (Activity) context;
                        new BasicSnackbar(activity.findViewById(android.R.id.content),
                                msg + ". Please try again later.", "error");
                        Log.e("property-card-adapter", "delete property failure: " + msg);
                    }
                });
            }
        });
        AppCompatActivity activity = (AppCompatActivity) context;
        dialog.show(activity.getSupportFragmentManager(), "confirm-delete-property-dialog");
    }

}
