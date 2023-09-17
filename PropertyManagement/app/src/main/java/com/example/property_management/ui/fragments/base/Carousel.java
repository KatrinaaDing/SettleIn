package com.example.property_management.ui.fragments.base;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.CarouselAdapter;
import com.example.property_management.ui.activities.ImageViewActivity;
import com.example.property_management.ui.activities.PropertyDetailActivity;

import java.util.ArrayList;

public class Carousel extends RecyclerView{

    private ArrayList<String> imageUrls;
    private CarouselAdapter adapter;

    public Carousel(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.custom_carousel, this, true);

        // receive attributes
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Carousel);
//
//        int valueArrayResId = a.getResourceId(R.styleable.Carousel_imageUrls, 0);
//
//        if (valueArrayResId != 0) {
//            int[] values = getResources().getIntArray(valueArrayResId);
//            // Now, you can use the 'values' array as needed.
//        }
//        a.recycle();
//
//        RecyclerView recyclerView = findViewById(R.id.recycler);
//        ArrayList<String> arrayList = new ArrayList<>();

        //Add multiple images to arraylist.

//        CarouselAdapter adapter = new CarouselAdapter();

//        adapter.setOnItemClickListener(new CarouselAdapter.OnItemClickListener() {
//            @Override
//            public void onClick(ImageView imageView, String url) {
//                //Do something like opening the image in new activity or showing it in full screen or something else.
//                startActivity(new Intent(PropertyDetailActivity.this,
//                                ImageViewActivity.class).putExtra("image", url),
//                        ActivityOptions.makeSceneTransitionAnimation(PropertyDetailActivity.this, imageView, "image").toBundle());
//            }
//        });
//        recyclerView.setAdapter(adapter);
    }

    public void setImageUrls(AppCompatActivity activity, ArrayList<String> imageUrls) {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        adapter = new CarouselAdapter(activity, imageUrls);
        recyclerView.setAdapter(adapter);
    }
}
