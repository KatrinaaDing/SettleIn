package com.example.property_management.ui.fragments.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddNewFacilityDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.custom_add_new_facility_dialog, null);
        builder.setView(rootView);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // get profile fragment
                        ProfileFragment profileFragment = (ProfileFragment) AddNewFacilityDialogFragment.this.getParentFragment();

                        if (profileFragment != null) {
                            // add new facility
                            if (rootView == null) {
                                System.out.println("getView() is null");
                                return;
                            }
                            EditText facilityEditText = rootView.findViewById(R.id.facilityEditText);
                            String facilityToAdd = facilityEditText.getText().toString();
                            profileFragment.addNewFacility(facilityToAdd);
                            // refresh ProfileFragment
                            new Handler().postDelayed(() -> {
                                profileFragment.refreshFragment();
                            }, 5000);

                        } else {
                            Log.d("AddNewFacilityDialogFragmentError", "ProfileFragment is null");
                            new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Error: ProfileFragment is null", "error");
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddNewFacilityDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}

