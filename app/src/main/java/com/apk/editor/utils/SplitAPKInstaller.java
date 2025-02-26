package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.apk.editor.R;
import com.apk.editor.activities.FilePickerActivity;
import com.apk.editor.activities.InstallerActivity;
import com.apk.editor.services.InstallerService;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerParams;
import in.sunilpaulmathew.sCommon.InstallerUtils.sInstallerUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class SplitAPKInstaller {

    private static Intent getCallbackIntent(Context context) {
        return new Intent(context, InstallerService.class);
    }

    public static void handleAppBundle(ActivityResultLauncher<Intent> activityResultLauncher, String path, Activity activity) {
        new sExecutor() {
            private final File mFile = new File(activity.getExternalCacheDir(), "splits");
            private ProgressDialog mProgressDialog;

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setTitle(activity.getString(R.string.preparing_bundle_install, new File(path).getName()));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();

                if (sFileUtils.exist(mFile)) {
                    sFileUtils.delete(mFile);
                }
            }

            @Override
            public void doInBackground() {
                APKEditorUtils.unzip(path, mFile.getAbsolutePath());
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Intent installer = new Intent(activity, FilePickerActivity.class);
                installer.putExtra(FilePickerActivity.TITLE_INTENT, activity.getString(R.string.select_apk));
                installer.putExtra(FilePickerActivity.PATH_INTENT, mFile.getAbsolutePath());
                activityResultLauncher.launch(installer);
            }
        }.execute();
    }

    public static void installSplitAPKs(String path, Activity activity) {
        installSplitAPKs(false, null, new File(path), activity);
    }

    public static void installSplitAPKs(boolean exit, List<String> apks, Activity activity) {
        installSplitAPKs(exit, apks, null, activity);
    }

    public static void installSplitAPKs(boolean exit, List<String> apks, File apkFile, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                sCommonUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.split_apk_installer));
                if (apks != null) {
                    installIntent.putStringArrayListExtra(InstallerActivity.APK_LIST_INTENT, getAPKList());
                } else if (apkFile != null) {
                    installIntent.putExtra(InstallerActivity.PATH_INTENT, apkFile.getAbsolutePath());
                }
                activity.startActivity(installIntent);
            }

            private ArrayList<String> getAPKList() {
                ArrayList<String> arrayList = new ArrayList<>(apks.size());
                arrayList.addAll(apks);
                return arrayList;
            }

            @Override
            public void doInBackground() {
                List<String> apkList;
                int sessionId = 0;
                if (apkFile != null && apkFile.exists()) {
                    apkList = APKData.splitApks(apkFile);
                    sessionId = sInstallerUtils.runInstallCreate(sInstallerUtils.makeInstallParams(getTotalSize(apkList)), activity);
                    try {
                        for (String mSplits : Objects.requireNonNull(apkList)) {
                            File mFile = new File(mSplits);
                            if (mFile.exists()) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    } catch (NullPointerException ignored) {}
                } else if (apks != null && !apks.isEmpty()) {
                    sessionId = sInstallerUtils.runInstallCreate(sInstallerUtils.makeInstallParams(getTotalSize(apks)), activity);
                    for (String string : apks) {
                        if (sFileUtils.exist(new File(string))) {
                            File mFile = new File(string);
                            if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    }
                }


                sInstallerUtils.doCommitSession(sessionId, getCallbackIntent(activity), activity);
            }

            private long getTotalSize(List<String> apkList) {
                int totalSize = 0;
                if (apkList != null) {
                    for (String mSplits : apkList) {
                        File mFile = new File(mSplits);
                        if (mFile.exists() && mSplits.endsWith(".apk")) {
                            totalSize += (int) mFile.length();
                        }
                    }
                }
                return totalSize;
            }

            @Override
            public void onPostExecute() {
                if (exit) {
                    activity.finish();
                }
            }
        }.execute();
    }

    public static void installAPK(File APK, Activity activity) {
        installAPK(false, APK, activity);
    }

    public static void installAPK(boolean exit, File APK, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                sCommonUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, APK.getAbsolutePath());
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(APK.length());
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    sInstallerUtils.runInstallWrite(APK.length(), sessionId, APK.getName(), APK.getAbsolutePath(), activity);
                } catch (NullPointerException ignored) {}
                sInstallerUtils.doCommitSession(sessionId, getCallbackIntent(activity), activity);
            }

            @Override
            public void onPostExecute() {
                if (exit) {
                    activity.finish();
                }
            }
        }.execute();
    }

}