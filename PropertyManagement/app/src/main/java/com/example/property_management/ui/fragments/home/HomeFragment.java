package com.example.property_management.ui.fragments.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.PropertyCardAdapter;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.FragmentHomeBinding;
import com.example.property_management.sensors.LocationSensor;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.ui.fragments.base.PropertyCard;
import com.example.property_management.utils.Helpers;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

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
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        waitForAuth();

        return root;
    }

    public void onEvent(boolean hasProperty) {
        if (hasProperty) {
            binding.hint.setVisibility(View.GONE);
        } else {
            binding.hint.setVisibility(View.VISIBLE);
        }
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
//        getAllProperties(this.getContext());
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
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                binding.loadingText.setText("Loading properties...");
                getAllProperties();
            } else {
                // User is signed out
                binding.loadingText.setText("Sign in failed. Please try again.");
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
                    }
                }
            }

            @Override
            public void onError(String msg) {
                new BasicSnackbar(getView(), msg, "error", Snackbar.LENGTH_LONG);
            }
        });
//        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
//        firebaseFunctionsHelper.getAllProperties()
//                .addOnSuccessListener(result -> {
//                    ArrayList<Property> properties = (ArrayList<Property>) result;
//                    if (properties.isEmpty()) {
//                        // show hint if no property exists
//                        binding.hint.setVisibility(View.VISIBLE);
//                    }
//
//                    RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
//                    if (propertiesRecyclerView != null) {
    //                    renderProperties(properties);
    //                    binding.loadingText.setVisibility(View.GONE);
    //                    allProperties = properties;
    //                    // initialize and display toolbar after all properties are loaded
    //                    initToolbar();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("get-all-properties-fail", e.getMessage());
//                    binding.loadingText.setText("Failed to load properties. Please try again later.");
//                });
    }

    private void renderProperties(ArrayList<Property> properties) {
        RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
        propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties, this);
        propertiesRecyclerView.setAdapter(propertyCardAdapter);

    }

}