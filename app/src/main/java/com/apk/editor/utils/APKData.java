package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.activities.APKTasksActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKData {

    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        for (File mFile : getAPKList(context)) {
            if (sUtils.getString("apkTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.isDirectory() && sUtils.exist(new File(mFile.toString(), "base.apk"))) {
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
        if (!sUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private static File[] getAPKList(Context context) {
        if (!getExportAPKsPath(context).exists()) {
            sUtils.mkdir(getExportAPKsPath(context));
        }
        return getExportAPKsPath(context).listFiles();
    }

    public static File getExportAPKsPath(Context context) {
        if (Build.VERSION.SDK_INT < 29 && sUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
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
        File privateKey = new File(getSigningEnvironmentDir(context), "APKEditor.pk8");

        if (privateKey.exists()) {
            return;
        }

        sUtils.mkdir(getSigningEnvironmentDir(context));

        sUtils.copyAssetFile("APKEditor.pk8", privateKey, context);
    }

    private static File getSigningEnvironmentDir(Context context) {
        return new File(context.getFilesDir(), "signing");
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

    public static void shareAPK(String apkPath, Context context) {
        Uri uriFile = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", new File(apkPath));
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/java-archive");
        share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_summary, BuildConfig.VERSION_NAME));
        share.putExtra(Intent.EXTRA_STREAM, uriFile);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_with)));
    }

    public static void showSignatureErrorDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.signature_warning))
                .setPositiveButton(R.string.got_it, (dialog, id) ->
                        sUtils.saveBoolean("signature_warning", true, context)).show();
    }

    @SuppressLint("StringFormatInvalid")
    private static void prepareSource(File buildDir, File exportPath, File backupPath, Context context) {
        if (!Common.isCancelled()) {
            for (File file : Objects.requireNonNull(exportPath.listFiles())) {
                if (!fileToExclude(file)) {
                    if (file.isDirectory()) {
                        sUtils.copyDir(file, new File(buildDir, file.getName()));
                    } else {
                        sUtils.copy(file, new File(buildDir, file.getName()));
                    }
                }
                if (file.isDirectory() && file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                    // Build new dex file if the smali files are modified
                    if (APKExplorer.isSmaliEdited(new File(context.getCacheDir(), Common.getAppID() + "/.aeeBackup/appData").getAbsolutePath())) {
                        Common.setStatus(context.getString(R.string.building, file.getName()));
                        new SmaliToDex(file, new File(buildDir, file.getName()), 0, context).execute();
                    } else {
                        // Otherwise, use the original one from the backup folder
                        if (sUtils.exist(new File(backupPath, file.getName()))) {
                            sUtils.copy(new File(backupPath, file.getName()), new File(buildDir, file.getName()));
                        }
                    }
                }
            }
        }
    }

    public static void prepareSignedAPK(Activity activity) {
        new sExecutor() {
            private File mBackUpPath = null, mBuildDir = null;
            private final File mExportPath = new File(activity.getCacheDir(), Common.getAppID()),
                    mTMPZip = new File(activity.getCacheDir(), "tmp.apk");

            @Override
            public void onPreExecute() {
                Common.setFinishStatus(false);
                Common.isCancelled(false);
                Common.isBuilding(true);
                Common.setStatus(null);
                Intent apkTasks = new Intent(activity, APKTasksActivity.class);
                activity.startActivity(apkTasks);
                Common.setStatus(activity.getString(R.string.preparing_apk, Common.getAppID()));

                mBuildDir = new File(mExportPath, ".aeeBuild");
                mBackUpPath = new File(mExportPath, ".aeeBackup");
                if (mBuildDir.exists()) {
                    sUtils.delete(mBuildDir);
                }
                sUtils.mkdir(mBuildDir);

                if (mTMPZip.exists()) {
                    sUtils.delete(mTMPZip);
                }
            }

            @Override
            public void doInBackground() {
                Common.setStatus(activity.getString(R.string.preparing_source));
                prepareSource(mBuildDir, mExportPath, mBackUpPath, activity);
                if (Common.getError() > 0) {
                    return;
                }
                APKEditorUtils.zip(mBuildDir, mTMPZip);
                File mParent;
                if (sPackageUtils.isPackageInstalled(Common.getAppID(), activity) && APKData.isAppBundle(sPackageUtils
                        .getSourceDir(Common.getAppID(), activity))) {
                    mParent = new File(getExportAPKsPath(activity), Common.getAppID() + "_aee-signed");
                    if (mParent.exists()) {
                        sUtils.delete(mParent);
                    }
                    sUtils.mkdir(mParent);
                    for (String mSplits : splitApks(sPackageUtils.getSourceDir(Common.getAppID(), activity))) {
                        if (!new File(mSplits).getName().equals("base.apk")) {
                            Common.setStatus(activity.getString(R.string.signing, new File(mSplits).getName()));
                            signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), activity);
                        }
                    }
                    Common.setStatus(activity.getString(R.string.signing, "base.apk"));
                    signApks(mTMPZip, new File(mParent, "base.apk"), activity);
                } else {
                    mParent = new File(getExportAPKsPath(activity), Common.getAppID() + "_aee-signed.apk");
                    if (mParent.exists()) {
                        sUtils.delete(mParent);
                    }
                    Common.setStatus(activity.getString(R.string.signing, mParent.getName()));
                    signApks(mTMPZip, mParent, activity);
                }
                if (Common.isCancelled()) {
                    sUtils.delete(mParent);
                }
            }

            @Override
            public void onPostExecute() {
                sUtils.delete(mTMPZip);
                sUtils.delete(mBuildDir);
                if (!Common.isFinished()) {
                    Common.setFinishStatus(true);
                }
                activity.finish();
            }
        }.execute();
    }

    public static void reSignAPKs(String packageName, boolean install, Activity activity) {
        new sExecutor() {
            private File mParent = null;
            private ProgressDialog mProgressDialog;
            private String mPackageName = null;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(packageName != null ? activity.getString(R.string.signing, sPackageUtils.getAppName(
                        packageName, activity)) : activity.getString(R.string.resigning_apks));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if (packageName == null) {
                    // Find package name from the selected APK's
                    mPackageName = findPackageName(activity);
                }
            }

            @Override
            public void doInBackground() {
                if (packageName != null) {
                    Common.getAPKList().clear();
                    if (APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, activity))) {
                        Common.getAPKList().addAll(splitApks(sPackageUtils.getSourceDir(packageName, activity)));
                    } else {
                        Common.getAPKList().add(sPackageUtils.getSourceDir(packageName, activity));
                    }
                }
                if (mPackageName != null || packageName != null) {
                    String apkNameString;
                    if (packageName != null) {
                        apkNameString = packageName;
                    } else {
                        apkNameString = mPackageName;
                    }
                    if (Common.getAPKList().size() > 1) {
                        if (install) {
                            mParent = new File(activity.getCacheDir(), "aee-signed");
                        } else {
                            mParent = new File(getExportAPKsPath(activity), apkNameString + "_aee-signed");
                        }
                        if (mParent.exists()) {
                            sUtils.delete(mParent);
                        }
                        sUtils.mkdir(mParent);
                        for (String mSplits : Common.getAPKList()) {
                            signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), activity);
                        }
                    } else {
                        if (install) {
                            mParent = new File(activity.getCacheDir(), "aee-signed.apk");
                        } else {
                            mParent = new File(getExportAPKsPath(activity), apkNameString + "_aee-signed.apk");
                        }
                        if (mParent.exists()) {
                            sUtils.delete(mParent);
                        }
                        signApks(new File(Common.getAPKList().get(0)), mParent, activity);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (mPackageName == null && packageName == null) {
                    sUtils.snackBar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks)).show();
                } else {
                    if (packageName == null) {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        if (install) {
                            if (Common.getAPKList().size() > 1) {
                                List<String> signedAPKs = new ArrayList<>();
                                for (File apkFile : Objects.requireNonNull(mParent.listFiles())) {
                                    signedAPKs.add(apkFile.getAbsolutePath());
                                }
                                SplitAPKInstaller.installSplitAPKs(signedAPKs, null, activity);
                            } else {
                                SplitAPKInstaller.installAPK(mParent, activity);
                            }
                            if (!Common.isFinished()) {
                                Common.setFinishStatus(true);
                            }
                        } else {
                            new MaterialAlertDialogBuilder(activity)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(mPackageName)
                                    .setMessage(activity.getString(
                                            R.string.resigned_apks_path, mParent.getAbsolutePath()))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.cancel, (dialog, id) -> {
                                        Common.isReloading(true);
                                        if (Common.isFinished()) {
                                            Common.setFinishStatus(false);
                                        } else {
                                            activity.finish();
                                        }
                                    }).show();
                        }
                    }
                }

            }
        }.execute();
    }

    public static void exportApp(String packageName, Context context) {
        new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting, sPackageUtils.getAppName(packageName, context)));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (!getExportAPKsPath(context).exists()) {
                    sUtils.mkdir(getExportAPKsPath(context));
                }
            }

            @Override
            public void doInBackground() {
                if (APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, context))) {
                    File mParent = new File(getExportAPKsPath(context) , packageName);
                    if (mParent.exists()) {
                        sUtils.delete(mParent);
                    }
                    sUtils.mkdir(mParent);
                    for (String mSplits : splitApks(sPackageUtils.getSourceDir(packageName, context))) {
                        if (mSplits.endsWith(".apk")) {
                            sUtils.copy(new File(mSplits), new File(mParent, new File(mSplits).getName()));
                        }
                    }
                } else {
                    sUtils.copy(new File(sPackageUtils.getSourceDir(packageName, context)), new File(getExportAPKsPath(context),  packageName + ".apk"));
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    public static MaterialAlertDialogBuilder shareAppBundleDialog(String path, Context context) {
        return new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.share_message, new File(path).getName()))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.share), (dialog, id) ->
                        shareAppBundle(path, false, context).execute());
    }

    public static sExecutor shareAppBundle(String path, boolean exportOnly, Context context) {
        return new sExecutor() {
            private File mFile;
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(exportOnly ? R.string.saving : R.string.preparing_bundle));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (!sUtils.exist(new File(Projects.getExportPath(context)))) {
                    sUtils.mkdir(new File(Projects.getExportPath(context)));
                }
                mFile = new File(Projects.getExportPath(context), new File(path).getName() + ".xapk");
            }

            @Override
            public void doInBackground() {
                if (mFile.exists()) {
                    sUtils.delete(mFile);
                }
                APKEditorUtils.zip(new File(path), mFile);
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (!exportOnly) {
                    Uri uriFile = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider", mFile);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("application/zip");
                    share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                    share.putExtra(Intent.EXTRA_STREAM, uriFile);
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(share, context.getString(R.string.share_with)));
                }
            }
        };
    }

    public static sExecutor saveToDownloads(File file, Context context) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.saving));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void doInBackground() {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    sUtils.copyStream(inputStream, outputStream);
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        };
    }

}