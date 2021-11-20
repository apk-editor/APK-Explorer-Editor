package com.apk.editor.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import in.sunilpaulmathew.sCommon.Utils.sInstallerUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstallerUtils.setStatus(intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999), intent, this);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}