package com.example.property_management.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.data.Property;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;

import org.w3c.dom.Text;

import java.util.List;

public class PropertyCardAdapter extends RecyclerView.Adapter<PropertyCardAdapter.ViewHolder>{

    private List<Property> properties;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
//        holder.addressView.setText(property.getAddress());
//        holder.priceView.setText("$" + String.valueOf(property.getPrice()));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AmenitiesGroup amenitiesGroup;
        TextView addressView;
        TextView priceView;
        ImageView thumbnailView;

        public ViewHolder(View itemView) {
            super(itemView);
            amenitiesGroup = itemView.findViewById(R.id.amenitiesGroup);
            addressView = itemView.findViewById(R.id.addressTextView);
            priceView = itemView.findViewById(R.id.priceTextView);
            thumbnailView = itemView.findViewById(R.id.thumbnailImageView);
        }
    }
}
