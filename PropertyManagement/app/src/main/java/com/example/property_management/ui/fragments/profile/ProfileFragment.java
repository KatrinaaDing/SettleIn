package com.example.property_management.ui.fragments.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.CustomListRecyclerViewAdapter;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.callbacks.DeleteInterestedFacilityCallback;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.User;
import com.example.property_management.data.UserProperty;
import com.example.property_management.databinding.FragmentProfileBinding;
import com.example.property_management.ui.activities.AddPropertyActivity;
import com.example.property_management.ui.activities.LoginActivity;
import com.example.property_management.ui.activities.MainActivity;
import com.example.property_management.ui.fragments.base.AutocompleteFragment;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener{

    private FragmentProfileBinding binding;
    private AppCompatActivity activity;
    PlacesClient placesClient;
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
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = (AppCompatActivity) getActivity();

        final TextView textView = binding.textProfile;
//        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ========================= Components ==========================
        Button editBtn = binding.editBtn;
        ImageButton addFacilityBtn = binding.addFacilityBtn;
        ImageButton addLocationBtn = binding.addLocationBtn;
        Button logoutBtn = binding.logoutBtn;

        getUserInfo();

        // ========================= Listeners ==========================
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });

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

        addFacilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFacilityDialog();
            }
        });

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

    private void logout() {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        firebaseAuthHelper.signout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

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

    public void editProfile() {
        EditProfileDialogFragment dialog = new EditProfileDialogFragment(user);
        dialog.setOnProfileUpdatedListener(this);
        dialog.show(getChildFragmentManager(), "EditProfileDialogFragment");
    }

    public void showAddFacilityDialog() {
        DialogFragment dialog = new AddNewFacilityDialogFragment();
        dialog.show(getChildFragmentManager(), "AddNewFacilityDialogFragment");
    }

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
            System.out.println("SelectedAddress: " + selectedAddress);
            System.out.println("SelectedName: " + selectedName);

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

    @Override
    public void onProfileUpdated(String newUsername, String newEmail) {

        TextView UsernameTextView = getView().findViewById(R.id.userName);
        UsernameTextView.setText(newUsername);

        TextView emailTextView = getView().findViewById(R.id.userEmail);
        emailTextView.setText(newEmail);
    }

    public void setInterestedLocationsRecycleView(){
        RecyclerView interestedLocationsRecyclerView = binding.interestedLocationsRecyclerView;
        interestedLocationsAdapter = new CustomListRecyclerViewAdapter(currentUser, false);
        interestedLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedLocationsRecyclerView.setAdapter(interestedLocationsAdapter);
    }

    public void setInterestedFacilitiesRecycleView(){
        RecyclerView interestedFacilitiesRecyclerView = binding.interestedFacilitiesRecyclerView;
        interestedFacilitiesAdapter = new CustomListRecyclerViewAdapter(currentUser, true);
        interestedFacilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedFacilitiesRecyclerView.setAdapter(interestedFacilitiesAdapter);

    }

    public void addNewFacility(String facilityToAdd) {
        interestedFacilitiesAdapter.addNewInterestedFacility(facilityToAdd);
    }

    public void addNewLocation(String locationAddress, String locationName) {
        interestedLocationsAdapter.addNewInterestedLocation(locationAddress, locationName);
    }

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

    public void setLocationNameTxt(String name) {
        TextView locationNameTxt = (TextView) alertDialog.findViewById(R.id.locationNameTxt);
        locationNameTxt.setText(name);
    }
}