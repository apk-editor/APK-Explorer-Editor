package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;

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
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportApp extends sExecutor {

    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private final List<String> mPackageNames;

    public ExportApp(List<String> packageNames, Context context) {
        mPackageNames = packageNames;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.exporting_batch));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        if (!APKData.getExportAPKsPath(mContext).exists()) {
            sFileUtils.mkdir(APKData.getExportAPKsPath(mContext));
        }
    }

    @Override
    public void doInBackground() {
        for (String packageName : mPackageNames) {
            if (APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, mContext))) {
                File mParent = new File(APKData.getExportAPKsPath(mContext) , packageName);
                if (mParent.exists()) {
                    sFileUtils.delete(mParent);
                }
                sFileUtils.mkdir(mParent);
                for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(packageName, mContext))) {
                    if (mSplits.endsWith(".apk")) {
                        sFileUtils.copy(new File(mSplits), new File(mParent, new File(mSplits).getName()));
                    }
                }
            } else {
                sFileUtils.copy(new File(sPackageUtils.getSourceDir(packageName, mContext)), new File(APKData.getExportAPKsPath(mContext),  packageName + ".apk"));
            }
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mContext.getString(R.string.exported_apks_path, APKData.getExportAPKsPath(mContext).getAbsolutePath()))
                .setCancelable(false)
                .setPositiveButton(R.string.cancel, (dialog, id) -> {}
                ).show();
    }

}