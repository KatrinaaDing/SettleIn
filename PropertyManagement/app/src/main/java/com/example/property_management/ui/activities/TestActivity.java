package com.example.property_management.ui.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.databinding.ActivityTestBinding;
import com.example.property_management.ui.fragments.base.BasicDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TestActivity  extends AppCompatActivity {
    private ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Test");

        // =================================== Components ======================================
        Button openDialogBtn = binding.openDialogBtn;

        // =================================== Listeners =======================================
        openDialogBtn.setOnClickListener(view -> {
            // open dialog
//            BasicDialog dialog = new BasicDialog("Test dialog", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras molestie urna in nisl viverra condimentum.", "Cancel", "Save");
//            dialog.show();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.show();
        });

    }
}
