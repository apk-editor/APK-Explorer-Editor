package com.apk.editor.interfaces;

import android.app.Activity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct 10, 2024
 */
public abstract class KeyStoreVerifierInterface {

    private final Activity mActivity;
    private final MaterialAlertDialogBuilder mDialogBuilder;
    private final String mText;

    public KeyStoreVerifierInterface(String text, Activity activity) {
        this.mText = text;
        this.mActivity = activity;
        this.mDialogBuilder = new MaterialAlertDialogBuilder(activity);
    }

    private void startDialog() {
        LayoutInflater mLayoutInflator = LayoutInflater.from(mActivity);
        View layout = mLayoutInflator.inflate(R.layout.layout_keystoreverifier, null);
        AppCompatAutoCompleteTextView editText = layout.findViewById(R.id.text);

        editText.setSingleLine(true);
        editText.requestFocus();

        mDialogBuilder.setView(layout);
        if (mText != null) {
            mDialogBuilder.setTitle(mText);
        }
        mDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        mDialogBuilder.setPositiveButton(R.string.ok, (dialog, id) ->
                positiveButtonLister(editText.getText())
        ).show();
    }

    public void show() {
        startDialog();
    }

    public abstract void positiveButtonLister(Editable s);

}