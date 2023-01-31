package com.apk.editor.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;

import com.apk.editor.R;
import com.apk.editor.interfaces.EditTextInterface;
import com.apk.editor.utils.tasks.ExportProject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class Projects {

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : Objects.requireNonNull(new File(context.getCacheDir().toString()).listFiles())) {
            if (mFile.exists() && mFile.isDirectory() && new File(mFile, ".aeeBackup/appData").exists()) {
                if (Common.getSearchWord() == null) {
                    mData.add(mFile.getAbsolutePath());
                } else if (Common.isTextMatched(mFile.getName(), Common.getSearchWord())) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        }
        Collections.sort(mData);
        if (!sUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static String getExportPath(Context context) {
        if (Build.VERSION.SDK_INT < 29 && sUtils.getString("exportPath", null, context) != null) {
            return sUtils.getString("exportPath", null, context);
        } else {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        }
    }

    public static void exportProject(File file, Context context) {
        new EditTextInterface(null, context.getString(R.string.app_name), context) {

            @Override
            public void positiveButtonLister(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    sUtils.toast(context.getString(R.string.name_empty), context).show();
                    return;
                }
                if (text.contains(" ")) {
                    text = text.replace(" ", "_");
                }
                String name = text;
                if (sUtils.exist(new File(Projects.getExportPath(context), text))) {
                    new MaterialAlertDialogBuilder(context)
                            .setMessage(context.getString(R.string.export_project_replace, text))
                            .setNegativeButton(R.string.cancel, (dialog2, ii) -> {
                            })
                            .setPositiveButton(R.string.replace, (dialog2, iii) -> new ExportProject(file, name, context).execute())
                            .show();
                } else {
                    new ExportProject(file, name, context).execute();
                }
            }
        }.show();
    }

}