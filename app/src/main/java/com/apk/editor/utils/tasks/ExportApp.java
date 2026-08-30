package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.Serializables.BatchItems;
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

    private boolean isEmpty = true;
    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private final List<BatchItems> mPackageNames;

    public ExportApp(List<BatchItems> packageNames, Context context) {
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
        if (!APKData.getExportPath(mContext).exists()) {
            sFileUtils.mkdir(APKData.getExportPath(mContext));
        }
    }

    @Override
    public void doInBackground() {
        for (BatchItems batchItems : mPackageNames) {
            if (batchItems.isSelected()) {
                if (isEmpty) isEmpty = false;
                String packageName = batchItems.getPackageName();
                if (APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, mContext))) {
                    File mParent = new File(APKData.getExportPath(mContext), packageName);
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
                    sFileUtils.copy(new File(sPackageUtils.getSourceDir(packageName, mContext)), new File(APKData.getExportPath(mContext), packageName + ".apk"));
                }
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
        if (isEmpty) return;
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mContext.getString(R.string.exported_apks_path,
                        "Download > AEE"))
                .setCancelable(false)
                .setPositiveButton(R.string.cancel, (dialog, id) -> {}
                ).show();
    }

}