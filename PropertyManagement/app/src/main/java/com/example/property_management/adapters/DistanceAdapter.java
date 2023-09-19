package com.example.property_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.data.DistanceInfo;

import java.util.ArrayList;

public class DistanceAdapter extends RecyclerView.Adapter<DistanceAdapter.DistanceViewHolder> {

    Context context;
    ArrayList<DistanceInfo> distanceInfoList;

    public DistanceAdapter(Context context, ArrayList<DistanceInfo> distanceInfoList) {
        this.context = context;
        this.distanceInfoList = distanceInfoList;
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

        holder.businessName.setText(distanceInfoList.get(position).getBusinessName());
        holder.distance.setText(String.valueOf(distanceInfoList.get(position).getDistance()) +" km");
        holder.driving.setText(distanceInfoList.get(position).getDriving() + " min");
        holder.publicTransport.setText(distanceInfoList.get(position).getPublicTransport() + " min");
        holder.walking.setText(distanceInfoList.get(position).getWalking() + " min");
    }

    @Override
    public int getItemCount() {
        return distanceInfoList.size();
    }

    public static final class DistanceViewHolder extends RecyclerView.ViewHolder{
        TextView businessName, distance, driving, publicTransport, walking;

        public DistanceViewHolder(@NonNull View itemView) {
            super(itemView);

            businessName = itemView.findViewById(R.id.detail_business_name);
            distance = itemView.findViewById(R.id.detail_distance);
            driving = itemView.findViewById(R.id.driveTime);
            publicTransport = itemView.findViewById(R.id.transitTime);
            walking = itemView.findViewById(R.id.walkTime);

        }
    }
}
