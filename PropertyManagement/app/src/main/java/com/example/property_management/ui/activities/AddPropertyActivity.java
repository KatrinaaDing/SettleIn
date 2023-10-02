package com.example.property_management.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;

public class AddPropertyActivity extends AppCompatActivity {
    private ActivityAddPropertyBinding binding;
    private String url = "";
    private int bedroomNumber = 0;
    private int bathroomNumber = 0;
    private int parkingNumber = 0;
    private float lat = 0;
    private float lng = 0;
    private int price = 0;
    private ArrayList<String> images;


    AutocompleteFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_add_property);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ================================== Components =======================================
//        // inspection date label
//        TextView inspectionDate = binding.inspectionDate;
//        // date picker
//        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());
//        MaterialDatePicker<Long> datePicker =
//                MaterialDatePicker
//                .Builder
//                .datePicker()
//                .setTitleText("Select Inspection date")
//                .setCalendarConstraints(constraintsBuilder.build())
//                .build();
//        // inspection time label
//        TextView inspectionTime = findViewById(R.id.inspectionTime);
//        // inspection time
//        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
//                .setTimeFormat(TimeFormat.CLOCK_12H)
//                .setHour(12)
//                .setMinute(0)
//                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
//                .setTitleText("Select Inspection Time")
//                .build();

        // initialise components
        initSubmitButton();
        initUrlInputLayout();
        initScrapeUrlButton();
        initArrowNumberPickers();


        // ================================== listeners =======================================
//        datePicker.addOnPositiveButtonClickListener(selection -> {
//            // on click set date
//           inspectionDate.setText(datePicker.getHeaderText());
//        });
//        timePicker.addOnPositiveButtonClickListener(selection -> {
//            // on click set time
//            inspectionTime.setText(timePicker.getHour() + ":" + timePicker.getMinute());
//        });
//        inspectionDate.setOnClickListener(v -> {
//            // on select date
//            datePicker.show(getSupportFragmentManager(), "date_picker");
//        });
//        inspectionTime.setOnClickListener(v -> {
//            // on select time
//            timePicker.show(getSupportFragmentManager(), "time_picker");
//        });
    }

    /**
     * Handle back button on action bar
     * @param item menu item
     * @return true if handled, false otherwise
     */
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

    /**
     * Initialise property amenities arrow number pickers
     */
    private void initArrowNumberPickers() {
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        bedroomNumberPicker.setOnValueChangeListener(newValue -> bedroomNumber = newValue);
        bathroomNumberPicker.setOnValueChangeListener(newValue -> bathroomNumber = newValue);
        parkingNumberPicker.setOnValueChangeListener(newValue -> parkingNumber = newValue);
    }
    /**
     * Initialise submit property button
     */
    private void initSubmitButton() {
        // on submit new property
        Button submitBtn = findViewById(R.id.submitBtn);
        // disable submit button by default
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(v -> submitProperty(this));
    }

    /**
     * Enable and disable new property submission button
     */
    private void enableSubmit() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(true);
    }
    private void disableSubmit() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
    }
    public void setSubmitLoading() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setText("Submitting...");
        submitBtn.setEnabled(false);
    }

    /**
     * Initialise url text input layout
     */
    private void initUrlInputLayout() {
        TextInputLayout urlInputLayout = findViewById(R.id.urlInputLayout);

        // on change url text
        urlInputLayout.addOnEditTextAttachedListener(textInputLayout ->
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    urlInputLayout.setError(null);
                    urlInputLayout.setHelperText("");
                }
                @Override
                public void afterTextChanged(Editable editable) {}
            })
        );
    }

    /**
     * Initialise scrape url button
     */
    private void initScrapeUrlButton() {
        // scrape url button
        MaterialButton scrapeUrlBtn = findViewById(R.id.scrapeUrlBtn);
        // url input
        TextInputLayout urlInputLayout = findViewById(R.id.urlInputLayout);
        scrapeUrlBtn.setOnClickListener(v -> {
            // close keyboard on click
            Helpers.closeKeyboard(this);
            // scrape advertisement from url
            if (!validateUrl()) return;
            // disable search and submit button on start scraping
            scrapeUrlBtn.setEnabled(false);
            disableSubmit();
            urlInputLayout.setHelperText("Getting information...");
            fetchPropertyInfo(urlInputLayout, () -> {
                // enable click action on finish scraping
                scrapeUrlBtn.setEnabled(true);
                fetchCoordinates(() -> {
                    // enable submit button on finish fetching coordinates
                    enableSubmit();
                });
            });

        });
    }

    /**
     * Submit new property to firebase
     * @param activity current activity
     */
    private void submitProperty(AppCompatActivity activity) {
        // set submit button to loading state
        setSubmitLoading();
        // check if property exists
        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("href", url);
        payLoad.put("address", autocompleteFragment.getSelectedPlaceName());
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.checkPropertyExists(payLoad)
            .addOnSuccessListener(result -> {
                if (result == null) {
                    // property does not exist, add the property
                    Log.d("add-property-exists", "can add property");
                    addProperty(activity);
                } else {
                    // property exists, return the document id
                    // TODO: pop dialog to ask user if redirect to property page
                    Log.d("add-property-exists", "property exists with document id " + result);
                    enableSubmit();
                    new BasicSnackbar(findViewById(android.R.id.content),
                            "You have already added this property.",
                            "error");
                }
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                Log.e("add-property", e.getMessage());
                enableSubmit();
                new BasicSnackbar(findViewById(android.R.id.content),
                        "Error: " + e.getMessage(),
                        "error");
            });

    }

    /**
     * Add new property to firebase
     * @param activity current activity
     */
    private void addProperty(AppCompatActivity activity) {
        // post to firebase
        // create new property object
        NewProperty newProperty = new NewProperty(url, bedroomNumber, bathroomNumber, parkingNumber,
                autocompleteFragment.getSelectedPlaceName(), lat, lng, price, images);
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
    /**
     * Fetch coordinates from address
     * @param onComplete callback task
     */
    private void fetchCoordinates(Runnable onComplete) {
        // TODO: fetch coordinates
        this.lat = 0;
        this.lng = 0;
        // run callback task (allow submit)
        // TODO: only allow submit when coordinates are successfully fetched
        onComplete.run();
    }

    /**
     * Fetch property info from url
     * @param urlInputLayout the text input layout to enter url
     * @param onComplete callback task
     */
    private void fetchPropertyInfo(TextInputLayout urlInputLayout, Runnable onComplete) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.scrapeProperty(urlInputLayout.getEditText().getText().toString())
            .addOnSuccessListener(resultProperty -> {
                // set property info
                setPropertyInfo(resultProperty);
                // set url input helper text
                urlInputLayout.setHelperText("");
                // run callback task
                onComplete.run();
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                Log.e("scrape-url-error", e.getMessage());
                urlInputLayout.setError("Error: " + e.getMessage());
                // run callback task
                onComplete.run();
            });

    }

    /**
     * Validate url input
     * @return true if valid, false otherwise
     */
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

    /**
     * Set scrapped property info to UI
     * @param property property object returned from scraping
     */
    private void setPropertyInfo(NewProperty property) {
        // set properties
        this.bedroomNumber = property.getNumBedrooms();
        this.bathroomNumber = property.getNumBathrooms();
        this.parkingNumber = property.getNumParking();
        this.url = property.getHref();
        this.price = property.getPrice();
        this.images = property.getImages();

        // set UI
        TextInputLayout priceInputLayout = findViewById(R.id.priceInputLayout);
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        priceInputLayout.getEditText().setText(String.valueOf(this.price));
        bedroomNumberPicker.setValue(this.bedroomNumber);
        bathroomNumberPicker.setValue(this.bathroomNumber);
        parkingNumberPicker.setValue(this.parkingNumber);

        // set address to autocomplete fragment
        autocompleteFragment = (AutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.auto_property_fragment);
        if (autocompleteFragment == null) {
            Log.e("isnull", "autocompleteFragment is null");
            return;
        }
        autocompleteFragment.setPlaceNameText(property.getAddress());
    }

    /**
     * Update user document with new property
     * @param activity current activity
     * @param newProperty new property object
     * @param propertyId property id returned from firebase
     */
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
                enableSubmit();
                // redirect to main activity on success
                new BasicSnackbar(findViewById(android.R.id.content),
                        "Property added successfully",
                        "success");
                // delay redirection to main activity for showing snackbar
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(AddPropertyActivity.this, MainActivity.class);
                    startActivity(intent);
                }, 1000);
            }
            @Override
            public void onError(String msg) {
                enableSubmit();
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }
}
