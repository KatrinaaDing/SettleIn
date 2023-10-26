package com.example.property_management.ui.fragments.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.User;
import com.example.property_management.ui.activities.LoginActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class EditProfileDialogFragment extends DialogFragment {
    private String username;
    private String email;
    private String uid;
    private EditText editUsername;
    private EditText editEmail;
    private FirebaseUser user;
    private Button btnUpdateUsername;
    private Button btnUpdateEmail;
    private Button btnResetPassword;
    private View view;


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
        view = inflater.inflate(R.layout.custom_edit_profile_dialog, null);

        editUsername = view.findViewById(R.id.editUsername);
        editUsername.setText(username);
        Log.d("update-name", "onCreateDialog: " + user.getDisplayName());
        editEmail = view.findViewById(R.id.editEmail);
        editEmail.setText(email);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);

//        btnUpdateUsername.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String userInputUsername = editUsername.getText().toString();
//                View rootView = getActivity().findViewById(android.R.id.content);
//                TextInputLayout editUsernameLayout = view.findViewById(R.id.editUsernameLayout);
//                if (userInputUsername.isEmpty()) {
//                    editUsernameLayout.setError("Username Cannot be Empty.");
//                    Toast.makeText(getActivity(), "Username Cannot be Empty.", Toast.LENGTH_SHORT).show();
//                } else {
//                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                            .setDisplayName(userInputUsername)
//                            .build();
//
//                    user.updateProfile(profileUpdates)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Log.d("update-username", "Username updated.");
//                                        notifyProfileUpdated(userInputUsername, "");
//                                        dismiss();
//                                        new BasicSnackbar(rootView, "Username updated.", "success");
//                                    } else {
//                                        dismiss();
//                                        Log.d("update-username", "Failed to update username");
//                                        new BasicSnackbar(rootView, "Failed to update username", "error");
//                                    }
//                                }
//                            });
//                }
//
//            }
//        });


        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
//                                    new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Check email to reset your password!", "success");
                                    Toast.makeText(getActivity(), "Check email to reset your password!", Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                    dismiss();
                                } else {
                                    Toast.makeText(getActivity(), "Fail to send reset password email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        builder.setView(view)
                .setPositiveButton("Save", null);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);

            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        String userInputUsername = editUsername.getText().toString();
                        String userInputEmail = editEmail.getText().toString();
                        TextInputEditText providePassword = view.findViewById(R.id.providePassword);
                        String userInputProvidePassword = providePassword.getText().toString();
                        TextInputLayout editUsernameLayout = view.findViewById(R.id.editUsernameLayout);
                        TextInputLayout editEmailLayout = view.findViewById(R.id.editEmailLayout);
                        TextInputLayout providedPasswordLayout = view.findViewById(R.id.providePasswordLayout);

                        View rootView = getActivity().findViewById(android.R.id.content);
                        if (userInputUsername.isEmpty()) {
                            Log.d("checkcheck", "onClick: " + editUsernameLayout);
                            editUsernameLayout.setError("Username Cannot be Empty.");
                        } else if (userInputEmail.isEmpty()) {
                            editEmailLayout.setError("Email Cannot be Empty.");
                        } else if (!userInputEmail.isEmpty() && !userInputEmail.equals(email) && userInputProvidePassword.isEmpty()) {
                            providedPasswordLayout.setError("Must provide password to update email");
                        } else {
                            editUsernameLayout.setError(null);
                            editEmailLayout.setError(null);
                            providedPasswordLayout.setError(null);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userInputUsername)
                                    .build();

                            if (!userInputUsername.equals(username)) {
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("update-username", "Username updated.");
                                                    notifyProfileUpdated(userInputUsername, "");
                                                    if (userInputEmail.equals(email)) {
                                                        dialog.dismiss();
                                                        new BasicSnackbar(rootView, "Username Updated Successfully.", "success");
                                                        dismiss();
                                                    }
                                                } else {
                                                    dismiss();
                                                    Log.d("update-username", "Failed to update username");
                                                    new BasicSnackbar(rootView, "Failed to update username", "error");
                                                }
                                            }
                                        });
                            }

                            if (!userInputEmail.equals(email)) {
                                // re-authenticate user
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(email, providePassword.getText().toString());

                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("user-auth", "User re-authenticated.");
                                                    providedPasswordLayout.setError(null);
                                                    user.updateEmail(userInputEmail)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        dismiss();
                                                                        Log.d("update-email", "User email address updated.");
                                                                        notifyProfileUpdated("", userInputEmail);
                                                                        new BasicSnackbar(rootView, "Profile Updated Successfully.", "success");
                                                                    } else {
                                                                        Toast.makeText(getActivity(), "Update email failed.", Toast.LENGTH_SHORT).show();
                                                                        dismiss();
                                                                        Log.d("update-email", "Update email failed.", task.getException());
//                                                            new BasicSnackbar(rootView, "Update email failed.", "error");
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Log.d("user-auth", "User re-authentication failed.", task.getException());
                                                    providedPasswordLayout.setError("Wrong Password.");
                                                    Toast.makeText(getActivity(), "User re-authentication failed.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                }
            });
        }
    }

}
