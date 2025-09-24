package com.apk.editor.utils;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.Locale;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 22, 2025
 */
public class APKPicker {

    public static boolean isSelectedAPK(File file, Context context) {
        return file.getName().endsWith(".apk") && file.getName().contains(Build.SUPPORTED_ABIS[0].replace("-","_"))
                || file.getName().contains(Locale.getDefault().getLanguage()) || file.getName().contains("base.apk")
                || file.getName().contains(getScreenDensity(context));
    }

    private static String getScreenDensity(Context context) {
        int screenDPI = context.getResources().getDisplayMetrics().densityDpi;
        if (screenDPI <= 140) {
            return "ldpi";
        } else if (screenDPI <= 200) {
            return "mdpi";
        } else if (screenDPI <= 280) {
            return "hdpi";
        } else if (screenDPI <= 400) {
            return "xhdpi";
        } else if (screenDPI <= 560) {
            return "xxhdpi";
        } else {
            return "xxxhdpi";
        }
    }

}