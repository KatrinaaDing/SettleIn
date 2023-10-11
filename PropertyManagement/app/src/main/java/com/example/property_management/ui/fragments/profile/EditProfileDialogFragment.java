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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.User;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class EditProfileDialogFragment extends DialogFragment {
    private String username;
    private String email;
    private String uid;
    private EditText editUsername;
    private EditText editEmail;
    private EditText providePassword;
    private FirebaseUser user;

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newUsername, String newEmail);
    }

    private OnProfileUpdatedListener listener;

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    private void notifyProfileUpdated(String newUsername, String newEmail) {
        if (listener != null) {
            listener.onProfileUpdated(newUsername, newEmail);
        }
    }

    public EditProfileDialogFragment(FirebaseUser user) {
        this.user = user;
        this.username = user.getDisplayName();
        this.email = user.getEmail();
        this.uid = user.getUid();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_edit_profile_dialog, null);

        editUsername = view.findViewById(R.id.editUsername);
        editUsername.setText(username);
        Log.d("update-name", "onCreateDialog: " + user.getDisplayName());
        editEmail = view.findViewById(R.id.editEmail);
        editEmail.setText(email);
        providePassword = view.findViewById(R.id.providePassword);

        builder.setView(view)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String userInputUsername = editUsername.getText().toString();
                    String userInputEmail = editEmail.getText().toString();
                    String userInputProvidePassword = providePassword.getText().toString();
                    View rootView = getActivity().findViewById(android.R.id.content);
                    if (userInputUsername.isEmpty() || userInputEmail.isEmpty()) {
                        new BasicSnackbar(rootView, "Username or Email Cannot be Empty.", "error");
                    } else if (userInputUsername.equals(username) && userInputEmail.equalsIgnoreCase(email)) {
                        new BasicSnackbar(rootView, "Cannot Input the Same Username and Email.", "error");
                    } else if (userInputProvidePassword.isEmpty()) {
                        new BasicSnackbar(rootView, "You Must Provide Password to Edit Profile.", "error");
                    } else {
                        // re-authenticate user
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(email, providePassword.getText().toString());

                        user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("user-auth", "User re-authenticated.");

                                        user.updateEmail(userInputEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("update-email", "User email address updated.");
                                                        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper((AppCompatActivity)getActivity());
                                                        firebaseAuthHelper.addUsernameToFirestore(uid, userInputUsername);
                                                        notifyProfileUpdated(userInputUsername, userInputEmail);
                                                        new BasicSnackbar(rootView, "Profile Updated Successfully.", "success");
                                                    } else {
                                                        Log.d("update-email", "Update email failed.", task.getException());
                                                        new BasicSnackbar(rootView, "Update email failed.", "error");
                                                    }
                                                }
                                            });
                                    } else {
                                        Log.d("user-auth", "User re-authentication failed.", task.getException());
                                        new BasicSnackbar(rootView, "User re-authentication failed.", "error");
                                    }
                                }
                            });

                    }
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

