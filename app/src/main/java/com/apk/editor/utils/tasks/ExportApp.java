package com.apk.editor.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

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

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.exporting, sPackageUtils.getAppName(mPackageName, mContext)));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        if (!APKData.getExportAPKsPath(mContext).exists()) {
            sUtils.mkdir(APKData.getExportAPKsPath(mContext));
        }
    }

    @Override
    public void doInBackground() {
        if (APKData.isAppBundle(sPackageUtils.getSourceDir(mPackageName, mContext))) {
            File mParent = new File(APKData.getExportAPKsPath(mContext) , mPackageName);
            if (mParent.exists()) {
                sUtils.delete(mParent);
            }
            sUtils.mkdir(mParent);
            for (String mSplits : APKData.splitApks(sPackageUtils.getSourceDir(mPackageName, mContext))) {
                if (mSplits.endsWith(".apk")) {
                    sUtils.copy(new File(mSplits), new File(mParent, new File(mSplits).getName()));
                }
            }
        } else {
            sUtils.copy(new File(sPackageUtils.getSourceDir(mPackageName, mContext)), new File(APKData.getExportAPKsPath(mContext),  mPackageName + ".apk"));
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