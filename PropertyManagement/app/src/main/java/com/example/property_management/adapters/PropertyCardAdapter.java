package com.example.property_management.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.callbacks.DeletePropertyByIdCallback;
import com.example.property_management.data.Property;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * Adapter for property card
 */
public class PropertyCardAdapter extends RecyclerView.Adapter<PropertyCardAdapter.ViewHolder>{

    private List<Property> properties;
    Context context;

    EventListener listener;

    /**
     * Interface for event listener to show/hide no property placeholder
     */
    public interface EventListener {
        void onHasProperties(boolean hasProperty);
    }

    public PropertyCardAdapter(List<Property> properties, EventListener listener) {
        this.properties = properties;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view and get the context
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_property_card, parent, false);
        context = parent.getContext();
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // set values to the view
        Property property = properties.get(position);
        holder.addressView.setText(property.getAddress());

        if (property.getPrice() == 0) {
            holder.priceView.setText("Price not available");
        } else {
            holder.priceView.setText("$" + property.getPrice() + "pw");
        }
        holder.inspectedView.setText(property.getInspected() ? "Inspected" : "Not Inspected");
        if (property.getInspected()) {
            holder.inspectedView.setText("Inspected");
            holder.inspectedView.setTextColor(ContextCompat.getColor(context, R.color.success));
        }

        if ((property.getImages() != null) && !property.getImages().isEmpty()) {
            // set the first image as thumbnail
            Glide.with(holder.thumbnailView.getContext())
                    .load(property.getImages().get(0))
                    .into(holder.thumbnailView);
        }
        holder.amenitiesGroup.setValues(property.getNumBedrooms(), property.getNumBathrooms(), property.getNumParking());

        // set on click a property card
        holder.propertyCard.setOnClickListener (v-> {
            Intent intent = new Intent (context, PropertyDetailActivity.class);
            intent.putExtra ("property_id", properties.get (position).getPropertyId());
            context.startActivity(intent);
        });

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.propertyCard.getLayoutParams();
        layoutParams.setMargins(0, 10, 0, 10);
        holder.propertyCard.setLayoutParams(layoutParams);

        // set on click listener for menu button
        // item 1: view property on map. item 2: delete property
        holder.menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.menuBtn);
            popup.inflate(R.menu.property_card_menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.property_option_delete) {
                        // handle delete property
                        confirmDeleteProperty(property, position);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AmenitiesGroup amenitiesGroup;
        MaterialCardView propertyCard;
        TextView addressView;
        TextView priceView;
        ImageView thumbnailView;
        TextView inspectedView;
        MaterialButton menuBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            propertyCard = itemView.findViewById(R.id.propertyCard);
            amenitiesGroup = itemView.findViewById(R.id.amenitiesGroup);
            addressView = itemView.findViewById(R.id.addressTextView);
            priceView = itemView.findViewById(R.id.priceTextView);
            thumbnailView = itemView.findViewById(R.id.thumbnailImageView);
            menuBtn = itemView.findViewById(R.id.menuBtn);
            inspectedView = itemView.findViewById(R.id.inspectionStatusTextView);
        }
    }

    /**
     * Show a dialog to confirm delete property
     * @param property the property to be deleted
     */
    private void confirmDeleteProperty(Property property, int position) {
        BasicDialog dialog = new BasicDialog(true,
                "Are you sure you want to delete this property?",
                "Your collected data will also be removed. This action cannot be undone.",
                "Cancel", "Delete");
        dialog.setCallback(new BasicDialogCallback() {
            @Override
            public void onLeftBtnClick() {
                dialog.dismiss();
            }
            @Override
            public void onRightBtnClick() {
                FirebaseUserRepository firebaseUserRepository = new FirebaseUserRepository();
                // delete property from firebase
                firebaseUserRepository.deleteUserProperty(property.getPropertyId(), new DeletePropertyByIdCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        Activity activity = (Activity) context;
                        new BasicSnackbar(activity.findViewById(android.R.id.content), msg, "success");
                        properties.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, properties.size());
                        // show hint if property list is empty
                        listener.onHasProperties(properties.size() != 0);
                    }
                    @Override
                    public void onError(String msg) {
                        Activity activity = (Activity) context;
                        new BasicSnackbar(activity.findViewById(android.R.id.content),
                                msg + ". Please try again later.", "error");
                    }
                });
            }
        });
        AppCompatActivity activity = (AppCompatActivity) context;
        // show confirm dialog
        dialog.show(activity.getSupportFragmentManager(), "confirm-delete-property-dialog");
    }
}
