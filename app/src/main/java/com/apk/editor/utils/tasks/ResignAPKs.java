package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.WindowManager;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ResignAPKs extends sExecutor {

    private final Activity mActivity;
    private final boolean mExit, mInstall;
    private final String mPackageName;
    private File mParent = null;
    private ProgressDialog mProgressDialog;
    private String mDetectedPackageName = null;

    public ResignAPKs(String packageName, boolean install, boolean exit, Activity activity) {
        mPackageName = packageName;
        mInstall = install;
        mExit = exit;
        mActivity = activity;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(mPackageName != null ? mActivity.getString(R.string.signing, sPackageUtils.getAppName(
                mPackageName, mActivity)) : mActivity.getString(R.string.resigning_apks));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void doInBackground() {
        if (mPackageName == null) {
            // Find package name from the selected APK's
            mDetectedPackageName = APKData.findPackageName(mActivity);
        }

        if (mPackageName != null && sPackageUtils.isPackageInstalled(mPackageName, mActivity)) {
            Common.getAPKList().clear();
            if (APKData.isAppBundle(sPackageUtils.getSourceDir(mPackageName, mActivity))) {
                Common.getAPKList().addAll(APKData.splitApks(sPackageUtils.getSourceDir(mPackageName, mActivity)));
            } else {
                Common.getAPKList().add(sPackageUtils.getSourceDir(mPackageName, mActivity));
            }
        }

        if (Common.getAPKList().size() > 1) {
            if (mInstall) {
                mParent = new File(mActivity.getExternalCacheDir(), "aee-signed");
            } else {
                mParent = new File(APKData.getExportAPKsPath(mActivity), mPackageName != null ? mPackageName : mDetectedPackageName + "_aee-signed");
            }
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            sFileUtils.mkdir(mParent);
            for (String mSplits : Common.getAPKList()) {
                APKData.signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), mActivity);
            }
        } else {
            if (mInstall) {
                mParent = new File(mActivity.getCacheDir(), "aee-signed.apk");
            } else {
                mParent = new File(APKData.getExportAPKsPath(mActivity), mPackageName != null ? mPackageName : mDetectedPackageName + "_aee-signed.apk");
            }
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            APKData.signApks(new File(Common.getAPKList().get(0)), mParent, mActivity);
        }

        APKExplorer.setSuccessIntent(false, mActivity);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        if (mDetectedPackageName == null && mPackageName == null) {
            sCommonUtils.snackBar(mActivity.findViewById(android.R.id.content), mActivity.getString(R.string.installation_status_bad_apks)).show();
        } else {
            if (mPackageName == null) {
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (mInstall) {
                    if (Common.getAPKList().size() > 1) {
                        List<String> signedAPKs = new ArrayList<>();
                        for (File apkFile : Objects.requireNonNull(mParent.listFiles())) {
                            signedAPKs.add(apkFile.getAbsolutePath());
                        }
                        SplitAPKInstaller.installSplitAPKs(mExit, signedAPKs, null, mActivity);
                    } else {
                        SplitAPKInstaller.installAPK(mExit, mParent, mActivity);
                    }
                } else {
                    new MaterialAlertDialogBuilder(mActivity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(mActivity.getString(R.string.resigned_apks_path, mParent.getAbsolutePath()))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                                        if (mExit) {
                                            mActivity.finish();
                                        }
                                    }
                            ).show();
                }
            }
        }
    }

}