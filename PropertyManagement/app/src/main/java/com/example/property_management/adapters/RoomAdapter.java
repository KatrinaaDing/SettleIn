package com.example.property_management.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private final List<String> roomNames;
    private Context context;
    private Set<Integer> initializedRooms = new HashSet<>();
    private LightSensor lightSensor;
    private CompassSensor compassSensor;
    private AudioSensor audioSensor;
    private List<List<Bitmap>> roomImages = new ArrayList<>();
    private List<LightSensor> lightSensors = new ArrayList<>();
    private List<CompassSensor> compassSensors = new ArrayList<>();
    private List<AudioSensor> audioSensors = new ArrayList<>();

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0).equals("UPDATE_PHOTO_COUNT")) {
            // update the photo count only
            int updatedPhotoCount = roomImages.get(position).size();
            Log.d("RoomAdapter", "Updated photo count: " + updatedPhotoCount);
            holder.photoCount.setText(updatedPhotoCount + " added");
        } else {
            // rebind
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind room name
        holder.roomName.setText("Room " + (position + 1));

        AudioSensor currentAudioSensor = audioSensors.get(position);
        LightSensor currentLightSensor = lightSensors.get(position);
        CompassSensor currentCompassSensor = compassSensors.get(position);

        // create a callback for this room
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
            // initialize the sensors
            audioSensor = new AudioSensor(roomCallback);
            lightSensor = new LightSensor(context, roomCallback);
            compassSensor = new CompassSensor(context, roomCallback);
            initializedRooms.add(position);
        }

        /**
         * camera sensor for each room
         */
        holder.openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ((Activity) context).startActivityForResult(open_camera, currentPosition);
                }
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
    }

    private void showPhotosDialog(int roomPosition) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_photo_gallery);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        ImageAdapter imageAdapter = new ImageAdapter(roomImages.get(roomPosition), recyclerView) {
            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
                super.onBindViewHolder(holder, position);
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roomImages.get(roomPosition).remove(position);
                        //update photo count
                        updatePhotoCountForRoom(roomPosition);
                        dialog.dismiss();
                    }
                });
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(imageAdapter);
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

    // Calculate direction of compass
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        }
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        private final List<Bitmap> images;
        private final RecyclerView recyclerView;

        public ImageAdapter(List<Bitmap> images, RecyclerView recyclerView) {
            this.images = images;
            this.recyclerView = recyclerView;
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
            float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
            int width = recyclerView.getWidth();
            int height = Math.round(width / ratio);

            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.width = bitmap.getWidth();
            params.height = bitmap.getHeight();
            holder.imageView.setLayoutParams(params);

            holder.imageView.setImageBitmap(bitmap);

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    images.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, images.size());
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
