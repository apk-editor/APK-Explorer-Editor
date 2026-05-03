package com.apk.editor.utils;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
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
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

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

    private String getBaseAPKPath(Context context) {
        if (isDirectory()) {
            File baseAPK = new File(getAbsoluteFile(), "base.apk");
            if (baseAPK.exists()) {
                return baseAPK.getAbsolutePath();
            }

            File[] files = listFiles();
            if (files == null) return null;

            for (File file : files) {
                if (!file.isFile() || !file.getName().toLowerCase().endsWith(".apk")) {
                    continue;
                }

                if (sAPKUtils.getPackageName(file.getAbsolutePath(), context) != null) {
                    return file.getAbsolutePath();
                }
            }
        }

        return getAbsolutePath();
    }

    public String getPackageName(Context context) {
        return sAPKUtils.getPackageName(getBaseAPKPath(context), context);
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

    public void load(ImageView icon, TextView name, TextView path, TextView size, TextView version) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                CharSequence appName;
                Drawable appIcon;
                String appSize, appVersion;
                String packageName = getPackageName(name.getContext());

                if (packageName != null) {
                    appName = sAPKUtils.getAPKName(getBaseAPKPath(name.getContext()), name.getContext());
                    appIcon = sAPKUtils.getAPKIcon(getBaseAPKPath(icon.getContext()), icon.getContext());
                    appSize = getSize(size.getContext());
                    appVersion = version.getContext().getString(R.string.version, sAPKUtils.getVersionName(getBaseAPKPath(version.getContext()), version.getContext()));
                } else {
                    appName = getName();
                    appIcon = sCommonUtils.getDrawable(R.drawable.ic_android_app, icon.getContext());
                    appSize = getSize(size.getContext());
                    appVersion = version.getContext().getString(R.string.version, "");
                }

                handler.post(() -> {
                    name.setText(appName);
                    path.setText(getName());
                    icon.setImageDrawable(appIcon);
                    version.setVisibility(VISIBLE);
                    if (packageName == null) {
                        name.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    version.setText(appVersion);
                    size.setVisibility(VISIBLE);
                    size.setText(appSize);
                });
            });
        }
    }

}