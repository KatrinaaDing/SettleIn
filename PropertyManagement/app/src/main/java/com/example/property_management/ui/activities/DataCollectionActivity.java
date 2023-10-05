package com.example.property_management.ui.activities;
import com.example.property_management.adapters.RoomAdapter;
import com.example.property_management.callbacks.SensorCallback;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.databinding.ActivityDataCollectionBinding;
import com.example.property_management.sensors.AudioSensor;
import com.example.property_management.sensors.CompassSensor;
import com.example.property_management.sensors.LightSensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        roomsRecyclerView = findViewById(R.id.recycler_view);

        List<String> roomNames = new ArrayList<>();
        // assuming  2 rooms
        for (int i = 1; i <= 2; i++) {
            roomNames.add("Room " + i);
        }

        //Setup the adapter for rooms
        roomAdapter = new RoomAdapter(this, roomNames);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(roomAdapter);

        /**
         *  Sensor work and click event
         *  Audio Sensor section
         *  get microphone permission for audio
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        /**
         * note function
         */
        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE);
        Button buttonNote = findViewById(R.id.buttonNote);

        buttonNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog();
            }
        });

        /**
         * Click Finish Button to return property detail page
         */
        binding.finishButton.setOnClickListener(view -> {
            //Need to define the logic of return tested data
            finish();
        });
    }

    /**
     * Helpers to be transport
     * @param item
     * @return
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
     * collect note
     * Show note
     */
    private void showNoteDialog() {
        noteDialog = new Dialog(this);
        noteDialog.setContentView(R.layout.dialog_note);

        final EditText editTextNote = noteDialog.findViewById(R.id.editTextNote);
        Button buttonSave = noteDialog.findViewById(R.id.buttonSave);

        // load existing note
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
}
