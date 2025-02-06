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
import java.util.List;

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

    private static long getTotalSize(String path) {
        int totalSize = 0;
        if (path != null) {
            for (String mSplits : APKData.splitApks(path)) {
                File mFile = new File(mSplits);
                if (mFile.exists() && mSplits.endsWith(".apk")) {
                    totalSize += (int) mFile.length();
                }
            }
        } else if (!Common.getAPKList().isEmpty()) {
            for (String string : Common.getAPKList()) {
                if (sFileUtils.exist(new File(string))) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += (int) mFile.length();
                    }
                }
            }
        }
        return totalSize;
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
                Common.getAPKList().clear();
                Intent installer = new Intent(activity, FilePickerActivity.class);
                installer.putExtra(FilePickerActivity.TITLE_INTENT, activity.getString(R.string.select_apk));
                installer.putExtra(FilePickerActivity.PATH_INTENT, mFile.getAbsolutePath());
                activityResultLauncher.launch(installer);
            }
        }.execute();
    }

    public static void installSplitAPKs(boolean exit, List<String> apks, String path, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                sCommonUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.split_apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, path);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final sInstallerParams installParams = sInstallerUtils.makeInstallParams(getTotalSize(path));
                sessionId = sInstallerUtils.runInstallCreate(installParams, activity);
                try {
                    if (path != null) {
                        for (String mSplits : APKData.splitApks(path)) {
                            File mFile = new File(mSplits);
                            if (mFile.exists()) {
                                sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    } else {
                        for (String string : apks) {
                            if (sFileUtils.exist(new File(string))) {
                                File mFile = new File(string);
                                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                    sInstallerUtils.runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                                }
                            }
                        }
                    }
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