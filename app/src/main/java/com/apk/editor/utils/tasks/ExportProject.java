package com.apk.editor.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.Projects;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportProject extends sExecutor {

    private final Context mContext;
    private final File mFile;
    private ProgressDialog mProgressDialog;
    private final String mName;

    public ExportProject(File file, String name, Context context) {
        mFile = file;
        mName = name;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.exporting, mFile.getName()));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        if (sUtils.exist(new File(Projects.getExportPath(mContext), mName))) {
            sUtils.delete(new File(Projects.getExportPath(mContext), mName));
        }
    }

    @Override
    public void doInBackground() {
        sUtils.copyDir(mFile, new File(Projects.getExportPath(mContext), mName));
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
    }

}