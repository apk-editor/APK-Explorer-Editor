package com.apk.editor.utils.SerializableItems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.apk.editor.R;

import java.io.File;
import java.io.Serializable;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Feb. 18, 2025
 */
public class APKItems implements Serializable {

    private final File mAPKFile, mBaseAPK;

    public APKItems(File apkFile, File baseAPK) {
        this.mAPKFile = apkFile;
        this.mBaseAPK = baseAPK;
    }

    public boolean isDirectory() {
        return mAPKFile.isDirectory();
    }

    public CharSequence getAppName(Context context) {
        return sAPKUtils.getAPKName(isDirectory() ? getBaseAPKPath() : getPath(), context);
    }

    public Drawable getImageDrawable(Context context) {
        return sAPKUtils.getAPKIcon(isDirectory() ? getBaseAPKPath() : getPath(), context);
    }

    public File getAPKFile() {
        return mAPKFile;
    }

    public File getBaseAPK() {
        return mBaseAPK;
    }

    public String getBaseAPKPath() {
        return mBaseAPK.getAbsolutePath();
    }

    public String getName() {
        return mAPKFile.getName();
    }

    public String getPackageName(Context context) {
        return sAPKUtils.getPackageName(isDirectory() ? getBaseAPKPath() : getPath(), context);
    }

    public String getPath() {
        return mAPKFile.getAbsolutePath();
    }

    @SuppressLint("StringFormatInvalid")
    public String getSize(Context context) {
        return context.getString(R.string.size, sAPKUtils.getAPKSize(getAPKFile().length()));
    }

    public String getVersionName(Context context) {
        return context.getString(R.string.version, sAPKUtils.getVersionName(isDirectory() ? getBaseAPKPath() : getPath(), context));
    }

}