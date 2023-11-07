package com.example.property_management.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.databinding.ActivityLoginBinding;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.utils.EmailValidator;
import com.example.property_management.utils.DateTimeFormatter;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.PasswordValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import org.checkerframework.checker.nullness.qual.NonNull;

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
            if (validateEmail()) {
                emailLayout.setError(null);
                String email = emailLayout.getEditText().getText().toString();
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();
                                    if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                                        // email found in firebase auth
                                        sendPasswordResetEmail(email);
                                    } else {
                                        // Cannot find this email address in firebase auth
                                        emailLayout.setError("This email is not registered!");
                                    }
                                } else {
                                    new BasicSnackbar(LoginActivity.this.findViewById(android.R.id.content), "Error checking email!", "error");
                                }
                            }
                        });
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
        setLoginLoading(true);
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        firebaseAuthHelper.signinWithEmail(email, password, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                setLoginLoading(false);
            }
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

    private void setLoginLoading(boolean isLoading) {
        Button submitLoginBtn = findViewById(R.id.submitLoginBtn);
        if (isLoading) {
            submitLoginBtn.setText("Logging you in...");
            submitLoginBtn.setEnabled(false);
        } else {
            submitLoginBtn.setText("Login");
            submitLoginBtn.setEnabled(true);
        }
    }

    /**
     * Sends an email to reset the user's password.
     * @param email The email address of the user to send the password reset email to.
     */
    private void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideSoftKeyboard(LoginActivity.this);
                            new BasicSnackbar(LoginActivity.this.findViewById(android.R.id.content), "Check email to reset your password!", "success");
                        } else {
                            hideSoftKeyboard(LoginActivity.this);
                            new BasicSnackbar(LoginActivity.this.findViewById(android.R.id.content), "Fail to send reset password email!", "error");
                        }
                    }
                });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
