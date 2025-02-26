package com.apk.editor.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import com.apk.editor.utils.SerializableItems.PackageItems;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AppData {

    public static List<PackageItems> getData(Context context) {
        return getData(null, context);
    }

    public static List<PackageItems> getData(String searchWord, Context context) {
        List<PackageItems> mData = new CopyOnWriteArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        boolean mAppType;
        for (ApplicationInfo packageInfo: packages) {
            PackageItems packageItem = new PackageItems(packageInfo.packageName, context);
            if (sCommonUtils.getString("appTypes", "all", context).equals("system")) {
                mAppType = sPackageUtils.isSystemApp(packageItem.getPackageName(), context);
            } else if (sCommonUtils.getString("appTypes", "all", context).equals("user")) {
                mAppType = !sPackageUtils.isSystemApp(packageItem.getPackageName(), context);
            } else {
                mAppType = true;
            }
            if (mAppType) {
                if (searchWord == null) {
                    mData.add(packageItem);
                } else if (Common.isTextMatched(packageItem.getAppName(), searchWord)
                        || Common.isTextMatched(packageItem.getPackageName(), searchWord)) {
                    mData.add(packageItem);
                }
            }
        }
        if (sCommonUtils.getInt("sort_apps", 1, context) == 0) {
            Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getInt("sort_apps", 1, context) == 4) {
            Collections.sort(mData, Comparator.comparingLong(PackageItems::getAPKSize));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getInt("sort_apps", 1, context) == 2) {
            Collections.sort(mData, Comparator.comparingLong(PackageItems::getInstalledTime));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sCommonUtils.getInt("sort_apps", 1, context) == 3) {
            Collections.sort(mData, Comparator.comparingLong(PackageItems::getUpdatedTime));
        } else {
            Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));
        }
        if (!sCommonUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (Exception ignored) {
        }
        return null;
    }

    /*
     * Based on the work of https://github.com/ZenerDeveloper
     * Ref: https://github.com/SmartPack/PackageManager/commit/1ac499d0ed8922c02875df029ead80a17f1c40e1
     */
    public static void toggleKeyboard(int mode, MaterialAutoCompleteTextView textView, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mode == 1) {
            if (textView.requestFocus()) {
                imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        }
    }

}
