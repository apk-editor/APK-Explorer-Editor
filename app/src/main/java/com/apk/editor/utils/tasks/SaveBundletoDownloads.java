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
public class SaveBundletoDownloads extends sExecutor {

    private final boolean mExportOnly;
    private final Context mContext;
    private File mFile;
    private ProgressDialog mProgressDialog;
    private final String mPath;

    public SaveBundletoDownloads(String path, boolean exportOnly, Context context) {
        mPath = path;
        mExportOnly = exportOnly;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(mExportOnly ? R.string.saving : R.string.preparing_bundle));
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
        if (mExportOnly) {
            APKData.saveToDownload(mFile, mFile.getName(), mContext);
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        if (!mExportOnly) {
            APKData.shareFile(mFile, "application/zip", mContext);
        }
    }

}