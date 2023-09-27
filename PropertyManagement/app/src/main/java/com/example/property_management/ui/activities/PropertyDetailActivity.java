package com.example.property_management.ui.activities;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.property_management.data.DistanceInfo;
import com.example.property_management.R;
import com.example.property_management.adapters.CarouselAdapter;
import com.example.property_management.adapters.DistanceAdapter;
import com.example.property_management.databinding.ActivityPropertyDetailBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;

public class PropertyDetailActivity extends AppCompatActivity {
    private ActivityPropertyDetailBinding binding;
    private String propertyId;

    DistanceAdapter distanceAdapter;

    RecyclerView distanceRecycler;

    String date = "";

    String time = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Property Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get property id
        Intent intent = getIntent();
        this.propertyId = intent.getStringExtra("property_id"); // -1 is default value
        setTitle("Property Detail (" + this.propertyId + ")");


        // ================================== Components =======================================
        // ===== amenities group =====
        // TODO fetch bedrooms, bathrooms, parkings from firebase
        Integer bathrooms = 2;
        Integer bedrooms = 3;
        Integer parkings = 1;
        AmenitiesGroup amenitiesGroup = findViewById(R.id.amenitiesGroup);
        amenitiesGroup.setValues(bedrooms, bathrooms, parkings);
        // ===== inspection time =====
        Button addInspectionTimeBtn = binding.addInspectionTimeBtn;
        ConstraintLayout inspectionTimeLayout = binding.inspectionTimeLayout;
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;
        Button editInspectionTimeBtn = binding.editInspectionTimeBtn;

        // Conditionally display Button or TextView
//        String inspectionDate = "10/10/2023"; // TODO fetch ifInspectionDateExist from firebase
//        String inspectionTime = "10:00"; // TODO fetch ifInspectionTimeExist from firebase
        String inspectionDate = null;
        String inspectionTime = null;
        if (inspectionDate != null && inspectionTime != null) {
            inspectionTimeTxt.setText(inspectionTime + " " + inspectionDate);
            date = inspectionDate;
            time = inspectionTime;
            addInspectionTimeBtn.setVisibility(View.GONE);
        } else if (inspectionDate != null) {
            inspectionTimeTxt.setText(inspectionDate);
            addInspectionTimeBtn.setVisibility(View.VISIBLE);
            date = inspectionDate;
            addInspectionTimeBtn.setVisibility(View.GONE);
        } else {
            inspectionTimeLayout.setVisibility(View.GONE);
        }

        addInspectionTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });

        editInspectionTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });

        // ===== carousel =====
        // Insert carousel imageUrls data
        RecyclerView recyclerView = findViewById(R.id.recycler);

        // Create an ArrayList of image URLs (you can replace these with your actual URLs)
        ArrayList<String> imageUrls = new ArrayList<>();
        // TODO fetch imageUrls from firebase
        imageUrls.add("https://images.unsplash.com/photo-1668889716746-fd2ca90373f7?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwzfHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");
        imageUrls.add("https://images.unsplash.com/photo-1614174124242-4b3656523295?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw1fHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");
        imageUrls.add("https://images.unsplash.com/photo-1694449263303-a90c4ce18112?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw4fHx8ZW58MHx8fHx8&auto=format&fit=crop&w=900&q=60");

        CarouselAdapter adapter = new CarouselAdapter(PropertyDetailActivity.this, imageUrls);

        // on click open image
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
        Button linkButton = findViewById(R.id.linkButton);

        // on click redirect to property ad site
        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the URL you want to open
                String url = "https://www.domain.com.au/410-673-latrobe-street-docklands-vic-3008-16651885"; // TODO fetch site url from firebase

                // Create an Intent to open a web browser with the specified URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                // Start the web browser activity
                startActivity(intent);
            }
        });

        // ===== interested facilities distance =====
        ArrayList<DistanceInfo> distanceInfoList = new ArrayList<>();
        distanceInfoList.add(new DistanceInfo("Coles Spencer St", 0.5F, 1, 5, 6)); // TODO fetch distance from firebase
        distanceInfoList.add(new DistanceInfo("Melbourne Central Station", 0.8F, 2, 6, 13));
        distanceInfoList.add(new DistanceInfo("Southern Cross Station", 0.8F, 2, 6, 13));
        distanceInfoList.add(new DistanceInfo("The university of melbourne", 0.8F, 2, 6, 13));
        setDistanceRecycler(distanceInfoList);

        // ===== dataCollectionBtn =====
        Button dataCollectionBtn = findViewById(R.id.dataCollectionBtn);
        dataCollectionBtn.setOnClickListener(view -> {
            Intent newIntent = new Intent(this, DataCollectionActivity.class);
            startActivity(newIntent);
        });

    }

    private void setDistanceRecycler(ArrayList<DistanceInfo> distanceInfoList){
        distanceRecycler = binding.distanceRecycler;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        distanceRecycler.setLayoutManager(layoutManager);
        distanceAdapter = new DistanceAdapter(this, distanceInfoList);
        distanceRecycler.setAdapter(distanceAdapter);
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

    private void showCustomDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_add_inspection_time_dialog, null);

        Button addDateBtn = dialogView.findViewById(R.id.addDateBtn);
        Button addTimeBtn = dialogView.findViewById(R.id.addTimeBtn);
        Button resetBtn = dialogView.findViewById(R.id.resetBtn);
        TextView dateTxt = dialogView.findViewById(R.id.dateTxt);
        TextView timeTxt = dialogView.findViewById(R.id.timeTxt);

        // display date and time
        if (date != "")
            dateTxt.setText(date);
        else
            dateTxt.setText("Not Set");
        if (time != "")
            timeTxt.setText(time);
        else
            timeTxt.setText("Not Set");

        // ===== dialog =====
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(PropertyDetailActivity.this)
                .setTitle("Schedule Inspection")
                .setView(dialogView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Button addInspectionTimeBtn = binding.addInspectionTimeBtn;
                        ConstraintLayout inspectionTimeLayout = binding.inspectionTimeLayout;
                        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;
                        // on click set date
                        if (date != "" || time != "") {
                            inspectionTimeTxt.setText(time + " " + date);
                            addInspectionTimeBtn.setVisibility(View.GONE);
                            inspectionTimeLayout.setVisibility(View.VISIBLE);
                        } else {
                            addInspectionTimeBtn.setVisibility(View.VISIBLE);
                            inspectionTimeLayout.setVisibility(View.GONE);
                        }

                        dialogInterface.dismiss();
                        // TODO put date and time to firebase
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        alertDialog.show();

        // ===== datePicker =====
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker
                        .Builder
                        .datePicker()
                        .setTitleText("Select Inspection date")
                        .setCalendarConstraints(constraintsBuilder.build())
                        .build();
        // on click set date
        datePicker.addOnPositiveButtonClickListener(selection -> {
            dateTxt.setText(datePicker.getHeaderText());
            date = datePicker.getHeaderText();
        });
        // on click show date picker
        addDateBtn.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "date_picker");
        });

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Inspection Time")
                .build();
        timePicker.addOnPositiveButtonClickListener(selection -> {
            // on click set time
            timeTxt.setText(timePicker.getHour() + ":" + timePicker.getMinute());
            time = timePicker.getHour() + ":" + timePicker.getMinute();
        });
        addTimeBtn.setOnClickListener(v -> {
            // on select time
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        resetBtn.setOnClickListener(v -> {
            // on click reset date and time
            dateTxt.setText("Not Set");
            timeTxt.setText("Not Set");
            date = "";
            time = "";
        });

    }
}
