package com.example.property_management.ui.fragments.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AppCompatActivity activity;
    PlacesClient placesClient;
    String selectedAddress = "";
    CustomListRecyclerViewAdapter interestedLocationsAdapter;
    CustomListRecyclerViewAdapter interestedFacilitiesAdapter;


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
        ImageButton editBtn = binding.editBtn;
        ImageButton addFacilityBtn = binding.addFacilityBtn;
        Button logoutBtn = binding.logoutBtn;
        TextView userEmail = binding.userEmail;
        TextView userId = binding.userId;

        // ========================= Set Adapters ==========================
        RecyclerView interestedLocationsRecyclerView = binding.interestedLocationsRecyclerView;
        interestedLocationsAdapter = new CustomListRecyclerViewAdapter(new ArrayList<>());
        interestedLocationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedLocationsRecyclerView.setAdapter(interestedLocationsAdapter);

        RecyclerView interestedFacilitiesRecyclerView = binding.interestedFacilitiesRecyclerView;
        interestedFacilitiesAdapter = new CustomListRecyclerViewAdapter(new ArrayList<>());
        interestedFacilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        interestedFacilitiesRecyclerView.setAdapter(interestedFacilitiesAdapter);

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
        addFacilityTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = user.getUid();
                FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
                // TODO change hardcode here
                String facility = "MelbourneCentral";
                firebaseFunctionsHelper.addInterestedFacility(userId, facility)
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
                String userId = user.getUid();
                FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
                // TODO change hardcode here
                String location = "Melbourne Central";
                firebaseFunctionsHelper.addInterestedLocation(userId, location)
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
//        getUserInfo();
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
        FirebaseUser user = firebaseAuthHelper.getCurrentUser();
        assert user != null;
        binding.userEmail.setText("Email: " + user.getEmail());
        binding.userId.setText("User ID: " + user.getUid());
        binding.userId.setVisibility(View.INVISIBLE);
        FirebaseUserRepository db = new FirebaseUserRepository();
        db.getUserInfoById(user.getUid(), new GetUserInfoByIdCallback() {
            @Override
            public void onSuccess(User userObj) {
                binding.userName.setText(userObj.getUserName());
                ArrayList<String> userInterestedLocations = userObj.getInterestedLocations();
                if (userInterestedLocations != null && !userInterestedLocations.isEmpty()) {
                    interestedLocationsAdapter.updateData(userInterestedLocations);
                }

                ArrayList<String> userInterestedFacilities = userObj.getInterestedFacilities();
                if (userInterestedFacilities != null && !userInterestedFacilities.isEmpty()) {
                    interestedFacilitiesAdapter.updateData(userInterestedFacilities);
                }
            }

            @Override
            public void onError(String msg) {

            }
        });
    }

    public void editProfile() {
        DialogFragment dialog = new EditProfileDialogFragment();
        dialog.show(getChildFragmentManager(), "EditProfileDialogFragment");
    }

    public void addFacility() {
        DialogFragment dialog = new AddNewFacilityDialogFragment();
        dialog.show(getChildFragmentManager(), "AddNewFacilityDialogFragment");
    }

    // params: propertyIds: all the property ids of the user
    // isFacility: true if the interest is a facility, false if it is a location
    // interestedList: the list of interested facilities or locations before the delete
    // interest_: the facility or location to be deleted
    public void deleteInterest(FirebaseUserRepository userRepository, ArrayList<String> propertyIds, Boolean isFacility, ArrayList<String> interestedList,String interest_) {
        userRepository.deleteInterestedFacilityLocation(propertyIds, isFacility, interestedList, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                new BasicSnackbar(getActivity().findViewById(android.R.id.content),
                        "Success: Deleted interested " + interest_, "success");
                // do more actions
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(getActivity().findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }
}