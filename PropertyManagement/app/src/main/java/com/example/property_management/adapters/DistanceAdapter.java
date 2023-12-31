package com.example.property_management.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.data.DistanceInfo;
import com.example.property_management.utils.Helpers;

import java.util.ArrayList;

/**
 * Adapter for the distance recycler view
 */
public class DistanceAdapter extends RecyclerView.Adapter<DistanceAdapter.DistanceViewHolder> {

    Context context;
    ArrayList<DistanceInfo> distanceInfoList;
    String propertyLocation;

    public DistanceAdapter(Context context, ArrayList<DistanceInfo> distanceInfoList, String propertyLocation) {
        this.context = context;
        this.distanceInfoList = distanceInfoList;
        this.propertyLocation = propertyLocation;
    }

    @NonNull
    @Override
    public DistanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.facilities_distance_item, parent, false);

        // here we create a recyclerview row item layout file
        return new DistanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DistanceViewHolder holder, int position) {
        // get address and name of the interested facility/location
        String address = distanceInfoList.get(position).getAddress();
        String name = distanceInfoList.get(position).getName();

        // set the distance data to text views
        holder.name.setText(name);
        holder.address.setText(address);
        holder.distance.setText(String.valueOf(distanceInfoList.get(position).getDistance()));
        holder.driving.setText(distanceInfoList.get(position).getDriving());
        holder.transit.setText(distanceInfoList.get(position).getTransit());
        holder.walking.setText(distanceInfoList.get(position).getWalking());

        // open in google map when click on business name
        holder.interestContainer.setOnClickListener(view -> {
            // if address is null or empty, only open the destination location in google map
            // else, open the direction from the property to the interested facility
            if (address == null && address.isEmpty()) {
                Helpers.openInGoogleMap(address, context);
            } else {
                Helpers.openDirectionInGoogleMap(propertyLocation, address, context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return distanceInfoList.size();
    }

    public static final class DistanceViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout interestContainer;
        TextView name, address, distance, driving, transit, walking;

        public DistanceViewHolder(@NonNull View itemView) {
            super(itemView);
            interestContainer = itemView.findViewById(R.id.interest_container);
            name = itemView.findViewById(R.id.detail_name);
            address = itemView.findViewById(R.id.detail_address);
            distance = itemView.findViewById(R.id.detail_distance);
            driving = itemView.findViewById(R.id.driveTime);
            transit = itemView.findViewById(R.id.transitTime);
            walking = itemView.findViewById(R.id.walkTime);

        }
    }
}
