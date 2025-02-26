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
import com.apk.editor.utils.SerializableItems.APKItems;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKData {

    public static List<APKItems> getData(String searchWord, Context context) {
        List<APKItems> mData = new CopyOnWriteArrayList<>();
        for (File mFile : getAPKList(context)) {
            if (sCommonUtils.getString("apkTypes", "apks", context).equals("bundles")) {
                if (mFile.isDirectory() && !mFile.getName().equals("APK") && getBaseAPK(mFile, context) != null) {
                    if (searchWord == null) {
                        mData.add(new APKItems(mFile, getBaseAPK(mFile, context)));
                    } else if (Common.isTextMatched(mFile.getAbsolutePath(), searchWord)) {
                        mData.add(new APKItems(mFile, getBaseAPK(mFile, context)));
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (searchWord == null) {
                        mData.add(new APKItems(mFile, null));
                    } else if (sAPKUtils.getAPKName(mFile.getAbsolutePath(), context) != null && Common.isTextMatched(Objects.requireNonNull(
                            sAPKUtils.getAPKName(mFile.getAbsolutePath(), context)).toString(), searchWord)) {
                        mData.add(new APKItems(mFile, null));
                    } else if (Common.isTextMatched(mFile.getName(), searchWord)) {
                        mData.add(new APKItems(mFile, null));
                    }
                }
            }
        }
        Collections.sort(mData, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAPKFile().getName(), rhs.getAPKFile().getName()));
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

    public static File getBaseAPK(File dir, Context context) {
        for (File apkFile : Objects.requireNonNull(dir.listFiles()))  {
            if (sAPKUtils.getPackageName(apkFile.getAbsolutePath(), context) != null) {
                return apkFile;
            }
        }
        return null;
    }

    public static String findPackageName(List<String> apkList, Context context) {
        String name = null;
        for (String mAPKs : apkList) {
            if (sAPKUtils.getPackageName(mAPKs, context) != null) {
                name = Objects.requireNonNull(sAPKUtils.getPackageName(mAPKs, context));
                break;
            }
        }
        return name;
    }

    public static List<String> splitApks(File file) {
        List<String> list = new CopyOnWriteArrayList<>();
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File mFile : Objects.requireNonNull(file.listFiles())) {
                    if (mFile.getName().endsWith(".apk")) {
                        list.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                return splitApks(Objects.requireNonNull(file.getParentFile()));
            }
        }
        return list;
    }

    public static List<String> splitApks(String apkPath) {
        return splitApks(new File(apkPath));
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
        if (!Common.isCancelled(context)) {
            for (File file : Objects.requireNonNull(exportPath.listFiles())) {
                if (file.isDirectory() && file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                    // Build new dex file if the smali files are modified
                    if (APKExplorer.isSmaliEdited(new File(backupPath, "appData").getAbsolutePath())) {
                        Common.setStatus(context.getString(R.string.building, file.getName()), context);
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