package com.example.property_management.ui.fragments.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private EditText providePassword;
    private EditText editOldPassword;
    private EditText editNewPassword;
    private EditText editConfirmPassword;
    private FirebaseUser user;
    private EditText etEmail;
    private Button btnResetPassword;


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
//        providePassword = view.findViewById(R.id.providePassword);
//        editOldPassword = view.findViewById(R.id.editOldPassword);
//        editNewPassword = view.findViewById(R.id.editNewPassword);
//        editConfirmPassword = view.findViewById(R.id.editConfirmPassword);
        etEmail = view.findViewById(R.id.etEmail);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter your email and we will send you a link to reset password", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
//                                    new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Check email to reset your password!", "success");
                                    Toast.makeText(getActivity(), "Check email to reset your password!", Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    dismiss();
                                } else {
//                                    new BasicSnackbar(getActivity().findViewById(android.R.id.content), "Fail to send reset password email!", "error");
                                    Toast.makeText(getActivity(), "Fail to send reset password email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userInputUsername = editUsername.getText().toString();
                        String userInputEmail = editEmail.getText().toString();
                        String userInputProvidePassword = providePassword.getText().toString();
                        String oldPassword = editOldPassword.getText().toString();
                        String newPassword = editNewPassword.getText().toString();
                        String confirmPassword = editConfirmPassword.getText().toString();
                        TextInputLayout editUsernameLayout = view.findViewById(R.id.editUsernameLayout);
                        TextInputLayout editEmailLayout = view.findViewById(R.id.editEmailLayout);


                        View rootView = getActivity().findViewById(android.R.id.content);
                        if (userInputUsername.isEmpty()) {
                            editUsernameLayout.setError("Username Cannot be Empty.");
                            new BasicSnackbar(rootView, "Username Cannot be Empty.", "error");
                        } else if (userInputEmail.isEmpty()) {
//                        editEmailLayout.setError("Email Cannot be Empty.");
                            new BasicSnackbar(rootView, "Email Cannot be Empty.", "error");
                        } else if (userInputUsername.equals(username) && userInputEmail.equalsIgnoreCase(email) && !providePassword.getText().toString().isEmpty()) {
                            new BasicSnackbar(rootView, "Cannot Input the Same Username and Email.", "error");
                        } else if (userInputProvidePassword.isEmpty() && (!userInputUsername.equals(username) || !userInputEmail.equalsIgnoreCase(email)))  {
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
//                        if (!oldPassword.isEmpty()) {
//                            if (newPassword.isEmpty()) {
//                                new BasicSnackbar(rootView, "New Password Cannot be Empty.", "error");
//                            } else if (!newPassword.isEmpty() && confirmPassword.isEmpty()) {
//                                new BasicSnackbar(rootView, "Please Confirm New Password.", "error");
//                            } else if (!newPassword.isEmpty() && !confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
//                                new BasicSnackbar(rootView, "Different New Password and Confirm Password.", "error");
//                            } else if (!oldPassword.isEmpty() && !newPassword.isEmpty() && oldPassword.equals(newPassword)) {
//                                new BasicSnackbar(rootView, "Same Old Password and New Password.", "error");
//                            } else {
//                                AuthCredential credential = EmailAuthProvider
//                                        .getCredential(user.getEmail(), oldPassword);
//
//                                user.reauthenticate(credential)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    Log.d("user-auth", "User re-authenticated.");
//                                                    user.updatePassword(newPassword)
//                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                    if (task.isSuccessful()) {
//                                                                        Log.d("reset-password-success", "User password updated.");
//                                                                        new BasicSnackbar(rootView, "Password Updated Successfully.", "success");
//                                                                    } else {
//                                                                        Log.d("reset-password-failed", "Reset password failed.", task.getException());
//                                                                        new BasicSnackbar(rootView, "Reset password failed.", "error");
//                                                                    }
//                                                                }
//                                                            })
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//                                                                    Log.e("update-password-error", e.getMessage());
//                                                                }
//                                                            });
//                                                } else {
//                                                    Log.d("user-auth", "User re-authentication failed.", task.getException());
//                                                    new BasicSnackbar(rootView, "User re-authentication failed.", "error");
//                                                }
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                // Handle specific exceptions here for re-authentication
//                                                Log.e("re-auth-error", e.getMessage());
//                                            }
//                                        });
//
//                            }
//                        }

//                        if (TextUtils.isEmpty(email)) {
//                            Toast.makeText(getApplicationContext(), "Enter your email!", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        firebaseAuth.sendPasswordResetEmail(email)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(ResetPasswordActivity.this, "Check email to reset your password!", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            Toast.makeText(ResetPasswordActivity.this, "Fail to send reset password email!", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
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
