package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.utils.tasks.ResignAPKs;
import com.apk.editor.utils.tasks.ResignBatchAPKs;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 24, 2025
 */
public class BatchSigningOptionsDialog extends sSingleItemDialog {

    private final Context mContext;
    private final List<String> mPackageNames;

    public BatchSigningOptionsDialog(List<String> packageNames, Context context) {
        super(0, null, new String[] {
                context.getString(R.string.signing_default),
                context.getString(R.string.signing_custom)
        }, context);
        mPackageNames = packageNames;
        mContext = context;
    }

    @Override
    public void onItemSelected(int position) {
        sCommonUtils.saveBoolean("firstSigning", true, mContext);
        if (position == 0) {
            new ResignBatchAPKs(mPackageNames, (Activity) mContext).execute();
        } else {
            Intent signing = new Intent(mContext, APKSignActivity.class);
            mContext.startActivity(signing);
        }
    }

}