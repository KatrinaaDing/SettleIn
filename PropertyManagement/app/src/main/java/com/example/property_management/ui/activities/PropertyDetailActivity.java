package com.example.property_management.ui.activities;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.property_management.ui.fragments.property.AmenitiesGroup;
import com.example.property_management.utils.Helpers;
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
import java.time.LocalDate;
import java.time.LocalTime;
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
    // inspection date and time
    String date = "";
    String time = "";
    // map fragment
    GoogleMap gMap;
    // constant
    String NO_DATE_HINT = "Date not set";
    String NO_TIME_HINT = "Time not set";
    String NO_DATE_TIME_HINT = "Not set";

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
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;

        // display date and time
        if (date != "")
            dateTxt.setText(date);
        else
            dateTxt.setText(NO_DATE_HINT);
        if (time != "")
            timeTxt.setText(time);
        else
            timeTxt.setText(NO_TIME_HINT);

        // ===== dialog =====
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(PropertyDetailActivity.this)
            .setTitle("Schedule Inspection")
            .setView(dialogView)
            .setPositiveButton("Save", (dialogInterface, i) -> {
                // disable dismiss dialog on click outside during saving
                ((AlertDialog) dialogInterface).setCanceledOnTouchOutside(false);
                this.setTitle("Saving...");
                // update to firebase
                updateInspectionDateTime(() -> {
                    // on success
                    inspectionTimeTxt.setText((date != "" || time != "")
                            ? formatDateTime(date, time)
                            : NO_DATE_TIME_HINT
                    );
                    this.userProperty.setInspectionDate(date == "" ? null : Helpers.stringToDate(date));
                    this.userProperty.setInspectionTime(time == "" ? null : Helpers.stringToTime(time));
                }, () -> {
                    // on error
                    // enable dismiss dialog on click outside
                    ((AlertDialog) dialogInterface).setCanceledOnTouchOutside(true);
                });

            }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                // reset date and time
                setInspectionDateTimeText();
                dialogInterface.dismiss();

            }).create();

        alertDialog.show();

        // ===== datePicker =====
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints
            .Builder()
            .setValidator(DateValidatorPointForward.now());
        MaterialDatePicker<Long> datePicker =
            MaterialDatePicker
                .Builder
                .datePicker()
                .setTitleText("Select Inspection date")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        // on click set date
        datePicker.addOnPositiveButtonClickListener(selection -> {
            String formattedDate = setInspectionDate(new Date(selection));
            dateTxt.setText(formattedDate);
        });
        // on click show date picker
        addDateBtn.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "date_picker");
        });
        // ===== timePicker =====
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText("Select Inspection Time")
            .build();
        timePicker.addOnPositiveButtonClickListener(selection -> {
            // on click set time
            String formattedTime = setInspectionTime(timePicker.getHour(), timePicker.getMinute());
            timeTxt.setText(formattedTime);
        });
        addTimeBtn.setOnClickListener(v -> {
            // on select time
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        resetBtn.setOnClickListener(v -> {
            // on click reset date and time
            dateTxt.setText(NO_DATE_HINT);
            timeTxt.setText(NO_TIME_HINT);
            date = "";
            time = "";
        });
    }

    private void getPropertyById(String propertyId) {
        setLoading(true);
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
                // set ui
                binding.detailAddressTxt.setText(property.getAddress());
                setInspectionDateTimeText();
                setIsInspected();
                setAmenitiesGroup(property);
                setCarousel(property);
                setLinkButton(property);
                setDistances(userProperty.getDistances());
                initMap();
                setLoading(false);
            })
            .addOnFailureListener(e -> {
                // if error happens, show error message and hide detail content
                Log.e("get-property-by-id-fail", e.getMessage());
                ScrollView detailContent = binding.detailContent;
                detailContent.setVisibility(View.GONE);
                setError(true);
            });
    }

    private void setIsInspected() {
        CheckBox isInspectedCheckbox = findViewById(R.id.isInspectedCheckbox);
        isInspectedCheckbox.setChecked(userProperty.getInspected());
        isInspectedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateIsInspectedStatus(isChecked, () -> {
                // on success
                buttonView.setChecked(isChecked);
                this.userProperty.setInspected(isChecked);
            });
        });
    }

    /**
     * Set inspection date and time to UI
     */
    private void setInspectionDateTimeText() {
        if (userProperty == null)
            return;
        // get fetched inspection date and time
        LocalDate date = userProperty.getInspectionDate();
        LocalTime time = userProperty.getInspectionTime();
        // get ui components
        Button editInspectionTimeBtn = binding.editInspectionTimeBtn;
        ConstraintLayout inspectionTimeLayout = binding.inspectionTimeLayout;
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;

        if (date != null || time != null) {
            String formattedDate = date == null ? "" : Helpers.dateFormatter(date);
            String formattedTime = time == null ? "" : Helpers.timeFormatter(time.getHour(), time.getMinute());
            // set field
            this.date = formattedDate;
            this.time = formattedTime;
            // set ui
            inspectionTimeTxt.setText(formatDateTime(formattedDate, formattedTime));
            inspectionTimeLayout.setVisibility(View.VISIBLE);
        } else {
            inspectionTimeTxt.setText(NO_DATE_TIME_HINT);
        }
        editInspectionTimeBtn.setOnClickListener(v -> showCustomDialog());
    }

    /**
     * Set amenities group data to UI
     */
    private void setAmenitiesGroup(Property property) {
        binding.detailPriceTxt.setText("$" + property.getPrice() + " per week");
        binding.amenitiesGroup.setValues(
                property.getNumBedrooms(),
                property.getNumBathrooms(),
                property.getNumParking());
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

    /**
     * update isInspected status to firebase
     * @param isChecked checkbox status
     * @param onSuccess callback on success
     */
    private void updateIsInspectedStatus(boolean isChecked, Runnable onSuccess) {
        // update ispected status to firebase
        HashMap<String, Object> updateDatePayload = new HashMap<>();
        updateDatePayload.put("properties." + this.propertyId + ".inspected", isChecked);
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        userRepository.updateUserFields(this.userId, updateDatePayload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                Log.i("update-inspected-failure", msg);
                onSuccess.run();
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("update-inspected-failure", msg);
            }
        });

    }
    /**
     * update inspection date and time to firebase
     * @param onSuccess callback on success
     * @param onError callback on error
     */
    private void updateInspectionDateTime(Runnable onSuccess, Runnable onError) {
        // update date to firebase
        HashMap<String, Object> updateDatePayload = new HashMap<>();
        updateDatePayload.put("properties." + this.propertyId + ".inspectionDate", this.date);
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        userRepository.updateUserFields(this.userId, updateDatePayload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                // update time to firebase
                HashMap<String, Object> updateTimePayload = new HashMap<>();
                updateTimePayload.put("properties." + propertyId + ".inspectionTime", time);
                FirebaseUserRepository userRepository = new FirebaseUserRepository();
                userRepository.updateUserFields(userId, updateTimePayload, new UpdateUserCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        new BasicSnackbar(findViewById(android.R.id.content),
                                "Inspection date and time added successfully",
                                "success");
                        Log.i("add-property-success", msg);
                        onSuccess.run();
                    }
                    @Override
                    public void onError(String msg) {
                        String errorMsg = "Error: " + msg;
                        new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                        Log.e("add-property-failure", msg);
                        onError.run();
                    }
                });
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
                onError.run();
            }
        });
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

    private String formatDateTime(String date, String time) {
        boolean noDate = date == null || date.equals("");
        boolean noTime = time == null || time.equals("");
        String separator = (noDate || noTime) ? "" : " on ";
        return time + separator + date;
    }

    private void setLoading(boolean isLoading) {
        binding.hintText.setText("Getting your property...");
        binding.detailContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.hintText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void setError(boolean isError) {
        binding.hintText.setText("Error getting your property. Please try again later.");
        binding.detailContent.setVisibility(isError ? View.GONE : View.VISIBLE);
        binding.hintText.setVisibility(isError ? View.VISIBLE : View.GONE);
    }

    private String setInspectionTime(int hour, int minute) {
        // store date in format for easily parsing by LocalTime
        time = Helpers.timeFormatter(hour, minute);
        return time;
    }

    private String setInspectionDate(Date newDate) {
        // display more sensible date format
        date = Helpers.dateFormatter(newDate);
        return date;
    }

    private void setDistances(HashMap<String, DistanceInfo> distances) {
        ArrayList<DistanceInfo> distanceInfoList = new ArrayList<>();
        for (DistanceInfo distanceInfo : distances.values()) {
            distanceInfoList.add(distanceInfo);
        }
        setDistanceRecycler(distanceInfoList);
    }
}
