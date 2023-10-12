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
import com.example.property_management.R;
import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.widget.Toast;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private final List<String> roomNames;
    private int roomCount;
    private Context context;
    private Set<Integer> initializedRooms = new HashSet<>();
    public TextView photoCount;
    private LightSensor lightSensor;
    private CompassSensor compassSensor;
    private AudioSensor audioSensor;
    private static List<List<Bitmap>> roomImages = new ArrayList<>();
    private List<LightSensor> lightSensors = new ArrayList<>();
    private List<CompassSensor> compassSensors = new ArrayList<>();
    private List<AudioSensor> audioSensors = new ArrayList<>();
    private ArrayList<ArrayList<String>> roomImagePaths = new ArrayList<>();
    private List<Preview> cameraPreviews = new ArrayList<>();
    private List<ImageCapture> imageCaptures = new ArrayList<>();

    public RoomAdapter(Context context, List<String> roomNames) {
        this.context = context;
        this.roomNames = roomNames;
        for (int i = 0; i < roomNames.size(); i++) {
            roomImages.add(new ArrayList<>());

            audioSensors.add(new AudioSensor(null));
            lightSensors.add(new LightSensor(context, null));
            compassSensors.add(new CompassSensor(context, null));

            cameraPreviews.add(new Preview.Builder().build());
            imageCaptures.add(new ImageCapture.Builder().build());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.contains("UPDATE_ROOM_NAME")) {
            holder.roomName.setText(roomNames.get(position));
        } else if (!payloads.isEmpty() && payloads.get(0).equals("UPDATE_PHOTO_COUNT")) {

            int updatedPhotoCount = roomImages.get(position).size();
            Log.d("RoomAdapter", "Updated photo count: " + updatedPhotoCount);
            holder.photoCount.setText(updatedPhotoCount + " added");
        } else {

            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Others room
        String currentRoomName = roomNames.get(position);
        if ("Others".equals(currentRoomName)) {
            holder.roomName.setText("Others");
            // camera function
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

            holder.photoCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPhotosDialog(holder.getAdapterPosition());
                }
            });

            // other components invisible
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
            return;
        }

        // Bind room name
        if (position == 0) {
            holder.roomName.setText("Lounge Room");
        } else {
            holder.roomName.setText("Room " + position);
        }

        AudioSensor currentAudioSensor = audioSensors.get(position);
        LightSensor currentLightSensor = lightSensors.get(position);
        CompassSensor currentCompassSensor = compassSensors.get(position);

        // Create a callback for this room
        SensorCallback roomCallback = new SensorCallback() {
            @Override
            public void onSensorDataChanged(String sensorType, float value) {
                switch (sensorType) {
                    case "Light":
                        updateLightData(holder, value);
                        Log.d("SensorCallback", "onSensorDataChanged called for " + sensorType);
                        break;
                    case "Compass":
                        updateCompassData(holder, value);
                        Log.d("SensorCallback", "onSensorDataChanged called for " + sensorType);
                        break;
                }
            }

            @Override
            public void onCurrentDbCalculated(double currentDb) {
                ((Activity) context).runOnUiThread(() -> {

                    holder.noiseValueTextView.setText(String.format("%.2f dB", currentDb));
                });
            }

            @Override
            public void onAverageDbCalculated(double averageDb) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseValueTextView.setText(String.format("%.2f dB", averageDb));
                });
            }
        };

        currentAudioSensor.setCallback(roomCallback);
        currentLightSensor.setCallback(roomCallback);
        currentCompassSensor.setCallback(roomCallback);

        // Use the callback with the sensors
        holder.noiseTestButton.setOnClickListener(v -> {
            currentAudioSensor.startTest();
            Log.d("AiSensor", "startTest() called");
        });

        holder.lightTestButton.setOnClickListener(v -> {
            currentLightSensor.startTest();
        });

        holder.compassTestButton.setOnClickListener(v -> {
            currentCompassSensor.startTest();
        });

        if (!initializedRooms.contains(position)) {
            //Initialize sensors
            audioSensor = new AudioSensor(roomCallback);
            lightSensor = new LightSensor(context, roomCallback);
            compassSensor = new CompassSensor(context, roomCallback);

            initializedRooms.add(position);
        }

        // camera function
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

        holder.photoCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotosDialog(holder.getAdapterPosition());
            }
        });

        holder.editRoomNameIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameRoomDialog(position, holder);
            }
        });
    }

    //Use cameraX library
    private void startCamera(PreviewView previewView, Button captureButton, Dialog cameraDialog, ViewHolder holder) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        Executor executor = ContextCompat.getMainExecutor(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                ImageCapture imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                captureButton.setOnClickListener(v -> {
                    File photoFile = new File(context.getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
                    imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            String photoPath = photoFile.getAbsolutePath();
                            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

                            try {
                                bitmap = rotateImageIfRequired(bitmap, photoPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            roomImages.get(holder.getAdapterPosition()).add(bitmap);
                            notifyDataSetChanged();
                            //close camera page
                            cameraDialog.dismiss();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e("RoomAdapter", "Image capture failed", exception);
                            Toast.makeText(context, "Error capturing the image.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (ExecutionException | InterruptedException e) {
                Log.e("RoomAdapter", "Use case binding failed", e);
            }
        }, executor);
    }

    // handle rotated photos
    private Bitmap rotateImageIfRequired(Bitmap img, String path) throws IOException {
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

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

    // keep original rotation
    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    // test save function
    private void showSaveImageDialog(Bitmap image, int roomPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Save Image");
        builder.setMessage("Do you want to save this image to your gallery?");
        builder.setPositiveButton("Save Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the single image to gallery
                saveImageToGallery(image, roomPosition);
                //检查保存路径
                logRoomImagePaths();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveImageToGallery(Bitmap image, int roomPosition) {
        String imageUri = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                image,
                "Room Image",
                "Image of a room"
        );
        String imagePath = getPathFromUri(Uri.parse(imageUri));
        Toast.makeText(context, "Image saved to gallery at: " + imagePath, Toast.LENGTH_SHORT).show();

        while (roomPosition >= roomImagePaths.size()) {

            roomImagePaths.add(new ArrayList<>());
        }

        roomImagePaths.get(roomPosition).add(imagePath);
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(columnIndex);
        cursor.close();
        return imagePath;
    }

    // test photo path
    private void logRoomImagePaths() {
        for (int i = 0; i < roomImagePaths.size(); i++) {
            List<String> imagePathList = roomImagePaths.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("Room ").append(i).append(": ");
            for (String path : imagePathList) {
                sb.append(path).append(", ");
            }
            Log.d("RoomImagePaths", sb.toString());
        }
    }

    // take photo or upload from library
    private void showCameraOptionsDialog(ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_camera_options, null);

        Button btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        Button btnAddFromLibrary = view.findViewById(R.id.btn_add_from_library);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View cameraView = inflater.inflate(R.layout.camera_preview_layout, null);

                    PreviewView previewView = cameraView.findViewById(R.id.previewView);
                    Button captureButton = cameraView.findViewById(R.id.captureButton);

                    Dialog cameraDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    cameraDialog.setContentView(cameraView);
                    cameraDialog.show();

                    startCamera(previewView, captureButton, cameraDialog, holder);
                }

                bottomSheetDialog.dismiss();
            }
        });

        btnAddFromLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity) context).startActivityForResult(intent, holder.getAdapterPosition());

                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

   // test camera function
    public static List<List<Bitmap>> getRoomImages() {
        return roomImages;
    }

    // photo display
    private void showPhotosDialog(int roomPosition) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_photo_gallery);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);

        // show photo with window size
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        ImageAdapter imageAdapter = new ImageAdapter(roomImages.get(roomPosition), recyclerView, roomPosition) {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
                super.onBindViewHolder(holder, position);
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // delete notification
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete Photo");
                        builder.setMessage("Are you sure to delete the photo?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                roomImages.get(roomPosition).remove(position);
                                // change made
                                notifyDataSetChanged();
                                updatePhotoCountForRoom(roomPosition);
                            }
                        });
                        // No action
                        builder.setNegativeButton("No", null);
                        builder.show();
                    }
                });
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(imageAdapter);

        Button backButtonGallery = dialog.findViewById(R.id.back_button_gallery);
        backButtonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updatePhotoCountForRoom(int roomPosition) {
        notifyItemChanged(roomPosition, "UPDATE_PHOTO_COUNT");
    }

    private void updateLightData(ViewHolder holder, float lightValue) {
        ((Activity) context).runOnUiThread(() -> {
            holder.lightValueTextView.setText(String.valueOf(lightValue));
        });
    }

    private void updateCompassData(ViewHolder holder, float combinedValue) {
        int degree = (int) combinedValue;
        float directionDecimal = combinedValue - degree;
        String direction = getDirectionFromDecimal(directionDecimal);
        ((Activity) context).runOnUiThread(() -> {
            holder.compassValueTextView.setText(String.format(Locale.US, "%d° %s", degree, direction));
        });
    }

    private String getDirectionFromDecimal(float directionDecimal) {
        // Convert the decimal part to an integer
        int directionCode = (int)(directionDecimal * 100);
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
            default: return "";
        }
    }

    @Override
    public int getItemCount() {
        return roomNames.size();
    }

    public void addImageToRoom(int roomPosition, Bitmap image) {
        roomImages.get(roomPosition).add(image);
        notifyItemChanged(roomPosition, "UPDATE_PHOTO_COUNT");
    }

    private void showRenameRoomDialog(int position, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_rename_room, null);

        EditText roomNameEditText = view.findViewById(R.id.roomNameEditText);
        roomNameEditText.setText(roomNames.get(holder.getAdapterPosition()));

        builder.setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            String newName = roomNameEditText.getText().toString();
                            roomNames.set(currentPosition, newName);
                            notifyItemChanged(currentPosition, "UPDATE_ROOM_NAME");
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView editRoomNameIcon;
        TextView roomName;
        ImageView cameraIcon, noiseIcon, lightIcon, compassIcon;
        TextView imageView, noiseView, lightView, compassView;
        TextView photoCount, noiseValueTextView, lightValueTextView, compassValueTextView;
        Button openCameraButton, noiseTestButton, lightTestButton, compassTestButton;
        PreviewView previewView;

        public ViewHolder(View itemView) {
            super(itemView);

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

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        private final List<Bitmap> images;
        private final RecyclerView recyclerView;
        private final int roomPosition;

        public ImageAdapter(List<Bitmap> images, RecyclerView recyclerView, int roomPosition) {
            this.images = images;
            this.recyclerView = recyclerView;
            this.roomPosition = roomPosition;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_data_collection_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Bitmap bitmap = images.get(position);

            holder.imageView.setImageBitmap(bitmap);

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

            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showSaveImageDialog(images.get(position), roomPosition);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public ImageButton deleteButton;

            public ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.image);
                deleteButton = view.findViewById(R.id.delete_button);
            }
        }
    }
}
