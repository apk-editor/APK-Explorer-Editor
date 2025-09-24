package com.apk.editor.utils.SerializableItems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Feb. 18, 2025
 */
public class APKItems implements Serializable {

    private final File mAPKFile;

    public APKItems(File apkFile) {
        this.mAPKFile = apkFile;
    }

    public boolean isDirectory() {
        return mAPKFile.isDirectory();
    }

    public CharSequence getAppName(Context context) {
        return sAPKUtils.getAPKName(isDirectory() ? getBaseAPKPath(context) : getPath(), context);
    }

    private Drawable getAPKIcon(Context context) {
        return sAPKUtils.getAPKIcon(isDirectory() ? getBaseAPKPath(context) : getPath(), context);
    }

    public File getAPKFile() {
        return mAPKFile;
    }

    public File getBaseAPK(Context context) {
        if (isDirectory()) {
            for (File file : Objects.requireNonNull(mAPKFile.listFiles())) {
                if (file.isFile() && sAPKUtils.getPackageName(file.getAbsolutePath(), context) != null) {
                    return file;
                }
            }
        }
        return null;
    }

    public String getBaseAPKPath(Context context) {
        return getBaseAPK(context).getAbsolutePath();
    }

    public String getName() {
        return mAPKFile.getName();
    }

    public String getPackageName(Context context) {
        return sAPKUtils.getPackageName(isDirectory() ? getBaseAPKPath(context) : getPath(), context);
    }

    public String getPath() {
        return mAPKFile.getAbsolutePath();
    }

    @SuppressLint("StringFormatInvalid")
    public String getSize(Context context) {
        long length = 0;
        if (isDirectory()) {
            for (File file : Objects.requireNonNull(mAPKFile.listFiles())) {
                if (file.isFile() && sAPKUtils.getPackageName(file.getAbsolutePath(), context) != null) {
                    length += file.length();
                }
            }
        } else {
            length = getAPKFile().length();
        }
        return context.getString(R.string.size, sAPKUtils.getAPKSize(length));
    }

    public String getVersionName(Context context) {
        return context.getString(R.string.version, sAPKUtils.getVersionName(isDirectory() ? getBaseAPKPath(context) : getPath(), context));
    }

    public void loadAppIcon(ImageView view) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                Drawable drawable;
                if (getAPKIcon(view.getContext()) != null) {
                    drawable = sAPKUtils.getAPKIcon(isDirectory() ? getBaseAPKPath(view.getContext()) : getPath(), view.getContext());
                } else {
                    drawable = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_android_app);
                    view.setColorFilter(APKEditorUtils.getThemeAccentColor(view.getContext()));
                }

                handler.post(() -> view.setImageDrawable(drawable));
            });
        }
    }

}