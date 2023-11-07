package com.example.property_management.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.property_management.R;
import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.data.RoomData;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.widget.Toast;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private final List<String> roomNames;
    private final List<String> roomNamesOrigin = new ArrayList<>();
    private Context context;
    private Set<Integer> initializedRooms = new HashSet<>();
    private LightSensor lightSensor;
    private CompassSensor compassSensor;
    private AudioSensor audioSensor;
    private  List<List<Bitmap>> roomImages = new ArrayList<>();
    private List<LightSensor> lightSensors = new ArrayList<>();
    private List<CompassSensor> compassSensors = new ArrayList<>();
    private List<AudioSensor> audioSensors = new ArrayList<>();
    private ArrayList<ArrayList<String>> roomImagePaths = new ArrayList<>();
    private List<Preview> cameraPreviews = new ArrayList<>();
    private List<ImageCapture> imageCaptures = new ArrayList<>();
    private boolean isFirstBind = true;
    private HashMap<String, RoomData> roomData = new HashMap<>();
    private LinkedHashMap<String, RoomData> inspectedRoomData = new LinkedHashMap<>();

    /**
     * Constructor for RoomAdapter.
     * @param context The current context.
     * @param roomNames A list of room names.
     * @param roomData A map of room names to their respective RoomData.
     */
    public RoomAdapter(Context context, List<String> roomNames, HashMap<String, RoomData> roomData) {
        this.context = context;
        this.roomNames = roomNames;

        // Initialize the original room names list
        for (String names:roomNames){
            this.roomNamesOrigin.add(names);
        }

        // Populate the inspected room data map
        for (String names:roomNames){
            inspectedRoomData.put(names,roomData.get(names));
        }

        // Initialize lists for images and sensors for each room
        for (int i = 0; i < roomNames.size(); i++) {
            // Image list for the room
            roomImages.add(new ArrayList<>());
            // Sensor objects for the room
            audioSensors.add(new AudioSensor(null));
            lightSensors.add(new LightSensor(context, null));
            compassSensors.add(new CompassSensor(context, null));
            // Camera preview and capture objects
            cameraPreviews.add(new Preview.Builder().build());
            imageCaptures.add(new ImageCapture.Builder().build());
        }
        this.roomData = roomData;

        // Decode and add images to the roomImages list for each room
        int i = 0;
        for (String roomName: roomNames){
            for (String imgPath: roomData.get(roomName).getImages()){
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                roomImages.get(i).add(bitmap);
            }
            i++;
        }
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the room layout XML and create a new ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_layout, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param payloads A list of payload objects, can be used to update partial changes to the item's view.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            // Partial update based on the payload
            for (Object payload : payloads) {
                if ("UPDATE_PHOTO_COUNT".equals(payload)) {
                    // Update only photo count related views
                    int updatedPhotoCount = roomImages.get(position).size();
                    holder.photoCount.setText(updatedPhotoCount + " added");
                } else if ("UPDATE_ROOM_NAME".equals(payload)) {
                    // Update only room name related views
                    holder.roomName.setText(roomNames.get(position));
                }
            }
        } else {
            // If no payloads are available, call the full bind method
            onBindViewHolder(holder, position);
        }
    }

    /**
     * Binds the data to the view holder at the specified position.
     * @param holder The view holder to bind to.
     * @param position The position in the data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Set up the room name
        String currentRoomName = roomNames.get(position);
        if ("Others".equals(currentRoomName)) {
            holder.roomName.setText("Others");
            // Make camera function visible
            holder.openCameraButton.setVisibility(View.VISIBLE);
            holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                    public void onClick(View v) {
                        showCameraOptionsDialog(holder);
                    }
                });

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                holder.photoCount.setText(roomImages.get(currentPosition).size() + " added");
            }

            // Set click listener for photo count
            holder.photoCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        showPhotosDialog(holder.getAdapterPosition());
                    }
            });

            // Other components invisible
            holder.noiseIcon.setVisibility(View.GONE);
            holder.lightIcon.setVisibility(View.GONE);
            holder.compassIcon.setVisibility(View.GONE);

            holder.noiseView.setVisibility(View.GONE);
            holder.lightView.setVisibility(View.GONE);
            holder.compassView.setVisibility(View.GONE);

            holder.noiseValueTextView.setVisibility(View.GONE);
            holder.lightValueTextView.setVisibility(View.GONE);
            holder.compassValueTextView.setVisibility(View.GONE);

            holder.noiseTestButton.setVisibility(View.GONE);
            holder.lightTestButton.setVisibility(View.GONE);
            holder.compassTestButton.setVisibility(View.GONE);

            holder.editRoomNameIcon.setVisibility(View.GONE);
            holder.lightValueTextView.setText("11");
            holder.noiseValueTextView.setText("11");
            holder.compassValueTextView.setText("11");
            return;
        }else{
            holder.roomName.setText(currentRoomName);
        }

        // Set sensor values
        if (roomData.get(roomNamesOrigin.get(position)).getBrightness() == -1) {
            // Display noise sensor value or "--" if not available
            holder.lightValueTextView.setText("--");
        } else {
            holder.lightValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getBrightness() + " Lux");
        }

        // Display noise sensor value or "--" if not available
        if (roomData.get(roomNamesOrigin.get(position)).getNoise() == -1) {
            holder.noiseValueTextView.setText("--");
        } else {
            holder.noiseValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getNoise() + " dB");
        }
        // Display compass sensor orientation
        holder.compassValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getWindowOrientation());

        // Retrieve current sensors for the room
        AudioSensor currentAudioSensor = audioSensors.get(position);
        LightSensor currentLightSensor = lightSensors.get(position);
        CompassSensor currentCompassSensor = compassSensors.get(position);

        // Create a callback for this room
        SensorCallback roomCallback = new SensorCallback() {
            // Define behavior for sensor data changes
            @Override
            public void onSensorDataChanged(String sensorType, float value) {
                switch (sensorType) {
                    case "Light":
                        updateLightData(holder, value);
                        break;
                    case "Compass":
                        updateCompassData(holder, value);
                        break;
                }
            }

            // Define behavior for current dB calculations
            @Override
            public void onCurrentDbCalculated(double currentDb) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseValueTextView.setText(String.format("%.2f dB", currentDb));
                });
            }

            // Define behavior for current dB calculations
            @Override
            public void onAverageDbCalculated(double averageDb) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseValueTextView.setText(String.format("%.2f dB", averageDb));
                    inspectedRoomData.get(roomNames.get(position)).setNoise((float)averageDb);

                });
            }

            // Define behavior for Audio test
            @Override
            public void onAudioTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseTestButton.setText("Test");
                    holder.noiseTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isNoiseTesting = false;
                });
            }

            // Define behavior for light test
            @Override
            public void onLightTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.lightTestButton.setText("Test");
                    holder.lightTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isLightTesting = false;
                });
            }

            // Define behavior for compass test
            @Override
            public void onCompassTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.compassTestButton.setText("Test");
                    holder.compassTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isCompassTesting = false;
                });
            }

            // Define behavior for Audio test completion
            @Override
            public void onAudioTestCompletedFull() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseTestButton.setText("Test");
                    holder.noiseTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isNoiseTesting = false;
                });
            }

            // Define behavior for light test completion
            @Override
            public void onLightTestCompletedFull() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.lightTestButton.setText("Test");
                    holder.lightTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isLightTesting = false;
                    String value = holder.lightValueTextView.getText().toString();
                    float numValue = Float.valueOf(extractNumber(value));
                    inspectedRoomData.get(roomNames.get(position)).setBrightness(numValue);
                });
            }

            // Define behavior for compass test completion
            @Override
            public void onCompassTestCompletedFull() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.compassTestButton.setText("Test");
                    holder.compassTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                    holder.isCompassTesting = false;
                    String value = holder.compassValueTextView.getText().toString();
                    inspectedRoomData.get(roomNames.get(position)).setWindowOrientation(value);
                });
            }
        };

        currentAudioSensor.setCallback(roomCallback);
        currentLightSensor.setCallback(roomCallback);
        currentCompassSensor.setCallback(roomCallback);

        // Use the callback with the sensors
        holder.noiseTestButton.setOnClickListener(v -> {

            if (!holder.isNoiseTesting) {
                // Start noise level testing
                holder.isNoiseTesting = true;
                holder.noiseTestButton.setText("Cancel");
                holder.noiseTestButton.setBackgroundColor(Color.RED);

                // Initialize and start the noise test thread
                holder.testAudioThread = new Thread(() -> {
                    currentAudioSensor.startTest();
                });
                holder.testAudioThread.start();

            } else {
                // Cancel the noise level testing
                holder.isNoiseTesting = false;
                holder.noiseTestButton.setText("Test");
                holder.noiseTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));

                // Stop the noise test thread if it's running
                if (holder.testAudioThread != null) {
                    currentAudioSensor.stopTest();
                    holder.testAudioThread.interrupt();
                    holder.testAudioThread = null;

                    // Reset the noise value text view to previous state or to "--" if no data
                    if (roomData.get(roomNamesOrigin.get(position)).getNoise() == -1) {
                        holder.noiseValueTextView.setText("--");
                    } else {
                        holder.noiseValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getNoise() + " dB");
                    }
                }
            }
        });

        // Set a click listener for the light test button
        holder.lightTestButton.setOnClickListener(v -> {
            if (!holder.isLightTesting) {
                // Start light level testing
                holder.isLightTesting = true;
                holder.lightTestButton.setText("Cancel");
                holder.lightTestButton.setBackgroundColor(Color.RED);

                // Initialize and start the light test thread
                holder.testLightThread = new Thread(() -> {
                    currentLightSensor.startTest();
                });
                holder.testLightThread.start();

            } else {
                // Cancel the light level testing
                holder.isLightTesting = false;
                holder.lightTestButton.setText("Test");
                holder.lightTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));

                // Stop the light test thread if it's running
                if (holder.testLightThread != null) {
                    currentLightSensor.stopTest();
                    holder.testLightThread.interrupt();
                    holder.testLightThread = null;

                    // Reset the light value text view to previous state or to "--" if no data
                    if (roomData.get(roomNamesOrigin.get(position)).getBrightness() == -1) {
                        holder.lightValueTextView.setText("--");
                    } else {
                        holder.lightValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getBrightness() + " Lux");
                    }
                }
            }
        });

        // Set a click listener for the compass test button
        holder.compassTestButton.setOnClickListener(v -> {
            if (!holder.isCompassTesting) {
                // Start compass orientation testing
                holder.isCompassTesting = true;
                holder.compassTestButton.setText("Cancel");
                holder.compassTestButton.setBackgroundColor(Color.RED);

                // Initialize and start the compass test thread
                holder.testCompassThread = new Thread(() -> {
                    currentCompassSensor.startTest();
                });
                holder.testCompassThread.start();

            } else {
                // Cancel the compass orientation testing
                holder.isCompassTesting = false;
                holder.compassTestButton.setText("Test");
                holder.compassTestButton.setBackgroundColor(Color.parseColor("#FF6200EE"));

                // Stop the compass test thread if it's running
                if (holder.testCompassThread != null) {
                    currentCompassSensor.stopTest();
                    holder.testCompassThread.interrupt();
                    holder.testCompassThread = null;

                    holder.compassValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getWindowOrientation());
                }
            }
        });

        // Initialize sensors if they haven't been already
        if (!initializedRooms.contains(position)) {
            //Initialize sensors
            audioSensor = new AudioSensor(roomCallback);
            lightSensor = new LightSensor(context, roomCallback);
            compassSensor = new CompassSensor(context, roomCallback);

            initializedRooms.add(position);
        }

        // Set a click listener for the camera button
        holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraOptionsDialog(holder);
            }
        });

        int currentPosition = holder.getAdapterPosition();
        // Update the photo count if the current position is valid
        if (currentPosition != RecyclerView.NO_POSITION) {
            holder.photoCount.setText(roomImages.get(currentPosition).size() + " added");
        }

        // Set a click listener for the photo count view
        holder.photoCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotosDialog(holder.getAdapterPosition());
            }
        });

        // Set a click listener for the edit room name icon
        holder.editRoomNameIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameRoomDialog(position, holder);
            }
        });
    }

    /**
     * Initialize and start the camera for taking photos.
     * @param previewView The view to display the camera preview.
     * @param captureButton The button used to capture the photo.
     * @param cameraDialog The dialog within which the camera UI is displayed.
     * @param holder The ViewHolder associated with the current item.
     */
    private void startCamera(PreviewView previewView, Button captureButton, Dialog cameraDialog, ViewHolder holder) {
        // Obtain the future for the camera provider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        // Get the main thread executor to run the camera operations
        Executor executor = ContextCompat.getMainExecutor(context);

        // Add a listener to set up the camera once the camera provider is available
        cameraProviderFuture.addListener(() -> {
            try {
                // Obtain the camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                // Set up the image capture use case
                ImageCapture imageCapture = new ImageCapture.Builder().build();

                // Select the back camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Unbind any previous use cases and bind the current ones to the lifecycle
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set up the capture button to take a photo when clicked
                captureButton.setOnClickListener(v -> {
                    // Create a file to save the captured image
                    File photoFile = new File(context.getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                    // Take the picture and save it to the file created
                    imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            // Process the saved image
                            String photoPath = photoFile.getAbsolutePath();
                            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

                            try {
                                // Rotate the image if required
                                bitmap = rotateImageIfRequired(bitmap, photoPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // Add the image to the collection and notify the adapter
                            int position = holder.getAdapterPosition();
                            roomImages.get(position).add(bitmap);
                            notifyItemChanged(position, "UPDATE_PHOTO_COUNT");

                            //close camera page
                            cameraDialog.dismiss();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            new BasicSnackbar(holder.itemView, "Error capturing the image.", "error");
                        }
                    });
                });
            } catch (ExecutionException | InterruptedException e) {
                Log.e("RoomAdapter", "Use case binding failed", e);
            }
        }, executor);
    }

    /**
     * Rotates an image if required based on its EXIF orientation tag.
     * @param img The image to be rotated.
     * @param path The file path of the image to check EXIF data.
     * @return The rotated image if rotation was necessary, or the original image otherwise.
     * @throws IOException If reading EXIF data fails.
     */
    private Bitmap rotateImageIfRequired(Bitmap img, String path) throws IOException {
        // Read the EXIF data from the image file
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Rotate the image based on the orientation tag
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    /**
     * Rotates an image by a specific degree.
     * @param img The image bitmap to rotate.
     * @param degree The degree to rotate the image.
     * @return The rotated image.
     */
    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        // Rotate the matrix by the specified degree
        matrix.postRotate(degree);
        // Create and return the new bitmap rotated using the matrix
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    /**
     * Displays a dialog with options to take a photo or upload from the library.
     * @param holder The ViewHolder associated with the current item.
     */
    private void showCameraOptionsDialog(ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_camera_options, null);

        // Initialize buttons for taking photo and adding from library
        Button btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        Button btnAddFromLibrary = view.findViewById(R.id.btn_add_from_library);

        // Set up click listener for the take photo button
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    // Inflate camera preview layout
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View cameraView = inflater.inflate(R.layout.camera_preview_layout, null);

                    // Initialize preview view and capture button
                    PreviewView previewView = cameraView.findViewById(R.id.previewView);
                    Button captureButton = cameraView.findViewById(R.id.captureButton);

                    // Set up and show camera dialog
                    Dialog cameraDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    cameraDialog.setContentView(cameraView);
                    cameraDialog.show();

                    // Start the camera
                    startCamera(previewView, captureButton, cameraDialog, holder);
                }

                // Dismiss the bottom sheet dialog
                bottomSheetDialog.dismiss();
            }
        });

        // Set up click listener for the add from library button
        btnAddFromLibrary.setOnClickListener(new View.OnClickListener() {
            // Start intent to pick image from the gallery
            @Override
            public void onClick(View v) {
                // Start intent to pick image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity) context).startActivityForResult(intent, holder.getAdapterPosition());

                bottomSheetDialog.dismiss();
            }
        });

        // Set the custom view to the bottom sheet and show it
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    /**
     * Retrieves all images for each room.
     * @return A list of lists containing bitmaps for each room.
     */
   public List<List<Bitmap>> getAllRoomImages() {
       return roomImages;
   }

    /**
     * Retrieves the file paths for all images of each room.
     * @return A list of lists containing the file paths for each room.
     */
    public ArrayList<ArrayList<String>> getAllRoomImagePaths() {
        return roomImagePaths;
    }

    /**
     * Displays a dialog with a gallery of photos for a specific room.
     * @param roomPosition The position of the room in the adapter.
     */
    private void showPhotosDialog(int roomPosition) {
        // Create and set up a new dialog for displaying photos
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_photo_gallery);

        // Initialize the RecyclerView for displaying images
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);

        // Adjust the dialog window size to match the content
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        // Set up the adapter for the RecyclerView
        ImageAdapter imageAdapter = new ImageAdapter(roomImages.get(roomPosition), recyclerView, roomPosition) {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
                super.onBindViewHolder(holder, position);
                // Set up the delete button click listener
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    // Show confirmation dialog for deleting a photo
                    @Override
                    public void onClick(View v) {
                        // delete notification
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete Photo");
                        builder.setMessage("Are you sure to delete the photo?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            // Remove the image from the list and update the adapter
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                roomImages.get(roomPosition).remove(position);
                                // Update the photo count display
                                notifyDataSetChanged();
                                updatePhotoCountForRoom(roomPosition);
                            }
                        });
                        // Set up the negative button with no action
                        builder.setNegativeButton("No", null);
                        builder.show();
                    }
                });
            }
        };

        // Set up the layout manager and adapter for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(imageAdapter);

        // Initialize and set up the back button
        Button backButtonGallery = dialog.findViewById(R.id.back_button_gallery);
        backButtonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Show the dialog
        dialog.show();
    }

    /**
     * Notifies that the photo count has been updated for a specific room.
     * @param roomPosition The position of the room in the adapter.
     */
    private void updatePhotoCountForRoom(int roomPosition) {
        notifyItemChanged(roomPosition, "UPDATE_PHOTO_COUNT");
    }

    /**
     * Updates the display of light data for a particular ViewHolder.
     * @param holder The ViewHolder associated with the current item.
     * @param lightValue The light value to display.
     */
    private void updateLightData(ViewHolder holder, float lightValue) {
        // Update the light value on the UI thread
        ((Activity) context).runOnUiThread(() -> {
            holder.lightValueTextView.setText(lightValue + " Lux");
        });
    }

    /**
     * Updates the compass data display for a particular ViewHolder.
     * @param holder The ViewHolder associated with the current item.
     * @param combinedValue The raw compass value combining degree and direction decimal.
     */
    private void updateCompassData(ViewHolder holder, float combinedValue) {
        // Extract degree and direction from the combined value
        int degree = (int) combinedValue;
        float directionDecimal = combinedValue - degree;
        String direction = getDirectionFromDecimal(directionDecimal);
        // Update the compass value on the UI thread
        ((Activity) context).runOnUiThread(() -> {
            holder.compassValueTextView.setText(String.format(Locale.US, "%d° %s", degree, direction));
        });
    }

    /**
     * Converts the decimal part of a compass value to a cardinal direction.
     * @param directionDecimal The decimal part of a compass value.
     * @return The cardinal direction as a string.
     */
    private String getDirectionFromDecimal(float directionDecimal) {
        // Convert the decimal part to an integer
        int directionCode = (int)(directionDecimal * 100);
        // Determine the cardinal direction from the direction code
        switch (directionCode) {
            case 1: return "N";
            case 2: return "NE";
            case 3: return "E";
            case 4: return "SE";
            case 5: return "S";
            case 6: return "SW";
            case 7: return "W";
            case 8: return "NW";
            // Return an empty string if the direction code is invalid
            default: return "N";
        }
    }

    /**
     * Gets the total count of items in the adapter.
     * @return The total number of rooms.
     */
    @Override
    public int getItemCount() {
        return roomNames.size();
    }

    /**
     * Adds an image to the specified room and updates the display.
     * @param roomPosition The position of the room in the list.
     * @param image The image bitmap to add.
     */
    public void addImageToRoom(int roomPosition, Bitmap image) {
        roomImages.get(roomPosition).add(image);
        notifyItemChanged(roomPosition, "UPDATE_PHOTO_COUNT");
    }

    /**
     * Displays a dialog for renaming a room.
     * @param position The position of the room in the adapter.
     * @param holder   The ViewHolder associated with the room.
     */
    private void showRenameRoomDialog(int position, ViewHolder holder) {
        // Prevent the dialog from showing on the initial bind
        isFirstBind = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_rename_room, null);

        // Set up the EditText with the current room name
        EditText roomNameEditText = view.findViewById(R.id.roomNameEditText);
        roomNameEditText.setText(roomNames.get(holder.getAdapterPosition()));

        // Build and show the dialog with Confirm and Cancel actions
        builder.setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Update the room name if it's not a duplicate
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            String newName = roomNameEditText.getText().toString().trim();

                            // Check for duplicate room names
                            boolean isDuplicate = false;
                            for (int i = 0; i < roomNames.size(); i++) {
                                if (newName.equalsIgnoreCase(roomNames.get(i).trim()) && currentPosition != i) {
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            // Update and notify if no duplicate found
                            if (!isDuplicate) {
                                alterRoomName(inspectedRoomData,roomNames.get(currentPosition),newName);
                                roomNames.set(currentPosition, newName);
                                notifyItemChanged(currentPosition, "UPDATE_ROOM_NAME");
                            } else {
                                // Show a snackbar if there's a duplicate name
                                View view = holder.itemView;
                                new BasicSnackbar(view, "Room name cannot be duplicated", "error", Snackbar.LENGTH_LONG);

                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * Alters the name of a room within a LinkedHashMap while preserving order.
     * @param originRoomData The original LinkedHashMap of room data.
     * @param oldRoomName    The current name of the room.
     * @param newRoomName    The new name for the room.
     */
    public static void alterRoomName(LinkedHashMap<String, RoomData> originRoomData, String oldRoomName, String newRoomName) {
        // Check if the original map contains the old key
        if (!originRoomData.containsKey(oldRoomName)) {
            System.out.println("The old room name does not exist in the data.");
            return;
        }

        // Get the value associated with the old key
        RoomData value = originRoomData.get(oldRoomName);

        // Create a new LinkedHashMapRoomData
        LinkedHashMap<String, RoomData> newRoomData = new LinkedHashMap<>();

        for (Map.Entry<String, RoomData> entry : originRoomData.entrySet()) {
            if (entry.getKey().equals(oldRoomName)) {
                // Replace old key with the new key
                newRoomData.put(newRoomName, value);
            } else {
                // Copy other entries as they are
                newRoomData.put(entry.getKey(), entry.getValue());
            }
        }

        // Clear the original map and put all new entries to keep the insertion order
        originRoomData.clear();
        originRoomData.putAll(newRoomData);
    }

    /**
     * Extracts the first number found in a string.
     * @param input The string containing potential number(s).
     * @return The first number found as a string, or "-1" if none found.
     */
    private String extractNumber(String input) {
        // Define a pattern to identify numbers in the input string
        Pattern pattern = Pattern.compile("[+-]?([0-9]*[.])?[0-9]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return "-1";
        }
    }

    /**
     * Retrieves the map containing data of all inspected rooms.
     * @return A LinkedHashMap with room names as keys and RoomData as values.
     */
    public LinkedHashMap<String, RoomData> getInspectedRoomData () {return inspectedRoomData;};

    /**
     * ViewHolder for displaying room information and handling test actions.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public boolean isNoiseTesting = false;
        public boolean isLightTesting = false;
        public boolean isCompassTesting = false;
        public Thread testAudioThread;
        public Thread testLightThread;
        public Thread testCompassThread;
        ImageView editRoomNameIcon;
        public TextView roomName;
        ImageView cameraIcon, noiseIcon, lightIcon, compassIcon;
        TextView imageView, noiseView, lightView, compassView;
        public TextView photoCount;
        public TextView noiseValueTextView;
        public TextView lightValueTextView;
        public TextView compassValueTextView;
        Button openCameraButton, noiseTestButton, lightTestButton, compassTestButton;
        PreviewView previewView;

        /**
         * Constructor for initializing the ViewHolder with itemView.
         * @param itemView The view of the individual list items.
         */
        public ViewHolder(View itemView) {
            super(itemView);

            // Binding the UI elements to their respective views in the layout
            roomName = itemView.findViewById(R.id.room_name);
            cameraIcon = itemView.findViewById(R.id.ic_camera);
            noiseIcon = itemView.findViewById(R.id.ic_noise);
            lightIcon = itemView.findViewById(R.id.ic_light);
            compassIcon = itemView.findViewById(R.id.ic_window);

            imageView = itemView.findViewById(R.id.image_View);
            noiseView = itemView.findViewById(R.id.noiseView);
            lightView = itemView.findViewById(R.id.lightView);
            compassView = itemView.findViewById(R.id.windowView);

            photoCount = itemView.findViewById(R.id.photoCount);
            noiseValueTextView = itemView.findViewById(R.id.noiseValue1);
            lightValueTextView = itemView.findViewById(R.id.lightValue1);
            compassValueTextView = itemView.findViewById(R.id.windowValue1);

            openCameraButton = itemView.findViewById(R.id.openCamera);
            noiseTestButton = itemView.findViewById(R.id.noise_test1);
            lightTestButton = itemView.findViewById(R.id.light_test1);
            compassTestButton = itemView.findViewById(R.id.window_test1);

            editRoomNameIcon = itemView.findViewById(R.id.editRoomNameIcon);
            previewView = itemView.findViewById(R.id.previewView);
        }
    }

    /**
     * Adapter for handling a list of images within a RecyclerView.
     */
    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final List<Bitmap> images;
        private final RecyclerView recyclerView;
        private final int roomPosition;

        /**
         * Constructor for ImageAdapter.
         * @param images List of Bitmap objects representing the images.
         * @param recyclerView The RecyclerView instance.
         * @param roomPosition The position of the room in the list.
         */
        public ImageAdapter(List<Bitmap> images, RecyclerView recyclerView, int roomPosition) {
            this.images = images;
            this.recyclerView = recyclerView;
            this.roomPosition = roomPosition;
        }

        /**
         * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_data_collection_image, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Bitmap bitmap = images.get(position);

            // If the bitmap is not null, display it in the ImageView
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                // If the bitmap is null, display a placeholder image
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.cannot_load_photo)
                        .override(400, 400)
                        .into(holder.imageView);
            }


            // Set click listener for the delete button
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Delete Photo");
                    builder.setMessage("Do you want to delete it?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            images.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, images.size());
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();
                }
            });
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return images.size();
        }

        /**
         * ViewHolder for image items.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public MaterialButton deleteButton;

            /**
             * Constructor for the ViewHolder.
             * @param view The underlying view for the ViewHolder.
             */
            public ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.image);
                deleteButton = view.findViewById(R.id.delete_button);
            }
        }

    }
}
