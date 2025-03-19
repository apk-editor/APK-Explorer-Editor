package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.apk.editor.R;
import com.apk.editor.utils.tasks.ExportProject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class Projects {

    public static List<String> getData(String searchWord, Context context) {
        List<String> mData = new CopyOnWriteArrayList<>();
        for (File mFile : Objects.requireNonNull(context.getCacheDir().listFiles())) {
            if (mFile.exists() && mFile.isDirectory() && new File(mFile, ".aeeBackup/appData").exists()) {
                if (searchWord == null) {
                    mData.add(mFile.getAbsolutePath());
                } else if (Common.isTextMatched(mFile.getName(), searchWord)) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        }
        Collections.sort(mData);
        if (!sCommonUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static String getExportPath(Context context) {
        if (Build.VERSION.SDK_INT < 29 && sCommonUtils.getString("exportPath", null, context) != null) {
            return sCommonUtils.getString("exportPath", null, context);
        } else {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        }
    }

    @SuppressLint("StringFormatInvalid")
    public static void exportProject(File file, Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(75, 75, 75, 75);
        final MaterialAutoCompleteTextView editText = new MaterialAutoCompleteTextView(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setSingleLine(true);
        editText.requestFocus();
        layout.addView(editText);

        new MaterialAlertDialogBuilder(context)
                .setView(layout)
                .setTitle(context.getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    String text = editText.toString().trim();
                    if (text.isEmpty()) {
                        sCommonUtils.toast(context.getString(R.string.name_empty), context).show();
                        return;
                    }
                    if (text.contains(" ")) {
                        text = text.replace(" ", "_");
                    }
                    String name = text;
                    if (sFileUtils.exist(new File(Projects.getExportPath(context), text))) {
                        new MaterialAlertDialogBuilder(context)
                                .setMessage(context.getString(R.string.export_project_replace, text))
                                .setNegativeButton(R.string.cancel, (dialog2, ii) -> {
                                })
                                .setPositiveButton(R.string.replace, (dialog2, iii) -> new ExportProject(file, name, context).execute())
                                .show();
                    } else {
                        new ExportProject(file, name, context).execute();
                    }
                }).show();
    }

}