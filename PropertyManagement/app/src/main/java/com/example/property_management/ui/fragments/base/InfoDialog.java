package com.example.property_management.ui.fragments.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.property_management.callbacks.BasicDialogCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class InfoDialog extends AppCompatDialogFragment {
    private String title;
    private String content;

    /**
     * Constructor for a dialog with 2 buttons
     * @param title the title of the dialog
     * @param content the content of the dialog
     */
    public InfoDialog (String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        AlertDialog dialog = builder.setTitle(this.title)
                .setMessage(this.content)
                .create();

        return dialog;
    }

}
