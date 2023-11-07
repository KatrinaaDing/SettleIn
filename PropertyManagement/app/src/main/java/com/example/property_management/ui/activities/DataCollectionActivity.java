package com.example.property_management.ui.activities;
import com.example.property_management.adapters.RoomAdapter;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.SensorCallback;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.Property;
import com.example.property_management.data.RoomData;
import com.example.property_management.data.UserProperty;
import com.example.property_management.databinding.ActivityDataCollectionBinding;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.material.snackbar.Snackbar;

/**
 * Activity for collecting data on room conditions including images, noise, light, and compass orientation.
 */
public class DataCollectionActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    private HashMap<String, RoomData> initialInspectedData;
    private String propertyId;
    private boolean hasRecordAudioPermission = false;
    private @NonNull ActivityDataCollectionBinding binding;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private ArrayList<String> roomNames = new ArrayList<>();
    private AlertDialog noteDialog;
    private SharedPreferences sharedPreferences;
    private Map<Integer, List<String>> roomImagePathsMap = new LinkedHashMap<>();
    private int room_num;
    private String notes;
    private LinkedHashMap<String, RoomData> inspectedRoomData = new LinkedHashMap<>();

    /**
     * Handles the result from a previous activity, specifically for receiving images selected or taken by the camera.
     * @param requestCode The integer request code originally supplied to startActivityForResult()
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // select from library
            if (data != null && data.getData() != null) {
                // If an image is selected from the gallery
                try {
                    // Retrieve the image as a Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    roomAdapter.addImageToRoom(requestCode, bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (data != null && data.getExtras() != null) {
                // If the image is captured by the camera
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                roomAdapter.addImageToRoom(requestCode, photo);
            }
        }
    }

    /**
     * Requests necessary permissions for the app, specifically camera and audio recording permissions.
     */
    private void requestPermissions() {
        boolean shouldRequestCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
        boolean shouldRequestRecordAudioPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;

        // Request both camera and audio recording permissions if neither are granted
        if (shouldRequestCameraPermission && shouldRequestRecordAudioPermission) {

            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_REQUEST_CAMERA);
        } else if (shouldRequestCameraPermission) {
            // Request only camera permission if it hasn't been granted
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_REQUEST_CAMERA);
        } else if (shouldRequestRecordAudioPermission) {
            // Request only audio recording permission if it hasn't been granted
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.RECORD_AUDIO
            }, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            // If all permissions are granted, initialize the RecyclerView
            initRecyclerView();
        }
    }

    /**
     * Called when the activity is starting. This is where most initialization should go.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this activity
        binding = ActivityDataCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Collect data mode");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // retrieve user's collected data from previous intent
        // if no inspected data, initialInspectedDat will be empty HashMap {}
        Intent intent = getIntent();
        propertyId = intent.getStringExtra("propertyId");
        room_num = intent.getIntExtra("roomNum", 0);
        notes = intent.getStringExtra("notes") != null ? intent.getStringExtra("notes") : "";
        initialInspectedData = (HashMap<String, RoomData>) intent.getSerializableExtra("inspectedData");

        // Initialize the list of room names
        roomNames = (ArrayList<String>) intent.getSerializableExtra("roomNames");
        // Create a new list if none was passed with the intent
        if (roomNames == null) {
            roomNames = new ArrayList<>();
        }

        // Handle the case where there is no previously inspected data
        if (initialInspectedData.size() == 0){
            // Define default room names based on the number of rooms
            roomNames.add("Lounge Room");
            for (int i = 1; i <= room_num; i++) {
                roomNames.add("Room " + i);
            }
            roomNames.add("Others");
            // Initialize room data with default values
            for (String roomname: roomNames){
                initialInspectedData.put(roomname, new RoomData(-1,-1,"--",new ArrayList<String>()));
            }
        }
        // Request permissions necessary for the app to function
        requestPermissions();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE);
        // Save the retrieved notes into SharedPreferences
        saveNote(notes);

        // Set up the note button with an event listener
        Button buttonNote = findViewById(R.id.buttonNote);
        buttonNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog();
            }
        });

        // Set up the finish button with an event listener
        binding.finishButton.setOnClickListener(view -> {
            // Set a flag in SharedPreferences to indicate that details have been updated
            SharedPreferences sharedPreferences = getSharedPreferences("propertyDetailUpdated", MODE_PRIVATE);
            sharedPreferences
                    .edit()
                    .putBoolean("isUpdated", true)
                    .apply();

            // Disable the finish button while updating
            binding.finishButton.setEnabled(false);
            binding.finishButton.setText("Updating...");

            // Prepare a map to hold all room data
            LinkedHashMap<String, Object> roomDataMap = new LinkedHashMap<>();

            // Compile a pattern for extracting numbers from strings
            Pattern pattern = Pattern.compile("[+-]?([0-9]*[.])?[0-9]+");
            // Retrieve room data from the adapter
            inspectedRoomData = roomAdapter.getInspectedRoomData();

            // Iterate over each room to extract and map its data
            for (String roomName: inspectedRoomData.keySet()) {
                RoomData singleRoomData = inspectedRoomData.get(roomName);
                HashMap<String, String> roomInfo = new HashMap<>();
                roomInfo.put("images", "--");
                roomInfo.put("noise", String.format("%.2f", singleRoomData.getNoise()));
                roomInfo.put("brightness", String.format("%.2f", singleRoomData.getBrightness()));
                roomInfo.put("windowOrientation", singleRoomData.getWindowOrientation());
                roomDataMap.put(roomName, roomInfo);
            }

            // Collect photos for each room
            collectRoomPhotos();
            // Create a new RoomData LinkedHashMap
            LinkedHashMap<String, RoomData> roomData = new LinkedHashMap<>();

            // Process and map the room data
            int count = 0;
            for (String roomName:roomDataMap.keySet()){
                HashMap<String,String> singleRoomData = (HashMap<String,String>)roomDataMap.get(roomName);
                ArrayList<String> imgs = new ArrayList<>();
                imgs = (ArrayList<String>) roomImagePathsMap.get(count) != null ?
                        (ArrayList<String>) roomImagePathsMap.get(count) :
                        new ArrayList<String>();
                count++;

                // Create a RoomData object for the room
                RoomData singleRoom = new RoomData(Float.valueOf(singleRoomData.get("brightness")), Float.valueOf(singleRoomData.get("noise")),singleRoomData.get("windowOrientation"), imgs);
                // Add the RoomData object to the map
                roomData.put(roomName, singleRoom);
            }

            // Save the order of room names from the RecyclerView
            ArrayList<String> roomName = new ArrayList<>();
            for (String roomName1: roomDataMap.keySet()){
                roomName.add(roomName1);
            }
            // Update the inspected data in the database
            updateInspectedData(propertyId, roomData, roomName);
            // Show a Snackbar to indicate successful upload
            new BasicSnackbar(findViewById(android.R.id.content), "Upload data successfully!", "success");

            // Delay finishing the activity for a brief period
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    finish();
                }
            }, 3500);
        });
        // Retrieve SharedPreferences for the application
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        // Check if the info dialog has been shown before
        boolean hasShownInfo = prefs.getBoolean("has_shown_info", false);
        // If the info dialog has not been shown, display it
        if (!hasShownInfo) {
            showInfoDialog();
        }
    }

    /**
     * Collects photos from all rooms and handles logging of image paths.
     */
    public void collectRoomPhotos() {
        // get image from adapter
        List<List<Bitmap>> allRoomImages = roomAdapter.getAllRoomImages();

        // get and save image from each room
        for (int roomPosition = 0; roomPosition < allRoomImages.size(); roomPosition++) {
            List<Bitmap> images = allRoomImages.get(roomPosition);
            for (Bitmap image : images) {
                saveImageToGallery(image, roomPosition);
            }
        }

        // get room image path
        ArrayList<ArrayList<String>> allRoomImagePaths = roomAdapter.getAllRoomImagePaths();
        // Log image paths for each room
        for (int i = 0; i < allRoomImagePaths.size(); i++) {
            List<String> imagePathList = allRoomImagePaths.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("Room ").append(i).append(": ");
            for (String path : imagePathList) {
                sb.append(path).append(", ");
            }
        }
        // Log all room image paths
        logRoomImagePaths();
    }

    /**
     * Saves a given image to the device's gallery in a specific room folder.
     * @param image Bitmap representation of the image to save.
     * @param roomPosition The position of the room in the list, used to name the folder.
     */
    private void saveImageToGallery(Bitmap image, int roomPosition) {
        // Set up metadata for the image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Specify the directory path for the image
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Room_" + roomPosition);

        // Insert the metadata into the MediaStore and get the URI for the image
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            // Open an output stream to the URI and compress the image
            OutputStream os = getContentResolver().openOutputStream(uri);
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            // Get the actual file path from the URI
            String realPath = getRealPathFromURI(uri);

            // If the room doesn't have an entry in the map, create one
            if (!roomImagePathsMap.containsKey(roomPosition)) {
                roomImagePathsMap.put(roomPosition, new ArrayList<>());
            }
            // Add the image path to the map
            roomImagePathsMap.get(roomPosition).add(realPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs the paths of all saved room images.
     */
    private void logRoomImagePaths() {
        Log.d("RoomImagePaths", roomImagePathsMap.toString());
    }

    /**
     * Retrieves the real file system path from a content URI.
     * @param uri The content URI to resolve.
     * @return The real path as a String.
     */
    private String getRealPathFromURI(Uri uri) {
        String path = "";
        // Query the content resolver for the file path
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            // Get the index of the column containing the file path
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            // Retrieve the file path
            path = cursor.getString(idx);
            cursor.close();
        }
        return path;
    }

    /**
     * Handles action bar item clicks.
     * @param item The menu item that was clicked.
     * @return true if the event was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button in the action bar
        if (item.getItemId() == android.R.id.home) {
            // Close the activity when the back button is pressed
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            // Show the info dialog when the info button is pressed
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the contents of the Activity's standard options menu.
     * @param menu The options menu in which items are placed.
     * @return true for the menu to be displayed; if false, it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info_data_collection, menu);
        return true;
    }

    /**
     * Displays an informational dialog to the user with guidance on how to use the app.
     */
    private void showInfoDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Tutorial");

        // Use a SpannableStringBuilder to format dialog message text
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        // Arrays to hold titles and descriptions for each tutorial section
        String[] titles = {
                "Taking photos",
                "Collecting brightness data",
                "Collecting noise data",
                "Collecting orientation data",
                "Note",
                "Changing room name"
        };
        String[] descriptions = {
                "You can take photo or upload photos from library for each room by pressing the button of \"Add\" in the line of \"Photos\". And then you can show or delete photo in the bottom of \"X added\", where X is the number of collected photos.",
                "You can collect brightness data for each room by pressing the button of \"Add\" in the line of \"Light Level\".",
                "You can collect noise data for each room by pressing the button of \"Add\" in the line of \"Noise Level\".",
                "You can collect orientation data for each room by pressing the button of \"Add\" in the line of \"Window Orientation\".",
                "You can write your note for property in the note area.",
                "You can change names of rooms by pressing the edit icon."
        };

        // Loop to append each title and description to the SpannableStringBuilder
        for (int i = 0; i < titles.length; i++) {
            SpannableString title = new SpannableString(titles[i]);
            title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(title);
            ssb.append("\n");

            SpannableString description = new SpannableString(descriptions[i]);
            ssb.append(description);
            if (i < titles.length - 1) {
                ssb.append("\n\n");
            }
        }

        // Set the message and the positive button for the dialog
        builder.setMessage(ssb);
        builder.setPositiveButton("OK", (dialog, id) -> {
            dialog.dismiss();

            //  Saved to SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
            editor.putBoolean("has_shown_info", true);
            editor.apply();
        });
        builder.show();
    }

    /**
     * Displays a dialog for taking notes.
     */
    private void showNoteDialog() {
        noteDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add Note")
                .setView(R.layout.dialog_note)
                .setPositiveButton("Save", (dialog, which) -> {
                    EditText editTextNote = noteDialog.findViewById(R.id.editTextNote);
                    String note = editTextNote.getText().toString();
                    saveNote(note);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create();
        // prevent accidental dismiss
        noteDialog.setCanceledOnTouchOutside(false);
        noteDialog.show();
    }

    /**
     * Saves the user's note to SharedPreferences.
     * @param note The note text to be saved.
     */
    private void saveNote(String note) {
        // Save the note in SharedPreferences
        sharedPreferences.edit().putString("note", note).apply();
    }

    /**
     * Updates the inspected data in Firebase.
     * @param propertyId   The ID of the property.
     * @param inspectedData The data that has been inspected.
     * @param roomNames    The names of the rooms.
     */
    private void updateInspectedData(String propertyId, HashMap<String, RoomData> inspectedData, ArrayList<String> roomNames) {

        // update ispected status to firebase
        HashMap<String, Object> payload = new HashMap<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        payload.put("properties." + propertyId + ".inspectedData", inspectedData);
        payload.put("properties." + propertyId + ".notes", sharedPreferences.getString("note", ""));
        payload.put("properties." + propertyId + ".roomNames", roomNames);
        FirebaseUserRepository userRepository = new FirebaseUserRepository();

        // Perform the update and handle callbacks for success or error
        userRepository.updateUserFields(userId, payload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                // Re-enable the button and reset its text after successful update
                runOnUiThread(() -> {
                    //binding.finishButton.setEnabled(true);
                    binding.finishButton.setText("Finish");
                });
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                // Display an error message using Snackbar
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("update-inspected-failure", msg);
                // Re-enable the button and reset its text after successful update
                runOnUiThread(() -> {
                    //binding.finishButton.setEnabled(true);
                    binding.finishButton.setText("Finish");
                });
            }
        });
    }

    /**
     * Initializes the RecyclerView for displaying room data.
     */
    private void initRecyclerView() {
        // Initialize rooms RecyclerView
        roomsRecyclerView = findViewById(R.id.recycler_view);
        // Set up the adapter with room names and initial data
        roomAdapter = new RoomAdapter(this, roomNames, initialInspectedData);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(roomAdapter);
    }

    /**
     * Callback for the result from requesting permissions.
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the permission request was for audio recording
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, initialize the RecyclerView
                hasRecordAudioPermission = true;
                initRecyclerView();
            } else {
                // Permission was denied, inform the user with a Snackbar
                new BasicSnackbar(findViewById(android.R.id.content), "The app needs audio recording permission to function properly.", "info", Snackbar.LENGTH_LONG);
            }
        }
    }

    /**
     * Converts the decimal part of a compass value to a cardinal direction.
     * @param directionDecimal The decimal part of a compass value.
     * @return The cardinal direction as a string.
     */
    private String getDirectionFromDecimal(float directionDecimal) {
        int directionCode = (int)(directionDecimal * 100);  // Convert the decimal part to an integer
        switch (directionCode) {
            case 1: return "N";
            case 2: return "NE";
            case 3: return "E";
            case 4: return "SE";
            case 5: return "S";
            case 6: return "SW";
            case 7: return "W";
            case 8: return "NW";
            default: return "";  // Return an empty string if the direction code is invalid
        }
    }
}
