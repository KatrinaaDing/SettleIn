package com.example.property_management.ui.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.property_management.R;
import com.example.property_management.callbacks.BasicDialogCallback;
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
        addBasicDialog();
        addBasicDialogCannotCloseFromOutSide();

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
}
