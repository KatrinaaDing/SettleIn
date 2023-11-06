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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

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
    private int roomCount;
    private Context context;
    private Set<Integer> initializedRooms = new HashSet<>();
    public TextView photoCount;
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

    public RoomAdapter(Context context, List<String> roomNames, HashMap<String, RoomData> roomData) {
        this.context = context;
        this.roomNames = roomNames;

        for (String names:roomNames){
            this.roomNamesOrigin.add(names);
        }

        for (String names:roomNames){
            inspectedRoomData.put(names,roomData.get(names));
            Log.d("XX roomNames name set roomAdapter", names);
            Log.d("XX inspectedRoomData name set roomAdapter", inspectedRoomData.keySet().toString());
        }

        for (int i = 0; i < roomNames.size(); i++) {
            roomImages.add(new ArrayList<>());

            audioSensors.add(new AudioSensor(null));
            lightSensors.add(new LightSensor(context, null));
            compassSensors.add(new CompassSensor(context, null));

            cameraPreviews.add(new Preview.Builder().build());
            imageCaptures.add(new ImageCapture.Builder().build());
        }
        this.roomData = roomData;
        Log.d("new RoomAdapter created","new RoomAdapter created");
        for (String key: roomData.keySet()){
            Log.d("new RoomAdapter data for " + key,roomData.toString());
        }

        int i = 0;
        for (String roomName: roomNames){
            for (String imgPath: roomData.get(roomName).getImages()){
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                roomImages.get(i).add(bitmap);
            }
            i++;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_layout, parent, false);
        return new ViewHolder(view);
    }





    /**
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
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Log.d("payloads",payloads.toString());
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if ("UPDATE_PHOTO_COUNT".equals(payload)) {
                    // Update only photo count related views
                    int updatedPhotoCount = roomImages.get(position).size();
                    Log.d("RoomAdapter", "Updated photo count: " + updatedPhotoCount);
                    holder.photoCount.setText(updatedPhotoCount + " added");
                } else if ("UPDATE_ROOM_NAME".equals(payload)) {
                    // Update only room name related views
                    holder.roomName.setText(roomNames.get(position));
                } else if ("DISABLE_BUTTON".equals(payload)) {
                    holder.noiseTestButton.setEnabled(false);
                    holder.lightTestButton.setEnabled(false);
                    holder.compassTestButton.setEnabled(false);
                }
                // Handle other specific updates with else-if statements
            }
        } else {
            // If no payloads are available, call the full bind method
            Log.d("full bind called","full bind called");
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
                holder.lightValueTextView.setText("11");
                holder.noiseValueTextView.setText("11");
                holder.compassValueTextView.setText("11");
                return;
            }else{
                holder.roomName.setText(currentRoomName);
            }


            // Bind room name
            //if (position == 0) {
            //    holder.roomName.setText("Lounge Room");
            //} else {
            //    holder.roomName.setText("Room " + position);
            //}

            //测试
                Log.d("bindView Called, currentRoomName: ",currentRoomName);
                Log.d("bindView Called, current position roomName: ",roomNames.get(position));
                Log.d("bindView Called, room set  ",roomData.keySet().toString());
                if (roomData.get(roomNamesOrigin.get(position)).getBrightness() == -1) {
                    holder.lightValueTextView.setText("--");
                } else {
                    holder.lightValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getBrightness() + " Lux");
                }

                if (roomData.get(roomNamesOrigin.get(position)).getNoise() == -1) {
                    holder.noiseValueTextView.setText("--");
                } else {
                    holder.noiseValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getNoise() + " dB");
                }
                holder.compassValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getWindowOrientation());


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
                    Log.d("onCurrentDbCalculated", "onCurrentDbCalculated called");
                });
            }

            @Override
            public void onAverageDbCalculated(double averageDb) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseValueTextView.setText(String.format("%.2f dB", averageDb));
                    inspectedRoomData.get(roomNamesOrigin.get(position)).setNoise((float)averageDb);

                });
            }

            @Override
            public void onAudioTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.noiseTestButton.setText("Test");
                    holder.noiseTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色
                    holder.isNoiseTesting = false;
                });
            }

            @Override
            public void onLightTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.lightTestButton.setText("Test");
                    holder.lightTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色
                    holder.isLightTesting = false;
                    String value = holder.lightValueTextView.getText().toString();
                    float numValue = Float.valueOf(extractNumber(value));
                    inspectedRoomData.get(roomNamesOrigin.get(position)).setBrightness(numValue);
                });
            }

            @Override
            public void onCompassTestCompleted() {
                ((Activity) context).runOnUiThread(() -> {
                    holder.compassTestButton.setText("Test");
                    holder.compassTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色
                    holder.isCompassTesting = false;
                    String value = holder.compassValueTextView.getText().toString();
                    inspectedRoomData.get(roomNamesOrigin.get(position)).setWindowOrientation(value);
                });
            }
        };

        currentAudioSensor.setCallback(roomCallback);
        currentLightSensor.setCallback(roomCallback);
        currentCompassSensor.setCallback(roomCallback);

        // Use the callback with the sensors
        holder.noiseTestButton.setOnClickListener(v -> {

            if (!holder.isNoiseTesting) {
                // 开始测试

                notifyItemChanged(position,"DISABLE_BUTTON");
                holder.isNoiseTesting = true;
                holder.noiseTestButton.setText("Cancel");
                holder.noiseTestButton.setBackgroundColor(Color.RED); // 设置为您选择的红色色值

                // 启动测试线程
                holder.testAudioThread = new Thread(() -> {
                    currentAudioSensor.startTest();
                    // 你可以在这里添加其他逻辑
                });
                holder.testAudioThread.start();

            } else {
                // 取消测试
                holder.isNoiseTesting = false;
                holder.noiseTestButton.setText("Test");
                holder.noiseTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色

                // 停止测试线程
                if (holder.testAudioThread != null) {
                    currentAudioSensor.stopTest(); // 这将设置isRecording为false，并停止音频记录
                    holder.testAudioThread.interrupt(); // 这将中断线程
                    holder.testAudioThread = null;

                    // 清除TextView
                    if (roomData.get(roomNamesOrigin.get(position)).getNoise() == -1) {
                        holder.noiseValueTextView.setText("--");
                    } else {
                        holder.noiseValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getNoise() + " dB");
                    }
                }
            }

        });

        holder.lightTestButton.setOnClickListener(v -> {
            if (!holder.isLightTesting) {
                // 开始测试
                holder.isLightTesting = true;
                holder.lightTestButton.setText("Cancel");
                holder.lightTestButton.setBackgroundColor(Color.RED); // 设置为您选择的红色色值

                // 启动测试线程
                holder.testLightThread = new Thread(() -> {
                    currentLightSensor.startTest();
                    // 你可以在这里添加其他逻辑
                });
                holder.testLightThread.start();

            } else {
                // 取消测试
                holder.isLightTesting = false;
                holder.lightTestButton.setText("Test");
                holder.lightTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色

                // 停止测试线程
                if (holder.testLightThread != null) {
                    currentLightSensor.stopTest(); // 这将设置isRecording为false，并停止音频记录
                    holder.testLightThread.interrupt(); // 这将中断线程
                    holder.testLightThread = null;

                    // 清除TextView
                    if (roomData.get(roomNamesOrigin.get(position)).getBrightness() == -1) {
                        holder.lightValueTextView.setText("--");
                    } else {
                        holder.lightValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getBrightness() + " Lux");
                    }
                }
            }

        });

        holder.compassTestButton.setOnClickListener(v -> {


            if (!holder.isCompassTesting) {
                // 开始测试
                holder.isCompassTesting = true;
                holder.compassTestButton.setText("Cancel");
                holder.compassTestButton.setBackgroundColor(Color.RED); // 设置为您选择的红色色值

                // 启动测试线程
                holder.testCompassThread = new Thread(() -> {
                    currentCompassSensor.startTest();
                    // 你可以在这里添加其他逻辑
                });
                holder.testCompassThread.start();

            } else {
                // 取消测试
                holder.isCompassTesting = false;
                holder.compassTestButton.setText("Test");
                holder.compassTestButton.setBackgroundColor(Color.parseColor("#FF6200EE")); // 使用 16 进制字符串设置颜色

                // 停止测试线程
                if (holder.testCompassThread != null) {
                    currentCompassSensor.stopTest(); // 这将设置isRecording为false，并停止音频记录
                    holder.testCompassThread.interrupt(); // 这将中断线程
                    holder.testCompassThread = null;

                    // 清除TextView
                    holder.compassValueTextView.setText(roomData.get(roomNamesOrigin.get(position)).getWindowOrientation());
                }
            }


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
                            //roomImages.get(holder.getAdapterPosition()).add(bitmap);
                            //notifyDataSetChanged();
                            int position = holder.getAdapterPosition();
                            roomImages.get(position).add(bitmap);
                            notifyItemChanged(position, "UPDATE_PHOTO_COUNT");

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
   public List<List<Bitmap>> getAllRoomImages() {
       return roomImages;
   }

    public ArrayList<ArrayList<String>> getAllRoomImagePaths() {
        return roomImagePaths;
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
            holder.lightValueTextView.setText(lightValue + " Lux");
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
        isFirstBind = false;
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
                            alterRoomName(inspectedRoomData,roomNames.get(currentPosition),newName);
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

    private String extractNumber(String input) {
        Pattern pattern = Pattern.compile("[+-]?([0-9]*[.])?[0-9]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);  // 返回找到的第一个数字
        } else {
            return "-1";  // 如果没有找到数字，返回0
        }
    }





    public LinkedHashMap<String, RoomData> getInspectedRoomData () {return inspectedRoomData;};

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //test
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

            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.cannot_load_photo)
                        .override(400, 400) // 以像素为单位指定尺寸
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

            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showSaveImageDialog(images.get(position), roomPosition);
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
            public MaterialButton deleteButton;

            public ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.image);
                deleteButton = view.findViewById(R.id.delete_button);
            }
        }





    }
}
