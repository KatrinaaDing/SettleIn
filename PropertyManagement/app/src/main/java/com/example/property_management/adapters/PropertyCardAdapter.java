package com.example.property_management.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.data.Property;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.fragments.base.PropertyCard;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.google.android.material.card.MaterialCardView;

import org.w3c.dom.Text;

import java.util.List;

public class PropertyCardAdapter extends RecyclerView.Adapter<PropertyCardAdapter.ViewHolder>{

    private List<Property> properties;
    Context context;

    public PropertyCardAdapter(List<Property> properties) {
        this.properties = properties;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view and get the context
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_property_card, parent, false);
        context = parent.getContext();
        return new ViewHolder (inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        System.out.println(position);
        Property property = properties.get(position);
        holder.addressView.setText(property.getAddress());
//        holder.priceView.setText("$" + String.valueOf(property.getPrice()));
        if ((property.getImages() != null) && !property.getImages().isEmpty()) {
            Glide.with(holder.thumbnailView.getContext())
                    .load(property.getImages().get(0))
                    .into(holder.thumbnailView);
        }
        holder.amenitiesGroup.setValues(property.getNumBedrooms(), property.getNumBathrooms(), property.getNumParking());

        // set on click listener
        holder.propertyCard.setOnClickListener (v-> {
            Intent intent = new Intent (context, PropertyDetailActivity.class);
            intent.putExtra ("property_id", properties.get (position).getPropertyId());
            context.startActivity(intent);
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

        public ViewHolder(View itemView) {
            super(itemView);
            propertyCard = itemView.findViewById(R.id.propertyCard);
            amenitiesGroup = itemView.findViewById(R.id.amenitiesGroup);
            addressView = itemView.findViewById(R.id.addressTextView);
            priceView = itemView.findViewById(R.id.priceTextView);
            thumbnailView = itemView.findViewById(R.id.thumbnailImageView);
        }
    }
}
