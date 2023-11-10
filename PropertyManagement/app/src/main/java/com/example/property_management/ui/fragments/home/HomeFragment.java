package com.example.property_management.ui.fragments.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.adapters.PropertyCardAdapter;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.FragmentHomeBinding;
import com.example.property_management.sensors.LocationSensor;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.ui.fragments.base.PropertyCard;
import com.example.property_management.utils.DateTimeFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Home fragment
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback, PropertyCardAdapter.EventListener {

    private FragmentHomeBinding binding;

    private ArrayList<Property> allProperties;

    private boolean sortAscending = true;
    private boolean sortTypeIsTime = true;
    // map fragment
    GoogleMap gMap;
    private final String ASCENDING_LABEL = "Asc";
    private final String DESCENDING_LABEL = "Desc";
    private final String OLDEST_LABEL = "Oldest";
    private final String NEWEST_LABEL = "Newest";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        waitForAuth();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh properties when user returns to home fragment
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("propertyUpdated",
                Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isUpdated", false)) {
            getAllProperties();
            sharedPreferences.edit()
                .remove("isUpdated")
                .apply();
        }
    }

    /**
     * Show hint when there is no property, hide hint otherwise
     * @param hasProperty
     */
    public void onHasProperties(boolean hasProperty) {
        if (hasProperty) {
            binding.hint.setVisibility(View.GONE);
        } else {
            binding.hint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show alert when user is near a property that has not been inspected and it's close
     * to the inspection date and time
     */
    public void showNearbyInspectionAlert(ArrayList<Property> allProperties) {
        // get system date and time
        Date now = new Date();

        // loop all properties and check if there is any property that is close to inspection date
        for (Property property : allProperties) {
            AtomicBoolean showInspectionAlert = new AtomicBoolean(false);
            // skip inspected properties
            if (property.getInspected()) {
                continue;
            }
            // skip if user has set preference to not show this dialog again
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("showInspectAlertPref", Context.MODE_PRIVATE);
            if (!sharedPreferences.getBoolean(property.getPropertyId(), true)) {
                continue;
            }

            // get inspection date and time. skip if none.
            LocalDate inspectionDate = property.getInspectionDate();
            LocalTime inspectionTime = property.getInspectionTime();
            if (inspectionDate == null || inspectionTime == null) {
                continue;
            }

            // if the difference is less than 10min, show dialog
            long inspectionDateTimeMillis = DateTimeFormatter.localDateToMillis(LocalDateTime.of(inspectionDate, inspectionTime));
            long nowMillis = now.getTime();
            if (Math.abs(inspectionDateTimeMillis - nowMillis) < 10 * 60 * 1000) {
                // get current location
                LocationSensor locationSensor = new LocationSensor(getActivity(), null);
                locationSensor.requiresPermissions(getActivity());

                if (locationSensor.hasPermission(getActivity())) {
                    locationSensor.getCurrentLocation().addOnSuccessListener(myLocation -> {
                        if (myLocation != null) {
                            // get distance between current location and property location
                            Location propertyLocation = new Location("");
                            propertyLocation.setLatitude(property.getLat());
                            propertyLocation.setLongitude(property.getLng());

                            // if distance is less than 500 meters, show dialog
                            float distance = myLocation.distanceTo(propertyLocation);
                            if (distance < 500) {
                                showInspectionDialog(
                                        property.getPropertyId(),
                                        "You are near " + property.getAddress() +
                                                ". Would you like to start the inspection now?"
                                );
                            }
                        }
                    });
                } else {
                    // if no location permission, show dialog
                    showInspectionDialog(
                            property.getPropertyId(),
                            "Almost there! You're within 10 minutes of the scheduled inspection for " +
                                    property.getAddress() +
                                    ". Would you like to start the inspection now?"
                    );
                    showInspectionAlert.set(true);
                }
                showInspectionAlert.set(true);
            }
            // only show one dialog
            if (showInspectionAlert.get()) {
                break;
            }
        }
    }

    /**
     * show inspection dialog and take user to property detail page if user clicks "Yes".
     * If user clicks "Do not show again", set preference to not show this dialog again.
     * @param propertyId the property id of the property to be inspected
     * @param message the message to be displayed in the dialog
     */
    private void showInspectionDialog(String propertyId, String message) {
        BasicDialog dialog = new BasicDialog(true,
                "Ready for inspection?",
                message + "\n\nClose this alert by clicking outside of it.",
                "Do not show again for this property",
                "Yes, let's go!");
        dialog.setCallback(new BasicDialogCallback() {
            @Override
            public void onLeftBtnClick() {
                // set preference to not show this dialog again
                SharedPreferences sharedPreferences = getContext()
                        .getSharedPreferences("showInspectAlertPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(propertyId, false);
                editor.apply();
            }
            @Override
            public void onRightBtnClick() {
                Intent intent = new Intent(getActivity(), PropertyDetailActivity.class);
                intent.putExtra("property_id", propertyId);
                startActivity(intent);
            }
        });
        dialog.show(getChildFragmentManager(), "inspection alert dialog");
    }
    /**
     * Initialize the filter menu
     */
    private void initFilterMenu() {
        TextInputLayout filterMenu = binding.filterMenu;
        AutoCompleteTextView filterTypeMenu = (AutoCompleteTextView) filterMenu.getEditText();
        filterTypeMenu.setText("All", false);
        filterTypeMenu.setOnItemClickListener((adapterView, view, i, l) -> {
            String selected = (String) adapterView.getItemAtPosition(i);

            if (selected.equals("All")) {
                // on select "All", show all properties
                renderProperties(allProperties);
                // reset map
                if (gMap != null) {
                    gMap.clear();
                    addMarkersToMap(gMap, allProperties);
                }
            } else {
                // otherwise, filter properties by "inspected" status
                // reference: https://stackoverflow.com/questions/9146224/arraylist-filter
                boolean filterIsInspected = selected.equals("Inspected");
                ArrayList<Property> filteredProperties = allProperties.stream()
                        .filter(property -> property.getInspected() == filterIsInspected)
                        .collect(Collectors.toCollection(ArrayList::new));
                // render filtered properties
                renderProperties(filteredProperties);
                // reset map
                if (gMap != null) {
                    gMap.clear();
                    addMarkersToMap(gMap, filteredProperties);
                }
            }
        });
    }

    /**
     * Initialize the sort menu
     */
    private void initSortMenu() {
        // handle select sort type
        TextInputLayout sortMenu = binding.sortMenu;
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) sortMenu.getEditText();
        autoCompleteTextView.setText("Create Time", false);
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            String selected = (String) adapterView.getItemAtPosition(i);
            sortProperties(selected);
        });

        // handle toggle sort order
        MaterialButton sortOrderBtn = binding.sortOrderButton;
        sortOrderBtn.setOnClickListener(view -> {
            sortAscending = !sortAscending;
            // change icon
            updateSortOrderBtnIcon();
            // sort and render properties
            sortProperties(autoCompleteTextView.getText().toString());
        });
    }

    /**
     * Sort and rendre properties by the given sort type and existing sort order
     * @param sortType the sort type
     */
    private void sortProperties(String sortType) {
        MaterialButton sortOrderBtn = binding.sortOrderButton;

        // create comparator
        Comparator<Property> comparator = null;
        switch (sortType) {
            case "Price":
                comparator = Comparator.comparing(Property::getPrice);
                sortTypeIsTime = false;
                break;
            case "Beds":
                comparator = Comparator.comparing(Property::getNumBedrooms);
                sortTypeIsTime = false;
                break;
            case "Bathrooms":
                comparator = Comparator.comparing(Property::getNumBathrooms);
                sortTypeIsTime = false;
                break;
            case "Parking Spaces":
                comparator = Comparator.comparing(Property::getNumParking);
                sortTypeIsTime = false;
                break;
            default:
                // sort by create time by default
            case "Create Time":
                comparator = Comparator.comparing(Property::getCreatedAt);
                // default "sort by create time" is new to old
                comparator = comparator.reversed();
                sortTypeIsTime = true;
        }
        if (comparator == null) {
            return;
        }
        // update order label
        updateSortOrderBtnIcon();
        // apply sort order
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        allProperties.sort(comparator);
        renderProperties(allProperties);

    }

    /**
     * Initialize the toolbar
     */
    private void initToolbar() {
        ConstraintLayout toolbar = binding.toolbar;
        initFilterMenu();
        initSortMenu();
        initViewSelectionGroup();
        toolbar.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize the view selection group buttons
     */
    private void initViewSelectionGroup() {
        MaterialButtonToggleGroup listMapToggleButtons = binding.listMapToggleButtons;
        // select List view by default
        listMapToggleButtons.check(R.id.listViewBtn);
        listMapToggleButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // show corresponding view
            if (isChecked) {
                if (checkedId == R.id.listViewBtn) {
                    binding.propertiesRecyclerView.setVisibility(View.VISIBLE);
                    binding.sortMenuContainer.setVisibility(View.VISIBLE);
                    binding.propertiesMapView.setVisibility(View.INVISIBLE);

                } else if (checkedId == R.id.mapViewBtn) {
                    binding.propertiesRecyclerView.setVisibility(View.GONE);
                    binding.sortMenuContainer.setVisibility(View.GONE);
                    binding.propertiesMapView.setVisibility(View.VISIBLE);
                    binding.selectedPropertyCard.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Initialize map fragment
     */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.propertiesMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("home-fragment", "Map fragment is null.");
        }
    }

    /**
     * Add marker to map showing current property location
     * @param googleMap google map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // request location permission
        LocationSensor locationSensor = new LocationSensor(getActivity(), null);
        locationSensor.requiresPermissions(getActivity());
        // add user Location
        if (locationSensor.hasPermission(getActivity())) {
            googleMap.setMyLocationEnabled(true);
            // move camera to my location
            locationSensor.getCurrentLocation().addOnSuccessListener(myLocation -> {
                if (myLocation != null) {
                    LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12));
                }
            });
        } else {
            // if not location, set view to Australia by default
            LatLng australia = new LatLng(-25.2744, 133.7751);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(australia));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(australia, 4));
        }
        addMarkersToMap(googleMap, allProperties);

        // show property card onclick markers
        googleMap.setOnMarkerClickListener(marker -> {
            Property clickedProperty = (Property) marker.getTag();
            if (clickedProperty == null) {
                return false;
            }
            // show property card on click open google map
            PropertyCard propertyCard = binding.selectedPropertyCard;
            String image;
            if (clickedProperty.getImages() == null || clickedProperty.getImages().isEmpty()) {
                image = null;
            } else {
                image = clickedProperty.getImages().get(0);
            }
            propertyCard.setValues(
                    clickedProperty.getPropertyId(),
                    clickedProperty.getAddress(),
                    clickedProperty.getPrice(),
                    image,
                    clickedProperty.getInspected(),
                    clickedProperty.getNumBedrooms(),
                    clickedProperty.getNumBathrooms(),
                    clickedProperty.getNumParking()
            );

            propertyCard.bringToFront();
            propertyCard.setVisibility(View.VISIBLE);
            return true;
        });

        // hide selected property card when map is clicked
        googleMap.setOnMapClickListener(latLng -> {
            binding.selectedPropertyCard.setVisibility(View.GONE);
        });

        this.gMap = googleMap;
    }

    /**
     * Add markers to show added properties on map
     * @param googleMap
     * @param properties
     */
    private void addMarkersToMap(GoogleMap googleMap, ArrayList<Property> properties) {
        // a set for tracking duplicate locations
        HashSet<LatLng> addedMarkers = new HashSet<>();
        float displacement = 0.00001f;

        for (Property property : properties) {
            // get coordinate of property
            double latitude = property.getLat();
            double longitude = property.getLng();
            LatLng propertyLatLng = new LatLng(latitude, longitude);

            // slightly shift the marker location if a marker with the same location already beed added
            if (addedMarkers.contains(propertyLatLng)) {
                double newLat = latitude + (Math.random() - 0.5) * displacement;
                double newLng = longitude + (Math.random() - 0.5) * displacement;
                propertyLatLng = new LatLng(newLat, newLng);
            }

            // add property marker
            // reference: https://developers.google.com/maps/documentation/android-sdk/marker#maps_android_markers_tag_sample-java
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(propertyLatLng)
                    .title(property.getAddress());
            // set color to indicate if property is inspected
            if (property.getInspected()) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
            // add marker to map
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(property);
            // add marker to addedMarkers set
            addedMarkers.add(propertyLatLng);
        }
    }

    /**
     * Update text on sortOrderBtn
     */
    private void updateSortOrderBtnIcon() {
        MaterialButton sortOrderBtn = binding.sortOrderButton;
        if (sortAscending) {
            sortOrderBtn.setText(sortTypeIsTime ? NEWEST_LABEL : ASCENDING_LABEL);
        } else {
            sortOrderBtn.setText(sortTypeIsTime ? OLDEST_LABEL : DESCENDING_LABEL);
        }
    }

    /**
     * Wait for firebase authentication to finish
     */
    private void waitForAuth() {
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (isAdded() && !isRemoving() && !isDetached()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (binding != null && user != null) {
                    // User is signed in
                    binding.loadingText.setText("Loading properties...");
                    getAllProperties();
                } else {
                    // User is signed out
                    binding.loadingText.setText("Sign in failed. Please try again.");
                }
            }
        });

    }

    /**
     * Get all properties from the db and display them in the recycler view
     */
    private void getAllProperties() {
        FirebaseUserRepository db = new FirebaseUserRepository();
        db.getAllUserProperties(new GetAllUserPropertiesCallback() {
            @Override
            public void onSuccess(ArrayList<Property> properties) {
                allProperties = properties;
                // if the view is valid, sort and render properties
                if (binding != null && binding.propertiesRecyclerView != null) {
                    sortProperties(binding.sortMenu.getEditText().getText().toString());
                    binding.loadingText.setVisibility(View.GONE);
                    if (properties.isEmpty()) {
                        // show hint if no property exists
                        binding.hint.setVisibility(View.VISIBLE);
                    } else {
                        // otherwise initialize and display toolbar after all properties are loaded
                        initToolbar();
                        initMap();
                        showNearbyInspectionAlert(properties);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                new BasicSnackbar(getView(), msg, "error", Snackbar.LENGTH_LONG);
            }
        });
    }

    /**
     * Given properties, render properties in the recycler view
     * @param properties
     */
    private void renderProperties(ArrayList<Property> properties) {
        RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
        propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties, this);
        propertiesRecyclerView.setAdapter(propertyCardAdapter);

    }

}