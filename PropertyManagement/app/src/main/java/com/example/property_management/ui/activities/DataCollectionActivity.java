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
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import com.example.property_management.R;
import com.google.android.material.snackbar.Snackbar;

import android.app.AlertDialog;


public class DataCollectionActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    // initial room data and property id
    private HashMap<String, RoomData> initialInspectedData;
    private String propertyId;
    private boolean hasRecordAudioPermission = false;
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
    private ArrayList<String> roomNames = new ArrayList<>();
    private Dialog noteDialog;
    private SharedPreferences sharedPreferences;
    private Map<Integer, List<String>> roomImagePathsMap = new LinkedHashMap<>();
    private int room_num;
    private String notes;
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }else {
            // 如果权限已经被授予，初始化 RecyclerView
            hasRecordAudioPermission = true;
            initRecyclerView();
        }

        requestStoragePermission();


        // retrieve user's collected data from previous intent
        // if no inspected data, initialInspectedDat will be empty HashMap {}
        Intent intent = getIntent();
        propertyId = intent.getStringExtra("propertyId");
        room_num = intent.getIntExtra("roomNum", 0);
        //room_num = 5;
        notes = intent.getStringExtra("notes") != null ? intent.getStringExtra("notes") : "";
        initialInspectedData = (HashMap<String, RoomData>) intent.getSerializableExtra("inspectedData");


        // Define the list of room names
        roomNames = (ArrayList<String>) intent.getSerializableExtra("roomNames");
        if (roomNames == null) {
            roomNames = new ArrayList<>(); // 初始化一个新的 ArrayList
        }
        //处理property第一次收集数据时，firebase相关字段为空的问题。此时根据房间数量设置房间名字，再设置初始值。
        if (initialInspectedData.size() == 0){
            for (int i = 0; i <= room_num; i++) {
                if (i == 0) {
                    roomNames.add("Lounge Room");
                } else if (i == room_num) {
                    roomNames.add("Others");
                } else {
                    roomNames.add("Room " + i);
                }
            }
            for (String roomname: roomNames){
                initialInspectedData.put(roomname, new RoomData(0,0,"--",new ArrayList<String>()));
            }
        }
        Log.i("get-initial-inspectedData", initialInspectedData.toString());
        Log.i("get-propertyId", propertyId);
        Log.i("get-initial-roomNames", roomNames.toString());

        //查看得到的房间数据具体值
        for (String roomname: initialInspectedData.keySet()){
            Log.i("get-" + roomname + " data", initialInspectedData.get(roomname).toString());
        }

        //查看得到的房间数量数据
        Log.i("get-room num", String.valueOf(room_num));



        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE);
        Log.d("loading note in datacollection",notes);
        saveNote(notes);

        Button buttonNote = findViewById(R.id.buttonNote);

        buttonNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog();
            }
        });

        binding.finishButton.setOnClickListener(view -> {
            //更新成功前禁用按钮
            binding.finishButton.setEnabled(false);
            binding.finishButton.setText("Updating...");

            // 创建一个映射来保存所有房间的数据
            LinkedHashMap<String, Object> roomDataMap = new LinkedHashMap<>();

            // 正则表达式用于找到数字（包括小数点）
            Pattern pattern = Pattern.compile("[+-]?([0-9]*[.])?[0-9]+");

            for (int i = 0; i < roomsRecyclerView.getChildCount(); i++) {
                View itemView = roomsRecyclerView.getChildAt(i);
                RoomAdapter.ViewHolder viewHolder = (RoomAdapter.ViewHolder) roomsRecyclerView.getChildViewHolder(itemView);

                String roomName = viewHolder.roomName.getText().toString();

                // 以下方法将尝试从字符串中提取数字
                String photoCount = viewHolder.photoCount.getText().toString();
                String noiseValue = extractNumber(viewHolder.noiseValueTextView.getText().toString(), pattern);
                String lightValue = extractNumber(viewHolder.lightValueTextView.getText().toString(), pattern);
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
            collectRoomPhotos();
            LinkedHashMap<String, RoomData> roomData = new LinkedHashMap<>(); //将获取的房间数据转化为roomData类

            int count = 0;
            for (String roomName:roomDataMap.keySet()){
                HashMap<String,String> singleRoomData = (HashMap<String,String>)roomDataMap.get(roomName);
                ArrayList<String> imgs = new ArrayList<>();
                imgs = (ArrayList<String>) roomImagePathsMap.get(count) != null ?
                        (ArrayList<String>) roomImagePathsMap.get(count) :
                        new ArrayList<String>();
                count++;

                Log.d("brightness",String.valueOf(singleRoomData.get("brightness")) );
                Log.d("noise",String.valueOf(singleRoomData.get("noise")) );
                Log.d("windowOrientation",String.valueOf(singleRoomData.get("windowOrientation")) );

                RoomData singleRoom = new RoomData(Float.valueOf(singleRoomData.get("brightness")), Float.valueOf(singleRoomData.get("noise")),singleRoomData.get("windowOrientation"), imgs);

                roomData.put(roomName, singleRoom);
            }

            //将recycler view 里的房间名有序保存
            ArrayList<String> roomName = new ArrayList<>();
            for (String roomName1: roomDataMap.keySet()){
                roomName.add(roomName1);
            }

            updateInspectedData(propertyId, roomData, roomName);

            Toast.makeText(this, "Upload data successfully! ", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 这里调用 finish() 方法
                    finish();
                }
            }, 3500); // 2000 是延迟的时间（毫秒），即 2 秒

            Log.d("Saved images",roomImagePathsMap.toString());
        });

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean hasShownInfo = prefs.getBoolean("has_shown_info", false);

        if (!hasShownInfo) {
            showInfoDialog();
        }
    }


    public void collectRoomPhotos() {
        // get image from adapter
        List<List<Bitmap>> allRoomImages = roomAdapter.getAllRoomImages();

        Log.d("allRoomImages key",allRoomImages.toString());
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

            //Toast.makeText(this, "Image saved as: " + realPath, Toast.LENGTH_SHORT).show();

            if (!roomImagePathsMap.containsKey(roomPosition)) {
                roomImagePathsMap.put(roomPosition, new ArrayList<>());
            }
            roomImagePathsMap.get(roomPosition).add(realPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logRoomImagePaths() {
        Log.d("RoomImagePaths", roomImagePathsMap.toString());
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

    /**
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_info:
                showInfoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info_data_collection, menu);
        return true;
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tutorial");

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        // format all text
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

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(noteDialog.getWindow().getAttributes());
        int dialogWidth = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
        layoutParams.width = dialogWidth;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        noteDialog.getWindow().setAttributes(layoutParams);

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

    private void updateInspectedData(String propertyId, HashMap<String, RoomData> inspectedData, ArrayList<String> roomNames) {
        // update ispected status to firebase
        HashMap<String, Object> payload = new HashMap<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        payload.put("properties." + propertyId + ".inspectedData", inspectedData);
        payload.put("properties." + propertyId + ".notes", sharedPreferences.getString("note", ""));
        payload.put("properties." + propertyId + ".roomNames", roomNames);
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        userRepository.updateUserFields(userId, payload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                Log.i("update-inspectedData-successfully", msg);
                // Re-enable the button and reset its text after successful update
                runOnUiThread(() -> {
                    //binding.finishButton.setEnabled(true);
                    binding.finishButton.setText("Finish");
                });
            }
            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
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


    //从得到的字符inspected结果中提取数字。
    private String extractNumber(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);  // 返回找到的第一个数字
        } else {
            return "0";  // 如果没有找到数字，返回0
        }
    }

    private void initRecyclerView() {
        // Initialize rooms RecyclerView
        roomsRecyclerView = findViewById(R.id.recycler_view);
        // Setup the adapter for rooms
        roomAdapter = new RoomAdapter(this, roomNames, initialInspectedData);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(roomAdapter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，初始化 RecyclerView
                hasRecordAudioPermission = true;
                initRecyclerView();
            } else {
                // 权限被拒绝，向用户解释为什么需要这个权限
                Toast.makeText(this, "The app needs audio recording permission to function properly.", Toast.LENGTH_LONG).show();
                // 处理权限被拒绝的情况
            }
        }
    }



}
