package com.apk.editor.utils.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class InvalidFileDialog {

    private final MaterialAlertDialogBuilder mDialogBuilder;

    @SuppressLint("StringFormatInvalid")
    public InvalidFileDialog(boolean exit, Activity activity) {
        mDialogBuilder = new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.split_apk_installer)
                .setMessage(activity.getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                .setCancelable(false)
                .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                    if (exit) {
                        activity.finish();
                    }
                });
    }

    public void show() {
        mDialogBuilder.show();
    }

}