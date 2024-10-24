package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKData {

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : getAPKList(context)) {
            if (sCommonUtils.getString("apkTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.isDirectory() && sFileUtils.exist(new File(mFile.toString(), "base.apk"))) {
                    if (Common.getSearchWord() == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (Common.isTextMatched(mFile.getAbsolutePath(), Common.getSearchWord())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (Common.getSearchWord() == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (sAPKUtils.getAPKName(mFile.getAbsolutePath(), context) != null && Common.isTextMatched(Objects.requireNonNull(sAPKUtils.getAPKName(
                            mFile.getAbsolutePath(), context)).toString(), Common.getSearchWord())) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (Common.isTextMatched(mFile.getName(), Common.getSearchWord())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            }
        }
        Collections.sort(mData);
        if (!sCommonUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private static File[] getAPKList(Context context) {
        if (!getExportAPKsPath(context).exists()) {
            sFileUtils.mkdir(getExportAPKsPath(context));
        }
        return getExportAPKsPath(context).listFiles();
    }

    public static File getExportAPKsPath(Context context) {
        if (Build.VERSION.SDK_INT < 29 && sCommonUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
            return new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
        } else {
            return context.getExternalFilesDir("");
        }
    }

    public static void signApks(File apk, File signedAPK, Context context) {
        try {
            checkAndPrepareSigningEnvironment(context);

            APKSigner apkSigner = new APKSigner(context);
            apkSigner.sign(apk, signedAPK);
        } catch (Exception ignored) {}
    }

    private static void checkAndPrepareSigningEnvironment(Context context) {
        if (APKSigner.getPK8PrivateKey(context).exists()) {
            return;
        }

        sFileUtils.mkdir(new File(context.getFilesDir(), "signing"));
        sFileUtils.copyAssetFile("APKEditor.pk8", APKSigner.getPK8PrivateKey(context), context);
    }

    private static String getParentFile(String path) {
        return Objects.requireNonNull(new File(path).getParentFile()).toString();
    }

    public static String findPackageName(Context context) {
        String name = null;
        for (String mAPKs : Common.getAPKList()) {
            if (sAPKUtils.getPackageName(mAPKs, context) != null) {
                name = Objects.requireNonNull(sAPKUtils.getPackageName(mAPKs, context));
            }
        }
        return name;
    }

    public static List<String> splitApks(String path) {
        List<String> list = new ArrayList<>();
        if (new File(path).getName().equals("base.apk") && new File(path).exists()) {
            for (File mFile : Objects.requireNonNull(new File(getParentFile(path)).listFiles())) {
                if (mFile.getName().endsWith(".apk")) {
                    list.add(mFile.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static boolean isAppBundle(String path) {
        return splitApks(path).size() > 1;
    }

    private static boolean fileToExclude(File file) {
        return file.isDirectory() && file.getName().equals(".aeeBackup") || file.isDirectory() && file.getName().equals(".aeeBuild")
                || file.isDirectory() && file.getName().equals("META-INF") || file.isDirectory() && file.getName().startsWith("classes")
                && file.getName().endsWith(".dex");
    }

    public static void shareFile(File file, String type, Context context) {
        Uri uriFile = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(type);
        share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_summary, BuildConfig.VERSION_NAME));
        share.putExtra(Intent.EXTRA_STREAM, uriFile);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_with)));
    }

    @SuppressLint("StringFormatInvalid")
    public static void prepareSource(File buildDir, File exportPath, File backupPath, Context context) {
        if (!Common.isCancelled()) {
            for (File file : Objects.requireNonNull(exportPath.listFiles())) {
                if (file.isDirectory() && file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                    // Build new dex file if the smali files are modified
                    if (APKExplorer.isSmaliEdited(new File(context.getCacheDir(), Common.getAppID() + "/.aeeBackup/appData").getAbsolutePath())) {
                        Common.setStatus(context.getString(R.string.building, file.getName()));
                        new SmaliToDex(file, new File(buildDir, file.getName()), 0, context).execute();
                    } else {
                        // Otherwise, use the original one from the backup folder
                        if (sFileUtils.exist(new File(backupPath, file.getName()))) {
                            sFileUtils.copy(new File(backupPath, file.getName()), new File(buildDir, file.getName()));
                        }
                    }
                } else if (file.isDirectory() && file.getName().equals("META-INF")) {
                    if (new File(file, "services").exists()) {
                        sFileUtils.copyDir(new File(file, "services"), new File(buildDir, "META-INF/services"));
                    }
                } else {
                    if (!fileToExclude(file)) {
                        if (file.isDirectory()) {
                            sFileUtils.copyDir(file, new File(buildDir, file.getName()));
                        } else {
                            sFileUtils.copy(file, new File(buildDir, file.getName()));
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void saveToDownload(File file, String name, Context context) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            OutputStream outputStream = context.getContentResolver().openOutputStream(Objects.requireNonNull(uri));
            sFileUtils.copyStream(inputStream, outputStream);
        } catch (IOException ignored) {
        }
    }

}