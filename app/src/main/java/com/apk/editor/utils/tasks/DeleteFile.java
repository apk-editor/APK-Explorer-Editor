package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.apk.editor.R;
import com.apk.editor.utils.dialogs.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 05, 2023
 */
public abstract class DeleteFile {

    private final Context mContext;
    private final ExecutorService executors;
    private final File mFile;
    private final List<File> mFiles;
    private final String mBackupFilePath;
    private ProgressDialog mProgressDialog;

    public DeleteFile(File file, List<File> files, String backupFilePath, Context context) {
        mFile = file;
        mFiles = files;
        mBackupFilePath = backupFilePath;
        mContext = context;
        this.executors = Executors.newSingleThreadExecutor();
    }

    private boolean isSmaliEdited() {
        if (mFiles != null && !mFiles.isEmpty()) {
            for (File file : mFiles) {
                return file.getName().endsWith(".smali");
            }
        } else if (mFile.exists()) {
            return mFile.getName().endsWith(".smali");
        }
        return false;
    }

    @SuppressLint("StringFormatInvalid")
    public void execute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.deleting, mFiles != null && !mFiles.isEmpty() ?
                mContext.getString(R.string.delete_selected_files) : mFile.getName()));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        executors.execute(() -> {
            if (mFiles != null && !mFiles.isEmpty()) {
                for (File file : mFiles) {
                    sFileUtils.delete(file);
                }
            } else if (mFile.exists()) {
                sFileUtils.delete(mFile);
            }

            if (isSmaliEdited()) {
                try {
                    JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(mBackupFilePath)));
                    jsonObject.put("smali_edited", true);
                    sFileUtils.create(jsonObject.toString(), new File(mBackupFilePath));
                } catch (JSONException ignored) {
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {

                onPostExecute();

                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (!executors.isShutdown()) executors.shutdown();
            });
        });
    }

    public abstract void onPostExecute();

}