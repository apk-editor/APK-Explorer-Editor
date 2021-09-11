package com.apk.editor.utils.recyclerViewItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on September 05, 2021
 */
public class PackageItems implements Serializable {

    private final Drawable mAppIcon;
    private final long mAPKSize, mInstalledTime, mUpdatedTime;
    private final String mAppName, mPackageName, mVersion;

    public PackageItems(String name, String packageName, String version, long apkSize, long installedTime,
                        long updatedTime, Drawable icon) {
        this.mAppName = name;
        this.mPackageName = packageName;
        this.mVersion = version;
        this.mAPKSize = apkSize;
        this.mInstalledTime = installedTime;
        this.mUpdatedTime = updatedTime;
        this.mAppIcon = icon;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public long getAPKSize() {
        return mAPKSize;
    }

    public long getInstalledTime() {
        return mInstalledTime;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    public String getAppName() {
        return mAppName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppVersion() {
        return mVersion;
    }

}