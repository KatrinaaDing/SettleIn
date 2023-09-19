package com.example.property_management.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.databinding.ActivityImageViewBinding;
import com.example.property_management.databinding.ActivityPropertyDetailBinding;

public class ImageViewActivity extends AppCompatActivity {

    private ActivityImageViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ImageView imageView = findViewById(R.id.imageView);
        getSupportActionBar().hide(); // hide action bar on view image

        // Load the image using Glide
        Glide.with(ImageViewActivity.this).load(getIntent().getStringExtra("image")).into(imageView);

        // Click the image to return back
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the activity when the ImageView is clicked
                finish();
            }
        });
    }
}
