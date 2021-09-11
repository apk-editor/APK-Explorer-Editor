package com.apk.editor.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.apk.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class Projects {

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : Objects.requireNonNull(new File(context.getCacheDir().toString()).listFiles())) {
            if (mFile.exists() && mFile.isDirectory() && new File(mFile, "AndroidManifest.xml").exists()) {
                if (Common.getSearchWord() == null) {
                    mData.add(mFile.getAbsolutePath());
                } else if (Common.isTextMatched(mFile.getName(), Common.getSearchWord())) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        }
        Collections.sort(mData);
        if (!APKEditorUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static String getExportPath(Context context) {
        if (APKEditorUtils.getString("exportPath", null, context) != null) {
            return APKEditorUtils.getString("exportPath", null, context);
        } else {
            return Environment.getExternalStorageDirectory().toString() + "/AEE";
        }
    }

    public static void exportProject(File path, String name, Context context) {
        new AsyncTasks() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting, path.getName()));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (APKEditorUtils.exist(getExportPath(context) + "/" + name)) {
                    APKEditorUtils.delete(getExportPath(context) + "/" + name);
                }
            }

            @Override
            public void doInBackground() {
                APKEditorUtils.copyDir(path, new File(getExportPath(context), name));
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    public static void deleteProject(File path, Context context) {
        new AsyncTasks() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.deleting, path.getName()));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            public void doInBackground() {
                APKEditorUtils.delete(path.getAbsolutePath());
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

}