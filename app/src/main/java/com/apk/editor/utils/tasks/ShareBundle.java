package com.apk.editor.utils.tasks;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ShareBundle extends sExecutor {

    private final Context mContext;
    private File mFile;
    private ProgressDialog mProgressDialog;
    private final String mPath;

    public ShareBundle(String path, Context context) {
        mPath = path;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.preparing_bundle));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        mFile = new File(mContext.getExternalFilesDir("APK"), new File(mPath).getName() + ".xapk");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void doInBackground() {
        if (mFile.exists()) {
            sFileUtils.delete(mFile);
        }
        APKEditorUtils.zip(new File(mPath), mFile);
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        APKData.shareFile(mFile, "application/zip", mContext);
    }

}