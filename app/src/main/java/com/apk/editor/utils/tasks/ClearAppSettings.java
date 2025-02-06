package com.apk.editor.utils.tasks;

import android.app.Activity;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ClearAppSettings extends sExecutor {

    private final Activity mActivity;
    private ProgressDialog mProgressDialog;

    public ClearAppSettings(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(mActivity.getString(R.string.clearing_cache_message));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sFileUtils.delete(mActivity.getCacheDir());
        sFileUtils.delete(mActivity.getFilesDir());
        if (APKEditorUtils.isFullVersion(mActivity) && AppSettings.isCustomKey(mActivity)) {
            sCommonUtils.saveString("PrivateKey", null, mActivity);
            sFileUtils.delete(new File(mActivity.getFilesDir(), "signing/APKEditor.pk8"));
            sCommonUtils.saveString("X509Certificate", null, mActivity);
            sFileUtils.delete(new File(mActivity.getFilesDir(), "signing/APKEditorCert"));
        }
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        mActivity.finish();
    }

}