package com.example.property_management.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    public static Uri currentPhotoUri;

    public RoomAdapter(Context context, List<String> roomNames) {
        this.context = context;
        this.roomNames = roomNames;
        for (int i = 0; i < roomNames.size(); i++) {
            roomImages.add(new ArrayList<>());

            audioSensors.add(new AudioSensor(null));
            lightSensors.add(new LightSensor(context, null));
            compassSensors.add(new CompassSensor(context, null));
        }
    }

    //新的camera功能，无法打开相机
    /**
     // 创建一个图像文件
     private File createImageFile() throws IOException {
     String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
     String imageFileName = "JPEG_" + timeStamp + "_";
     File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
     File image = File.createTempFile(imageFileName, ".jpg", storageDir);
     return image;
     }

     // 启动相机
     private void launchCamera(ViewHolder holder) {
     Log.d("CameraDebug", "launchCamera called");

     PackageManager packageManager = context.getPackageManager();

     Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     if (open_camera.resolveActivity(context.getPackageManager()) != null) {
     Log.d("CameraDebug", "Camera package found");
     File photoFile;
     try {
     photoFile = createImageFile();
     currentPhotoUri = FileProvider.getUriForFile(context,
     context.getApplicationContext().getPackageName() + ".provider", photoFile);
     open_camera.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
     ((Activity) context).startActivityForResult(open_camera, position);
     } catch (IOException e) {
     e.printStackTrace();
     Log.d("CameraDebug", "IOException: " + e.getMessage());
     }
     } else {
     Log.d("CameraDebug", "Camera package not found");
     List<ResolveInfo> listCam = packageManager.queryIntentActivities(open_camera, 0);
     if (listCam.size() > 0) {
     Intent chooserIntent = Intent.createChooser(open_camera, "Capture Image with...");
     ((Activity) context).startActivityForResult(chooserIntent, holder.getAdapterPosition());
     } else {
     // 没有可用的相机应用
     Toast.makeText(context, "No camera apps found!", Toast.LENGTH_SHORT).show();
     }
     }
     }
     */

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
            // 只更新照片数量的显示
            int updatedPhotoCount = roomImages.get(position).size();
            Log.d("RoomAdapter", "Updated photo count: " + updatedPhotoCount);
            holder.photoCount.setText(updatedPhotoCount + " added");
        } else {
            // 全部重新绑定
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Other room 的代码，可以使用，但为了测试方便

        String currentRoomName = roomNames.get(position);
        if ("Others".equals(currentRoomName)) {
            holder.roomName.setText("Others");
            // 显示摄像头按钮
            holder.openCameraButton.setVisibility(View.VISIBLE);
            holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCameraOptionsDialog(holder);
                }
            });

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {  // 检查位置是否有效
                holder.photoCount.setText(roomImages.get(currentPosition).size() + " added");

            }

            holder.photoCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPhotosDialog(holder.getAdapterPosition());
                }
            });

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
            // 初始化传感器
            audioSensor = new AudioSensor(roomCallback);
            lightSensor = new LightSensor(context, roomCallback);
            compassSensor = new CompassSensor(context, roomCallback);

            // 存储这个房间的传感器已经被初始化
            initializedRooms.add(position);
        }


        // camera function

        holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraOptionsDialog(holder);
            }
        });




        //之前的camera，缩略图，但可以正常运行
        /**
         holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        int currentPosition = holder.getAdapterPosition();
        if (currentPosition != RecyclerView.NO_POSITION) {  // 检查位置是否有效
        Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ((Activity) context).startActivityForResult(open_camera, currentPosition); // 使用currentPosition作为requestCode
        }
        }
        });
         */

        /**
         * 新的camera功能，无法打开相机
         holder.openCameraButton.setTag(position);  // 将位置存储为标签
         holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        Log.d("CameraDebug", "openCameraButton clicked");
        int clickedPosition = (int) v.getTag();  // 从标签中检索位置
        launchCamera(holder, clickedPosition);
        }
        });
         */


        int currentPosition = holder.getAdapterPosition();
        if (currentPosition != RecyclerView.NO_POSITION) {  // 检查位置是否有效
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
                    Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ((Activity) context).startActivityForResult(open_camera, currentPosition);
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


    public static List<List<Bitmap>> getRoomImages() {
        return roomImages;
    }

    private void showPhotosDialog(int roomPosition) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_photo_gallery);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);

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
                        // 显示确认删除的对话框
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete Photo");
                        builder.setMessage("Are you sure to delete the photo?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                roomImages.get(roomPosition).remove(position);
                                notifyDataSetChanged();  // 通知数据发生变化
                                updatePhotoCountForRoom(roomPosition);
                            }
                        });
                        builder.setNegativeButton("No", null); // 不执行任何操作
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
        }
    }
    /**
     private void updatePhotoCount() {
     String text = images.size() + " added";
     photoCountTextView.setText(text);
     }

     public void addImageToRoom(int roomPosition, Bitmap image) {
     roomImages.get(roomPosition).add(image);
     notifyItemChanged(roomPosition);
     }
     */



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
                    // 显示确认删除的对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Delete Photo");
                    builder.setMessage("Do you want to delete it?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 删除照片
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
                    return true;  // Indicate that the long click was handled
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
