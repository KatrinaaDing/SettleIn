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
import com.example.property_management.adapters.PropertyConditionAdapter;
import com.example.property_management.ui.activities.ImageViewActivity;
import com.example.property_management.ui.fragments.base.InfoDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Adapter for a RecyclerView that displays property condition details per room.
 */
public class PropertyConditionAdapter extends RecyclerView.Adapter<PropertyConditionAdapter.ViewHolder> {

    private List<String> roomNames;
    private ArrayList<ArrayList<String>> imagesPerRoom;
    private ArrayList<Float> brightnessList;
    private ArrayList<Float> noiseList;
    private ArrayList<String> windowOrientationList;

    /**
     * Constructor for the adapter.
     * @param roomNames List of room names.
     * @param imagesPerRoom List of image lists, each corresponding to a room.
     * @param brightnessList List of brightness values for rooms.
     * @param noiseList List of noise values for rooms.
     * @param windowOrientationList List of window orientations for rooms.
     */
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
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.property_condition, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentRoomName = roomNames.get(position);
        holder.roomName.setText(currentRoomName);

        float noise = noiseList.get(position);
        // Set the noise value for the current item.
        holder.noiseValue.setText(noise == -1 ? "--" : String.format(Locale.getDefault(), "%.0f dB", noise));
        float brightness = brightnessList.get(position);
        holder.lightValue.setText(brightness == -1 ? "--" : String.format(Locale.getDefault(), "%.0f Lux", brightness));

        holder.windowValue.setText(windowOrientationList.get(position));
        // Handle display for the "Others" room type, hiding certain elements.
        if ("Others".equals(currentRoomName)) {
            holder.noiseValue.setVisibility(View.GONE);
            holder.lightValue.setVisibility(View.GONE);
            holder.windowValue.setVisibility(View.GONE);
            holder.noiseLevelText.setVisibility(View.GONE);

            holder.infoButton.setVisibility(View.GONE);

            holder.noiseIcon.setVisibility(View.GONE);
            holder.lightIcon.setVisibility(View.GONE);
            holder.compassIcon.setVisibility(View.GONE);

            holder.noiseView.setVisibility(View.GONE);
            holder.lightView.setVisibility(View.GONE);
            holder.compassView.setVisibility(View.GONE);
        } else {
            // Show all elements for normal room types.
            holder.noiseValue.setVisibility(View.VISIBLE);
            holder.lightValue.setVisibility(View.VISIBLE);
            holder.windowValue.setVisibility(View.VISIBLE);
            //holder.noiseLevelText.setVisibility(View.VISIBLE);
            holder.infoButton.setVisibility(View.VISIBLE);

            holder.noiseIcon.setVisibility(View.VISIBLE);
            holder.lightIcon.setVisibility(View.VISIBLE);
            holder.compassIcon.setVisibility(View.VISIBLE);

            holder.noiseView.setVisibility(View.VISIBLE);
            holder.lightView.setVisibility(View.VISIBLE);
            holder.compassView.setVisibility(View.VISIBLE);

            // set texts and colour for different noise level
            float noiseValue = noiseList.get(position);
            if (noiseValue == -1) {
                holder.noiseLevelText.setVisibility(View.GONE);
            } else if (noiseValue >= 55) {
                holder.noiseLevelText.setBackgroundColor(Color.RED);
                holder.noiseLevelText.setText("High Risk");
                holder.noiseLevelText.setVisibility(View.VISIBLE);
            } else if (noiseValue >= 35 && noiseValue < 55) {
                holder.noiseLevelText.setBackgroundColor(Color.parseColor("#FFA500")); // 橙色
                holder.noiseLevelText.setText("Risk");
                holder.noiseLevelText.setVisibility(View.VISIBLE);
            } else {
                holder.noiseLevelText.setBackgroundColor(Color.parseColor("#3CB371")); // 浅绿色
                holder.noiseLevelText.setText("Normal");
                holder.noiseLevelText.setVisibility(View.VISIBLE);
            }
        }

        // initialize PropertyConditionCarouselAdapter
        PropertyConditionCarouselAdapter propertyConditionCarouselAdapter = new PropertyConditionCarouselAdapter(holder.itemView.getContext(), imagesPerRoom.get(position));

        // Handle visibility of the image carousel depending on whether images are available.
        if (imagesPerRoom.get(position).isEmpty()) {
            holder.imageCarousel.setVisibility(View.GONE);
        } else {
            holder.imageCarousel.setVisibility(View.VISIBLE);

            // when click photo
            propertyConditionCarouselAdapter.setOnItemClickListener(new PropertyConditionCarouselAdapter.OnItemClickListener() {
                @Override
                public void onClick(ImageView imageView, String imageResourceOrUrl) {
                    // Launch an activity to show the image in full screen.
                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                    intent.putExtra("image", imageResourceOrUrl);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            (Activity) holder.itemView.getContext(),
                            imageView,
                            "image"
                    );
                    holder.itemView.getContext().startActivity(intent, options.toBundle());
                }
            });

            // Special handling if there is only one photo in the carousel.
            if (propertyConditionCarouselAdapter.getItemCount() == 1) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL, false);
                holder.imageCarousel.setLayoutManager(linearLayoutManager);

                linearLayoutManager.setStackFromEnd(true);
            }

            // Set the adapter for the image carousel.
            holder.imageCarousel.setAdapter(propertyConditionCarouselAdapter);
        }

    }

    /**
     * Displays an informational dialog with recommended lighting levels for various rooms in a home.
     * @param context The context where the dialog is to be displayed.
     */
    private void showInformationDialog(Context context) {
        InfoDialog dialog = new InfoDialog("Home Lighting: Recommended Lux Levels",
                "Bedroom general: 50-150 Lux\n" +
                        "Kitchen general: 150 Lux\n" +
                        "Kitchen working areas: 400 Lux\n" +
                        "Bathrooms: 150-300 Lux");
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "Hint dialog");
    }

    /**
     * Displays an informational dialog with recommended noise levels for a home.
     * @param context The context where the dialog is to be displayed.
     */
    private void showNoiseLevelDialog(Context context) {
        InfoDialog dialog = new InfoDialog("Home Noise: Recommended Decibel Levels",
                "Normal 0-35db: Have good sleep at night\n" +
                        "Risk 35-55db: Acceptable noise during the day\n" +
                        "High risk >55db: Not recommended to live");
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "Hint dialog");
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The number of items in the data set.
     */
    @Override
    public int getItemCount() {
        return roomNames.size();
    }

    /**
     * ViewHolder class for caching views associated with the default property condition item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, noiseValue, lightValue, windowValue;
        ImageView noiseIcon, lightIcon, compassIcon;
        TextView noiseView, lightView, compassView;
        RecyclerView imageCarousel;
        TextView noiseLevelText;
        ImageButton infoButton;
        Context context;

        /**
         * Constructor for the ViewHolder.
         * @param itemView The View for the individual item.
         * @param context The context in which the ViewHolder is operating.
         */
        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            roomName = itemView.findViewById(R.id.room_name);
            noiseValue = itemView.findViewById(R.id.noiseValue1);
            lightValue = itemView.findViewById(R.id.lightValue1);
            windowValue = itemView.findViewById(R.id.windowValue1);
            imageCarousel = itemView.findViewById(R.id.image_carousel);
            noiseLevelText = itemView.findViewById(R.id.noiseLevelText);

            noiseIcon = itemView.findViewById(R.id.ic_noise);
            lightIcon = itemView.findViewById(R.id.ic_light);
            compassIcon = itemView.findViewById(R.id.ic_window);

            noiseView = itemView.findViewById(R.id.noiseView);
            lightView = itemView.findViewById(R.id.lightView);
            compassView = itemView.findViewById(R.id.windowView);

            infoButton = itemView.findViewById(R.id.infoButton);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                // Display the information dialog for lighting levels.
                public void onClick(View v) {
                    showInformationDialog(context);
                }
            });

            noiseLevelText.setOnClickListener(new View.OnClickListener() {
                @Override
                // Display the information dialog for noise levels.
                public void onClick(View v) {
                    showNoiseLevelDialog(v.getContext());
                }
            });
        }
    }

}
