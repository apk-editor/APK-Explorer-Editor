package com.apk.editor.utils.SerializableItems;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.apk.editor.R;

import java.io.File;
import java.io.Serializable;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 22, 2025
 */
public class APKPickerItems implements Serializable {

    private boolean selected;
    private final File apkFile;

    public APKPickerItems(File apkFile, boolean selected) {
        this.apkFile = apkFile;
        this.selected = selected;
    }

    public boolean isSelected() {
        return apkFile.exists() && apkFile.isFile() && apkFile.getName().endsWith(".apk") && selected;
    }

    public Drawable getImageDrawable(Context context) {
        if (sAPKUtils.getAPKIcon(apkFile.getAbsolutePath(), context) != null) {
            return sAPKUtils.getAPKIcon(apkFile.getAbsolutePath(), context);
        } else {
            return sCommonUtils.getDrawable(R.drawable.ic_android_app, context);
        }
    }

    public String getAPKName() {
        return apkFile.getName();
    }

    public String getAPKSize() {
        return sAPKUtils.getAPKSize(apkFile.length());
    }

    public String getPackageName(Context context) {
        return sAPKUtils.getPackageName(apkFile.getAbsolutePath(), context);
    }

    public String getAPKPath() {
        return apkFile.getAbsolutePath();
    }

    public void isSelected(boolean b) {
        selected = b;
    }

}