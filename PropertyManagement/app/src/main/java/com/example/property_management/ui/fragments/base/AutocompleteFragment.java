package com.example.property_management.ui.fragments.base;

import static android.content.ContentValues.TAG;

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
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class AutocompleteFragment extends Fragment {
    private FragmentAutocompleteBinding binding;
    AutocompleteSupportFragment autocompleteFragment;
    private PlacesClient placesClient;
    String selectedPlaceName = "";

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
                    System.out.println("error: api key is null");
                }
                Places.initialize(getContext(), apiKey);
            } catch (PackageManager.NameNotFoundException e) {
                System.out.println("error: " + e.getMessage());
            }
        }
        this.placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        try {
            autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG,Place.Field.NAME));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    System.out.println("name: " + place.getName() + ", Address: " + place.getAddress());
                    selectedPlaceName = place.getName();
                }

                @Override
                public void onError(@NonNull Status status) {
                    selectedPlaceName = "";
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
        return root;
    }

    // get api key from AndroidManifest.xml
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
            System.out.println("error: " + e.getMessage());
            throw new PackageManager.NameNotFoundException("Unable to load meta-data: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("error: " + e.getMessage());
        }
        return null;
    }

    public String getSelectedPlaceName() {
        return selectedPlaceName;
    }

    public void setPlaceNameText(String placeNameText) {
        this.autocompleteFragment.setText(placeNameText);
        this.selectedPlaceName = placeNameText;
    }

}
