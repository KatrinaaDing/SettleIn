package com.example.property_management.ui.activities;

import static com.example.property_management.utils.Helpers.specialCharFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.AddUserCallback;
import com.example.property_management.callbacks.AuthCallback;
import com.example.property_management.data.User;
import com.example.property_management.databinding.ActivityRegisterBinding;
import com.example.property_management.utils.EmailValidator;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.PasswordValidator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This activity is used to register a new user
 */
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
        TextInputLayout usernameLayout = findViewById(R.id.editTextRegisterUsername);
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);
        EditText registerUsername = findViewById(R.id.registerUsername);
        registerUsername.setFilters(new InputFilter[]{specialCharFilter});

        // ========================== Listeners ==========================
        // register user when click on register button
        submitRegisterBtn.setOnClickListener(view -> {
            // close keyboard
            Helpers.closeKeyboard(this);
            if (validateEmail() && validateUsername() && validatePassword()) {
                String email = emailLayout.getEditText().getText().toString();
                String username = usernameLayout.getEditText().getText().toString();
                String password = passwordLayout.getEditText().getText().toString();
                registerUser(email, username, password);
            }
        });

        // go to login page when click on login button
        goToLoginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // clear error when user type in email
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

        // clear error when user type in username
        usernameLayout.addOnEditTextAttachedListener(textInputLayout -> {
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    clearUserNameError();
                }
                @Override
                public void afterTextChanged(Editable editable) {}

            });
        });

        // clear error when user type in password
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

        // clear error when user type in confirm password
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

    /**
     * Validate email
     * @return true if email is valid, false otherwise
     */
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

    private boolean validateUsername() {
        TextInputLayout usernameLayout = findViewById(R.id.editTextRegisterUsername);
        String username = usernameLayout.getEditText().getText().toString();
        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            return false;
        }
        Log.d("check-usernmae", "validateUsername: " + username);
        return true;
    }

    /**
     * Validate password
     * @return true if password is valid, false otherwise
     */
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

    /**
     * Clear error message for emailLayout
     */
    private void clearEmailError() {
        TextInputLayout emailLayout = findViewById(R.id.editTextRegisterEmail);
        emailLayout.setError(null);
    }

    /**
     * Clear error message for usernameLayout
     */
    private void clearUserNameError() {
        TextInputLayout usernameLayout = findViewById(R.id.editTextRegisterUsername);
        usernameLayout.setError(null);
    }

    /**
     * Clear error message for passwordLayout
     */
    private void clearPasswordError() {
        TextInputLayout passwordLayout = findViewById(R.id.editTextRegisterPassword);
        passwordLayout.setError(null);
    }

    /**
     * Clear error message for confirmPasswordLayout
     */
    private void clearConfirmPasswordError() {
        TextInputLayout confirmPasswordLayout = findViewById(R.id.editTextConfirmPassword);
        confirmPasswordLayout.setError(null);
    }

    /**
     * Conditionally set state for register button
     * @param isRegistering true if registering, false otherwise
     */
    private void setLoadingRegister(boolean isRegistering) {
        Button submitRegisterBtn = findViewById(R.id.submitRegisterBtn);
        if (isRegistering) {
            submitRegisterBtn.setText("Registering...");
        } else {
            submitRegisterBtn.setText("Register");
        }
        // enable register button if not registering, disable otherwise
        binding.submitRegisterBtn.setEnabled(!isRegistering);
    }

    /**
     * Register user
     * @param email
     * @param username
     * @param password
     */
    private void registerUser(String email, String username, String password) {
        setLoadingRegister(true);
        // create user in firebase auth
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        firebaseAuthHelper.createUser(email, username, password, new AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // add created user to firestore
                User newUserObj = new User(user.getUid(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>());
                FirebaseUserRepository firebaseUserRepository = new FirebaseUserRepository();
                firebaseUserRepository.addUser(newUserObj, new AddUserCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        // go to home screen after snackbar displayed
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }, 1000);
                    }
                    @Override
                    public void onError(String msg) {
                        setLoadingRegister(false);
                        Log.e("register-user", "Error adding user to firestore: " + msg);
                    }
                });
                firebaseAuthHelper.addUsernameToFirestore(user.getUid(), username);
            }
            @Override
            public void onFailure(Exception e) {
                setLoadingRegister(false);
                Log.e("register-user", "Error creating user: " + e.getMessage());
            }
        });
    }
}
