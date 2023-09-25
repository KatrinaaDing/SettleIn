package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.AddUserCallback;
import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.data.User;
import com.example.property_management.data.UserProperty;
import com.example.property_management.databinding.ActivityRegisterBinding;
import com.example.property_management.utils.EmailValidator;
import com.example.property_management.utils.PasswordValidator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Register");

        // ========================== Components ==========================
        Button submitRegisterBtn = findViewById(R.id.submitRegisterBtn);
        Button goToLoginBtn = findViewById(R.id.goToLoginBtn);
        TextInputLayout emailLayout = findViewById(R.id.editTextRegisterEmail);
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);

        // ========================== Listeners ==========================
        submitRegisterBtn.setOnClickListener(view -> {
            if (validateEmail() && validatePassword()) {
//                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                startActivity(intent);
                String email = emailLayout.getEditText().getText().toString();
                String password = passwordLayout.getEditText().getText().toString();
                registerUser(email, password);
            }
        });
        goToLoginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        emailLayout.addOnEditTextAttachedListener(textInputLayout -> {
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    clearEmailError();
                }
                @Override
                public void afterTextChanged(Editable editable) {}

            });
        });
        passwordLayout.addOnEditTextAttachedListener(textInputLayout -> {
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    clearPasswordError();
                }
                @Override
                public void afterTextChanged(Editable editable) {}

            });
        });
        confirmPasswordLayout.addOnEditTextAttachedListener(textInputLayout -> {
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    clearConfirmPasswordError();
                }
                @Override
                public void afterTextChanged(Editable editable) {}

            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if user logged in, go to home screen
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        if (firebaseAuthHelper.isUserSignedIn()) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    // ========================== Helper Methods ==========================
    private boolean validateEmail() {
        TextInputLayout emailLayout = findViewById(R.id.editTextRegisterEmail);
        String email = emailLayout.getEditText().getText().toString();
        if (email.isEmpty()) {
            emailLayout.setError("Email cannot be empty");
            return false;
        }
        if (!EmailValidator.isValidEmail(email)) {
            emailLayout.setError("Please enter a valid email.");
            return false;
        }
        return true;
    }
    private boolean validatePassword() {
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);
        String password = passwordLayout.getEditText().getText().toString();
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString();

        boolean isValid = true;
        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            isValid = false;
        }
        if (!PasswordValidator.isValidPassword(password)) {
            passwordLayout.setError("Password must be at least 8 characters long, no whitespaces, at least one for each: lowercase letter and digit");
            isValid = false;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Confirm Password cannot be empty");
            isValid = false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }
        return isValid;
    }
    private void clearEmailError() {
        TextInputLayout emailLayout = findViewById(R.id.editTextRegisterEmail);
        emailLayout.setError(null);
    }
    private void clearPasswordError() {
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        passwordLayout.setError(null);
    }
    private void clearConfirmPasswordError() {
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);
        confirmPasswordLayout.setError(null);
    }
    private void registerUser(String email, String password) {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        firebaseAuthHelper.createUser(email, password, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                User newUserObj = new User(user.getUid(), "New User", email, new ArrayList<>(), new ArrayList<>());
                FirebaseUserRepository firebaseUserRepository = new FirebaseUserRepository();
                firebaseUserRepository.addUser(newUserObj, new AddUserCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onError(String msg) {
                        System.out.println("Error: " + msg);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {}
        });

    }
}
