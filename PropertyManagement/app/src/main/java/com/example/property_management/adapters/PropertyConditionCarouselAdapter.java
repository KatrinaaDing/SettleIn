package com.example.property_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.property_management.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Adapter for a RecyclerView that displays a carousel of images representing the condition of a property.
 */
public class PropertyConditionCarouselAdapter extends RecyclerView.Adapter<PropertyConditionCarouselAdapter.ViewHolder> {

    Context context;
    private ArrayList<String> imageUrls;
    private OnItemClickListener onItemClickListener;

    /**
     * Constructor for the adapter.
     * @param context The current context.
     * @param imageUrls A list of image URLs for the carousel.
     */
    public PropertyConditionCarouselAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public PropertyConditionCarouselAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for individual carousel items.
        View view = LayoutInflater.from(context).inflate(R.layout.property_carousel_room, parent, false);
        return new PropertyConditionCarouselAdapter.ViewHolder(view);
    }


    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Flag to indicate whether the image is a placeholder.
        boolean isPlaceholder;

        // Check if the image exists locally
        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            isPlaceholder = false;
            // Load the image using Glide
            Glide.with(context).load(imageFile).into(holder.imageView);
        } else {
            // Load the placeholder and set the flag
            Glide.with(context).load(R.drawable.cannot_load_photo).into(holder.imageView);
            isPlaceholder = true;
        }

        holder.imageView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                // If it is a placeholder, use the placeholder resource ID, otherwise use the original URL
                if (isPlaceholder) {
                    onItemClickListener.onClick(holder.imageView, "android.resource://" + context.getPackageName() + "/" + R.drawable.cannot_load_photo);
                } else {
                    onItemClickListener.onClick(holder.imageView, imageUrl);
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The number of items in the data set.
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView The root view of the list item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the view holder's UI elements.
            imageView = itemView.findViewById(R.id.list_item_image);
        }
    }

    /**
     * Set the listener for item click events.
     * @param listener The listener to be registered.
     */
    public void setOnItemClickListener(PropertyConditionCarouselAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when an item in the RecyclerView is clicked.
     */
    public interface OnItemClickListener {
        void onClick(ImageView imageView, String path);
    }
}
