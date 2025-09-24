package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExportToStorage extends sExecutor {

    private final Context mContext;
    private final File mSourceFile;
    private final List<File> mSourceFiles;
    private final String mFolder;
    private ProgressDialog mProgressDialog;

    public ExportToStorage(File sourceFile, List<File> sourceFiles, String folder, Context context) {
        mSourceFile = sourceFile;
        mSourceFiles = sourceFiles;
        mFolder = folder;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.exporting, mSourceFile != null && mSourceFile.exists() ? mSourceFile.getName() : ""));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void doInBackground() {
        File mExportPath = new File(APKData.getExportPath(mContext), mFolder);
        if (!mExportPath.exists()) {
            sFileUtils.mkdir(mExportPath);
        }
        if (mSourceFiles != null && !mSourceFiles.isEmpty()) {
            for (File file : mSourceFiles) {
                if (file.exists()) {
                    sFileUtils.copy(file, new File(mExportPath, file.getName()));
                }
            }
        } else {
            sFileUtils.copy(mSourceFile, new File(mExportPath, mSourceFile.getName()));
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        new MaterialAlertDialogBuilder(mContext)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(mContext.getString(R.string.export_complete_message, "Download > AEE > " + mFolder))
                .setPositiveButton(mContext.getString(R.string.cancel), (dialog1, id1) -> {
                }).show();
    }

}