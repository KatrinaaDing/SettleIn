package com.example.property_management.ui.fragments.profile;

import static com.example.property_management.utils.Helpers.specialCharFilter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import java.util.regex.Pattern;

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
    private TextView errorResetPassword;
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

        editUsername.setFilters(new InputFilter[]{specialCharFilter});
        editEmail = view.findViewById(R.id.editEmail);
        editEmail.setText(email);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        errorResetPassword = view.findViewById(R.id.sendEmailErrorTextView);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorResetPassword.setVisibility(View.INVISIBLE);
                btnResetPassword.setEnabled(false);
                btnResetPassword.setText("Sending Email...");
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    btnResetPassword.setText("Email Sent! Logging out...");
                                    new Handler().postDelayed(() -> {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                        dismiss();
                                    }, 1000);

                                } else {
                                    btnResetPassword.setEnabled(true);
                                    btnResetPassword.setText("Send Password Reset Email");
                                    errorResetPassword.setVisibility(View.VISIBLE);
                                    Log.e("reset-password", "Fail to send reset password email!", task.getException());
                                }
                            }
                        });
            }
        });

        Dialog dialog = builder.setView(view)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        // Set the dialog to be full screen
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setLayout(width, height);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set background color according to light/dark theme
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        int color = typedValue.data;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userInputUsername = editUsername.getText().toString();
                    String userInputEmail = editEmail.getText().toString();
                    TextInputEditText providePassword = view.findViewById(R.id.providePassword);
                    String userInputProvidePassword = providePassword.getText().toString();

                    // Get references to the error-display layouts
                    TextInputLayout editUsernameLayout = view.findViewById(R.id.editUsernameLayout);
                    TextInputLayout editEmailLayout = view.findViewById(R.id.editEmailLayout);
                    TextInputLayout providedPasswordLayout = view.findViewById(R.id.providePasswordLayout);

                    View rootView = getActivity().findViewById(android.R.id.content);

                    // Check for any validation errors
                    if (userInputUsername.isEmpty()) {
                        editUsernameLayout.setError("Username Cannot be Empty.");
                    } else if (userInputEmail.isEmpty()) {
                        editEmailLayout.setError("Email Cannot be Empty.");
                    } else if (!userInputEmail.isEmpty() && !userInputEmail.equals(email) && userInputProvidePassword.isEmpty()) {
                        providedPasswordLayout.setError("Must provide password to update email");
                    } else if (userInputUsername.equals(username) && userInputEmail.equals(email)) {
                        // If no changes are made, just dismiss the dialog
                        dismiss();
                    } else {
                        // Saving status
                        positiveButton.setText("Saving");
                        positiveButton.setEnabled(false);

                        // Clear all the error prompts set earlier
                        editUsernameLayout.setError(null);
                        editEmailLayout.setError(null);
                        providedPasswordLayout.setError(null);

                        // Create the request to change username
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userInputUsername)
                                .build();

                        // If username is changed, update the profile
                        if (!userInputUsername.equals(username)) {
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            positiveButton.setText("Save");
                                            positiveButton.setEnabled(true);

                                            Log.d("update-username", "Username updated.");
                                            notifyProfileUpdated(userInputUsername, "");

                                            // If only the username was changed, display a message and dismiss the dialog
                                            if (userInputEmail.equals(email)) {
                                                dialog.dismiss();
                                                new BasicSnackbar(rootView, "Username Updated Successfully.", "success");
                                                dismiss();
                                            }
                                        } else {
                                            positiveButton.setText("Save");
                                            positiveButton.setEnabled(true);
                                            dismiss();
                                            Log.d("update-username", "Failed to update username");
                                            new BasicSnackbar(rootView, "Failed to update username", "error");
                                        }
                                    });
                        }

                        // If email is changed, re-authenticate the user before updating
                        if (!userInputEmail.equals(email)) {
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(email, providePassword.getText().toString());

                            user.reauthenticate(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("user-auth", "User re-authenticated.");
                                            user.updateEmail(userInputEmail)
                                                    .addOnCompleteListener(emailTask -> {
                                                        if (emailTask.isSuccessful()) {
                                                            positiveButton.setText("Save");
                                                            positiveButton.setEnabled(true);
                                                            dismiss();
                                                            Log.d("update-email", "User email address updated.");
                                                            notifyProfileUpdated("", userInputEmail);
                                                            new BasicSnackbar(rootView, "Profile Updated Successfully.", "success");
                                                        } else {
                                                            positiveButton.setText("Save");
                                                            positiveButton.setEnabled(true);
                                                            dismiss();
                                                            Log.d("update-email", "Update email failed.", emailTask.getException());
                                                            new BasicSnackbar(rootView, "Update email failed.", "error");
                                                        }
                                                    });
                                        } else {
                                            Log.d("user-auth", "User re-authentication failed.", task.getException());
                                            providedPasswordLayout.setError("Wrong Password.");
                                            positiveButton.setText("Save");
                                            positiveButton.setEnabled(true);
                                        }
                                    });
                        }
                    }
                }
            });

        }
    }

}
