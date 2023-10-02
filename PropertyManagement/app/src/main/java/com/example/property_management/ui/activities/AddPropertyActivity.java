package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebasePropertyRepository;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.AddPropertyCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.callbacks.onValueChangeCallback;
import com.example.property_management.data.NewProperty;
import com.example.property_management.databinding.ActivityAddPropertyBinding;
import com.example.property_management.ui.fragments.base.ArrowNumberPicker;
import com.example.property_management.ui.fragments.base.AutocompleteFragment;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.UrlValidator;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;

public class AddPropertyActivity extends AppCompatActivity {
    private ActivityAddPropertyBinding binding;
    private String url = "";
    private int bedroomNumber = 0;
    private int bathroomNumber = 0;
    private int parkingNumber = 0;
    private float lat = 0;
    private float lng = 0;
    private int price = 0;

    AutocompleteFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_add_property);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ================================== Components =======================================
        // inspection date label
        TextView inspectionDate = binding.inspectionDate;
        // date picker
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker
                .Builder
                .datePicker()
                .setTitleText("Select Inspection date")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        // inspection time label
        TextView inspectionTime = findViewById(R.id.inspectionTime);
        // inspection time
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Inspection Time")
                .build();
        // url input
        TextInputLayout urlInputLayout = findViewById(R.id.urlInputLayout);
        // scrape url button
        MaterialButton scrapeUrlBtn = findViewById(R.id.scrapeUrlBtn);
        // number pickers
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);

        // ================================== listeners =======================================
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // on click set date
           inspectionDate.setText(datePicker.getHeaderText());
        });
        timePicker.addOnPositiveButtonClickListener(selection -> {
            // on click set time
            inspectionTime.setText(timePicker.getHour() + ":" + timePicker.getMinute());
        });
        inspectionDate.setOnClickListener(v -> {
            // on select date
            datePicker.show(getSupportFragmentManager(), "date_picker");
        });
        inspectionTime.setOnClickListener(v -> {
            // on select time
            timePicker.show(getSupportFragmentManager(), "time_picker");
        });
        // on change url text
        urlInputLayout.addOnEditTextAttachedListener(textInputLayout ->
                textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        urlInputLayout.setError(null);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {}
                })
        );
        // on submit new property
        findViewById(R.id.submitBtn).setOnClickListener(v -> submitProperty(this));
        scrapeUrlBtn.setOnClickListener(v -> {
            // close keyboard
            Helpers.closeKeyboard(this);
            // scrape advertisement from url
            if (!validateUrl()) return;
            urlInputLayout.setHelperText("Getting information...");
            fetchPropertyInfo(urlInputLayout);
            fetchCoordinates();
        });
        // handle number picker value change
        bedroomNumberPicker.setOnValueChangeListener(new onValueChangeCallback() {
            @Override
            public void onChange(int newValue) {
                bedroomNumber = newValue;
            }});
        bathroomNumberPicker.setOnValueChangeListener(newValue -> bathroomNumber = newValue);
        parkingNumberPicker.setOnValueChangeListener(newValue -> parkingNumber = newValue);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void submitProperty(AppCompatActivity activity) {
        // create new property object
        NewProperty newProperty = new NewProperty(url, bedroomNumber, bathroomNumber, parkingNumber,
                autocompleteFragment.getSelectedPlaceName(), lat, lng, price);
        // post to firebase
        FirebasePropertyRepository db = new FirebasePropertyRepository();
        db.addProperty(newProperty, new AddPropertyCallback() {
            @Override
            public void onSuccess(String documentId) {
                updateUserProperty(activity, newProperty, documentId);
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }
    private void fetchCoordinates() {
        // TODO: fetch coordinates
        this.lat = 0;
        this.lng = 0;
    }

    private void fetchPropertyInfo(TextInputLayout urlInputLayout) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.scrapeProperty(urlInputLayout.getEditText().getText().toString())
            .addOnSuccessListener(resultProperty -> {
                // set property info
                setPropertyInfo(resultProperty);
                // set url input helper text
                urlInputLayout.setHelperText("");
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                Log.e("scrape-url-error", e.getMessage());
                urlInputLayout.setError("Error: " + e.getMessage());
            });
    }
    private boolean validateUrl() {
        String urlInput = binding.urlInputLayout.getEditText().getText().toString().trim();
        if (urlInput.isEmpty()) {
            binding.urlInputLayout.setError("Field can't be empty");
            return false;
        } else if (!UrlValidator.isValidUrl(urlInput)) {
            binding.urlInputLayout.setError("Invalid URL. Please check again.");
            return false;
        } else {
            binding.urlInputLayout.setError(null);
            return true;
        }
    }

    private void setPropertyInfo(NewProperty property) {
        // set properties
        this.bedroomNumber = property.getNumBedrooms();
        this.bathroomNumber = property.getNumBathrooms();
        this.parkingNumber = property.getNumParking();
        this.url = property.getHref();
        this.price = property.getPrice();

        // set UI
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        bedroomNumberPicker.setValue(this.bedroomNumber);
        bathroomNumberPicker.setValue(this.bathroomNumber);
        parkingNumberPicker.setValue(this.parkingNumber);

        autocompleteFragment = (AutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.auto_property_fragment);
        Log.i("isnull", "autocompleteFragment: " + (autocompleteFragment == null ? "null" : "not null"));

        if (autocompleteFragment == null) {
            Log.e("isnull", "autocompleteFragment is null");
            return;
        }
        // set address text to UI
        autocompleteFragment.setPlaceNameText(property.getAddress());
    }

    private void updateUserProperty(AppCompatActivity activity, NewProperty newProperty, String propertyId) {
        // create object to update user document
        HashMap<String, Object> propertyPayload = newProperty.toUpdateUserObject(propertyId);
        HashMap<String, Object> userUpdatePayload = new HashMap<>();
        userUpdatePayload.put("properties." + propertyId, propertyPayload);
        // get user id
        String userId = new FirebaseAuthHelper(activity).getCurrentUser().getUid();
        // update user document
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        userRepository.updateUserFields(userId, userUpdatePayload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                new BasicSnackbar(findViewById(android.R.id.content),
                        "Property added successfully",
                        "success");
                // redirect to main activity for showing snackbar
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(AddPropertyActivity.this, MainActivity.class);
                    startActivity(intent);
                }, 1000);
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }
}
