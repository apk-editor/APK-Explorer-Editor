package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportToStorage extends sExecutor {

    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private final String mSource, mName, mFolder;
    private String mExportPath = null;

    public ExportToStorage(String source, String name, String folder, Context context) {
        mSource = source;
        mName = name;
        mFolder = folder;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.exporting, new File(mSource).getName()));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            sFileUtils.mkdir(new File(Projects.getExportPath(mContext), mFolder));
            mExportPath = Projects.getExportPath(mContext) + "/" + Common.getAppID();
        } else {
            mExportPath = Projects.getExportPath(mContext);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            APKData.saveToDownload(new File(mSource), mName, mContext);
        } else {
            sFileUtils.copy(new File(mSource), new File(mExportPath, mName));
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mContext.getString(R.string.export_complete_message, mExportPath))
                .setPositiveButton(mContext.getString(R.string.cancel), (dialog1, id1) -> {
                }).show();
    }

}