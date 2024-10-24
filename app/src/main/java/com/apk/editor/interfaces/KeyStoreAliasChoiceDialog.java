package com.apk.editor.interfaces;

import android.content.Context;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct 10, 2024
 */
public abstract class KeyStoreAliasChoiceDialog {

    private final int mPosition;
    private final MaterialAlertDialogBuilder mDialogBuilder;
    private final String mText;
    private final String[] mSingleChoiceItems;

    public KeyStoreAliasChoiceDialog(String text, String[] singleChoiceItems, int position, Context context) {
        this.mText = text;
        this.mSingleChoiceItems = singleChoiceItems;
        this.mPosition = position;
        this.mDialogBuilder = new MaterialAlertDialogBuilder(context);
    }

    private void startDialog() {
        if (mText != null) {
            mDialogBuilder.setTitle(mText);
        }
        mDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        mDialogBuilder.setPositiveButton(R.string.select, (dialog, id) -> onItemSelected(mPosition)
        );
        mDialogBuilder.setSingleChoiceItems(mSingleChoiceItems, mPosition, (dialog, itemPosition) -> {
            onItemSelected(itemPosition);
            dialog.dismiss();
        }).show();
    }

    public void show() {
        startDialog();
    }

    public abstract void onItemSelected(int position);
}