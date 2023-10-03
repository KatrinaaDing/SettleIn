package com.example.property_management.ui.fragments.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.databinding.FragmentProfileBinding;
import com.example.property_management.ui.activities.LoginActivity;
import com.example.property_management.ui.fragments.base.AutocompleteFragment;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.libraries.places.api.net.PlacesClient;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AppCompatActivity activity;

    PlacesClient placesClient;

    String selectedAddress = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = (AppCompatActivity) getActivity();

        final TextView textView = binding.textProfile;
        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ========================= Components ==========================
        ImageButton editBtn = binding.editBtn;
        Button logoutBtn = binding.logoutBtn;
        TextView userEmail = binding.userEmail;
        TextView userId = binding.userId;

        // ===== Add Location Dialog =====
        Button addLocationBtn = binding.addLocationBtn;
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

        logoutBtn.setOnClickListener(v -> {
            logout();
        });

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
        getUserInfo();
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
//        binding.userId.setText("User ID: " + user.getUid());
    }

    public void editProfile() {
//        BasicDialog dialog = new BasicDialog(false,
//                "Test2 dialog",
//                "This dialog cannot be closed from outside and has 2 buttons",
//                "Cancel",
//                "Save");
//        dialog.setCallback(new BasicDialogCallback() {
//            @Override
//            public void onLeftBtnClick() {
//                dialog.dismiss();
//            }
//            @Override
//            public void onRightBtnClick() {
//                dialog.dismiss();
//            }
//        });
//        dialog.show(getParentFragmentManager(), "Test dialog");
    }
}