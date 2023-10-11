package com.example.property_management.api;

import static android.content.ContentValues.TAG;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthHelper {
    private AppCompatActivity activity;
    private FirebaseAuth mAuth;
// ...


    public FirebaseAuthHelper(AppCompatActivity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isUserSignedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    public void createUser(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    showSuccess("User created successfully.");
                    System.out.println(user);
                    callback.onSuccess(user);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    System.out.println("Login fail");
                    showError(task);
                    callback.onFailure(task.getException());
                }
            });
    }

    public void signinWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    showSuccess("Login successfully.");
                    callback.onSuccess(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    showError(task);
                    callback.onFailure(task.getException());
                }
            });
    }

    // sign out user
    public void signout() {
        FirebaseAuth.getInstance().signOut();
    }

    // show error snackbar based on error code
    private void showError(Task<AuthResult> task) {
        // handle network error
        if (task.getException() instanceof FirebaseNetworkException) {
            new BasicSnackbar(activity.findViewById(android.R.id.content),
                    "Network error. Please check your connection and try again later.",
                    "error",
                    Snackbar.LENGTH_LONG);
            return;
        }
        // handle other errors
        FirebaseAuthException e = (FirebaseAuthException) task.getException();
        String errorCode = e.getErrorCode();
        String errorMessage;
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
    }

    private void showSuccess(String msg) {
        new BasicSnackbar(activity.findViewById(android.R.id.content), msg, "success");
    }
}
