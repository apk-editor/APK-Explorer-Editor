package com.apk.editor.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apk.editor.activities.InstallerActivity;
import com.apk.editor.services.InstallerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        for (String mSplits : APKData.splitApks(path)) {
            File mFile = new File(mSplits);
            if (mFile.exists() && mSplits.endsWith(".apk")) {
                totalSize += mFile.length();
            }
        }
        return totalSize;
    }

    private static class InstallParams {
        PackageInstaller.SessionParams sessionParams;
    }

    public static void installSplitAPKs(String path, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                APKEditorUtils.saveString("installationStatus", "waiting", activity);
                Intent installIntent = new Intent(activity, InstallerActivity.class);
                installIntent.putExtra(InstallerActivity.PATH_INTENT, path);
                activity.startActivity(installIntent);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected Void doInBackground(Void... voids) {
                int sessionId;
                final InstallParams installParams = makeInstallParams(getTotalSize(path));
                sessionId = runInstallCreate(installParams, activity);
                try {
                    for (String mSplits : APKData.splitApks(path)) {
                        File mFile = new File(mSplits);
                        if (mFile.exists()) {
                            runInstallWrite(mFile.length(), sessionId, mFile.getName(), mFile.toString(), activity);
                        }
                    }
                } catch (NullPointerException ignored) {}
                doCommitSession(sessionId, activity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

}