package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.apk.editor.R;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class SelectBundleDialog {

    public SelectBundleDialog(String path, ActivityResultLauncher<Intent> activityResultLauncher, boolean exit, Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.split_apk_installer)
                .setMessage(activity.getString(R.string.install_bundle_question))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (exit) {
                        activity.finish();
                    }
                })
                .setPositiveButton(R.string.yes, (dialogInterface, i) ->
                        SplitAPKInstaller.handleAppBundle(activityResultLauncher, path, activity)
                ).show();
    }

}