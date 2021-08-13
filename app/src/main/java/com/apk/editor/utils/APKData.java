package com.apk.editor.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.activities.APKTasksActivity;
import com.apk.editor.apksigner.ApkSigner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKData {

    private static final List<String> mData = new ArrayList<>();

    public static List<String> getData(Context context) {
        mData.clear();
        for (File mFile : getAPKList(context)) {
            if (APKEditorUtils.getString("apkTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.isDirectory() && APKEditorUtils.exist(mFile.toString() + "/base.apk")) {
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
                    } else if (getAppName(mFile.getAbsolutePath(), context) != null && Common.isTextMatched(Objects.requireNonNull(getAppName(
                            mFile.getAbsolutePath(), context)).toString(), Common.getSearchWord())) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (Common.isTextMatched(mFile.getName(), Common.getSearchWord())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            }
        }
        Collections.sort(mData);
        if (!APKEditorUtils.getBoolean("az_order", true, context)) {
            Collections.reverse(mData);
        }
        return mData;
    }

    private static File[] getAPKList(Context context) {
        if (!getExportAPKsPath(context).exists()) {
            APKEditorUtils.mkdir(getExportAPKsPath(context).toString());
        }
        return getExportAPKsPath(context).listFiles();
    }

    public static File getExportAPKsPath(Context context) {
        if (APKEditorUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
            return new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
        } else {
            return context.getExternalFilesDir("");
        }
    }

    private static PackageInfo getPackageInfo(String path, Context context) {
        return AppData.getPackageManager(context).getPackageArchiveInfo(path, 0);
    }

    public static CharSequence getAppName(String path, Context context) {
        if (getPackageInfo(path, context) != null) {
            return getPackageInfo(path, context).applicationInfo.loadLabel(AppData.getPackageManager(context));
        } else {
            return null;
        }
    }

    public static CharSequence getAppID(String path, Context context) {
        if (getPackageInfo(path, context) != null) {
            return getPackageInfo(path, context).applicationInfo.packageName;
        } else {
            return null;
        }
    }

    public static Drawable getAppIcon(String path, Context context) {
        if (getPackageInfo(path, context) != null) {
            return getPackageInfo(path, context).applicationInfo.loadIcon(AppData.getPackageManager(context));
        } else {
            return null;
        }
    }

    public static String getVersionName(String path, Context context) {
        return Objects.requireNonNull(AppData.getPackageManager(context).getPackageArchiveInfo(path, 0)).versionName;
    }

    public static void signApks(File apk, File signedAPK, Context context) {
        try {
            checkAndPrepareSigningEnvironment(context);

            ApkSigner apkSigner = new ApkSigner(new File(getSigningEnvironmentDir(context), "APKEditor"), new File(getSigningEnvironmentDir(context), "APKEditor.pk8"));
            apkSigner.sign(apk, signedAPK);
        } catch (Exception ignored) {}
    }

    private static void checkAndPrepareSigningEnvironment(Context context) throws Exception {
        File signingEnvironment = getSigningEnvironmentDir(context);
        File pastFile = new File(signingEnvironment, "APKEditor");
        File privateKeyFile = new File(signingEnvironment, "APKEditor.pk8");

        if (pastFile.exists() && privateKeyFile.exists())
            return;

        signingEnvironment.mkdir();

        APKEditorUtils.copyFileFromAssets(context, "APKEditor", pastFile);
        APKEditorUtils.copyFileFromAssets(context, "APKEditor.pk8", privateKeyFile);
    }

    private static File getSigningEnvironmentDir(Context context) {
        return new File(context.getFilesDir(), "signing");
    }

    public static String getParentFile(String path) {
        return Objects.requireNonNull(new File(path).getParentFile()).toString();
    }

    public static String findPackageName(Context context) {
        String name = null;
        for (String mAPKs : Common.getAPKList()) {
            if (APKData.getAppID(mAPKs, context) != null) {
                name = Objects.requireNonNull(APKData.getAppID(mAPKs, context)).toString();
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

    public static void showSignatureErrorDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.signature_warning))
                .setPositiveButton(R.string.got_it, (dialog, id) ->
                        APKEditorUtils.saveBoolean("signature_warning", true, context)).show();
    }

    private static void prepareSource(File buildDir, File exportPath, File backupPath, Context context) {
        for (File file : Objects.requireNonNull(exportPath.listFiles())) {
            if (!fileToExclude(file)) {
                if (file.isDirectory()) {
                    APKEditorUtils.copyDir(file, new File(buildDir, file.getName()));
                } else {
                    APKEditorUtils.copy(file.getAbsolutePath(), new File(buildDir, file.getName()).getAbsolutePath());
                }
            }
            if (file.isDirectory() && file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                // Build new dex file if the smali files are modified
                if (APKEditorUtils.exist(new File(file, "edited").getAbsolutePath())) {
                    Common.setStatus(context.getString(R.string.building, file.getName()));
                    new SmaliToDex(file, new File(buildDir, file.getName()), 0, context).execute();
                } else {
                    // Otherwise, use the original one from the backup folder
                    if (APKEditorUtils.exist(new File(backupPath, file.getName()).getAbsolutePath())) {
                        APKEditorUtils.copy(new File(backupPath, file.getName()).getAbsolutePath(), new File(buildDir, file.getName()).getAbsolutePath());
                    }
                }
            }
        }
    }

    public static void prepareSignedAPK(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            private final File mTMPZip = new File(activity.getCacheDir(), "tmp.apk");
            private File mBuilDir;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Common.setFinishStatus(false);
                Common.isBuilding(true);
                Common.setStatus(null);
                Intent apkTasks = new Intent(activity, APKTasksActivity.class);
                activity.startActivity(apkTasks);
                Common.setStatus(activity.getString(R.string.preparing_apk, (Common.getAppID() != null ? Common.getAppID() :
                        new File(Common.getPath()).getName())));
                if (mTMPZip.exists()) {
                    APKEditorUtils.delete(mTMPZip.getAbsolutePath());
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Common.setStatus(activity.getString(R.string.preparing_source));
                if (Common.getAppID() != null) {
                    File mExportPath = new File(activity.getCacheDir().getPath(), Common.getAppID());
                    File mBackUpPath = new File(mExportPath, ".aeeBackup");
                    mBuilDir = new File(mExportPath, ".aeeBuild");
                    mBuilDir.mkdirs();
                    prepareSource(mBuilDir, mExportPath, mBackUpPath, activity);
                    APKEditorUtils.zip(mBuilDir, mTMPZip);
                    if (APKData.isAppBundle(AppData.getSourceDir(Common.getAppID(), activity))) {
                        File mParent = new File(getExportAPKsPath(activity), Common.getAppID() + "_aee-signed");
                        mParent.mkdirs();
                        for (String mSplits : splitApks(AppData.getSourceDir(Common.getAppID(), activity))) {
                            if (!new File(mSplits).getName().equals("base.apk")) {
                                Common.setStatus(activity.getString(R.string.signing, new File(mSplits).getName()));
                                signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), activity);
                            }
                        }
                        Common.setStatus(activity.getString(R.string.signing, "base.apk"));
                        signApks(mTMPZip, new File(mParent.toString() + "/base.apk"), activity);
                    } else {
                        if (!getExportAPKsPath(activity).exists()) {
                            getExportAPKsPath(activity).mkdirs();
                        }
                        Common.setStatus(activity.getString(R.string.signing, new File(getExportAPKsPath(activity), Common.getAppID() + "_aee-signed.apk").getName()));
                        signApks(mTMPZip, new File(getExportAPKsPath(activity), Common.getAppID() + "_aee-signed.apk"), activity);
                    }
                } else {
                    if (!getExportAPKsPath(activity).exists()) {
                        getExportAPKsPath(activity).mkdirs();
                    }
                    File mExportPath = new File(activity.getCacheDir().getPath() + "/" + new File(Common.getPath()).getName());
                    File mBackUpPath = new File(mExportPath, ".aeeBackup");
                    mBuilDir = new File(mExportPath, ".aeeBuild");
                    mBuilDir.mkdirs();
                    prepareSource(mBuilDir, mExportPath, mBackUpPath, activity);
                    APKEditorUtils.zip(mBuilDir, mTMPZip);
                    Common.setStatus(activity.getString(R.string.signing, new File(getExportAPKsPath(activity), new File(Common.getPath()).getName() + "_aee-signed.apk").getName()));
                    signApks(mTMPZip, new File(getExportAPKsPath(activity), new File(Common.getPath()).getName() + "_aee-signed.apk"), activity);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                APKEditorUtils.delete(mTMPZip.getAbsolutePath());
                APKEditorUtils.delete(mBuilDir.getAbsolutePath());
                if (!Common.isFinished()) {
                    Common.setFinishStatus(true);
                }
                activity.finish();
            }
        }.execute();
    }

    public static void signAPK(String packageName, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.signing, AppData.getAppName(packageName, context)));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (APKData.isAppBundle(AppData.getSourceDir(packageName, context))) {
                    File mParent = new File(getExportAPKsPath(context),packageName + "_aee-signed");
                    mParent.mkdirs();
                    for (String mSplits : splitApks(AppData.getSourceDir(packageName, context))) {
                        signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), context);
                    }
                } else {
                    if (!getExportAPKsPath(context).exists()) {
                        getExportAPKsPath(context).mkdirs();
                    }
                    signApks(new File(AppData.getSourceDir(packageName, context)), new File(getExportAPKsPath(context), packageName + "_aee-signed.apk"), context);
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
            }
        }.execute();
    }

    public static void reSignAPKs(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            private String mPackageName = null, mSignedAPKPath = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.resigning_apks));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Find package name from the selected APK's
                mPackageName = findPackageName(activity);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (mPackageName != null) {
                    if (Common.getAPKList().size() > 1) {
                        File mParent = new File(Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed");
                        mParent.mkdirs();
                        mSignedAPKPath = mParent.getAbsolutePath();
                        for (String mSplits : Common.getAPKList()) {
                            signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), activity);
                        }
                    } else {
                        new File(Projects.getExportPath(activity)).mkdirs();
                        mSignedAPKPath = Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed.apk";
                        signApks(new File(Common.getAPKList().get(0)), new File(Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed.apk"), activity);
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
                if (mPackageName == null) {
                    APKEditorUtils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks));
                } else {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(mPackageName)
                            .setMessage(activity.getString(
                                    R.string.resigned_apks_path, mSignedAPKPath))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialog, id) -> activity.finish()).show();
                }
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }.execute();
    }

    public static void reSignAndInstall(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            private File mParent = new File(activity.getCacheDir(), "aee-signed");
            private ProgressDialog mProgressDialog;
            private String mPackageName = null, mSignedAPKPath = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.resigning_apks));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Find package name from the selected APK's
                mPackageName = findPackageName(activity);
                if (mParent.exists()) {
                    mParent.delete();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (mPackageName != null) {
                    mParent = new File(activity.getCacheDir(), "aee-signed");
                    mParent.mkdirs();
                    if (Common.getAPKList().size() > 1) {
                        mSignedAPKPath = mParent.getAbsolutePath();
                        for (String mSplits : Common.getAPKList()) {
                            signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), activity);
                        }
                    } else {
                        mSignedAPKPath = mParent.toString() + "/" + "aee-signed.apk";
                        signApks(new File(Common.getAPKList().get(0)), new File(mParent, "aee-signed.apk"), activity);
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
                if (mPackageName == null) {
                    APKEditorUtils.snackbar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks));
                } else {
                    if (Common.getAPKList().size() > 1) {
                        List<String> signedAPKs = new ArrayList<>();
                        for (String mAPKs : Common.getAPKList()) {
                            signedAPKs.add(mSignedAPKPath + "/" + new File(mAPKs).getName());
                        }
                        SplitAPKInstaller.installSplitAPKs(signedAPKs, null, activity);
                    } else {
                        SplitAPKInstaller.installAPK(new File(mSignedAPKPath), activity);
                    }
                }
                Common.setFinishStatus(true);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }.execute();
    }

    public static void exportApp(String packageName, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exporting, AppData.getAppName(packageName, context)));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (APKData.isAppBundle(AppData.getSourceDir(packageName, context))) {
                    File mParent = new File(getExportAPKsPath(context) , packageName);
                    mParent.mkdirs();
                    for (String mSplits : splitApks(AppData.getSourceDir(packageName, context))) {
                        if (mSplits.endsWith(".apk")) {
                            APKEditorUtils.copy(mSplits, mParent.toString() + "/" + new File(mSplits).getName());
                        }
                    }
                } else {
                    if (!getExportAPKsPath(context).exists()) {
                        getExportAPKsPath(context).mkdirs();
                    }
                    APKEditorUtils.copy(AppData.getSourceDir(packageName, context), getExportAPKsPath(context) + "/" + packageName + ".apk");
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
            }
        }.execute();
    }

    public static void shareAppBundle(String name, String path, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.preparing_bundle));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (!APKEditorUtils.exist(Projects.getExportPath(context))) {
                    APKEditorUtils.mkdir(Projects.getExportPath(context));
                }
                new File(Projects.getExportPath(context), name + ".xapk").delete();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                APKEditorUtils.zip(new File(path), new File(Projects.getExportPath(context), name + ".xapk"));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Uri uriFile = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".provider", new File(Projects.getExportPath(context), name + ".xapk"));
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("application/zip");
                share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                share.putExtra(Intent.EXTRA_STREAM, uriFile);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(share, context.getString(R.string.share_with)));
            }
        }.execute();
    }

}