package com.apk.editor.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

import com.apk.editor.utils.recyclerViewItems.PackageItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AppData {

    public static List<PackageItems> getRawData(Context context) {
        List<PackageItems> mData = new ArrayList<>();
        List<ApplicationInfo> packages = getPackageManager(context).getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo: packages) {
            mData.add(new PackageItems(
                    getAppName(packageInfo.packageName, context).toString(),
                    packageInfo.packageName,
                    getVersionName(getSourceDir(packageInfo.packageName, context), context),
                    new File(getSourceDir(packageInfo.packageName, context)).length(),
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).firstInstallTime,
                    Objects.requireNonNull(getPackageInfo(packageInfo.packageName, context)).lastUpdateTime,
                    getAppIcon(packageInfo.packageName, context)
            ));
        }
        return mData;
    }

    public static List<PackageItems> getData(Context context) {
        List<PackageItems> mData = new ArrayList<>();
        try {
            boolean mAppType;
            for (PackageItems packageItem : Common.getPackageData()) {
                if (APKEditorUtils.getString("appTypes", "all", context).equals("system")) {
                    mAppType = isSystemApp(packageItem.getPackageName(), context);
                } else if (APKEditorUtils.getString("appTypes", "all", context).equals("user")) {
                    mAppType = !isSystemApp(packageItem.getPackageName(), context);
                } else {
                    mAppType = true;
                }
                if (mAppType) {
                    if (Common.getSearchWord() == null) {
                        mData.add(packageItem);
                    } else if (Common.isTextMatched(packageItem.getAppName(), Common.getSearchWord())
                            || Common.isTextMatched(packageItem.getPackageName(), Common.getSearchWord())) {
                        mData.add(packageItem);
                    }
                }
            }
            if (APKEditorUtils.getBoolean("sort_name", false, context)) {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && APKEditorUtils.getBoolean("sort_size", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getAPKSize));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && APKEditorUtils.getBoolean("sort_installed", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getInstalledTime));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && APKEditorUtils.getBoolean("sort_updated", false, context)) {
                Collections.sort(mData, Comparator.comparingLong(PackageItems::getUpdatedTime));
            } else {
                Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));
            }
            if (!APKEditorUtils.getBoolean("az_order", true, context)) {
                Collections.reverse(mData);
            }
        } catch (NullPointerException ignored) {}
        return mData;
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return getPackageManager(context).getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    /*
     * Based on the work of https://github.com/ZenerDeveloper
     * Ref: https://github.com/SmartPack/PackageManager/commit/1ac499d0ed8922c02875df029ead80a17f1c40e1
     */
    public static void toggleKeyboard(int mode, AppCompatEditText textView, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mode == 1) {
            if (textView.requestFocus()) {
                imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        }
    }

    public static PackageManager getPackageManager(Context context) {
        return context.getApplicationContext().getPackageManager();
    }

    public static ApplicationInfo getAppInfo(String packageName, Context context) {
        try {
            return getPackageManager(context).getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static CharSequence getAppName(String packageName, Context context) {
        return getPackageManager(context).getApplicationLabel(Objects.requireNonNull(getAppInfo(
                packageName, context)));
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        return getPackageManager(context).getApplicationIcon(Objects.requireNonNull(getAppInfo(packageName, context)));
    }

    public static String getSourceDir(String packageName, Context context) {
        return Objects.requireNonNull(getAppInfo(packageName, context)).sourceDir;
    }

    public static boolean isAppInstalled(String packageName, Context context) {
        try {
            getPackageManager(context).getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isSystemApp(String packageName, Context context) {
        try {
            return (Objects.requireNonNull(getAppInfo(packageName, context)).flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NullPointerException ignored) {}
        return false;
    }

    public static String getAPKSize(long apkSize) {
        long size = apkSize / 1024;
        long decimal = (size - 1024) / 1024;
        if (size > 1024) {
            return size / 1024 + "." + decimal + " MB";
        } else {
            return size  + " KB";
        }
    }

    public static String getVersionName(String path, Context context) {
        return getPackageManager(context).getPackageArchiveInfo(path, 0).versionName;
    }

}