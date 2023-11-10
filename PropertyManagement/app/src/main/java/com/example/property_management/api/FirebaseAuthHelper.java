package com.example.property_management.api;

import static android.content.ContentValues.TAG;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Helper class for Firebase authentication
 */
public class FirebaseAuthHelper {
    private AppCompatActivity activity;
    private FirebaseAuth mAuth;

    public FirebaseAuthHelper(AppCompatActivity activity) {
        this.activity = activity;
        init();
    }

    /**
     * Initialize Firebase Auth
     */
    private void init() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Check if user is signed in
     * @return true if user is signed in, false otherwise
     */
    public boolean isUserSignedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    /**
     * Get current user
     * @return FirebaseUser object
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }


    public void createUser(String email, String username, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        // Create a request to update the user's profile with the desired username
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        // Update the user's profile with the request
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Log.d(TAG, "Username added to user profile");
                                        showSuccess("User created successfully.");
                                        callback.onSuccess(user);
                                    } else {
                                        Log.w(TAG, "Failed to add username to user profile", profileTask.getException());
                                        callback.onFailure(profileTask.getException());
                                    }
                                });
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    showError(task);
                    callback.onFailure(task.getException());
                }
            });
    }

    /**
     * Sign in user with email and password
     * @param email user's email
     * @param password user's password
     * @param callback callback to handle success and failure
     */
    public void signinWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    callback.onSuccess(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    showError(task);
                    callback.onFailure(task.getException());
                }
            });
    }

    /**
     * Sign out user
     */
    public void signout() {
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Show error message based on the error code
     * @param task task that failed
     */
    private void showError(Task<AuthResult> task) {
        Log.e("firebase-auth", task.getException().getMessage());
        // handle network error
        if (task.getException() instanceof FirebaseNetworkException) {
            new BasicSnackbar(activity.findViewById(android.R.id.content),
                    "Network error. Please check your connection and try again later.",
                    "error",
                    Snackbar.LENGTH_LONG);

        } else if (task.getException() instanceof FirebaseAuthException) {
            // handle other errors
            FirebaseAuthException e = (FirebaseAuthException) task.getException();
            String errorCode = e.getErrorCode();
            String errorMessage;
            // show error message based on the error code
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    errorMessage = "Invalid email format.";
                    break;
                case "ERROR_USER_NOT_FOUND":
                    errorMessage = "User not found.";
                    break;
                case "ERROR_WRONG_PASSWORD":
                    errorMessage = "Wrong password.";
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    errorMessage = "Email already in use.";
                    break;
                default:
                    errorMessage = "Something went wrong. Please try again later.";
                    break;
            }
            new BasicSnackbar(activity.findViewById(android.R.id.content),
                    errorMessage,
                    "error",
                    Snackbar.LENGTH_LONG);

        } else {
            new BasicSnackbar(activity.findViewById(android.R.id.content),
                    "An unexpected error occurred. Please try again later.",
                    "error",
                    Snackbar.LENGTH_LONG);
        }
    }

    /**
     * Show success message
     * @param msg message to be shown
     */
    private void showSuccess(String msg) {
        new BasicSnackbar(activity.findViewById(android.R.id.content), msg, "success");
    }

    public void addUsernameToFirestore(String uid, String username) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }
}
