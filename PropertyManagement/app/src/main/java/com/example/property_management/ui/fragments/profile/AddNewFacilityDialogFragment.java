package com.example.property_management.ui.fragments.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.data.User;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AddNewFacilityDialogFragment extends DialogFragment {

    User user;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.custom_add_new_facility_dialog, null);
        builder.setView(rootView);
        builder.setCancelable(false);

        Dialog dialog = builder.setPositiveButton("Add", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddNewFacilityDialogFragment.this.getDialog().cancel();
                        dismiss();
                    }
                })
                .create();
        dialog.show();
        ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            // get profile fragment
            ProfileFragment profileFragment = (ProfileFragment) AddNewFacilityDialogFragment.this.getParentFragment();

            if (profileFragment != null) {
                // add new facility
                if (rootView == null) {
                    new BasicSnackbar(rootView.findViewById(android.R.id.content), "Error: rootView is null", "error");
                    dialog.dismiss();
                    return;
                }

                EditText facilityEditText = rootView.findViewById(R.id.facilityEditText);
                TextInputLayout facilityEditTextLayout = rootView.findViewById(R.id.facilityEditTextLayout);
                String facilityToAdd = facilityEditText.getText().toString().trim();

                // check if facilityToAdd is empty
                if (TextUtils.isEmpty(facilityToAdd)) {
                    facilityEditTextLayout.setError("Error: Facility cannot be empty");
                    return;
                }

                // check duplication
                if (profileFragment.ifDuplicate(facilityToAdd)) {
                    facilityEditTextLayout.setError("Error: Facility already exists");
                    return;
                }

                // add new facility
                profileFragment.addNewFacility(facilityToAdd);
                dialog.dismiss();

            } else {
                Log.d("AddNewFacilityDialogFragmentError", "ProfileFragment is null");
                new BasicSnackbar(rootView.findViewById(android.R.id.content), "Error: ProfileFragment is null", "error");
                dialog.dismiss();
            }
        });

        return dialog;
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

