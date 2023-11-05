package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.apk.editor.R;

import java.io.File;
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
    private ProgressDialog mProgressDialog;

    public DeleteFile(File file, Context context) {
        mFile = file;
        mContext = context;
        this.executors = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("StringFormatInvalid")
    private void startBackground() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.deleting, mFile.getName()));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        executors.execute(() -> {

            sFileUtils.delete(mFile);

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

    public void execute() {
        startBackground();
    }

    public abstract void onPostExecute();

}