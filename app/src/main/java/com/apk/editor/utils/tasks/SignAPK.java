package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.apk.editor.R;
import com.apk.editor.activities.APKTasksActivity;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.ZipAlign;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class SignAPK extends sExecutor {

    private final Activity mActivity;
    private File mBackUpPath = null, mBuildDir = null, mRootPath = null, mTMPZip = null;
    private final String mPackageName;

    public SignAPK(String packageName, Activity activity) {
        mPackageName = packageName;
        mActivity = activity;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mRootPath = new File(mActivity.getCacheDir(), mPackageName);
        mTMPZip = new File(mActivity.getCacheDir(), "tmp.apk");
        Common.isCancelled(false);
        Common.setStatus(null);
        Intent apkTasks = new Intent(mActivity, APKTasksActivity.class);
        apkTasks.putExtra(APKTasksActivity.PACKAGE_NAME_INTENT, mPackageName);
        apkTasks.putExtra(APKTasksActivity.BUILDING_INTENT, true);
        mActivity.startActivity(apkTasks);
        Common.setStatus(mActivity.getString(R.string.preparing_apk, mPackageName));

        mBuildDir = new File(mRootPath, ".aeeBuild");
        mBackUpPath = new File(mRootPath, ".aeeBackup");
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (mBuildDir.exists()) {
            sFileUtils.delete(mBuildDir);
        }
        sFileUtils.mkdir(mBuildDir);

        if (mTMPZip.exists()) {
            sFileUtils.delete(mTMPZip);
        }

        Common.setStatus(mActivity.getString(R.string.preparing_source));

        APKData.prepareSource(mBuildDir, mRootPath, mBackUpPath, mActivity);
        if (Common.getError() > 0) {
            return;
        }
        APKEditorUtils.zip(mBuildDir, mTMPZip);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Common.setStatus(mActivity.getString(R.string.zip_aligning));
            try {
                RandomAccessFile apkUnaligned = new RandomAccessFile(mTMPZip, "r");
                FileOutputStream apkAligned = new FileOutputStream(new File(mActivity.getCacheDir(), "tmp_zipAligned.apk"));
                ZipAlign.alignZip(apkUnaligned, apkAligned);
                mTMPZip = new File(mActivity.getCacheDir(), "tmp_zipAligned.apk");
                sFileUtils.delete(new File(mActivity.getCacheDir(), "tmp.apk"));
            } catch (IOException ignored) {
            }
        }
        File mParent;
        if (sPackageUtils.isPackageInstalled(mPackageName, mActivity) && APKData.isAppBundle(sPackageUtils
                .getSourceDir(mPackageName, mActivity))) {

            mParent = new File(APKData.getExportAPKsPath(mActivity), mPackageName.replace(".apk", "") + "_aee-signed");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            sFileUtils.mkdir(mParent);
            for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(mPackageName, mActivity))) {
                if (!new File(mSplits).getName().equals("base.apk")) {
                    Common.setStatus(mActivity.getString(R.string.signing, new File(mSplits).getName()));
                    APKData.signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), mActivity);
                }
            }
            Common.setStatus(mActivity.getString(R.string.signing, "base.apk"));

            APKData.signApks(mTMPZip, new File(mParent, "base.apk"), mActivity);
        } else {
            mParent = new File(APKData.getExportAPKsPath(mActivity), mPackageName.replace(".apk", "") + "_aee-signed.apk");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            Common.setStatus(mActivity.getString(R.string.signing, mParent.getName()));

            APKData.signApks(mTMPZip, mParent, mActivity);
        }
        if (Common.isCancelled()) {
            sFileUtils.delete(mParent);
        }
    }

    @Override
    public void onPostExecute() {
        sFileUtils.delete(mTMPZip);
        sFileUtils.delete(mBuildDir);
        APKExplorer.setSuccessIntent(true, mActivity);
    }

}