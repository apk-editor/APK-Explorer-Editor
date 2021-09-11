package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInstaller;

import com.apk.editor.R;
import com.apk.editor.activities.InstallerActivity;
import com.apk.editor.activities.InstallerFilePickerActivity;
import com.apk.editor.services.InstallerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class SplitAPKInstaller {

    private static int runInstallCreate(InstallParams installParams, Activity activity) {
        return doCreateSession(installParams.sessionParams, activity);
    }

    private static int doCreateSession(PackageInstaller.SessionParams params, Activity activity) {
        int sessionId = 0 ;
        try {
            sessionId = getPackageInstaller(activity).createSession(params);
        } catch (IOException ignored) {
        }
        return sessionId;
    }

    private static void runInstallWrite(long size, int sessionId, String splitName, String path, Activity activity) {
        long sizeBytes;
        sizeBytes = size;
        doWriteSession(sessionId, path, sizeBytes, splitName, activity);
    }

    private static void doWriteSession(int sessionId, String path, long sizeBytes, String splitName, Activity activity) {
        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            session = getPackageInstaller(activity).openSession(sessionId);
            if (path != null) {
                in = new FileInputStream(path);
            }
            out = session.openWrite(splitName, 0, sizeBytes);
            byte[] buffer = new byte[65536];
            int c;
            assert in != null;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } catch (IOException ignored) {
        } finally {
            try {
                assert out != null;
                out.close();
                assert in != null;
                in.close();
                session.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void doCommitSession(int sessionId, Activity activity) {
        PackageInstaller.Session session = null;
        try {
            try {
                session = getPackageInstaller(activity).openSession(sessionId);
            } catch (IOException ignored) {
            }
            Intent callbackIntent = new Intent(activity, InstallerService.class);
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getService(activity, 0, callbackIntent, 0);
            assert session != null;
            session.commit(pendingIntent.getIntentSender());
            session.close();
        } finally {
            assert session != null;
            session.close();
        }
    }

    private static InstallParams makeInstallParams(long totalSize) {
        final PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        final InstallParams params = new InstallParams();
        params.sessionParams = sessionParams;
        sessionParams.setSize(totalSize);
        return params;
    }

    private static PackageInstaller getPackageInstaller(Activity activity) {
        return AppData.getPackageManager(activity).getPackageInstaller();
    }

    private static long getTotalSize(String path) {
        int totalSize = 0;
        if (path != null) {
            for (String mSplits : APKData.splitApks(path)) {
                File mFile = new File(mSplits);
                if (mFile.exists() && mSplits.endsWith(".apk")) {
                    totalSize += mFile.length();
                }
            }
        } else if (Common.getAPKList().size() > 0) {
            for (String string : Common.getAPKList()) {
                if (APKEditorUtils.exist(string)) {
                    File mFile = new File(string);
                    if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                        totalSize += mFile.length();
                    }
                }
            }
        }
        return totalSize;
    }

    private static class InstallParams {
        PackageInstaller.SessionParams sessionParams;
    }

    public static void handleAppBundle(String path, Activity activity) {
        new AsyncTasks() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.preparing_bundle_install, new File(path).getName()));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                APKEditorUtils.delete(activity.getCacheDir().getPath() + "/splits");
            }

            @Override
            public void doInBackground() {
                if (path.endsWith(".apks")) {
                    APKEditorUtils.unzip(path,  activity.getCacheDir().getPath());
                } else if (path.endsWith(".xapk") || path.endsWith(".apkm")) {
                    APKEditorUtils.unzip(path,  activity.getCacheDir().getPath() + "/splits");
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Common.getAPKList().clear();
                Common.setPath(activity.getCacheDir().getPath() + "/splits");
                Intent installer = new Intent(activity, InstallerFilePickerActivity.class);
                installer.putExtra(InstallerFilePickerActivity.TITLE_INTENT, activity.getString(R.string.select_apk));
                activity.startActivity(installer);
            }
        }.execute();
    }

    public static void installSplitAPKs(List<String> apks, String path, Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                APKEditorUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.split_apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, path);
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final InstallParams installParams = makeInstallParams(getTotalSize(path));
                sessionId = runInstallCreate(installParams, activity);
                try {
                    if (path != null) {
                        for (String mSplits : APKData.splitApks(path)) {
                            File mFile = new File(mSplits);
                            if (mFile.exists()) {
                                runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                            }
                        }
                    } else {
                        for (String string : apks) {
                            if (APKEditorUtils.exist(string)) {
                                File mFile = new File(string);
                                if (mFile.exists() && mFile.getName().endsWith(".apk")) {
                                    runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                                }
                            }
                        }
                    }
                } catch (NullPointerException ignored) {}
                doCommitSession(sessionId, activity);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

    public static void installAPK(File APK, Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                APKEditorUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.HEADING_INTENT, activity.getString(R.string.apk_installer));
                installIntent.putExtra(InstallerActivity.PATH_INTENT, APK.getAbsolutePath());
                activity.startActivity(installIntent);
            }

            @Override
            public void doInBackground() {
                int sessionId;
                final InstallParams installParams = makeInstallParams(APK.length());
                sessionId = runInstallCreate(installParams, activity);
                try {
                    runInstallWrite(APK.length(), sessionId, APK.getName(), APK.getAbsolutePath(), activity);
                } catch (NullPointerException ignored) {}
                doCommitSession(sessionId, activity);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

}