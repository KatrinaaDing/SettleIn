//package com.example.property_management.ui.fragments.profile;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//
//import androidx.fragment.app.DialogFragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.example.property_management.R;
//import com.example.property_management.data.User;
//import com.example.property_management.ui.fragments.base.AutocompleteFragment;
//import com.example.property_management.ui.fragments.base.BasicSnackbar;
//import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//
//import java.util.ArrayList;
//
//public class AddNewLocationDialogFragment extends DialogFragment {
//
//    User user;
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // remove the existing fragment if exists to avoid duplicate id
//        AutocompleteFragment existingFragment = (AutocompleteFragment)
//                getChildFragmentManager().findFragmentById(R.id.auto_fragment);
//        // if fragment exists, remove it
//        if (existingFragment != null) {
//            getChildFragmentManager().beginTransaction().remove(existingFragment).commitNow();
//        }
//
//        // inflate the dialog
//        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View rootView = inflater.inflate(R.layout.custom_interested_location_dialog, null);
//        builder.setView(rootView);
//
//        // add autocomplete fragment
//        AutocompleteFragment autocompleteFragment = new AutocompleteFragment();
//
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//
//        FrameLayout fragmentLayout = rootView.findViewById(R.id.autocompleteContainer);
//        if (fragmentLayout == null) {
//            Log.e("isnull", "fragmentLayout is null");
//        }
//
//
//        transaction.replace(R.id.autocompleteContainer, autocompleteFragment).commit();
//
//
////        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
////        transaction.add(R.id.auto_fragment, this).commit();
//
////        AutocompleteFragment autocompleteFragment = (AutocompleteFragment)
////                getChildFragmentManager().findFragmentById(R.id.auto_fragment);
//
//        if (autocompleteFragment == null) {
//            Log.e("isnull1111111111", "autocompleteFragment is null");
//        }
//
//        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        // get profile fragment
//                        ProfileFragment profileFragment = (ProfileFragment) AddNewLocationDialogFragment.this.getParentFragment();
//
//                        if (profileFragment != null) {
//                            // add new location
//                            if (rootView == null) {
//                                System.out.println("getView() is null");
//                                return;
//                            }
//
//                            AutocompleteFragment autocompleteFragment = (AutocompleteFragment)
//                                    getChildFragmentManager().findFragmentById(R.id.auto_fragment);
//
//                            if (autocompleteFragment == null) {
//                                Log.e("isnull", "autocompleteFragment is null");
//                                return;
//                            }
//
//                            String locationToAdd = autocompleteFragment.getSelectedAddress();
//                            ArrayList<String> interestedLocations = profileFragment.getProfileUser().getInterestedLocations();
//
//                            // check if locationToAdd is empty
//                            if (locationToAdd == null || locationToAdd.equals("")) {
//                                new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Error: Location cannot be empty", "error");
//                                return;
//                            }
//
//                            // check duplication
//                            for (String location : interestedLocations) {
//                                String location_lower = location.toLowerCase();
//                                if (location_lower.equals(locationToAdd.toLowerCase())) {
//                                    new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Error: Location already exists", "error");
//                                    return;
//                                }
//                            }
//
//                            // add new location
//                            profileFragment.addNewLocation(locationToAdd);
//                        } else {
//                            Log.d("AddNewLocationDialogFragmentError", "ProfileFragment is null");
//                            new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Error: ProfileFragment is null", "error");
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", (dialog, id) -> {
//                    // close the dialog
//                    AddNewLocationDialogFragment.this.getDialog().cancel();
//                });
//            return builder.create();
//        }
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        dialog.setCanceledOnTouchOutside(false);
//        if (dialog != null) {
//            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
//            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
//            dialog.getWindow().setLayout(width, height);
//        }
//    }
//}
