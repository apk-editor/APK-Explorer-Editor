package com.apk.editor.utils.recyclerViewItems;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.utils.AppData;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on September 05, 2021
 */
public class PackageItems implements Serializable {
    private final String mPackageName;
    private final Context mContext;
    private Drawable mAppIcon;

    public PackageItems(String packageName, Context context) {
        this.mPackageName = packageName;
        this.mContext = context;
    }
    public String getPackageName() {
        return mPackageName;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public sExecutor loadAppIcon(AppCompatImageButton view) {
        return new sExecutor() {

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                mAppIcon = sPackageUtils.getAppIcon(mPackageName, mContext);
            }

            @Override
            public void onPostExecute() {
                view.setImageDrawable(mAppIcon);
            }
        };
    }
    public long getInstalledTime() {
        return Objects.requireNonNull(AppData.getPackageInfo(mPackageName, mContext)).firstInstallTime;
    }
    public long getUpdatedTime() {
        return Objects.requireNonNull(AppData.getPackageInfo(mPackageName, mContext)).lastUpdateTime;
    }
    public String getAppName() {
        return sPackageUtils.getAppName(mPackageName, mContext).toString();
    }
    public long getAPKSize() {
        return new File(sPackageUtils.getSourceDir(mPackageName, mContext)).length();
    }
    public String getAppVersion() {
        return sAPKUtils.getVersionName(sPackageUtils.getSourceDir(mPackageName, mContext), mContext);
    }

}