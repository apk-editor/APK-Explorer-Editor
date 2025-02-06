package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class DeleteProject extends sExecutor {

    private final Activity mActivity;
    private final boolean mSetSuccess;
    private final File mFile;
    private ProgressDialog mProgressDialog;

    public DeleteProject(File file, Activity activity, boolean setSuccess) {
        mFile = file;
        mActivity = activity;
        mSetSuccess = setSuccess;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(mActivity.getString(R.string.deleting, mFile.getName()));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sFileUtils.delete(mFile);
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        if (mSetSuccess) {
            APKExplorer.setSuccessIntent(true, mActivity);
        }
    }

}