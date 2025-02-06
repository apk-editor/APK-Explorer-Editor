package com.apk.editor.utils.dialogs;

import android.app.Activity;

import com.apk.editor.R;
import com.apk.editor.utils.tasks.ClearAppSettings;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class ClearAppSettingsDialog {

    public ClearAppSettingsDialog(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.warning)
                .setMessage(activity.getString(R.string.clear_cache_message))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.delete, (dialog, id) ->
                        new ClearAppSettings(activity).execute()
                ).show();
    }

}