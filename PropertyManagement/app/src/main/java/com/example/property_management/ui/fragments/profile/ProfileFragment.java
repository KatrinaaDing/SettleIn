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
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment implements EditProfileDialogFragment.OnProfileUpdatedListener{

    private FragmentProfileBinding binding;
    private AppCompatActivity activity;
    PlacesClient placesClient;
    String selectedAddress = "";
    String username;
    String email;
    String uid;
    FirebaseUser user;
    CustomListRecyclerViewAdapter interestedLocationsAdapter;
    CustomListRecyclerViewAdapter interestedFacilitiesAdapter;
    User currentUser;


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
        Button logoutBtn = binding.logoutBtn;
        TextView userEmail = binding.userEmail;
        TextView userId = binding.userId;

        // ========================= Set Adapters ==========================
//        RecyclerView interestedLocationsRecyclerView = binding.interestedLocationsRecyclerView;
//        interestedLocationsAdapter = new CustomListRecyclerViewAdapter();
//        interestedLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        interestedLocationsRecyclerView.setAdapter(interestedLocationsAdapter);
//
//        RecyclerView interestedFacilitiesRecyclerView = binding.interestedFacilitiesRecyclerView;
//        interestedFacilitiesAdapter = new CustomListRecyclerViewAdapter();
//        interestedFacilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        interestedFacilitiesRecyclerView.setAdapter(interestedFacilitiesAdapter);

        getUserInfo();

        // ===== Add Location Dialog =====
        ImageButton addLocationBtn = binding.addLocationBtn;
        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // launch dialog
                    showCustomDialog();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // ========================= Listeners ==========================
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });

        addFacilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFacility();
            }
        });

        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        // ========================= Add facility test ==========================
        Button addFacilityTestBtn = binding.addFacilityTest;
        Button addLocationTestBtn = binding.addLocationTest;
        Button deleteFacilityTestBtn = binding.deleteFacilityTest;
        Button deleteLocationTestBtn = binding.deleteLocationTest;

        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        FirebaseUser user = firebaseAuthHelper.getCurrentUser();
        assert user != null;
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        String userID = user.getUid();
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();

        // TODO: move it up
        getUserInfo();

        addFacilityTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
                // TODO change hardcode here
                String facility = "coles";
                firebaseFunctionsHelper.addInterestedFacility(userID, facility)
                        .addOnSuccessListener(result -> {
                            if (result.equals("success")) {
                                Log.i("add-interested-facility-success", result);
                                new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                        "Success: Added new facility.", "success");
                            } else {
                                Log.e("add-interested-facility-fail", result);
                                new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                        "Error: Failed to add new facility.", "error");
                            }
                        })
                        .addOnFailureListener(e -> {
                            // pop error at input box
                            Log.e("add-interested-facility-fail", e.getMessage());
                            new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                    e.getMessage(), "error");
                        });
            }
        });

        addLocationTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO change hardcode here
                String location = "Melbourne Central";
                firebaseFunctionsHelper.addInterestedLocation(userID, location)
                        .addOnSuccessListener(result -> {
                            if (result.equals("success")) {
                                Log.i("add-interested-location-success", result);
                                new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                        "Success: Added new location.", "success");
                            } else {
                                Log.e("add-interested-location-fail", result);
                                new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                        "Error: Failed to add new location.", "error");
                            }
                        })
                        .addOnFailureListener(e -> {
                            // pop error at input box
                            Log.e("add-interested-location-fail", e.getMessage());
                            new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                    e.getMessage(), "error");
                        });
            }
        });

        deleteFacilityTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser != null) {
                    // Create an ArrayList to store the keys
                    ArrayList<String> propertyIds = new ArrayList<>(currentUser.getProperties().keySet());
                    ArrayList<String> interestedFacilities = currentUser.getInterestedFacilities();
                    String facilityToDelete = "coles";
//                    deleteInterest(userRepository, propertyIds, true, interestedFacilities, facilityToDelete);
                }

            }
        });

        // ========================= End Add facility test ==========================
        return root;
    }

    // ========================= Functions ==========================
    private void showCustomDialog() throws PackageManager.NameNotFoundException {
        Log.i("dialogView", "here");
        View dialogView = getLayoutInflater().inflate(R.layout.custom_interested_location_dialog, null);
        Log.i("dialogView", "here2");

//        View autocompleteView = dialogView.findViewById(R.id.auto_fragment);


        // ===== dialog =====
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add Interested Location")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AutocompleteFragment autocompleteFragment = (AutocompleteFragment)
                                getChildFragmentManager().findFragmentById(R.id.auto_fragment);
                        if (autocompleteFragment == null) {
                            Log.e("isnull", "autocompleteFragment is null");
                            return;
                        }
                        selectedAddress = autocompleteFragment.getSelectedAddress();
                        System.out.println("Add Location: " + selectedAddress);
                        if (selectedAddress.equals("")) {
                            Toast.makeText(getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // TODO POST new location to firebase
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // clear selected address
                        selectedAddress = "";
                        dialogInterface.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

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
                ArrayList propertyIds = new ArrayList<>(userObj.getProperties().keySet());
                ArrayList<String> userInterestedLocations = userObj.getInterestedLocations();
                if (userInterestedLocations != null && !userInterestedLocations.isEmpty()) {
//                    interestedLocationsAdapter.setPropertyIds(new ArrayList<>(userObj.getProperties().keySet()));
//                    interestedLocationsAdapter.updateData(userInterestedLocations);
                    setInterestedLocationsRecycleView(userInterestedLocations, propertyIds);
                }

                ArrayList<String> userInterestedFacilities = userObj.getInterestedFacilities();
                if (userInterestedFacilities != null && !userInterestedFacilities.isEmpty()) {
//                    interestedFacilitiesAdapter.setPropertyIds(new ArrayList<>(userObj.getProperties().keySet()));
//                    interestedFacilitiesAdapter.updateData(userInterestedFacilities);
                    setInterestedFacilitiesRecycleView(userInterestedFacilities, propertyIds);
                }
                currentUser = userObj;
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

    public void addFacility() {
        DialogFragment dialog = new AddNewFacilityDialogFragment();
        dialog.show(getChildFragmentManager(), "AddNewFacilityDialogFragment");
    }

    @Override
    public void onProfileUpdated(String newUsername, String newEmail) {

        TextView UsernameTextView = getView().findViewById(R.id.userName);
        UsernameTextView.setText(newUsername);

        TextView emailTextView = getView().findViewById(R.id.userEmail);
        emailTextView.setText(newEmail);
    }

    public void setInterestedLocationsRecycleView(ArrayList<String> interests, ArrayList<String> propertyIds){
        RecyclerView interestedLocationsRecyclerView = binding.interestedLocationsRecyclerView;
        interestedLocationsAdapter = new CustomListRecyclerViewAdapter(interests, propertyIds, false);
        interestedLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedLocationsRecyclerView.setAdapter(interestedLocationsAdapter);
    }

    public void setInterestedFacilitiesRecycleView(ArrayList<String> interests, ArrayList<String> propertyIds){
        RecyclerView interestedFacilitiesRecyclerView = binding.interestedFacilitiesRecyclerView;
        interestedFacilitiesAdapter = new CustomListRecyclerViewAdapter(interests, propertyIds, true);
        interestedFacilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedFacilitiesRecyclerView.setAdapter(interestedFacilitiesAdapter);

    }



    public void refreshFragment() {
        // refresh the fragment
        getParentFragmentManager().beginTransaction().detach(this).commitNowAllowingStateLoss();
        getParentFragmentManager().beginTransaction().attach(this).commitAllowingStateLoss();
    }

    public void addNewFacility(String facilityToAdd) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();

        firebaseFunctionsHelper.addInterestedFacility(user.getUid(), facilityToAdd)
                .addOnSuccessListener(result -> {
                    if (result.equals("success")) {
                        Log.i("add-interested-facility-success", result);
                        new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                "Success: Added new facility.", "success");
                    } else {
                        Log.e("add-interested-facility-fail", result);
                        new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                                "Error: Failed to add new facility.", "error");
                    }
                })
                .addOnFailureListener(e -> {
                    // pop error at input box
                    Log.e("add-interested-facility-fail", e.getMessage());
                    new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                            e.getMessage(), "error");
                });
    }
}