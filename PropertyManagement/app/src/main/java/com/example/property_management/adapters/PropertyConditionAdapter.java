package com.example.property_management.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.property_management.R;
import com.example.property_management.data.RoomData;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class PropertyConditionAdapter extends RecyclerView.Adapter<PropertyConditionAdapter.ViewHolder> {

    private List<String> roomNames;
    private ArrayList<ArrayList<String>> imagesPerRoom;
    private HashMap<String, RoomData> roomDataMap;
    private ArrayList<RoomData> roomDataList;
    private ArrayList<Float> brightnessList;
    private ArrayList<Float> noiseList;
    private ArrayList<String> windowOrientationList;

    public PropertyConditionAdapter(
            List<String> roomNames,
            ArrayList<ArrayList<String>> imagesPerRoom,
            ArrayList<Float> brightnessList,
            ArrayList<Float> noiseList,
            ArrayList<String> windowOrientationList) {
        this.roomNames = roomNames;
        this.imagesPerRoom = imagesPerRoom;
        this.brightnessList = brightnessList;
        this.noiseList = noiseList;
        this.windowOrientationList = windowOrientationList;
    }
    /**
    public PropertyConditionAdapter(List<String> roomNames, ArrayList<ArrayList<Integer>> imagesPerRoom) {
        this.roomNames = roomNames;
        this.imagesPerRoom = imagesPerRoom;
    }
     */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.property_condition, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.roomName.setText(roomNames.get(position));
        holder.noiseValue.setText(String.format(Locale.getDefault(), "%.0fdb", noiseList.get(position)));

        holder.lightValue.setText(String.format(Locale.getDefault(), "%.0f Lux", brightnessList.get(position)));
        holder.windowValue.setText(windowOrientationList.get(position));

        CarouselAdapter carouselAdapter = new CarouselAdapter(holder.itemView.getContext(), imagesPerRoom.get(position));
        holder.imageCarousel.setAdapter(carouselAdapter);

        float noiseValue = noiseList.get(position);
        if (noiseValue >= 55) {
            holder.noiseLevelButton.setBackgroundColor(Color.RED);
            holder.noiseLevelButton.setText("High Risk");
        } else if (noiseValue >= 40) {
            holder.noiseLevelButton.setBackgroundColor(Color.parseColor("#FFA500"));
            holder.noiseLevelButton.setText("Risk");
        } else {
            holder.noiseLevelButton.setBackgroundColor(Color.GREEN);
            holder.noiseLevelButton.setText("Normal");
        }
    }

    private void showInformationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Recommended lighting levels for the Home in Lux");

        String[] lightingLevels = {
                "Bedroom general: 50-150 Lux",
                "Kitchen general: 150 Lux",
                "Kitchen working areas: 400 Lux",
                "Bathrooms: 150-300 Lux"
        };

        builder.setItems(lightingLevels, null);

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return roomNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, noiseValue, lightValue, windowValue;
        RecyclerView imageCarousel;
        Button noiseLevelButton;
        ImageButton infoButton;
        Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            roomName = itemView.findViewById(R.id.room_name);
            noiseValue = itemView.findViewById(R.id.noiseValue1);
            lightValue = itemView.findViewById(R.id.lightValue1);
            windowValue = itemView.findViewById(R.id.windowValue1);
            imageCarousel = itemView.findViewById(R.id.image_carousel);
            noiseLevelButton = itemView.findViewById(R.id.noiseLevelButton);

            infoButton = itemView.findViewById(R.id.infoButton);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInformationDialog(context);
                }
            });
        }

    }


}
