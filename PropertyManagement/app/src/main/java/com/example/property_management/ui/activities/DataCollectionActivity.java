package com.example.property_management.ui.activities;
import com.example.property_management.callbacks.SensorCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.callbacks.SensorCallback;
import com.example.property_management.databinding.ActivityDataCollectionBinding;
import com.example.property_management.sensors.SensorManagerClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataCollectionActivity extends AppCompatActivity implements SensorCallback{
    private ActivityDataCollectionBinding binding;
    private SensorManagerClass sensorManagerClass;
    private Uri photoUri;

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private TextView photoCountTextView;
    private final List<Bitmap> images = new ArrayList<>();

    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    images.add(photo);
                    imageAdapter.notifyDataSetChanged();
                    updatePhotoCount();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Collect data mode");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        sensorManagerClass = new SensorManagerClass((Context) this, (SensorCallback) this);

        recyclerView = findViewById(R.id.recyclerView);
        imageAdapter = new ImageAdapter(images, recyclerView);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(imageAdapter);
        photoCountTextView = findViewById(R.id.photoCount);

        binding.openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mGetContent.launch(open_camera);
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();

        sensorManagerClass.startAllSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManagerClass.stopAllSensors();
    }

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

    public void updateLightData(float lightValue) {

        binding.lightData.setText(String.valueOf(lightValue));
    }
    public void updateCompassData(float combinedValue) {

        TextView compassTextView = findViewById(R.id.compassData);
        int degree = (int) combinedValue;
        float directionDecimal = combinedValue - degree;
        String direction = getDirectionFromDecimal(directionDecimal);
        compassTextView.setText(String.format(Locale.US, "%d° %s", degree, direction));
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

}
