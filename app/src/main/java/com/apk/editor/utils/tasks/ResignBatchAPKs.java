package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.WindowManager;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 24, 2025
 */
public class ResignBatchAPKs extends sExecutor {

    private final Activity mActivity;
    private final List<String> mPackageNames;
    private ProgressDialog mProgressDialog;

    public ResignBatchAPKs(List<String> packageNames, Activity activity) {
        mPackageNames = packageNames;
        mActivity = activity;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(mActivity.getString(R.string.resigning_apks));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void doInBackground() {
        for (String packageName : mPackageNames) {
            if (APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, mActivity))) {
                File mParent = new File(APKData.getExportAPKsPath(mActivity) , packageName + "_aee-signed");
                if (mParent.exists()) {
                    sFileUtils.delete(mParent);
                }
                sFileUtils.mkdir(mParent);
                for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(packageName, mActivity))) {
                    if (mSplits.endsWith(".apk")) {
                        APKData.signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), mActivity);
                    }
                }
            } else {
                APKData.signApks(new File(sPackageUtils.getSourceDir(packageName, mActivity)), new File(APKData.getExportAPKsPath(mActivity) , packageName + "_aee-signed.apk"), mActivity);
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new MaterialAlertDialogBuilder(mActivity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mActivity.getString(R.string.resigned_apks_path, APKData.getExportAPKsPath(mActivity).getAbsolutePath()))
                .setCancelable(false)
                .setPositiveButton(R.string.cancel, (dialog, id) -> {}
                ).show();
    }

}