package com.apk.editor.utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apk.editor.R;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on May. 03, 2026
 */
public class APKFile extends File {

    public APKFile(File apk) {
        super(apk.getAbsolutePath());
    }

    public boolean isDirectory() {
        return getAbsoluteFile().isDirectory();
    }

    @SuppressLint("StringFormatInvalid")
    private String getSize(Context context) {
        long length = 0;
        if (isDirectory()) {
            for (File file : Objects.requireNonNull(listFiles())) {
                if (file.isFile() && sAPKUtils.getPackageName(file.getAbsolutePath(), context) != null) {
                    length += file.length();
                }
            }
        } else {
            length = length();
        }
        return context.getString(R.string.size, Formatter.formatFileSize(context, length));
    }

    private AppMetadata parseApk(PackageManager pm, String apkPath) {
        PackageInfo pi = pm.getPackageArchiveInfo(apkPath, 0);
        if (pi == null || pi.applicationInfo == null) {
            return null;
        }

        ApplicationInfo ai = pi.applicationInfo;
        ai.sourceDir = apkPath;
        ai.publicSourceDir = apkPath;

        AppMetadata data = new AppMetadata();
        data.packageName = ai.packageName;
        data.version = pi.versionName;

        try {
            data.name = pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            data.name = null;
        }

        try {
            data.icon = pm.getApplicationIcon(ai);
        } catch (Exception e) {
            data.icon = null;
        }

        return data;
    }

    public void load(ImageView icon, TextView name, TextView path, TextView size, TextView version) {
        Context context = icon.getContext().getApplicationContext();
        PackageManager pm = context.getPackageManager();

        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            executor.execute(() -> {
                AppMetadata result = null;
                String formattedSize = getSize(context);

                if (isDirectory()) {
                    File baseAPK = new File(getAbsoluteFile(), "base.apk");
                    if (baseAPK.exists()) {
                        result = parseApk(pm, baseAPK.getAbsolutePath());
                    } else {
                        File[] files = listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
                                    result = parseApk(pm, file.getAbsolutePath());
                                    if (result != null) break;
                                }
                            }
                        }
                    }
                } else {
                    result = parseApk(pm, getAbsolutePath());
                }

                final AppMetadata finalData = result;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (finalData != null && finalData.packageName != null) {
                        name.setText(finalData.name != null ? finalData.name : getName());
                        name.setPaintFlags(name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        path.setText(finalData.packageName);
                        icon.setImageDrawable(finalData.icon);
                        version.setText(version.getContext().getString(R.string.version, finalData.version));
                        version.setVisibility(VISIBLE);
                        size.setText(formattedSize);
                        path.setVisibility(VISIBLE);
                        size.setVisibility(VISIBLE);
                    } else {
                        path.setText(null);
                        name.setText(getName());
                        name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        icon.setImageResource(R.drawable.ic_android_app);
                        version.setVisibility(GONE);
                        path.setVisibility(GONE);
                        size.setVisibility(GONE);
                    }
                });
            });
        }
    }

    private static class AppMetadata {
        Drawable icon;
        String name;
        String packageName;
        String version;
    }

}