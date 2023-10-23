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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.Nullable;

public class DataCollectionActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private @NonNull ActivityDataCollectionBinding binding;
    private LightSensor lightSensor;
    private CompassSensor compassSensor;
    private AudioSensor audioSensor;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private RecyclerView recyclerView;
    private final List<Bitmap> images = new ArrayList<>();
    private TextView photoCountTextView;
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private List<String> roomNames = new ArrayList<>();
    private Dialog noteDialog;
    private SharedPreferences sharedPreferences;
    private Map<Integer, List<String>> roomImagePathsMap = new LinkedHashMap<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // select from library
            if (data != null && data.getData() != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    roomAdapter.addImageToRoom(requestCode, bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (data != null && data.getExtras() != null) {
                // take photo
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                roomAdapter.addImageToRoom(requestCode, photo);
            }
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Collect data mode");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        requestStoragePermission();
        //recycle room
        //int roomCount = 3;







        // Initialize rooms RecyclerView
        roomsRecyclerView = findViewById(R.id.recycler_view);

        // Define the list of room names
        List<String> roomNames = new ArrayList<>();
        // assuming  3 rooms
        for (int i = 0; i <= 3; i++) {
            if (i == 0) {
                roomNames.add("Lounge Room");
            } else if (i == 3) {
                roomNames.add("Others");
            } else {
                roomNames.add("Room " + i);
            }
        }

        // Setup the adapter for rooms
        roomAdapter = new RoomAdapter(this, roomNames);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(roomAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE);

        Button buttonNote = findViewById(R.id.buttonNote);

        buttonNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog();
            }
        });

        binding.finishButton.setOnClickListener(view -> {
            //Need to define the logic of return tested data
            //onFinishButtonClicked();

            // 创建一个映射来保存所有房间的数据
            HashMap<String, Object> roomDataMap = new HashMap<>();

            for (int i = 0; i < roomsRecyclerView.getChildCount(); i++) {
                View itemView = roomsRecyclerView.getChildAt(i);
                RoomAdapter.ViewHolder viewHolder = (RoomAdapter.ViewHolder) roomsRecyclerView.getChildViewHolder(itemView);

                String roomName = viewHolder.roomName.getText().toString();
                String photoCount = viewHolder.photoCount.getText().toString();
                String noiseValue = viewHolder.noiseValueTextView.getText().toString();
                String lightValue = viewHolder.lightValueTextView.getText().toString();
                String compassValue = viewHolder.compassValueTextView.getText().toString();

                HashMap<String, String> roomInfo = new HashMap<>();
                roomInfo.put("images", photoCount);
                roomInfo.put("noise", noiseValue);
                roomInfo.put("brightness", lightValue);
                roomInfo.put("windowOrientation", compassValue);

                roomDataMap.put(roomName, roomInfo);
            }


            // 转换为字符串并记录
            Log.d("AllRoomData", "Rooms Data: " + roomDataMap.toString());

            HashMap<String, RoomData> roomData = new HashMap<>(); //将获取的房间数据转化为roomData类

            for (String roomName:roomDataMap.keySet()){
                HashMap<String,String> singleRoomData = (HashMap<String,String>)roomDataMap.get(roomName);
                ArrayList<String> imgs = new ArrayList<>();
                imgs.add(singleRoomData.get("images"));

                RoomData singleRoom = new RoomData(Float.valueOf(singleRoomData.get("brightness")), Float.valueOf(singleRoomData.get("noise")),singleRoomData.get("windowOrientation"), imgs);

                roomData.put(roomName, singleRoom);
            }


            updateInspectedData(roomData);
            //collectRoomPhotos();

            finish();
        });
    }

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

         for (int i = 0; i < allRoomImagePaths.size(); i++) {
             List<String> imagePathList = allRoomImagePaths.get(i);
             StringBuilder sb = new StringBuilder();
             sb.append("Room ").append(i).append(": ");
             for (String path : imagePathList) {
                 sb.append(path).append(", ");
             }
             Log.d("RoomImagePaths", sb.toString());
         }
         logRoomImagePaths();


    }

    private void saveImageToGallery(Bitmap image, int roomPosition) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Room_" + roomPosition);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            String realPath = getRealPathFromURI(uri);

            Toast.makeText(this, "Image saved as: " + realPath, Toast.LENGTH_SHORT).show();

            if (!roomImagePathsMap.containsKey(roomPosition)) {
                roomImagePathsMap.put(roomPosition, new ArrayList<>());
            }
            roomImagePathsMap.get(roomPosition).add(realPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logRoomImagePaths() {
        StringBuilder logOutput = new StringBuilder("{\n");
        for (Map.Entry<Integer, List<String>> entry : roomImagePathsMap.entrySet()) {
            int roomPosition = entry.getKey();
            List<String> imagePathList = entry.getValue();
            logOutput.append("Room ").append(roomPosition).append(": ");
            for (String path : imagePathList) {
                logOutput.append(path).append(", ");
            }
            logOutput.append("\n");
        }
        logOutput.append("}");
        Log.d("RoomImagePaths", logOutput.toString());
    }

    private String getRealPathFromURI(Uri uri) {
        String path = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            path = cursor.getString(idx);
            cursor.close();
        }
        return path;
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

    private String getDirectionFromDecimal(float directionDecimal) {
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
            default: return "";
        }
    }

    private void updatePhotoCount() {
        String text = images.size() + " added";
        photoCountTextView.setText(text);
    }

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
                noteDialog.dismiss();
            }
        });

        noteDialog.show();
    }

    private void saveNote(String note) {
        // Save the note in SharedPreferences
        sharedPreferences.edit().putString("note", note).apply();
    }

    private void updateInspectedData(HashMap<String, RoomData> inspectedData) {
        // update ispected status to firebase
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("properties." + "AegAQm1de6rcwfhMtJog" + ".inspectedData: ", inspectedData);
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        userRepository.updateUserFields("cvOi8Z768aOvqWZxuCt0nifpsMy1", payload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                Log.i("update-inspectedData-successfully", msg);
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
                new BasicSnackbar(findViewById(android.R.id.content), errorMsg, "error");
                Log.e("update-inspected-failure", msg);
            }
        });

    }




}
