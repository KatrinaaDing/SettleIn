package com.example.property_management.ui.fragments.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.property_management.callbacks.BasicDialogCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BasicDialog extends AppCompatDialogFragment {
    private String title;
    private String content;
    private String LeftBtnText;
    private String RightBtnText;
    private boolean canCloseOutside;
    private BasicDialogCallback callback;

    /**
     * Constructor for a dialog with 2 buttons
     * @param canCloseOutside whether the dialog can be dismissed when clicking outside of it
     * @param title the title of the dialog
     * @param content the content of the dialog
     * @param LeftBtnText the text of the left button
     * @param RightBtnText the text of the right button
     */
    public BasicDialog (boolean canCloseOutside, String title, String content, String LeftBtnText, String RightBtnText) {
        this.canCloseOutside = canCloseOutside;
        this.title = title;
        this.content = content;
        this.LeftBtnText = LeftBtnText;
        this.RightBtnText = RightBtnText;
    }

    /**
     * Constructor for a dialog with 1 button
     * @param canCloseOutside whether the dialog can be dismissed when clicking outside of it
     * @param title the title of the dialog
     * @param content the content of the dialog
     * @param LeftBtnText the text of the left button
     */
    public BasicDialog (boolean canCloseOutside, String title, String content, String LeftBtnText) {
        this.canCloseOutside = canCloseOutside;
        this.title = title;
        this.content = content;
        this.LeftBtnText = LeftBtnText;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(this.title)
            .setMessage(this.content)
            // set left button
            .setNegativeButton(this.LeftBtnText, (dialogInterface, i) -> {
                if (callback != null) {
                    callback.onLeftBtnClick();
                }
            });
        // set right button
        if (this.RightBtnText != null) {
            builder.setPositiveButton(this.RightBtnText, (dialogInterface, i) -> {
                if (callback != null) {
                    callback.onRightBtnClick();
                }
            });
        }
        AlertDialog dialog = builder.create();
        // prevent dialog from being dismissed when clicking outside of it
        if (!this.canCloseOutside) {
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    public void setCallback(BasicDialogCallback callback) {
        this.callback = callback;
    }

}
