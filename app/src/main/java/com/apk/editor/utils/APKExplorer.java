package com.apk.editor.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.dongliu.apk.parser.ApkFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorer {

    public static List<String> getData(File[] files, boolean supported, Activity activity) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            // Add directories
            for (File mFile : files) {
                if (mFile.isDirectory() && !mFile.getName().matches(".aeeBackup|.aeeBuild")) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mDir);
            if (!APKEditorUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mDir);
            }
            mData.addAll(mDir);
            // Add files
            for (File mFile :files) {
                if (supported) {
                    if (mFile.isFile()) {
                        mFiles.add(mFile.getAbsolutePath());
                    }
                } else if (mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mFiles.add(mFile.getAbsolutePath());

                }
            }
            Collections.sort(mFiles);
            if (!APKEditorUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mFiles);
            }
            mData.addAll(mFiles);
        } catch (NullPointerException ignored) {
            activity.finish();
        }
        return mData;
    }

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF") || path.endsWith(".SF")
                || path.endsWith(".html") || path.endsWith(".ini") || path.endsWith(".smali");
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static boolean isBinaryXML(String path) {
        return path.endsWith(".xml") && (new File(path).getName().equals("AndroidManifest.xml") || path.contains(Common.getAppID() + "/res/"));
    }

    private static boolean isSupportedFile(String path) {
        return path.endsWith(".apk") || path.endsWith(".apks") || path.endsWith(".apkm") || path.endsWith(".xapk");
    }

    public static boolean isPermissionDenied(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return !Environment.isExternalStorageManager();
        } else {
            String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            return (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
        }
    }

    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
            activity.finish();
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            APKEditorUtils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.permission_denied_message));
        }
    }

    public static void launchPermissionDialog(Activity activity) {
        if (Build.VERSION.SDK_INT >= 30) {
            new MaterialAlertDialogBuilder(activity)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(activity.getString(R.string.important))
                    .setMessage(activity.getString(R.string.file_permission_request_message, activity.getString(R.string.app_name)))
                    .setCancelable(false)
                    .setNegativeButton(activity.getString(R.string.cancel), (dialogInterfacei, ii) -> {
                    })
                    .setPositiveButton(activity.getString(R.string.grant), (dialog1, id1) -> APKExplorer.requestPermission(activity)).show();
        } else {
            APKExplorer.requestPermission(activity);
        }
    }

    public static void setIcon(AppCompatImageButton icon, Drawable drawable, Context context) {
        icon.setImageDrawable(drawable);
        icon.setColorFilter(APKEditorUtils.isDarkTheme(context) ? context.getResources().getColor(R.color.colorWhite) :
                context.getResources().getColor(R.color.colorBlack));
    }

    public static int getSpanCount(Activity activity) {
        return APKEditorUtils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    public static String readXMLFromAPK(String apk, String path) {
        try (ApkFile apkFile = new ApkFile(new File(apk))) {
            String xmlData = apkFile.transBinaryXml(path);
            apkFile.close();
            return xmlData;
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static void saveImage(Bitmap bitmap, String path) {
        File file = new File(path);
        try {
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException ignored) {}
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void exploreAPK(String packageName, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private File mExplorePath;
            private File mBackUpPath;
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exploring, AppData.getAppName(packageName, context)));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                Common.setAppID(packageName);
                mExplorePath = new File(context.getCacheDir().getPath(), packageName);
                mBackUpPath = new File(mExplorePath, ".aeeBackup");
                Common.setPath(mExplorePath.getAbsolutePath());
            }
            @Override
            protected Void doInBackground(Void... voids) {
                if (!mExplorePath.exists()) {
                    mExplorePath.mkdirs();
                    APKEditorUtils.unzip(AppData.getSourceDir(packageName, context), mExplorePath.getAbsolutePath());
                    // Decompile dex file(s)
                    for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                        if (files.getName().startsWith("classes") && files.getName().endsWith(".dex")) {
                            mBackUpPath.mkdirs();
                            APKEditorUtils.copy(files.getAbsolutePath(), new File(mBackUpPath, files.getName()).getAbsolutePath());
                            APKEditorUtils.delete(files.getAbsolutePath());
                            File mDexExtractPath = new File(mExplorePath, files.getName());
                            mDexExtractPath.mkdirs();
                            mProgressDialog.setMessage(context.getString(R.string.decompiling, files.getName()));
                            new DexToSmali(false, new File(AppData.getSourceDir(Common.getAppID(), context)), mDexExtractPath, 0, files.getName()).execute();
                        }
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Intent explorer = new Intent(context, APKExploreActivity.class);
                context.startActivity(explorer);
            }
        }.execute();
    }

}