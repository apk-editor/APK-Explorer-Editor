package com.apk.editor.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
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

    private static List<String> mData = new ArrayList<>();
    public static String mSearchText;

    public static List<String> getData(Context context) {
        mData.clear();
        for (File mFile : getAPKList(context)) {
            if (APKEditorUtils.getString("apkTypes", "apks", context).equals("bundles")) {
                if (mFile.exists() && mFile.isDirectory() && APKEditorUtils.exist(mFile.toString() + "/base.apk")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (mFile.getAbsolutePath().toLowerCase().contains(mSearchText.toLowerCase())) {
                        mData.add(mFile.getAbsolutePath());
                    }
                }
            } else {
                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                    if (mSearchText == null) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (getAppName(mFile.getAbsolutePath(), context) != null && Objects.requireNonNull(getAppName(
                            mFile.getAbsolutePath(), context)).toString().toLowerCase().contains(mSearchText.toLowerCase())) {
                        mData.add(mFile.getAbsolutePath());
                    } else if (mFile.getName().toLowerCase().contains(mSearchText.toLowerCase())) {
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
        if (!APKEditorUtils.exist(Objects.requireNonNull(context.getExternalFilesDir("")).toString())) {
            APKEditorUtils.mkdir(Objects.requireNonNull(context.getExternalFilesDir("")).toString());
        }
        return new File(Objects.requireNonNull(context.getExternalFilesDir("")).toString()).listFiles();
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
        for (String mAPKs : APKExplorer.mAPKList) {
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

    public static void showSignatureErrorDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.signature_warning))
                .setPositiveButton(R.string.got_it, (dialog, id) -> {
                    APKEditorUtils.saveBoolean("signature_warning", true, context);
                }).show();
    }

    public static void prepareSignedAPK(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.preparing_apk, (APKExplorer.mAppID != null ? APKExplorer.mAppID :
                        new File(APKExplorer.mPath).getName())));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (APKExplorer.mAppID != null) {
                    APKEditorUtils.zip(new File(activity.getCacheDir().getPath() + "/" + APKExplorer.mAppID), new File(activity.getCacheDir(), "tmp.apk"));
                    if (APKData.isAppBundle(AppData.getSourceDir(APKExplorer.mAppID, activity))) {
                        File mParent = new File(activity.getExternalFilesDir("") + "/" + APKExplorer.mAppID + "_aee-signed");
                        mParent.mkdirs();
                        for (String mSplits : splitApks(AppData.getSourceDir(APKExplorer.mAppID, activity))) {
                            if (!new File(mSplits).getName().equals("base.apk")) {
                                signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), activity);
                            }
                        }
                        signApks(new File(activity.getCacheDir(), "tmp.apk"), new File(mParent.toString() + "/base.apk"), activity);
                    } else {
                        signApks(new File(activity.getCacheDir(), "tmp.apk"), new File(activity.getExternalFilesDir("") + "/" + APKExplorer.mAppID + "_aee-signed.apk"), activity);
                    }
                } else {
                    APKEditorUtils.zip(new File(activity.getCacheDir().getPath() + "/" + new File(APKExplorer.mPath).getName()), new File(activity.getCacheDir(), "tmp.apk"));
                    signApks(new File(activity.getCacheDir(), "tmp.apk"), new File(activity.getExternalFilesDir("") + "/" + new File(APKExplorer.mPath).getName() + "_aee-signed.apk"), activity);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                new File(activity.getCacheDir(), "tmp.apk").delete();
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
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
                    File mParent = new File(context.getExternalFilesDir("") + "/" + packageName + "_aee-signed");
                    mParent.mkdirs();
                    for (String mSplits : splitApks(AppData.getSourceDir(packageName, context))) {
                        signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), context);
                    }
                } else {
                    signApks(new File(AppData.getSourceDir(packageName, context)), new File(context.getExternalFilesDir("")
                            + "/" + packageName + "_aee-signed.apk"), context);
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
                // Find package name from the selected APK's
                mPackageName = findPackageName(activity);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (mPackageName != null) {
                    if (APKExplorer.mAPKList.size() > 1) {
                        File mParent = new File(Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed");
                        mParent.mkdirs();
                        mSignedAPKPath = mParent.getAbsolutePath();
                        for (String mSplits : APKExplorer.mAPKList) {
                            signApks(new File(mSplits), new File(mParent.toString() + "/" + new File(mSplits).getName()), activity);
                        }
                    } else {
                        new File(Projects.getExportPath(activity)).mkdirs();
                        mSignedAPKPath = Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed.apk";
                        signApks(new File(APKExplorer.mAPKList.get(0)), new File(Projects.getExportPath(activity) + "/" + mPackageName + "_aee-signed.apk"), activity);
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
                    if (APKExplorer.mAPKList.size() > 1) {
                        mSignedAPKPath = mParent.getAbsolutePath();
                        for (String mSplits : APKExplorer.mAPKList) {
                            signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), activity);
                        }
                    } else {
                        mSignedAPKPath = mParent.toString() + "/" + "aee-signed.apk";
                        signApks(new File(APKExplorer.mAPKList.get(0)), new File(mParent, "aee-signed.apk"), activity);
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
                    if (APKExplorer.mAPKList.size() > 1) {
                        List<String> signedAPKs = new ArrayList<>();
                        for (String mAPKs : APKExplorer.mAPKList) {
                            signedAPKs.add(mSignedAPKPath + "/" + new File(mAPKs).getName());
                        }
                        SplitAPKInstaller.installSplitAPKs(signedAPKs, null, activity);
                    } else {
                        SplitAPKInstaller.installAPK(new File(mSignedAPKPath), activity);
                    }
                }
                APKExplorer.mFinish = true;
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
                    File mParent = new File(context.getExternalFilesDir("") + "/" + packageName);
                    mParent.mkdirs();
                    for (String mSplits : splitApks(AppData.getSourceDir(packageName, context))) {
                        if (mSplits.endsWith(".apk")) {
                            APKEditorUtils.copy(mSplits, mParent.toString() + "/" + new File(mSplits).getName());
                        }
                    }
                } else {
                    APKEditorUtils.copy(AppData.getSourceDir(packageName, context), context.getExternalFilesDir("") + "/" + packageName + ".apk");
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