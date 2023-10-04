package com.example.property_management.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.RoomAdapter;
import com.example.property_management.databinding.ActivityDataCollectionBinding;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;

import java.util.ArrayList;
import java.util.List;

public class DataCollectionActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private @NonNull ActivityDataCollectionBinding binding;


    //Sensor variable
    private LightSensor lightSensor;
    private CompassSensor compassSensor;
    private AudioSensor audioSensor;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    // NEW: Camera and Image variables
    private RecyclerView recyclerView;
    //private ImageAdapter imageAdapter;
    private final List<Bitmap> images = new ArrayList<>();
    private TextView photoCountTextView;

    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private List<String> roomNames = new ArrayList<>();

    private Dialog noteDialog;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            roomAdapter.addImageToRoom(requestCode, photo);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Collect data mode");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recycle room
        //int roomCount = 3;

        // Initialize rooms RecyclerView
        roomsRecyclerView = findViewById(R.id.recycler_view);

        // Define the list of room names
        List<String> roomNames = new ArrayList<>();
        for (int i = 1; i <= 4; i++) { // assuming  3 rooms
            roomNames.add("Room " + i);
        }

        // Setup the adapter for rooms
        roomAdapter = new RoomAdapter(this, roomNames);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(roomAdapter);





        //=============================== Sensor work and click event--------------------------------
        //Audio Sensor section
        //get microphone permission for audio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        }
        /**
        audioSensor = new AudioSensor(this);

        binding.noiseTest1.setOnClickListener(v -> {
            audioSensor.startTest();
                }
        );

        //Light Sensor section
        lightSensor = new LightSensor(this, this);
        binding.lightTest1.setOnClickListener(v -> {
            lightSensor.startTest();
        });

        //Compass Sensor section
        compassSensor = new CompassSensor(this, this);
        binding.windowTest1.setOnClickListener(v -> {
            compassSensor.startTest();
        });
*/

        /**
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
         */


/**
        recyclerView = findViewById(R.id.recyclerView);
        imageAdapter = new ImageAdapter(images, recyclerView);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //recyclerView.setAdapter(imageAdapter);
        photoCountTextView = findViewById(R.id.photoCount);

        binding.openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mGetContent.launch(open_camera);
                } catch (Exception e) {
                    Log.e("Camera Error", "Error opening camera: " + e.getMessage());
                }
            }
        });
  */



        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE);

        Button buttonNote = findViewById(R.id.buttonNote);

        buttonNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog();
            }
        });


        //==========================Click Finish Button to return property detail page ====================================
        binding.finishButton.setOnClickListener(view -> {
            //Need to define the logic of return tested data
            finish();
        });

    }


















    //==============================Helpers to be transport===============================

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
    @Override
    protected void onResume() {
        super.onResume();
        // Start all sensors
        sensorManagerClass.startAllSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop all sensors
        sensorManagerClass.stopAllSensors();
    }
    **/
/**
    @Override
    public void onSensorDataChanged(String sensorType, float value) {

        switch (sensorType) {
            case "Light":
                updateLightData(value);
                break;
            case "Compass":
                updateCompassData(value);
                break;
        }

    }
*/
    /**

    @Override
    public void onCurrentDbCalculated(double currentDb) {
        runOnUiThread(() -> binding.noiseValue1.setText(String.format("%.2f dB", currentDb)));
    }

    @Override
    public void onAverageDbCalculated(double averageDb) {
        runOnUiThread(() -> binding.noiseValue1.setText(String.format("%.2f dB", averageDb)));
    }

    public void updateLightData(float lightValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the TextView with the light sensor value
                binding.lightValue1.setText(String.valueOf(lightValue));
            }
        });
    }
    public void updateCompassData(float combinedValue) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int degree = (int) combinedValue;
                float directionDecimal = combinedValue - degree;
                String direction = getDirectionFromDecimal(directionDecimal);
                binding.windowValue1.setText(String.format(Locale.US, "%d° %s", degree, direction));
            }
        });
    }
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

    private void updatePhotoCount() {
        String text = images.size() + " added";
        photoCountTextView.setText(text);
    }

    /**
    public void showPhotos(View view) {
        Log.d("DEBUG", "Number of images: " + images.size());
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_photo_gallery);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        ImageAdapter imageAdapter = new ImageAdapter(images, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(imageAdapter);

        dialog.show();
    }
     */

    //note
    private void showNoteDialog() {
        noteDialog = new Dialog(this);
        noteDialog.setContentView(R.layout.dialog_note);

        final EditText editTextNote = noteDialog.findViewById(R.id.editTextNote);
        Button buttonSave = noteDialog.findViewById(R.id.buttonSave);

        // Load existing note, if any
        String existingNote = sharedPreferences.getString("note", "");
        editTextNote.setText(existingNote);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note;
                note = editTextNote.getText().toString();
                saveNote(note);
                noteDialog.dismiss();  // Dismiss the dialog
            }
        });

        noteDialog.show();
    }

    private void saveNote(String note) {
        // Save the note in SharedPreferences
        sharedPreferences.edit().putString("note", note).apply();
    }

    /**
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

            // 获取 RecyclerView 的宽度
            int width = recyclerView.getWidth();

            // 根据图片的宽高比来计算 ImageView 的高度
            int height = Math.round(width / ratio);

            // 动态设置 ImageView 的高度
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.width = bitmap.getWidth();
            params.height = bitmap.getHeight();
            holder.imageView.setLayoutParams(params);
            // 设置图片
            holder.imageView.setImageBitmap(bitmap);

            // Set click listener for the delete button
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    images.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, images.size());
                    updatePhotoCount();  // 更新照片数量显示
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
        */

}
