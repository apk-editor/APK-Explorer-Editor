package com.apk.editor.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on May. 03, 2026
 */
public class BundleFile extends File {

    public BundleFile(File file) {
        super(file.getAbsolutePath());
    }

    public File getBaseAPK(Context context) {
        File baseAPK = new File(getAbsoluteFile(), "base.apk");
        if (baseAPK.exists()) {
            return baseAPK;
        }

        File[] files = listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".apk")) {
                continue;
            }

            if (sAPKUtils.getPackageName(file.getAbsolutePath(), context) != null) {
                return file;
            }
        }

        return null;
    }

    public String getBaseAPKPath(Context context) {
        return getBaseAPK(context).getAbsolutePath();
    }

    @NonNull
    public String getBaseAPKName(Context context) {
        return getBaseAPK(context).getName();
    }

}