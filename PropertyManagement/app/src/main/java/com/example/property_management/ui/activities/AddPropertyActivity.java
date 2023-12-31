package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebasePropertyRepository;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.AddPropertyCallback;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.NewProperty;
import com.example.property_management.databinding.ActivityAddPropertyBinding;
import com.example.property_management.ui.fragments.base.ArrowNumberPicker;
import com.example.property_management.ui.fragments.base.AutocompleteFragment;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.example.property_management.utils.Helpers;
import com.example.property_management.utils.UrlValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AddPropertyActivity
 * Allow user to add new property by entering url or manually entering property information
 */
public class AddPropertyActivity extends AppCompatActivity {
    public ActivityAddPropertyBinding binding;
    private String url = "";
    private int bedroomNumber = 0;
    private int bathroomNumber = 0;
    private int parkingNumber = 0;
    private int price = 0;
    private String selectedAddress = "";
    private double lat = Double.NaN;
    private double lng = Double.NaN;
    private ArrayList<String> images;
    AutocompleteFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_add_property);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialise components
        initSubmitButton();
        initUrlInputLayout();
        initScrapeUrlButton();
        initPriceArrowNumberPickers();
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
    private void initPriceArrowNumberPickers() {
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        bedroomNumberPicker.setOnValueChangeListener(newValue -> bedroomNumber = newValue);
        bathroomNumberPicker.setOnValueChangeListener(newValue -> bathroomNumber = newValue);
        parkingNumberPicker.setOnValueChangeListener(newValue -> parkingNumber = newValue);

        TextInputLayout priceInputLayout = findViewById(R.id.priceInputLayout);
        priceInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                price = Integer.parseInt(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
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
     * Enable new property submission button
     */
    private void enableSubmit() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setText("Submit");
        submitBtn.setEnabled(true);
    }

    /**
     * Disable new property submission button
     */
    private void disableSubmit() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
    }

    /**
     * Allow edit price and amenities
     * @param enable true to enable, false to disable
     */
    private void enableEditPrice(boolean enable) {
        TextInputLayout priceInputLayout = findViewById(R.id.priceInputLayout);
        priceInputLayout.setEnabled(enable);
    }

    /**
     * Allow edit price and amenities number picker
     * @param enable true to enable, false to disable
     */
    private void enableEditAmenities(boolean enable) {
        ArrowNumberPicker bedroomNumberPicker = findViewById(R.id.bedroomNumberPicker);
        ArrowNumberPicker bathroomNumberPicker = findViewById(R.id.bathroomNumberPicker);
        ArrowNumberPicker parkingNumberPicker = findViewById(R.id.parkingNumberPicker);
        bedroomNumberPicker.setEnabled(enable);
        bathroomNumberPicker.setEnabled(enable);
        parkingNumberPicker.setEnabled(enable);
    }

    /**
     * Set submit button to loading state
     */
    public void setSubmitLoading() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setText("Submitting...");
        submitBtn.setEnabled(false);
    }

    /**
     * Set submit button to fetching coordinates state
     */
    public void setSubmitFetchingCoordinates() {
        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setText("Validating address...");
        submitBtn.setEnabled(false);
    }

    /**
     * Initialise url text input layout
     */
    private void initUrlInputLayout() {
        TextInputLayout urlInputLayout = findViewById(R.id.urlInputLayout);
        MaterialButton scrapeUrlBtn = findViewById(R.id.scrapeUrlBtn);
        // on change url text
        urlInputLayout.addOnEditTextAttachedListener(textInputLayout ->
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    urlInputLayout.setError(null);
                    urlInputLayout.setHelperText("");
                    scrapeUrlBtn.setEnabled(true);
                    enableEditPrice(true);
                    enableEditAmenities(true);
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
            // assume scraped price and amenities are correct, disable edit
            enableEditPrice(false);
            enableEditAmenities(false);
            disableSubmit();
            urlInputLayout.setHelperText("Getting information...");
            fetchPropertyInfo(urlInputLayout, () -> {
                // enable click action on finish scraping
                scrapeUrlBtn.setEnabled(true);
                fetchCoordinates(() -> {
                    // run on finish fetching coordinates
                    // enable submit button
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
        autocompleteFragment = (AutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.auto_property_fragment);
        // invalid address if lat or lng is NaN
        if (Double.isNaN(autocompleteFragment.getLat()) || Double.isNaN(autocompleteFragment.getLng())) {
            new BasicSnackbar(findViewById(android.R.id.content), "Invalid address", "error");
            return;
        }

        // set submit button to loading state
        setSubmitLoading();
        // check if property exists
        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("href", url);
        payLoad.put("address", selectedAddress);
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.checkPropertyExists(payLoad)
            .addOnSuccessListener(propertId -> {
                if (propertId == null) {
                    // property does not exist, add the property
                    Log.d("add-property-exists", "can add property");
                    addProperty(activity);
                } else {
                    // property exists, take user to property detail page
                    BasicDialog dialog = new BasicDialog(true,
                            "You have already added this property.",
                            "Would you like to view the property?",
                            "No, thanks",
                            "Okay");
                    dialog.setCallback(new BasicDialogCallback() {
                        @Override
                        public void onLeftBtnClick() {
                            dialog.dismiss();
                        }
                        @Override
                        public void onRightBtnClick() {
                            Intent intent = new Intent(getApplicationContext(), PropertyDetailActivity.class);
                            intent.putExtra("property_id", propertId);
                            startActivity(intent);
                        }
                    });
                    enableSubmit();
                    dialog.show(getSupportFragmentManager(), "existing property dialog");
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
        if (url == "") {
            url = null;
        }
        NewProperty newProperty = new NewProperty(url, bedroomNumber, bathroomNumber, parkingNumber,
                selectedAddress, lat, lng, price, images);
        FirebasePropertyRepository db = new FirebasePropertyRepository();
        db.addProperty(newProperty, new AddPropertyCallback() {
            @Override
            public void onSuccess(String documentId) {
                Log.i("add-property-success", "property added with id " + documentId);
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
     * @param onSuccess callback task that runs on success
     */
    private void fetchCoordinates(Runnable onSuccess) {
        // check if autocompleteFragment is null
        if (autocompleteFragment == null) {
            new BasicSnackbar(findViewById(android.R.id.content), "Error: Cannot fetch property location. Try again later.", "error");
            enableEditPrice(true);
            enableEditAmenities(true);
            enableSubmit();
            return;
        }
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        Log.i("fetch-coordinates", "fetching coordinates of " + autocompleteFragment.getSelectedAddress());
        setSubmitFetchingCoordinates();
        // call firebase function to get lat and lng
        firebaseFunctionsHelper.getLngLatByAddress(autocompleteFragment.getSelectedAddress())
            .addOnSuccessListener(result -> {
                // set lat and lng
                double resultLat = result.get("lat");
                double resultLng = result.get("lng");
                Log.i("fetch-coordinates", "lat: " + resultLat + ", lng: " + resultLng);

                // set lat and lng to autocompleteFragment
                autocompleteFragment.setLat(resultLat);
                autocompleteFragment.setLng(resultLng);
                lat = resultLat;
                lng = resultLng;

                // run callback task (allow submit)
                // only allow submit when coordinates are successfully fetched
                onSuccess.run();
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                Log.e("get lng lat error", e.getMessage());
                new BasicSnackbar(findViewById(android.R.id.content), "Error: Cannot fetch property location. Try again later.", "error");
            });
    }

    /**
     * Fetch property info from url
     * @param urlInputLayout the text input layout to enter url
     * @param onSuccess callback task that run on success
     */
    private void fetchPropertyInfo(TextInputLayout urlInputLayout, Runnable onSuccess) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.scrapeProperty(urlInputLayout.getEditText().getText().toString())
            .addOnSuccessListener(resultProperty -> {
                // set property info
                setPropertyInfo(resultProperty);
                // set url input helper text
                urlInputLayout.setHelperText("");
                // run callback task
                onSuccess.run();
            })
            .addOnFailureListener(e -> {
                // pop error at input box
                Log.e("scrape-url-error", e.getMessage());
                if (e.getMessage().contains("INTERNAL")) {
                    urlInputLayout.setError("Error: Cannot scrape the property. Please try again later.");
                } else {
                    urlInputLayout.setError("Error: " + e.getMessage());
                }
                enableEditPrice(true);
                enableEditAmenities(true);
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
        // set address text to UI
        autocompleteFragment.setAddressText(property.getAddress());
        selectedAddress = property.getAddress();
    }

    /**
     * Update user document with new property
     * @param activity current activity
     * @param newProperty new property object
     * @param propertyId property id returned from firebase
     */
    private void updateUserProperty(AppCompatActivity activity, NewProperty newProperty, String propertyId) {
        Log.i("add-user-property", "updating user document" + propertyId);
        // create object to update user document
        HashMap<String, Object> propertyPayload = newProperty.toUpdateUserObject(propertyId);
        propertyPayload.put("createdAt", new Date());
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

    /**
     * Set selected address from autocomplete fragment to AddPropertyActivity
     * @param selectedAddress
     */
    public void setSelectedAddress(String selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    /**
     * Set lat and lng from autocomplete fragment to AddPropertyActivity
     * @param lat
     * @param lng
     */
    public void setCoordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
