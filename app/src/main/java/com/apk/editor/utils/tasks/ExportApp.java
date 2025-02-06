package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportApp extends sExecutor {

    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private final String mPackageName;

    public ExportApp(String packageName, Context context) {
        mPackageName = packageName;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.exporting, sPackageUtils.getAppName(mPackageName, mContext)));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        if (!APKData.getExportAPKsPath(mContext).exists()) {
            sFileUtils.mkdir(APKData.getExportAPKsPath(mContext));
        }
    }

    @Override
    public void doInBackground() {
        if (APKData.isAppBundle(sPackageUtils.getSourceDir(mPackageName, mContext))) {
            File mParent = new File(APKData.getExportAPKsPath(mContext) , mPackageName);
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            sFileUtils.mkdir(mParent);
            for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(mPackageName, mContext))) {
                if (mSplits.endsWith(".apk")) {
                    sFileUtils.copy(new File(mSplits), new File(mParent, new File(mSplits).getName()));
                }
            }
        } else {
            sFileUtils.copy(new File(sPackageUtils.getSourceDir(mPackageName, mContext)), new File(APKData.getExportAPKsPath(mContext),  mPackageName + ".apk"));
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}