package com.example.property_management.ui.fragments.home;

import android.content.Context;
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
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.FragmentHomeBinding;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private ArrayList<Property> allProperties;

    private boolean sortAscending = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        Button testBtn = binding.testBtn;
//        testBtn.setOnClickListener(view -> {
//            Intent intent = new Intent(getActivity(), TestActivity.class);
//            startActivity(intent);
//        });

        if (waitForAuth()) {
            getAllProperties(this.getContext());
        }

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
        getAllProperties(this.getContext());
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
            } else {
                // otherwise, filter properties by "inspected" status
                // reference: https://stackoverflow.com/questions/9146224/arraylist-filter
                boolean filterIsInspected = selected.equals("Inspected");
                ArrayList<Property> filteredProperties = allProperties.stream()
                        .filter(property -> property.getInspected() == filterIsInspected)
                        .collect(Collectors.toCollection(ArrayList::new));
                // render filtered properties
                renderProperties(filteredProperties);
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
            if (sortAscending) {
                sortOrderBtn.setIconResource(R.drawable.baseline_arrow_upward_24);
            } else {
                sortOrderBtn.setIconResource(R.drawable.baseline_arrow_downward_24);
            }
            // sort and render properties
            sortProperties(autoCompleteTextView.getText().toString());
        });
    }

    /**
     * Sort and rendre properties by the given sort type and existing sort order
     * @param sortType the sort type
     */
    private void sortProperties(String sortType) {
        // create comparator
        Comparator<Property> comparator = null;
        switch (sortType) {
            case "Price":
                comparator = Comparator.comparing(Property::getPrice);
                break;
            case "Beds":
                comparator = Comparator.comparing(Property::getNumBedrooms);
                break;
            case "Bathrooms":
                comparator = Comparator.comparing(Property::getNumBathrooms);
                break;
            case "Parking Spaces":
                comparator = Comparator.comparing(Property::getNumParking);
                break;
            default:
                // sort by create time by default
            case "Create Time":
                comparator = Comparator.comparing(Property::getCreatedAt);
                // default "sort by create time" is new to old
                comparator = comparator.reversed();
        }
        if (comparator == null) {
            return;
        }
        // apply sort order
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        allProperties.sort(comparator);
        renderProperties(allProperties);
    }

    private void initToolbar() {
        ConstraintLayout toolbar = binding.toolbar;
        initFilterMenu();
        initSortMenu();
        toolbar.setVisibility(View.VISIBLE);
    }

    /**
     * Wait for firebase authentication to finish
     * @return true if authentication is successful, false otherwise
     */
    private boolean waitForAuth() {
        // wait until firebase authentication is done
        binding.loadingText.setText("Signing you in...");
        // try 5 seconds
        int tryCount = 0;
        while (FirebaseAuth.getInstance().getCurrentUser() == null && tryCount < 10) {
            try {
                Thread.sleep(500);
                tryCount++;
                // set text to text view 'hint'
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.loadingText.setText("Sign in failed. Please try again.");
            return false;
        }
        binding.loadingText.setText("Loading properties...");
        return true;
    }

    /**
     * Get all properties from the db and display them in the recycler view
     * @param context the context of the fragment
     */
    private void getAllProperties(Context context) {
        FirebaseUserRepository db = new FirebaseUserRepository();
        db.getAllUserProperties(new GetAllUserPropertiesCallback() {
            @Override
            public void onSuccess(ArrayList<Property> properties) {
                allProperties = properties;
                // sort and render properties
                sortProperties(binding.sortMenu.getEditText().getText().toString());
                binding.loadingText.setVisibility(View.GONE);

                if (properties.isEmpty()) {
                    // show hint if no property exists
                    binding.hint.setVisibility(View.VISIBLE);
                } else {
                    // otherwise initialize and display toolbar after all properties are loaded
                    initToolbar();
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
        RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties);
        propertiesRecyclerView.setAdapter(propertyCardAdapter);
    }

}