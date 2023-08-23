package com.apk.editor.utils.dialogs;

import android.app.Activity;

import com.apk.editor.R;
import com.apk.editor.utils.tasks.ExportApp;
import com.apk.editor.utils.tasks.ResignAPKs;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class ExportOptionsDialog extends sSingleItemDialog {

    private final Activity mActivity;
    private final boolean mExit;
    private final String mPackageName;


    public ExportOptionsDialog(String packageName, boolean exit, Activity activity) {
        super(0, null, new String[] {
                activity.getString(R.string.export_storage),
                activity.getString(R.string.export_resign)
        }, activity);
        mPackageName = packageName;
        mExit = exit;
        mActivity = activity;
    }

    @Override
    public void onItemSelected(int position) {
        if (position == 0) {
            new ExportApp(mPackageName, mActivity).execute();
        } else {
            if (!sCommonUtils.getBoolean("firstSigning", false, mActivity)) {
                new SigningOptionsDialog(mPackageName, mExit, mActivity).show();
            } else {
                new ResignAPKs(mPackageName, false, mExit, mActivity).execute();
            }
        }
    }

}