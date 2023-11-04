package com.example.property_management.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.property_management.R;
import com.example.property_management.data.RoomData;
import com.example.property_management.adapters.CarouselAdapter;
import com.example.property_management.ui.activities.ImageViewActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.property_condition, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentRoomName = roomNames.get(position);
        holder.roomName.setText(currentRoomName);
        holder.noiseValue.setText(String.format(Locale.getDefault(), "%.0fdb", noiseList.get(position)));

        holder.lightValue.setText(String.format(Locale.getDefault(), "%.0f Lux", brightnessList.get(position)));
        holder.windowValue.setText(windowOrientationList.get(position));
        if ("Others".equals(currentRoomName)) {
            holder.noiseValue.setVisibility(View.GONE);
            holder.lightValue.setVisibility(View.GONE);
            holder.windowValue.setVisibility(View.GONE);
            holder.noiseLevelButton.setVisibility(View.GONE);

            holder.infoButton.setVisibility(View.GONE);

            holder.noiseIcon.setVisibility(View.GONE);
            holder.lightIcon.setVisibility(View.GONE);
            holder.compassIcon.setVisibility(View.GONE);

            holder.noiseView.setVisibility(View.GONE);
            holder.lightView.setVisibility(View.GONE);
            holder.compassView.setVisibility(View.GONE);
        } else {

            holder.noiseValue.setVisibility(View.VISIBLE);
            holder.lightValue.setVisibility(View.VISIBLE);
            holder.windowValue.setVisibility(View.VISIBLE);
            holder.noiseLevelButton.setVisibility(View.VISIBLE);
            holder.infoButton.setVisibility(View.VISIBLE);

            holder.noiseIcon.setVisibility(View.VISIBLE);
            holder.lightIcon.setVisibility(View.VISIBLE);
            holder.compassIcon.setVisibility(View.VISIBLE);

            holder.noiseView.setVisibility(View.VISIBLE);
            holder.lightView.setVisibility(View.VISIBLE);
            holder.compassView.setVisibility(View.VISIBLE);

            // set texts and colour for different noise level
            float noiseValue = noiseList.get(position);
            if (noiseValue >= 55) {
                holder.noiseLevelButton.setBackgroundColor(Color.RED);
                holder.noiseLevelButton.setText("High Risk");
            } else if (noiseValue >= 40) {
                holder.noiseLevelButton.setBackgroundColor(Color.parseColor("#FFA500"));
                holder.noiseLevelButton.setText("Risk");
            } else {
                holder.noiseLevelButton.setBackgroundColor(Color.parseColor("#3CB371"));
                holder.noiseLevelButton.setText("Normal");
            }
        }

        // Initialize CarouselAdapter
        CarouselAdapter carouselAdapter = new CarouselAdapter(holder.itemView.getContext(), imagesPerRoom.get(position));

        // if no image, hide carousel
        if (imagesPerRoom.get(position).isEmpty()) {
            holder.imageCarousel.setVisibility(View.GONE);
        } else {
            holder.imageCarousel.setVisibility(View.VISIBLE);

            // on click open image
            carouselAdapter.setOnItemClickListener(new CarouselAdapter.OnItemClickListener() {
                @Override
                public void onClick(ImageView imageView, String imageUrl) {
                    // show image
                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                    intent.putExtra("image", imageUrl);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            (Activity) holder.itemView.getContext(),
                            imageView,
                            "image"
                    );
                    holder.itemView.getContext().startActivity(intent, options.toBundle());
                }
            });

            // if only one image, set linear layout to center the image
            if (carouselAdapter.getItemCount() == 1) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL, false);
                holder.imageCarousel.setLayoutManager(linearLayoutManager);

                linearLayoutManager.setStackFromEnd(true);
            }

            // set adapter
            holder.imageCarousel.setAdapter(carouselAdapter);
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
        ImageView noiseIcon, lightIcon, compassIcon;
        TextView noiseView, lightView, compassView;
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

            noiseIcon = itemView.findViewById(R.id.ic_noise);
            lightIcon = itemView.findViewById(R.id.ic_light);
            compassIcon = itemView.findViewById(R.id.ic_window);

            noiseView = itemView.findViewById(R.id.noiseView);
            lightView = itemView.findViewById(R.id.lightView);
            compassView = itemView.findViewById(R.id.windowView);

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
