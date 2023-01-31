package com.apk.editor.utils.tasks;

import android.app.Activity;
import android.app.ProgressDialog;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

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
        mProgressDialog.setMessage(mActivity.getString(R.string.clearing_cache_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        sUtils.delete(mActivity.getCacheDir());
        sUtils.delete(mActivity.getFilesDir());
        if (APKEditorUtils.isFullVersion(mActivity) && AppSettings.isCustomKey(mActivity)) {
            sUtils.saveString("PrivateKey", null, mActivity);
            sUtils.delete(new File(mActivity.getFilesDir(), "signing/APKEditor.pk8"));
            sUtils.saveString("X509Certificate", null, mActivity);
            sUtils.delete(new File(mActivity.getFilesDir(), "signing/APKEditorCert"));
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