package com.example.property_management.ui.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.CarouselAdapter;
import com.example.property_management.databinding.ActivityPropertyDetailBinding;

import java.util.ArrayList;

public class PropertyDetailActivity extends AppCompatActivity {
    private ActivityPropertyDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Property Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ================================== Components =======================================
        Button dataCollectionBtn = findViewById(R.id.dataCollectionBtn);
        dataCollectionBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, DataCollectionActivity.class);
            startActivity(intent);
        });

        // Insert carousel imageUrls data
        RecyclerView recyclerView = findViewById(R.id.recycler);

        // Create an ArrayList of image URLs (you can replace these with your actual URLs)
        ArrayList<String> imageUrls = new ArrayList<>();

        // TODO fetch imageUrls from firebase
        imageUrls.add("https://images.unsplash.com/photo-1668889716746-fd2ca90373f7?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzfHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");
        imageUrls.add("https://images.unsplash.com/photo-1614174124242-4b3656523295?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw1fHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");
        imageUrls.add("https://images.unsplash.com/photo-1694449263303-a90c4ce18112?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw4fHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");

        CarouselAdapter adapter = new CarouselAdapter(PropertyDetailActivity.this, imageUrls);

        adapter.setOnItemClickListener(new CarouselAdapter.OnItemClickListener() {
            @Override
            public void onClick(ImageView imageView, String imageUrl) {
                startActivity(new Intent(PropertyDetailActivity.this,
                                ImageViewActivity.class).putExtra("image", imageUrl),
                        ActivityOptions.makeSceneTransitionAnimation(PropertyDetailActivity.this, imageView, "image").toBundle());
            }
        });
        recyclerView.setAdapter(adapter);

        // linkButton
        // Find the TextView by its ID
        Button linkButton = findViewById(R.id.linkButton);
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the URL you want to open
                String url = "https://www.domain.com.au/410-673-latrobe-street-docklands-vic-3008-16651885"; // Replace with your desired URL

                // Create an Intent to open a web browser with the specified URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                // Start the web browser activity
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
