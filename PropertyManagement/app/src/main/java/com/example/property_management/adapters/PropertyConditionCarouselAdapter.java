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
public class PropertyConditionCarouselAdapter extends RecyclerView.Adapter<PropertyConditionCarouselAdapter.ViewHolder> {

    Context context;
    private ArrayList<String> imageUrls;
    private OnItemClickListener onItemClickListener;

    public PropertyConditionCarouselAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;

    }

    @NonNull
    @Override
    public PropertyConditionCarouselAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.property_carousel_room, parent, false);
        return new PropertyConditionCarouselAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // 标记是否为占位符
        boolean isPlaceholder;

        // Check if the image exists locally
        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            isPlaceholder = false;
            // Load the image using your preferred image loading library
            Glide.with(context).load(imageFile).into(holder.imageView);
        } else {
            // Load the placeholder and set the flag
            Glide.with(context).load(R.drawable.property_image_placeholder).into(holder.imageView);
            isPlaceholder = true;
        }

        holder.imageView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                // If it is a placeholder, use the placeholder resource ID, otherwise use the original URL
                if (isPlaceholder) {
                    onItemClickListener.onClick(holder.imageView, "android.resource://" + context.getPackageName() + "/" + R.drawable.property_image_placeholder);
                } else {
                    onItemClickListener.onClick(holder.imageView, imageUrl);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
        }
    }

    // PropertyConditionCarouselAdapter 类中
    public void setOnItemClickListener(PropertyConditionCarouselAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    public interface OnItemClickListener {
        void onClick(ImageView imageView, String path);
    }
}
