package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebasePropertyRepository;
import com.example.property_management.callbacks.AddPropertyCallback;
import com.example.property_management.callbacks.onValueChangeCallback;
import com.example.property_management.data.NewProperty;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.ActivityAddPropertyBinding;
import com.example.property_management.ui.fragments.base.ArrowNumberPicker;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.UrlValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class AddPropertyActivity extends AppCompatActivity {
    private ActivityAddPropertyBinding binding;

    private String url = "";
    private String address = "";
    private int bedroomNumber = 0;
    private int bathroomNumber = 0;
    private int parkingNumber = 0;
    private float lat;
    private float lng;

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
        // property info
        TextView propertyAddress = findViewById(R.id.propertyAddress);
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

        findViewById(R.id.submitBtn).setOnClickListener(v -> {
            // on submit new property
            // TODO: post data here
            System.out.println("bedroom: " + bedroomNumber + ", bathroom: " + bathroomNumber +
                    ", parking: " + parkingNumber);
            System.out.println("address: " + address + ", url: " + url);
            submitProperty();
        });
        scrapeUrlBtn.setOnClickListener(v -> {
            Helpers.closeKeyboard(this, urlInputLayout);
            // on scrape url
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

    private void submitProperty() {
        // create new property object
        Property newProperty = new NewProperty(url, bedroomNumber, bathroomNumber, parkingNumber,
                address, (float) 0.0, (float) 0.0).castToProperty();
        // post to firebase
        FirebasePropertyRepository db = new FirebasePropertyRepository();
        db.addProperty(newProperty, new AddPropertyCallback() {
            @Override
            public void onSuccess(String documentId) {
                Intent intent = new Intent(AddPropertyActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(String msg) {
                System.out.println("error");
            }
        });
    }
    private void fetchCoordinates() {
        this.lat = 0;
        this.lng = 0;
    }

    private void fetchPropertyInfo(TextInputLayout urlInputLayout) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.scrapeProperty(urlInputLayout.getEditText().getText().toString())
            .addOnSuccessListener(result -> {
                // set property info
                NewProperty resultProperty = (NewProperty) result;
                setPropertyInfo(resultProperty);
                // set url input helper text
                urlInputLayout.setHelperText("");
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                System.out.println(e.getMessage());
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
        this.address = property.getAddress();
        this.url = property.getUrl();

        // set UI
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        TextView propertyAddress = findViewById(R.id.propertyAddress);
        bedroomNumberPicker.setValue(this.bedroomNumber);
        bathroomNumberPicker.setValue(this.bathroomNumber);
        parkingNumberPicker.setValue(this.parkingNumber);
        propertyAddress.setText(this.address);
    }

}
