package com.apk.editor.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;

import com.apk.editor.R;
import com.apk.editor.apksigner.APKSignerUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKEditorUtils {

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static void delete(String path) {
        if (new File(path).isDirectory())
            for (File child : Objects.requireNonNull(new File(path).listFiles()))
                delete(child.getAbsolutePath());

        new File(path).delete();
    }

    public static void mkdir(String path) {
       new File(path).mkdirs();
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }

    public static CharSequence fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void create(String text, String path) {
        try {
            File logFile = new File(path);
            logFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(logFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(text);
            myOutWriter.close();
            fOut.close();
        } catch (Exception ignored) {
        }
    }

    public static void copy(String source, String dest) {
        if (!exist(Objects.requireNonNull(new File(dest).getParentFile()).toString())) {
            mkdir(Objects.requireNonNull(new File(dest).getParentFile()).toString());
        }
        try {
            FileInputStream inputStream = new FileInputStream(new File(source));
            FileOutputStream outputStream = new FileOutputStream(new File(dest));

            copyStream(inputStream, outputStream);

            inputStream.close();
            outputStream.close();
        } catch (IOException ignored) {}
    }

    public static String read(String file) {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(file));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void copyFileFromAssets(Context context, String assetFileName, File destination) throws IOException {
        InputStream inputStream = context.getAssets().open(assetFileName);
        FileOutputStream outputStream = new FileOutputStream(destination);

        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }

        inputStream.close();
        outputStream.close();
    }

    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
        }
    }

    public static boolean exist(String file) {
        return new File(file).exists();
    }

    public static void unzip(String zip, String path) {
        try (FileInputStream fis = new FileInputStream(new File(zip))) {
            try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                try (ZipInputStream zis = new ZipInputStream(bis)) {
                    ZipEntry ze;
                    int count;
                    byte[] buffer = new byte[1024];
                    while ((ze = zis.getNextEntry()) != null) {
                        File file = new File(new File(path), ze.getName());
                        File dir = ze.isDirectory() ? file : file.getParentFile();
                        assert dir != null;
                        if (!dir.isDirectory() && !dir.mkdirs())
                            throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                        if (ze.isDirectory())
                            continue;
                        try (FileOutputStream fout = new FileOutputStream(file)) {
                            while ((count = zis.read(buffer)) != -1)
                                fout.write(buffer, 0, count);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean isWritePermissionGranted(Context context) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void launchUrl(String url, Activity activity) {
        if (url == null) return;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            activity.startActivity(i);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static void sleep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ignored) {}
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static String getString(String name, String defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, defaults);
    }

    public static void saveString(String name, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
    }

}