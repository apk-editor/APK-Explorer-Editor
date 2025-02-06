package com.apk.editor.utils.tasks;

import android.content.Context;
import android.os.Environment;

import com.apk.editor.R;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;
import java.util.Objects;

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
        mProgressDialog.setTitle(mContext.getString(R.string.transfer_exported_apk));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
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
        sFileUtils.copyDir(mSourceFile, Objects.requireNonNull(destDir));
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