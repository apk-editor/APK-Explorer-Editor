package com.apk.editor.utils.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.tasks.SaveBundletoDownloads;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class ShareBundleDialog {

    @SuppressLint("StringFormatInvalid")
    public ShareBundleDialog(String path, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.share_message, new File(path).getName()))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.share), (dialog, id) ->
                        new SaveBundletoDownloads(path, false, context).execute()
                ).show();
    }

}