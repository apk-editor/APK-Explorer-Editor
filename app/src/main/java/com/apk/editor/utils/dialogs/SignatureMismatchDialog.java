package com.apk.editor.utils.dialogs;

import android.content.Context;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class SignatureMismatchDialog extends MaterialAlertDialogBuilder {

    public SignatureMismatchDialog(Context context) {
        super(context);

        setIcon(R.mipmap.ic_launcher);
        setTitle(R.string.app_name);
        setMessage(context.getString(R.string.signature_warning));
        setPositiveButton(R.string.got_it, (dialog, id) ->
                sCommonUtils.saveBoolean("signature_warning", true, context)
        ).show();
    }

}