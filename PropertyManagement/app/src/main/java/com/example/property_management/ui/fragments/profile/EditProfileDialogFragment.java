package com.example.property_management.ui.fragments.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileDialogFragment extends DialogFragment {
    private String username;
    private String email;
    private String uid;
    private EditText editUsername;
    private EditText editEmail;

    public EditProfileDialogFragment(String username, String email, String uid) {
        this.username = username;
        this.email = email;
        this.uid = uid;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_edit_profile_dialog, null);

        editUsername = view.findViewById(R.id.editUsername);
        editUsername.setText(username);
        editEmail = view.findViewById(R.id.editEmail);
        editEmail.setText(email);

        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userInputUsername = editUsername.getText().toString();
                        String userInputEmail = editEmail.getText().toString();
                        String msg = "";
                        String type = "error";
                        View rootView = getActivity().findViewById(android.R.id.content);
                        if (userInputUsername.isEmpty() || userInputEmail.isEmpty()) {
                            msg = "Username or email cannot be empty";
                        } else if (userInputUsername.equals(username)) {
                            msg = "Cannot input the same username";
                        } else if (userInputEmail.equalsIgnoreCase(email)) {
                            msg = "Cannot input the same email";
                        } else {
                            // valid input
                            
                        }
                        new BasicSnackbar(rootView, msg, type);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditProfileDialogFragment.this.getDialog().cancel();
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
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}

