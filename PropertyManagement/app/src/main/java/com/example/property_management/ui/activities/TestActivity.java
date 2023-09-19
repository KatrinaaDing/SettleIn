package com.example.property_management.ui.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.R;
import com.example.property_management.adapters.PropertyCardAdapter;
import com.example.property_management.callbacks.BasicDialogCallback;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.ActivityTestBinding;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class TestActivity  extends AppCompatActivity {
    private ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Test");

        // =================================== Components ======================================
        addBasicDialog();
        addBasicDialogCannotCloseFromOutSide();
        addPropertiesRecycleView();
    }

    private void addBasicDialog() {
        Button openDialogBtn = binding.openDialogBtn;

        openDialogBtn.setOnClickListener(view -> {
            BasicDialog dialog = new BasicDialog(true,
                    "Test1 dialog",
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras molestie urna in nisl viverra condimentum.",
                    "Save");
            dialog.setCallback(new BasicDialogCallback() {
                @Override
                public void onLeftBtnClick() {
                    dialog.dismiss();
                }
                @Override
                public void onRightBtnClick() {
                    dialog.dismiss();
                }
            });
            dialog.show(getSupportFragmentManager(), "Test dialog");
        });
    }

    private void addBasicDialogCannotCloseFromOutSide() {
        Button openDialogBtn2 = binding.openDialogBtn2;
        openDialogBtn2.setOnClickListener(view -> {
            BasicDialog dialog = new BasicDialog(false,
                    "Test2 dialog",
                    "This dialog cannot be closed from outside and has 2 buttons",
                    "Cancel",
                    "Save");
            dialog.setCallback(new BasicDialogCallback() {
                @Override
                public void onLeftBtnClick() {
                    dialog.dismiss();
                }
                @Override
                public void onRightBtnClick() {
                    dialog.dismiss();
                }
            });
            dialog.show(getSupportFragmentManager(), "Test dialog");
        });
    }

    private void addPropertiesRecycleView() {
        ArrayList<Property> properties = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();
        images.add("https://cdn.vox-cdn.com/thumbor/0eoiN9XqqsSVbiCNo_h0hbUP_yI=/0x0:1023x682/1200x800/filters:focal(431x260:593x422)/cdn.vox-cdn.com/uploads/chorus_image/image/64006695/0b0cd00c_891f_49a5_a75c_cdd640a23020.f10.0.jpg");
        images.add("https://media-cdn.tripadvisor.com/media/vr-splice-j/06/f5/78/5b.jpg");
        images.add("https://th.bing.com/th/id/OIP.iE7mcw3w2aFFDhXP9A1lggHaE8?pid=ImgDet&rs=1");
        properties.add(new Property("0000000000000",
                "https://www.example1.com",
                "11 Charming Street Hampton East VIC 3188",
                (float) 144.022, (float) 132.102,
                4, 2, 1,
                null, images));
        properties.add(new Property("1111111111111",
                "https://www.example2.com",
                "12 Charming Street Hampton East VIC 3188",
                (float) 143.022, (float) 132.102,
                2, 1, 0,
                null, images));
        properties.add(new Property("2222222222222",
                "https://www.example3.com",
                "13 Charming Street Hampton East VIC 3188",
                (float) 145.022, (float) 132.102,
                3, 2, 2,
                null, null));
        properties.add(new Property("0000000000000",
                "https://www.example1.com",
                "11 Charming Street Hampton East VIC 3188",
                (float) 144.022, (float) 132.102,
                4, 2, 1,
                null, images));
        properties.add(new Property("1111111111111",
                "https://www.example2.com",
                "12 Charming Street Hampton East VIC 3188",
                (float) 143.022, (float) 132.102,
                2, 1, 0,
                null, images));
        properties.add(new Property("2222222222222",
                "https://www.example3.com",
                "13 Charming Street Hampton East VIC 3188",
                (float) 145.022, (float) 132.102,
                3, 2, 2,
                null, null));

        RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
        propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties);
        propertiesRecyclerView.setAdapter(propertyCardAdapter);
    }
}
