package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 19, 2025
 */
public class ProgressDialog extends MaterialAlertDialogBuilder {

    private AlertDialog mAlertDialog = null;
    private final ContentLoadingProgressBar mProgressBar;

    public ProgressDialog(Context context) {
        super(context);

        View progressLayout = View.inflate(context, R.layout.progress_layout, null);
        mProgressBar = progressLayout.findViewById(R.id.progress);

        setView(progressLayout);
        setCancelable(false);
    }

    public int getProgress() {
        return mProgressBar.getProgress();
    }

    public AlertDialog show() {
        if (mAlertDialog == null || !mAlertDialog.isShowing()) {
            mAlertDialog = create();
            mAlertDialog.show();
        }
        return mAlertDialog;
    }

    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void setIndeterminate(boolean b) {
        mProgressBar.setIndeterminate(b);
    }

    public void setMax(int max) {
        setIndeterminate(false);
        mProgressBar.setMax(max);
    }

    public void updateProgress(int progress) {
        if (mProgressBar.getProgress() < mProgressBar.getMax()) {
            mProgressBar.setProgress(mProgressBar.getProgress() + progress);
        } else {
            mProgressBar.setProgress(0);
            setIndeterminate(true);
        }
    }

}