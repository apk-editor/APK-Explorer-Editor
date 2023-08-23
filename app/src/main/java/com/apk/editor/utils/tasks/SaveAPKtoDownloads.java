package com.apk.editor.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class SaveAPKtoDownloads extends sExecutor {

    private final Context mContext;
    private final File mFile;
    private ProgressDialog mProgressDialog;

    public SaveAPKtoDownloads(File file, Context context) {
        mFile = file;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.saving));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void doInBackground() {
        APKData.saveToDownload(mFile, mFile.getName(), mContext);
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}