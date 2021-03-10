package com.apk.editor.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import com.apk.editor.utils.APKEditorUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                APKEditorUtils.saveString("installationStatus", "waiting", this);
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                assert confirmationIntent != null;
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(confirmationIntent);
                } catch (Exception ignored) {
                }
                break;
            case PackageInstaller.STATUS_SUCCESS:
                APKEditorUtils.saveString("installationStatus", "success", this);
                break;
            default:
                APKEditorUtils.saveString("installationStatus", "failed", this);
                break;
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}