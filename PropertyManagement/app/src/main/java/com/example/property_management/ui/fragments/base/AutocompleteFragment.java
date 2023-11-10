package com.example.property_management.ui.fragments.base;

import static android.content.ContentValues.TAG;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.property_management.R;
import com.example.property_management.databinding.FragmentAutocompleteBinding;
import com.example.property_management.ui.activities.AddPropertyActivity;
import com.example.property_management.ui.fragments.profile.ProfileFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;

/**
 * Custom autocomplete fragment for searching address
 */
public class AutocompleteFragment extends Fragment {
    private FragmentAutocompleteBinding binding;
    CustomPlaceAutoCompleteFragment autocompleteFragment;
    private PlacesClient placesClient;
    String selectedAddress = "";
    String selectedName = "";
    double lng = Double.NaN;
    double lat = Double.NaN;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAutocompleteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Places
        if (!Places.isInitialized()) {
            try {
                String apiKey = getApiKey();
                // if cannot get api key
                if (apiKey == null) {
                    Log.e("AutocompleteFragmentNullKey", "error: api key is null");
                }
                Places.initialize(getContext(), apiKey);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("AutocompleteFragmentError", "error: " + e.getMessage());
            }
        }
        this.placesClient = Places.createClient(getContext());

        // Initialize the CustomPlaceAutocompleteFragment.
        try {
            autocompleteFragment = (CustomPlaceAutoCompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG,Place.Field.NAME, Place.Field.ADDRESS, Place.Field.NAME));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // get profile fragment
                    ProfileFragment profileFragment = (ProfileFragment) AutocompleteFragment.this.getParentFragment();

                    autocompleteFragment.setPlace(place);
                    selectedAddress = place.getAddress();
                    selectedName = place.getName();
                    lat = place.getLatLng().latitude;
                    lng = place.getLatLng().longitude;

                    // Access the parent activity
                    Activity parentActivity = getActivity();
                    View submitBtn = null;
                    if (parentActivity instanceof AddPropertyActivity) {
                        // parent is add property activity, enable submit button
                        AddPropertyActivity addPropertyActivity = (AddPropertyActivity) parentActivity;
                        addPropertyActivity.setSelectedAddress(selectedAddress);
                        addPropertyActivity.setCoordinates(lat, lng);
                        submitBtn = addPropertyActivity.binding.submitBtn;
                    } else {
                        // parent is profile fragment, set location name to the dialog UI
                        profileFragment.setLocationNameTxt(place.getName());
                    }

                    if (submitBtn != null) {
                        // Call the method in the parent activity
                        submitBtn.setEnabled(true);
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    selectedAddress = "";
                    selectedName = "";
                    Log.i(TAG, "An error occurred: " + status);
                }

            });
        } catch (Exception e) {
            Log.e("AutocompleteFragmentError", "error: " + e.getMessage());
        }
        return root;
    }

    /**
     * Get API key from AndroidManifest.xml
     * @return API key
     * @throws PackageManager.NameNotFoundException
     */
    public String getApiKey() throws PackageManager.NameNotFoundException {
        try {
            // Get the package manager
            PackageManager packageManager = getContext().getPackageManager();

            // Get the ApplicationInfo object for your app
            ApplicationInfo appInfo = packageManager.getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);

            // Get the meta-data bundle from the ApplicationInfo
            Bundle metaData = appInfo.metaData;

            // Retrieve the API key using the key specified in AndroidManifest.xml
            String apiKey = metaData.getString("com.google.android.geo.API_KEY");
            return apiKey;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AutocompleteFragmentError", "error: " + e.getMessage());
            throw new PackageManager.NameNotFoundException("Unable to load meta-data: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("AutocompleteFragmentError", "error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get address of the selected place
     * @return
     */
    public String getSelectedAddress() {
        return selectedAddress;
    }

    /**
     * Get name of the selected place
     * @return
     */
    public String getSelectedName() {
        return selectedName;
    }

    /**
     * Set address text to search box
     * @param placeNameText
     */
    public void setAddressText(String placeNameText) {
        this.autocompleteFragment.setText(placeNameText);
        this.selectedAddress = placeNameText;
    }

    /**
     * Get longitude of the selected place
     * @return
     */
    public double getLng() {
        return lng;
    }

    /**
     * Get latitude of the selected place
     * @return
     */
    public double getLat() {
        return lat;
    }

    /**
     * Store longitude of the selected place
     * @param lng
     */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /**
     * Store latitude of the selected place
     * @param lat
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

}
