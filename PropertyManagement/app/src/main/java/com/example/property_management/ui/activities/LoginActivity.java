package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.databinding.ActivityLoginBinding;
import com.example.property_management.utils.EmailValidator;
import com.example.property_management.utils.DateTimeFormatter;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.PasswordValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Login");

        // ========================== Components ==========================
        Button submitLoginBtn = findViewById(R.id.submitLoginBtn);
        Button goToRegisterBtn = findViewById(R.id.goToRegisterBtn);
        TextInputLayout emailLayout = findViewById(R.id.editTextLoginEmail);
        TextInputLayout passwordLayout = findViewById(R.id.editTextLoginPassword);
        Button resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        TextInputLayout resetPasswordEmailLayout = findViewById(R.id.editTextResetPasswordEmail);
        Button sendResetEmailBtn = findViewById(R.id.sendResetEmailBtn);
        //

        // ========================== Listeners ==========================
        submitLoginBtn.setOnClickListener(view -> {
            // close keyboard
            Helpers.closeKeyboard(this);
            if (validateEmail() && validatePassword()) {
                String email = emailLayout.getEditText().getText().toString();
                String password = passwordLayout.getEditText().getText().toString();
                login(email, password);
            }
        });
        emailLayout.addOnEditTextAttachedListener(textInputLayout -> textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearEmailError();
            }
            @Override
            public void afterTextChanged(Editable editable) {}

        }));
        passwordLayout.addOnEditTextAttachedListener(textInputLayout -> textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearPasswordError();
            }
            @Override
            public void afterTextChanged(Editable editable) {}

        }));
        goToRegisterBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        resetPasswordBtn.setOnClickListener(v -> {
            if (resetPasswordEmailLayout.getVisibility() == View.GONE) {
                // Show the email input with an animation
                resetPasswordEmailLayout.setVisibility(View.VISIBLE);
                resetPasswordEmailLayout.setAlpha(0.0f);
                resetPasswordEmailLayout.animate().alpha(1.0f).setDuration(300).setListener(null);
                sendResetEmailBtn.setVisibility(View.VISIBLE);
                sendResetEmailBtn.setAlpha(0.0f);
                sendResetEmailBtn.animate().alpha(1.0f).setDuration(300).setListener(null);
            } else {
                resetPasswordEmailLayout.setVisibility(View.GONE);
                sendResetEmailBtn.setVisibility(View.GONE);
                // Process the email input
//                String email = resetPasswordEmailLayout.getEditText().getText().toString().trim();
//                if (TextUtils.isEmpty(email)) {
//                    resetPasswordEmailLayout.setError("Please enter an email");
//                    return;
//                }
//                sendPasswordResetLink(email);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // if user logged in, go to home screen
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        if (firebaseAuthHelper.isUserSignedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    // ========================== Helpers ==========================
    private void login(String email, String password) {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        firebaseAuthHelper.signinWithEmail(email, password, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }
    private void clearEmailError() {
        TextInputLayout emailLayout = findViewById(R.id.editTextLoginEmail);
        emailLayout.setError(null);
    }
    private void clearPasswordError() {
        TextInputLayout passwordLayout = findViewById(R.id.editTextLoginPassword);
        passwordLayout.setError(null);
    }
    private boolean validateEmail() {
        TextInputLayout emailLayout = findViewById(R.id.editTextLoginEmail);
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
        TextInputLayout passwordLayout = findViewById(R.id.editTextLoginPassword);
        String password = passwordLayout.getEditText().getText().toString();
        boolean isValid = true;
        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            isValid = false;
        }
        if (!PasswordValidator.isValidPassword(password)) {
            passwordLayout.setError("Password must be at least 8 characters long, no whitespaces, at least one for each: lowercase letter and digit");
            isValid = false;
        }
        return isValid;
    }
}
