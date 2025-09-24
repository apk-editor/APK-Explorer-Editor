package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 19, 2025
 */
public class ProgressDialog {

    private static AlertDialog mAlertDialog = null;
    private static ContentLoadingProgressBar mProgressBar = null;
    private static MaterialAlertDialogBuilder mDialogBuilder = null;

    public ProgressDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View progressLayout = layoutInflater.inflate(R.layout.progress_layout, null);
        mProgressBar = progressLayout.findViewById(R.id.progress);
        mDialogBuilder = new MaterialAlertDialogBuilder(context)
                .setView(progressLayout)
                .setCancelable(false);
    }

    public int getProgress() {
        return mProgressBar.getProgress();
    }

    public void show() {
        mAlertDialog = mDialogBuilder.create();
        mAlertDialog.show();
    }

    public void dismiss() {
        mAlertDialog.dismiss();
    }

    public void setIcon(int resourceID) {
        mDialogBuilder.setIcon(resourceID);
    }

    public void setIcon(Drawable icon) {
        mDialogBuilder.setIcon(icon);
    }

    public void setTitle(int resourceID) {
        mDialogBuilder.setTitle(resourceID);
    }

    public void setTitle(CharSequence charSequence) {
        mDialogBuilder.setTitle(charSequence);
    }

    public void setIndeterminate(boolean b) {
        mProgressBar.setIndeterminate(b);
    }

    public void setProgress(int progress) {
        setIndeterminate(false);
        mProgressBar.setProgress(progress);
    }

    public void setMax(int max) {
        setIndeterminate(false);
        mProgressBar.setMax(max);
    }

}