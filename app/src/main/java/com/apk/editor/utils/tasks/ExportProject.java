package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.dialogs.ProgressDialog;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

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

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.exporting, mFile.getName()));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        File outputDir = new File(APKData.getExportPath(mContext), mName);
        if (outputDir.exists()) {
            sFileUtils.delete(outputDir);
        }
        sFileUtils.mkdir(outputDir);

        File[] files = mFile.listFiles();
        mProgressDialog.setMax(Objects.requireNonNull(files).length);
        for (File file : files) {
            if (file.isDirectory()) {
                sFileUtils.copyDir(file, new File(outputDir, file.getName()));
            } else {
                sFileUtils.copy(file, new File(outputDir, file.getName()));
            }
            mProgressDialog.updateProgress(1);
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