package com.apk.editor.utils;

import android.content.Context;
import android.os.Environment;

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

}