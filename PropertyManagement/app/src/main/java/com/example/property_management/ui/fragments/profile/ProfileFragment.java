package com.example.property_management.ui.fragments.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.adapters.CustomListRecyclerViewAdapter;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.data.User;
import com.example.property_management.databinding.FragmentProfileBinding;
import com.example.property_management.ui.activities.LoginActivity;
import com.example.property_management.ui.fragments.base.AutocompleteFragment;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

/**
 * A fragment that displays the profile page.
 */
public class ProfileFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener, CustomListRecyclerViewAdapter.EventListener{
    private FragmentProfileBinding binding;
    private AppCompatActivity activity;
    String selectedAddress = "";
    String selectedName = "";
    String username;
    String email;
    String uid;
    FirebaseUser user;
    CustomListRecyclerViewAdapter interestedLocationsAdapter;
    CustomListRecyclerViewAdapter interestedFacilitiesAdapter;
    User currentUser;
    AlertDialog alertDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = (AppCompatActivity) getActivity();

        // ========================= Components ==========================
        Button editBtn = binding.editBtn;
        ImageButton addFacilityBtn = binding.addFacilityBtn;
        ImageButton addLocationBtn = binding.addLocationBtn;
        Button logoutBtn = binding.logoutBtn;

        getUserInfo();

        // ========================= Listeners ==========================
        // click edit button to show the edit profile dialog
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });

        // click add location button to show the add location dialog
        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showAddLocationDialog();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // click add facility button to show the add facility dialog
        addFacilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFacilityDialog();
            }
        });

        // click logout button to logout the user
        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        return root;
    }

    // ========================= Functions ==========================

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Logout the user
     */
    private void logout() {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        firebaseAuthHelper.signout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * Get the user information and set the user information in the profile page
     */
    private void getUserInfo() {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        user = firebaseAuthHelper.getCurrentUser();
        assert user != null;
        email = user.getEmail();
        uid = user.getUid();
        username = user.getDisplayName();
        binding.userEmail.setText("Email: " + email);
        binding.userId.setText("User ID: " + uid);
        binding.userId.setVisibility(View.INVISIBLE);
        binding.userName.setText(username);
        FirebaseUserRepository db = new FirebaseUserRepository();
        db.getUserInfoById(user.getUid(), new GetUserInfoByIdCallback() {
            @Override
            public void onSuccess(User userObj) {
                currentUser = userObj;
                setInterestedLocationsRecycleView();
                setInterestedFacilitiesRecycleView();
            }

            @Override
            public void onError(String msg) {

            }
        });
    }

    /**
     * Show the edit profile dialog
     */
    public void editProfile() {
        EditProfileDialogFragment dialog = new EditProfileDialogFragment(user);
        dialog.setOnProfileUpdatedListener(this);
        dialog.show(getChildFragmentManager(), "EditProfileDialogFragment");
    }

    /**
     * Show the add facility dialog
     */
    public void showAddFacilityDialog() {
        DialogFragment dialog = new AddNewFacilityDialogFragment();
        dialog.show(getChildFragmentManager(), "AddNewFacilityDialogFragment");
    }

    /**
     * Show the add location dialog
     * @throws PackageManager.NameNotFoundException
     */
    private void showAddLocationDialog() throws PackageManager.NameNotFoundException {
        // remove the existing fragment if exists to avoid duplicate id
        AutocompleteFragment existingFragment = (AutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.auto_fragment);
        // if fragment exists, remove it
        if (existingFragment != null) {
            getChildFragmentManager().beginTransaction().remove(existingFragment).commitNow();
        }
        // inflate the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.custom_interested_location_dialog, null);

        // ===== dialog =====
        alertDialog = new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add Interested Location")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // clear selected address
                        selectedAddress = "";
                    }
                }).create();
        alertDialog.show();

        // override positive button
        ((androidx.appcompat.app.AlertDialog) alertDialog).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {

            AutocompleteFragment autocompleteFragment = (AutocompleteFragment)
                    getChildFragmentManager().findFragmentById(R.id.auto_fragment);
            if (autocompleteFragment == null) {
                new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Error: autocompleteFragment is null", "error");
                alertDialog.dismiss();
                return;
            }

            selectedAddress = autocompleteFragment.getSelectedAddress();
            selectedName = autocompleteFragment.getSelectedName();
            Log.i("SelectedAddress",selectedAddress);
            Log.i("SelectedName",selectedName);

            TextInputLayout locationEditTextLayout = (TextInputLayout) alertDialog.findViewById(R.id.locationEditTextLayout);

            // check if selectedAddress is empty
            if (selectedAddress == null || selectedAddress.equals("") || selectedName == null || selectedName.equals("")) {
                locationEditTextLayout.setError("Error: Location address or business name cannot be empty");
                return;
            }
            // check duplication
            if (ifDuplicate(selectedAddress)) {
                locationEditTextLayout.setError("Error: Location already exists");
                return;
            }
            addNewLocation(selectedAddress, selectedName);
            alertDialog.dismiss();
        });
    }

    /**
     * Update the profile information
     * @param newUsername the new username
     * @param newEmail the new email
     */
    @Override
    public void onProfileUpdated(String newUsername, String newEmail) {
        if (!newUsername.isEmpty()) {
            TextView UsernameTextView = getView().findViewById(R.id.userName);
            UsernameTextView.setText(newUsername);
        }

        if (!newEmail.isEmpty()) {
            TextView emailTextView = getView().findViewById(R.id.userEmail);
            emailTextView.setText(newEmail);
        }
    }

    /**
     * Set interested location recycle view
     */
    public void setInterestedLocationsRecycleView(){
        RecyclerView interestedLocationsRecyclerView = binding.interestedLocationsRecyclerView;
        interestedLocationsAdapter = new CustomListRecyclerViewAdapter(currentUser, false, this);
        interestedLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedLocationsRecyclerView.setAdapter(interestedLocationsAdapter);
    }

    /**
     * Set interested facility recycle view
     */
    public void setInterestedFacilitiesRecycleView(){
        RecyclerView interestedFacilitiesRecyclerView = binding.interestedFacilitiesRecyclerView;
        interestedFacilitiesAdapter = new CustomListRecyclerViewAdapter(currentUser, true, this);
        interestedFacilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedFacilitiesRecyclerView.setAdapter(interestedFacilitiesAdapter);
    }

    /**
     * Add new facility to the interested facility list
     * @param facilityToAdd the facility to add
     */
    public void addNewFacility(String facilityToAdd) {
        interestedFacilitiesAdapter.addNewInterestedFacility(facilityToAdd);
    }

    /**
     * Add new location to the interested location list
     * @param locationAddress the address of the location
     * @param locationName the name of the location
     */
    public void addNewLocation(String locationAddress, String locationName) {
        interestedLocationsAdapter.addNewInterestedLocation(locationAddress, locationName);
    }

    /**
     * Check if the location/facility to add is already in the interested location/facility list
     * @param toAdd the location/facility to add
     * @return true if the location/facility to add is already in the interested location/facility list, false otherwise
     */
    public boolean ifDuplicate(String toAdd) {
        for (String location : currentUser.getInterestedLocations()) {
            String location_lower = location.toLowerCase();
            if (location_lower.equals(toAdd.toLowerCase())) {
                return true;
            }
        }

        for (String facility : currentUser.getInterestedFacilities()) {
            String facility_lower = facility.toLowerCase();
            if (facility_lower.equals(toAdd.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set location name text in the dialog
     * @param name the name of the location
     */
    public void setLocationNameTxt(String name) {
        TextView locationNameTxt = (TextView) alertDialog.findViewById(R.id.locationNameTxt);
        locationNameTxt.setText(name);
    }

    /**
     * Show no interested facility/location placeholder if there is no interested facility/location
     * @param isFacility true if the recycle view is for interested facility, false if it is for interested location
     * @param ifShowPlaceholder true if show placeholder, false if hide placeholder
     */
    @Override
    public void onEvent(boolean isFacility, boolean ifShowPlaceholder) {
        TextView noLocationTxt = binding.noLocationTxt;
        TextView noFacilityTxt = binding.noFacilityTxt;

        if (isFacility) {
            if (ifShowPlaceholder) {
                noFacilityTxt.setVisibility(View.VISIBLE);
            } else {
                noFacilityTxt.setVisibility(View.GONE);
            }
        } else {
            if (ifShowPlaceholder) {
                noLocationTxt.setVisibility(View.VISIBLE);
            } else {
                noLocationTxt.setVisibility(View.GONE);
            }
        }

    }
}