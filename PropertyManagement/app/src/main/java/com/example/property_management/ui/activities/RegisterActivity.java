package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.databinding.ActivityRegisterBinding;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Array;
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
            if (validatePassword() && validateEmail()) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
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
        if (email.length() < 6) {
            emailLayout.setError("Email must be at least 6 characters");
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
        String email = emailLayout.getEditText().getText().toString();
        emailLayout.setError(null);
    }
    private void clearPasswordError() {
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        String password = passwordLayout.getEditText().getText().toString();
        passwordLayout.setError(null);
    }
    private void clearConfirmPasswordError() {
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString();
        confirmPasswordLayout.setError(null);
    }
}
