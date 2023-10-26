package com.example.property_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.property_management.R;
import com.example.property_management.ui.activities.PropertyDetailActivity;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PropertyConditionAdapter extends RecyclerView.Adapter<PropertyConditionAdapter.ViewHolder> {

    private List<String> roomNames;

    public PropertyConditionAdapter(PropertyDetailActivity propertyDetailActivity, List<String> roomNames) {
        this.roomNames = roomNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.property_condition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // hard code
        holder.roomName.setText("room Name");
        holder.capturedImage.setImageResource(R.drawable.ic_camera);
        holder.noiseValue.setText("50db");
        holder.lightValue.setText("300 Lux");
        holder.windowValue.setText("North");

    }

    @Override
    public int getItemCount() {
        return roomNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, noiseValue, lightValue, windowValue;
        ImageView capturedImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            noiseValue = itemView.findViewById(R.id.noiseValue1);
            lightValue = itemView.findViewById(R.id.lightValue1);
            windowValue = itemView.findViewById(R.id.windowValue1);
            capturedImage = itemView.findViewById(R.id.capturedImage);
        }
    }
}
