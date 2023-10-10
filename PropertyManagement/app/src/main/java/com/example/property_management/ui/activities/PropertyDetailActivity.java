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
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebasePropertyRepository;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetPropertyByIdCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.Property;
import com.example.property_management.data.UserProperty;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PropertyDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityPropertyDetailBinding binding;

    private String userId;
    private String propertyId;

    private Property property;
    private UserProperty userProperty;

    DistanceAdapter distanceAdapter;

    RecyclerView distanceRecycler;

    String date = "";

    String time = "";
    GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Property Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get userId
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        FirebaseUser user = firebaseAuthHelper.getCurrentUser();
        assert user != null;
        this.userId = user.getUid();

        // get property id
        Intent intent = getIntent();
        this.propertyId = intent.getStringExtra("property_id"); // -1 is default value
        setTitle("Property Detail (" + this.propertyId + ")");

        // fetch property data from firebase
        getPropertyById(this.propertyId);


        // ================================== Components =======================================

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

    /**
     * Initialize map fragment
     */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.propertyMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("PropertyDetailActivity", "Map fragment is null.");
        }
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
                            // update date and time to firebase
                            updateInspectionDateTime();
                        // no date and time
                        } else {
                            addInspectionTimeBtn.setVisibility(View.VISIBLE);
                            inspectionTimeLayout.setVisibility(View.GONE);
                        }

                        dialogInterface.dismiss();

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
            // display more sensible date format
            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
            dateTxt.setText(formattedDate);
            date = formattedDate;
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
            // store date in format for easily parsing by LocalTime
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
            timeTxt.setText(formattedTime);
            time = formattedTime;
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

    private void getPropertyById(String propertyId) {
//        FirebasePropertyRepository firebasePropertyRepository = new FirebasePropertyRepository();
//        firebasePropertyRepository.getPropertyById(propertyId, new GetPropertyByIdCallback() {
//            @Override
//            public void onSuccess(Property property) {
//                // if success, set property data to UI
//                PropertyDetailActivity.this.property = property;
//                binding.detailAddressTxt.setText(property.getAddress());
//                setAmenitiesGroup(property);
//                setCarousel(property);
//                setLinkButton(property);
//            }
//
//            @Override
//            public void onError(String msg) {
//                // if error happens, show error message and hide detail content
//                ScrollView detailContent = binding.detailContent;
//                TextView errorMessage = binding.errorMessage;
//                detailContent.setVisibility(View.GONE);
//                errorMessage.setVisibility(View.VISIBLE);
//            }
//        });
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.getPropertyById(propertyId)
                .addOnSuccessListener(result -> {
                    Map<String, Object> resultObj = (Map<String, Object>) result;
                    // if success, set property data to UI
                    Log.i("get-property-by-id-success",
                            "successfully get property data " +
                            "and user collected property data");
                    property = (Property) resultObj.get("propertyData");
                    userProperty = (UserProperty) resultObj.get("userPropertyData");
                    binding.detailAddressTxt.setText(property.getAddress());
                    setAmenitiesGroup(property);
                    setCarousel(property);
                    setLinkButton(property);
                    // TODO
//                setInspectionDateTime(userProperty);
                    initMap();
                })
                .addOnFailureListener(e -> {
                    // if error happens, show error message and hide detail content
                    Log.e("get-property-by-id-fail", e.getMessage());
                    ScrollView detailContent = binding.detailContent;
                    TextView errorMessage = binding.errorMessage;
                    detailContent.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.VISIBLE);
                });
    }

    // set amenities group data to UI
    private void setAmenitiesGroup(Property property) {
        binding.detailPriceTxt.setText("$" + property.getPrice() + " per week");
        binding.amenitiesGroup.setValues(property.getNumBedrooms(), property.getNumBathrooms(), property.getNumParking());
    }

    // set images to carousel
    private void setCarousel(Property property) {
        ArrayList<String> images = property.getImages();
        RecyclerView recyclerView = findViewById(R.id.recycler);
        if (images == null || images.isEmpty()) {
            // if no image, hide carousel
            recyclerView.setVisibility(View.GONE);
        } else {
            // set adapter
            CarouselAdapter adapter = new CarouselAdapter(PropertyDetailActivity.this, images);

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
        }
    }

    // set link button href
    private void setLinkButton(Property property) {
        Button linkButton = findViewById(R.id.linkButton);
        String href = property.getHref();
        if (href == null || href == "") {
            // if no href, hide linkButton
            linkButton.setVisibility(View.GONE);
        } else {

            // on click redirect to property ad site
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an Intent to open a web browser with the specified URL
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));

                    // Start the web browser activity
                    startActivity(intent);
                }
            });
        }
    }

    private void setInspectionDateTime(UserProperty userProperty) {
        // ===== inspection time =====
        Button addInspectionTimeBtn = binding.addInspectionTimeBtn;
        ConstraintLayout inspectionTimeLayout = binding.inspectionTimeLayout;
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;
        Button editInspectionTimeBtn = binding.editInspectionTimeBtn;

        // Conditionally display Button or TextView
        // TODO
//        String inspectionDate = userProperty.getInspectionDate();
//        String inspectionTime = userProperty.getInspectionTime();
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
    }

    /**
     * update inspection date and time to firebase
     */
    private void updateInspectionDateTime() {
        // update date to firebase
        if (this.date == "") {
            HashMap<String, Object> updateDatePayload = new HashMap<>();
            updateDatePayload.put("properties." + this.propertyId + ".inspectionDate", this.date);
            FirebaseUserRepository userRepository = new FirebaseUserRepository();
            userRepository.updateUserFields(this.userId, updateDatePayload, new UpdateUserCallback() {
                @Override
                public void onSuccess(String msg) {
                    new BasicSnackbar(findViewById(android.R.id.content),
                            "Inspection date added successfully",
                            "success");
                }

                @Override
                public void onError(String msg) {
                    String errorMsg = "Error: " + msg;
                    new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                    Log.e("add-property-failure", msg);
                }
            });
        }

        // update time to firebase
        if (this.time == "") {
            HashMap<String, Object> updateTimePayload = new HashMap<>();
            updateTimePayload.put("properties." + this.propertyId + ".inspectionTime", this.time);
            FirebaseUserRepository userRepository = new FirebaseUserRepository();
            userRepository.updateUserFields(this.userId, updateTimePayload, new UpdateUserCallback() {
                @Override
                public void onSuccess(String msg) {
                    new BasicSnackbar(findViewById(android.R.id.content),
                            "Inspection time added successfully",
                            "success");
                }

                @Override
                public void onError(String msg) {
                    String errorMsg = "Error: " + msg;
                    new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                    Log.e("add-property-failure", msg);
                }
            });
        }
    }


    /**
     * Add marker to map showing current property location
     * @param googleMap google map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (property != null) {
            LatLng propertyLatLng = new LatLng(property.getLat(), property.getLng());
            // move camera to property location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(propertyLatLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(propertyLatLng, 13));
            // add property marker
            googleMap.addMarker(new MarkerOptions().position(propertyLatLng).title(property.getAddress()));

            // disable scrolling in scrollview when map is moving
            // reference: https://stackoverflow.com/questions/50505188/why-can-use-getparent-requ
            // estdisallowintercepttouchevent-true-disallow-inte
            googleMap.setOnCameraMoveListener(() -> {
                ScrollView detailContent = binding.detailContent;
                detailContent.requestDisallowInterceptTouchEvent(true);
            });
            // enable scrolling scrollview when map is idle
            googleMap.setOnCameraIdleListener(() -> {
                ScrollView detailContent = binding.detailContent;
                detailContent.requestDisallowInterceptTouchEvent(false);
            });
        }
        this.gMap = googleMap;
    }
}
