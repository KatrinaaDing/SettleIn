package com.example.property_management.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.adapters.PropertyConditionAdapter;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.Property;
import com.example.property_management.data.RoomData;
import com.example.property_management.data.UserProperty;
import com.example.property_management.sensors.CalendarSensor;
import com.example.property_management.sensors.LocationSensor;
import com.example.property_management.ui.fragments.base.InfoButton;
import com.example.property_management.utils.DateTimeFormatter;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.utils.Helpers;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This activity is used to display the property detail
 */
public class PropertyDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_AND_STORAGE = 2;
    private boolean hasCameraPermission = false;
    private boolean hasWriteExternalStoragePermission = false;
    private ActivityPropertyDetailBinding binding;
    // add calendar event flag
    private boolean firstTimeAddingCalendarEvent = false;
    // property data
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
        assert this.propertyId != null;
        setTitle("Property Detail");

        // fetch property data from firebase
        getPropertyById(this.propertyId);

        // ================================== Components =======================================
        initInfoButton();

        // ===== dataCollectionBtn =====
        Button dataCollectionBtn = findViewById(R.id.dataCollectionBtn);
        dataCollectionBtn.setOnClickListener(view -> {
            Intent newIntent = new Intent(this, DataCollectionActivity.class);
            newIntent.putExtra("propertyId", this.propertyId);
            newIntent.putExtra("inspectedData", this.userProperty.getInspectedData());
            newIntent.putExtra("roomNum", this.property.getNumBedrooms());
            newIntent.putExtra("notes", this.userProperty.getNotes());
            newIntent.putExtra("roomNames", this.userProperty.getRoomNames());
            startActivity(newIntent);
        });

    }

    /**
     * Called when the fragment's activity has been resumed and is visible again.
     */
    @Override
    public void onResume() {
        super.onResume();
        // When the fragment view is being created, check if property details have been updated.
        SharedPreferences sharedPreferences = this.getSharedPreferences("propertyDetailUpdated",
                Context.MODE_PRIVATE);
        // If property details have been updated, refresh the data displayed.
        if (sharedPreferences.getBoolean("isUpdated", false)) {
            // Retrieve the updated property details by ID.
            getPropertyById(this.propertyId);
            // Clear the update flag from shared preferences.
            sharedPreferences.edit()
                    .remove("isUpdated")
                    .apply();

            // Hide the "no data" text view if property details are updated.
            TextView noDataTextView = findViewById(R.id.no_data_text_view);
            noDataTextView.setVisibility(View.GONE);
        }
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

    /**
     * Pop up instruction when click on info button
     */
    private void initInfoButton() {
        InfoButton infoButton = findViewById(R.id.distanceHintBtn);
        infoButton.setTitle("Distances from interested facilities");
        infoButton.setContent(
            "- Here list the distance from the property to the nearest interested facilities and locations. \n" +
            "- You can add more interested locations in the profile page.\n" +
            "- The facilities that are not within 5km will not be shown here.\n" +
            "- The locations that are too far from the property will not be shown here.\n" +
            "- For new added interested locations/facilities, the distance will be updated in 3 minutes.");

    }

    /**
     * Set distanceRecycler view in PropertyDetailActivity
     * @param distanceInfoList
     */
    private void setDistanceRecycler(ArrayList<DistanceInfo> distanceInfoList) {
        distanceRecycler = binding.distanceRecycler;
        // if no distance info, hide distance recycler
        TextView noInterestTxt = findViewById(R.id.noInterestTxt);
        if (distanceInfoList.isEmpty()) {
            distanceRecycler.setVisibility(View.GONE);
            noInterestTxt.setVisibility(View.VISIBLE);
            return;
        } else {
            distanceRecycler.setVisibility(View.VISIBLE);
            noInterestTxt.setVisibility(View.GONE);
        }

        // set LayoutManager and Adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        distanceRecycler.setLayoutManager(layoutManager);
        distanceAdapter = new DistanceAdapter(this, distanceInfoList, property.getAddress());
        distanceRecycler.setAdapter(distanceAdapter);

        // dynamically set height according to the number of items (max 400dp)
        int itemHeight = Helpers.dpToPx(this, 57);
        int totalHeight = distanceInfoList.size() * itemHeight + Helpers.dpToPx(this, 40);
        int maxHeight = Helpers.dpToPx(this, 400);
        totalHeight = Math.min(totalHeight, maxHeight);

        ViewGroup.LayoutParams params = distanceRecycler.getLayoutParams();
        params.height = totalHeight;
        distanceRecycler.setLayoutParams(params);
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

    /**
     * Show dialog to select inspection date and time
     */
    private void showCustomDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_add_inspection_time_dialog, null);

        Button addDateBtn = dialogView.findViewById(R.id.addDateBtn);
        Button addTimeBtn = dialogView.findViewById(R.id.addTimeBtn);
        Button resetBtn = dialogView.findViewById(R.id.resetBtn);
        TextView dateTxt = dialogView.findViewById(R.id.dateTxt);
        TextView timeTxt = dialogView.findViewById(R.id.timeTxt);
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;

        // display date and time
        if (date == null || date.equals(""))
            dateTxt.setText(NO_DATE_HINT);
        else
            dateTxt.setText(date);
        if (time == null || time.equals(""))
            timeTxt.setText(NO_TIME_HINT);
        else
            timeTxt.setText(time);

        // ===== dialog =====
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(PropertyDetailActivity.this)
                .setTitle("Schedule Inspection")
                .setView(dialogView)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    // disable dismiss dialog on click outside during saving
                    ((AlertDialog) dialogInterface).setCanceledOnTouchOutside(false);
                    // update to firebase
                    updateInspectionDateTime(() -> {
                        // on success
                        inspectionTimeTxt.setText((!"".equals(date) || !"".equals(time))
                                ? formatDateTime(date, time)
                                : NO_DATE_TIME_HINT
                        );
                        setAddToCalendarButton();
                        this.userProperty.setInspectionDate(date);
                        this.userProperty.setInspectionTime(time);
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
        Long currentDate = "".equals(date)
                ? MaterialDatePicker.todayInUtcMilliseconds()
                : DateTimeFormatter.localDateToMillis(DateTimeFormatter.stringToDate(date));
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints
                .Builder()
                .setValidator(DateValidatorPointForward.now());
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker
                        .Builder
                        .datePicker()
                        .setSelection(currentDate)
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
        Log.i("date-time", "reseting date time to" + this.date + "," + this.time);
        // ===== timePicker =====
        int currentHour = "".equals(time)
                ? LocalTime.now().getHour()
                : DateTimeFormatter.stringToTime(time).getHour();
        int currentMinute = "".equals(time)
                ? LocalTime.now().getMinute()
                : DateTimeFormatter.stringToTime(time).getMinute();
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Inspection Time")
                .build();
        timePicker.addOnPositiveButtonClickListener(selection -> {
            // on click set time
            // if the time is outside working hours, pop up a dialog to confirm
            String formattedTime = setInspectionTime(timePicker.getHour(), timePicker.getMinute());
            if (timePicker.getHour() < 9|| timePicker.getHour() > 17) {
                new MaterialAlertDialogBuilder(PropertyDetailActivity.this)
                        .setTitle("Confirm Inspection Time")
                        .setMessage("The inspection time is outside working hours. " +
                                "Are you sure you want to set the inspection time to " +
                                formattedTime + "?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            timeTxt.setText(formattedTime);
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            // reset time
                            timePicker.show(getSupportFragmentManager(), "time_picker");
                        })
                        .show();
            } else {
                timeTxt.setText(formattedTime);
            }
        });
        addTimeBtn.setOnClickListener(v -> {
            // on select time
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        resetBtn.setOnClickListener(v -> {
            // on click reset date and time
            dateTxt.setText(NO_DATE_HINT);
            timeTxt.setText(NO_TIME_HINT);
            this.date = "";
            this.time = "";
        });
    }

    /**
     * Given propertyId, fetch property information from firebase and set to UI
     * @param propertyId
     */
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
                    setCreatedAtTime();
                    setAddToCalendarButton();
                    setIsInspected();
                    setAmenitiesGroup(property);
                    setCarousel(property);
                    setLinkButton(property);
                    setDistances(userProperty.getDistances());
                    setInspectedData(userProperty);
                    initMap();
                    setLoading(false);

                    //setInspectedData after fetch
                    requestStoragePermission();
                })
                .addOnFailureListener(e -> {
                    // if error happens, show error message and hide detail content
                    Log.e("get-property-by-id-fail", "failed to get property: " + e.getMessage());
                    ScrollView detailContent = binding.detailContent;
                    detailContent.setVisibility(View.GONE);
                    setError(true);
                });
    }

    /**
     * Set creation time of when the property is added
     */
    private void setCreatedAtTime() {
        TextView createdAtText = findViewById(R.id.createdAtText);
        createdAtText.setText("Added at " + DateTimeFormatter.dateTimeFormatter(userProperty.getCreatedAt()));
    }

    /**
     * Tick isInspectedCheckbox if property is inspected, otherwise untick
     */
    private void setIsInspected() {
        CheckBox isInspectedCheckbox = findViewById(R.id.isInspectedCheckbox);
        isInspectedCheckbox.setChecked(userProperty.getInspected());
        isInspectedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateIsInspectedStatus(isChecked, () -> {
                // on success
                buttonView.setChecked(isChecked);
                this.userProperty.setInspected(isChecked);
                SharedPreferences sharedPreferences = getSharedPreferences("propertyUpdated",
                        MODE_PRIVATE);
                sharedPreferences
                    .edit()
                    .putBoolean("isUpdated", true)
                    .apply();
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
        String date = userProperty.getInspectionDate();
        String time = userProperty.getInspectionTime();
        // get ui components
        Button editInspectionTimeBtn = binding.editInspectionTimeBtn;
        ConstraintLayout inspectionTimeLayout = binding.inspectionTimeLayout;
        TextView inspectionTimeTxt = binding.detailInspectionTimeTxt;
        // set field
        this.date = date;
        this.time = time;
        // set ui
        if (!date.equals("") || !time.equals("")) {
            inspectionTimeTxt.setText(formatDateTime(date, time));
            inspectionTimeLayout.setVisibility(View.VISIBLE);
        } else {
            inspectionTimeTxt.setText(NO_DATE_TIME_HINT);
        }
        Log.i("date-time", "reseting date time to" + this.date + "," + this.time);
        editInspectionTimeBtn.setOnClickListener(v -> showCustomDialog());
    }

    /**
     * Set price and amenities group data to UI
     */
    private void setAmenitiesGroup(Property property) {
        // set price
        if (property.getPrice() == 0) {
            binding.detailPriceTxt.setText("Price not available");
        } else {
            binding.detailPriceTxt.setText("$" + property.getPrice() + " per week");
        }
        // set amenities
        binding.amenitiesGroup.setValues(
                property.getNumBedrooms(),
                property.getNumBathrooms(),
                property.getNumParking());
    }

    /**
     * Set images to carousel
     * @param property
     */
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
            // if only one image, set linear layout to center the image
            if (adapter.getItemCount() == 1) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);
            }
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Set link button href
     * @param property
     */
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
     * set add to calendar button if inspection date (and time if any) is added
     */
    private void setAddToCalendarButton() {
        MaterialButton addToCalendarBtn = findViewById(R.id.addToCalendarBtn);
        CalendarSensor calendarSensor = new CalendarSensor(this, null);
        // show add to calendar button if date is set
        if (date != null && !date.equals("")) {
            addToCalendarBtn.setVisibility(View.VISIBLE);
        }
        addToCalendarBtn.setOnClickListener(view -> {
            // if first time adding calendar event, check calendar permission
            if (!calendarSensor.getHasPermission(this)) {
                firstTimeAddingCalendarEvent = true;
                calendarSensor.requiresPermissions();
            } else {
                firstTimeAddingCalendarEvent = false;
                addInspectionToCalendar();
            }

        });
    }

    /**
     * add inspection event to calendar
     */
    private void addInspectionToCalendar() {
        CalendarSensor calendarSensor = new CalendarSensor(this, null);
        String address = property.getAddress();
        String title = "Inspection at " + address;
        String description = "Inspection at " + address + " on " + date + " " + time;
        try {
            calendarSensor.createEvent(date, time, 30, title, description, address);
        } catch (Exception e) {
            new BasicSnackbar(findViewById(android.R.id.content), e.getMessage(), "error");
        }
    }
    /**
     * update isInspected status to firebase
     * @param isChecked checkbox status
     * @param onSuccess callback on success
     */
    private void updateIsInspectedStatus(boolean isChecked, Runnable onSuccess) {
        // update inspected status to firebase
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // request location permission
        LocationSensor locationSensor = new LocationSensor(this, null);
        locationSensor.requiresPermissions(this);

        // render map
        if (property != null) {
            LatLng propertyLatLng = new LatLng(property.getLat(), property.getLng());
            // move camera to property location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(propertyLatLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(propertyLatLng, 13));
            // add property marker
            googleMap.addMarker(new MarkerOptions().position(propertyLatLng).title(property.getAddress()));
            // add user Location
            if (locationSensor.hasPermission(this)) {
                googleMap.setMyLocationEnabled(true);
            }

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

            // go to google map or web version on marker click
            googleMap.setOnMarkerClickListener(marker -> {
                // on click open google map
                String address = property.getAddress();
                Helpers.openInGoogleMap(address, this);
                return true;
            });
        }
        this.gMap = googleMap;
    }

    /**
     * Request calendar permission
     * @param requestCode The request code passed in {@link #requestPermissions(
     * android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CalendarSensor calendarSensor = new CalendarSensor(this, null);
        // calendar permission granted asynchroneously, need to check before adding event
        if (calendarSensor.getMyCalendarRequestCode() == requestCode) {

            // if it's first time permission granted, need to explicitly add event here
            if (firstTimeAddingCalendarEvent) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted
                        addInspectionToCalendar();
                } else {
                    // Permission denied
                    new BasicSnackbar(findViewById(android.R.id.content),
                            "Permission denied. Please go to settings and enable calendar permission.",
                            "error");
                }
            }
        }else if (MY_PERMISSIONS_REQUEST_CAMERA_AND_STORAGE == requestCode){
            // if cancel
            if (grantResults.length > 0) {
                // check result
                for (int i = 0; i < permissions.length; i++) {
                    switch (permissions[i]) {
                        case Manifest.permission.CAMERA:
                            hasCameraPermission = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                            break;
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                            hasWriteExternalStoragePermission = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                            break;
                    }
                }
                // if get permission
                if (hasWriteExternalStoragePermission) {
                    setInspectedData(userProperty);
                }
            }
        }
    }

    /**
     * Format date and time to display
     * @param date
     * @param time
     * @return formatted date and time
     */
    private String formatDateTime(String date, String time) {
        boolean noDate = date == null || date.equals("");
        boolean noTime = time == null || time.equals("");
        String separator = (noDate || noTime) ? "" : " on ";
        return time + separator + date;
    }

    /**
     * Set loading state for PropertyDetailActivity
     * @param isLoading true if loading, false if not
     */
    private void setLoading(boolean isLoading) {
        binding.hintText.setText("Getting your property...");
        binding.detailContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.hintText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Set error state for PropertyDetailActivity
     * @param isError true if error, false if not
     */
    private void setError(boolean isError) {
        binding.hintText.setText("Error getting your property. Please try again later.");
        binding.detailContent.setVisibility(isError ? View.GONE : View.VISIBLE);
        binding.hintText.setVisibility(isError ? View.VISIBLE : View.GONE);
    }

    /**
     * Set formatted inspection time
     * @param hour
     * @param minute
     * @return formatted inspection time
     */
    private String setInspectionTime(int hour, int minute) {
        // store date in format for easily parsing by LocalTime
        time = DateTimeFormatter.timeFormatter(hour, minute);
        return time;
    }

    /**
     * Set formatted inspection date
     * @param newDate
     * @return formatted inspection date
     */
    private String setInspectionDate(Date newDate) {
        // display more sensible date format
        date = DateTimeFormatter.dateFormatter(newDate);
        return date;
    }

    /**
     * Set distance data to DistanceAdapter
     * @param distances HashMap of distance data
     */
    private void setDistances(HashMap<String, DistanceInfo> distances) {
        ArrayList<DistanceInfo> distanceInfoList = new ArrayList<>();
        for (DistanceInfo distanceInfo : distances.values()) {
            distanceInfoList.add(distanceInfo);
        }
        setDistanceRecycler(distanceInfoList);
    }

    /**
     * Sets the data for the inspected rooms.
     * @param userProperty The property containing room inspection data.
     */
    private void setInspectedData(UserProperty userProperty){
        // Retrieve the inspected data for all rooms.
        HashMap<String, RoomData> inspectedData = userProperty.getInspectedData();

        for (String roomName : inspectedData.keySet()){
            Log.d("inspectedData in propertydetail", inspectedData.get(roomName).toString());
        }

        // Get the inspection notes.
        String notes = userProperty.getNotes();
        TextView savedNotesTextView = findViewById(R.id.saved_notes);

        // Display inspection notes or a default message if notes are empty.
        if (notes == null || notes.trim().isEmpty()) {
            savedNotesTextView.setText("No inspected note, you can write your note in inspection!");
        } else {
            savedNotesTextView.setText(notes);
        }

        List<String> roomNames = userProperty.getRoomNames();

        // Check for existing inspected room data.
        if (roomNames == null || roomNames.isEmpty()){
            // If no room data is found, show a message prompting the user to start inspection.
            TextView noDataTextView = findViewById(R.id.no_data_text_view);
            noDataTextView.setText("No inspected data found, lets go to conduct your first inspection!");
            noDataTextView.setVisibility(View.VISIBLE);

            // Hide the RecyclerView if there are no rooms to display.
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setVisibility(View.GONE);
        } else {
            // If room data exists, prepare the RecyclerView to display it.
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize lists to hold room data for the adapter.
            ArrayList<ArrayList<String>> imagesPerRoom = new ArrayList<>();
            ArrayList<Float> brightnessList = new ArrayList<>();
            ArrayList<Float> noiseList = new ArrayList<>();
            ArrayList<String> windowOrientationList = new ArrayList<>();

            // Populate lists with room data.
            for (String roomName : roomNames) {
                RoomData roomData = inspectedData.get(roomName);
                if (roomData != null) {
                    imagesPerRoom.add(roomData.getImages());
                    brightnessList.add(roomData.getBrightness());
                    noiseList.add(roomData.getNoise());
                    windowOrientationList.add(roomData.getWindowOrientation());
                }
            }

            // Set up the adapter with the room data.
            PropertyConditionAdapter adapter = new PropertyConditionAdapter(
                    roomNames,
                    imagesPerRoom,
                    brightnessList,
                    noiseList,
                    windowOrientationList
            );
            recyclerView.setAdapter(adapter);
            Log.d("RecyclerViewSetup", "RecyclerView should be set. Room count: " + roomNames.size());
        }
    }

    /**
     * Requests permission to write to external storage if it has not already been granted.
     */
    private void requestStoragePermission() {
        // Check if write external storage permission has been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request the write external storage permission.
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    MY_PERMISSIONS_REQUEST_CAMERA_AND_STORAGE);
        } else {
            // Set flags indicating that camera and storage permissions have been granted.
            hasCameraPermission = true;
            hasWriteExternalStoragePermission = true;
        }
    }
}
