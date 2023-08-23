package com.apk.editor.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.apk.editor.R;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class TransferApps extends sExecutor {

    private final Context mContext;
    private File mSourceFile;
    private ProgressDialog mProgressDialog;

    public TransferApps(Context context) {
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.transfer_exported_apk));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        File destDir;
        if (sCommonUtils.getString("exportAPKsPath", "externalFiles", mContext).equals("internalStorage")) {
            mSourceFile = mContext.getExternalFilesDir("");
            destDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
        } else {
            destDir = mContext.getExternalFilesDir("");
            mSourceFile = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
        }
        sFileUtils.copyDir(mSourceFile, destDir);
    }

    @Override
    public void onPostExecute() {
        sFileUtils.delete(mSourceFile);
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}