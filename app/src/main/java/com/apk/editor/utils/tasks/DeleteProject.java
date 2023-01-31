package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.Common;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class DeleteProject extends sExecutor {

    private final Context mContext;
    private final File mFile;
    private ProgressDialog mProgressDialog;

    public DeleteProject(File file, Context context) {
        mFile = file;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.deleting, mFile.getName()));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sUtils.delete(mFile);
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        Common.isReloading(true);
    }

}