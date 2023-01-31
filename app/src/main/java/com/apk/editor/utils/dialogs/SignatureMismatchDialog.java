package com.apk.editor.utils.dialogs;

import android.content.Context;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class SignatureMismatchDialog {

    private final MaterialAlertDialogBuilder mDialogBuilder;

    public SignatureMismatchDialog(Context context) {
        mDialogBuilder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(context.getString(R.string.signature_warning))
                .setPositiveButton(R.string.got_it, (dialog, id) ->
                        sUtils.saveBoolean("signature_warning", true, context));
    }

    public void show() {
        mDialogBuilder.show();
    }

}