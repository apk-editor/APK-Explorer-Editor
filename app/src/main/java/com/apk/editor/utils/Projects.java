package com.apk.editor.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.apk.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class Projects {

    private static List<String> mData = new ArrayList<>();
    public static String mSearchText;

    public static List<String> getData(Context context) {
        mData.clear();
        for (File mFile : Objects.requireNonNull(new File(context.getCacheDir().toString()).listFiles())) {
            if (mFile.exists() && mFile.isDirectory() && !mFile.getName().equals("WebView")) {
                if (mSearchText == null) {
                    mData.add(mFile.getAbsolutePath());
                } else if (mFile.getName().toLowerCase().contains(mSearchText.toLowerCase())) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        }
        return mData;
    }

    public static String getExportPath() {
        return Environment.getExternalStorageDirectory().toString() + "/AEE";
    }

    public static void exportProject(File path, String name, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting, path.getName()));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (APKEditorUtils.exist(getExportPath() + "/" + name)) {
                    APKEditorUtils.delete(getExportPath() + "/" + name);
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                APKEditorUtils.copyDir(path, new File(getExportPath(), name));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

}