package com.apk.editor.interfaces;

import android.content.Context;
import android.text.Editable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Jan 17, 2023
 */
public abstract class EditTextInterface {

    private final Context mContext;
    private final MaterialAlertDialogBuilder mDialogBuilder;
    private final String mText, mTitle;

    public EditTextInterface(String text, String title, Context context) {
        this.mText = text;
        this.mTitle = title;
        this.mContext = context;
        this.mDialogBuilder = new MaterialAlertDialogBuilder(context);
    }

    private void startDialog() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(75, 75, 75, 75);
        final AppCompatEditText editText = new AppCompatEditText(mContext);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (mText != null) {
            editText.append(mText);
        }
        editText.setSingleLine(true);
        editText.requestFocus();
        layout.addView(editText);

        if (mTitle != null) {
            mDialogBuilder.setTitle(mTitle);
            mDialogBuilder.setIcon(R.mipmap.ic_launcher);
        }
        mDialogBuilder.setView(layout);
        mDialogBuilder.setIcon(R.mipmap.ic_launcher);
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