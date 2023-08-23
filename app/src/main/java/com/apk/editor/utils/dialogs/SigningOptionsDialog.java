package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.utils.tasks.ResignAPKs;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class SigningOptionsDialog extends sSingleItemDialog {

    private final Context mContext;
    private final boolean mExit;
    private final String mPackageName;

    public SigningOptionsDialog(String packageName, boolean exit, Context context) {
        super(0, null, new String[] {
                context.getString(R.string.signing_default),
                context.getString(R.string.signing_custom)
        }, context);
        mPackageName = packageName;
        mExit = exit;
        mContext = context;
    }

    @Override
    public void onItemSelected(int position) {
        sCommonUtils.saveBoolean("firstSigning", true, mContext);
        if (position == 0) {
            new ResignAPKs(mPackageName,false, mExit, (Activity) mContext).execute();
        } else {
            Intent signing = new Intent(mContext, APKSignActivity.class);
            mContext.startActivity(signing);
        }
    }

}