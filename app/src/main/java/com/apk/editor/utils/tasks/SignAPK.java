package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.apk.editor.R;
import com.apk.editor.activities.BuildingActivity;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.APKItems;
import com.apk.editor.utils.ZipAlign;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class SignAPK extends sExecutor {

    private final Activity mActivity;
    private final File mRoot;
    private File mBackUpPath = null, mBuildDir = null, mTMPZip = null;

    public SignAPK(File root, Activity activity) {
        mRoot = root;
        mActivity = activity;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        mTMPZip = new File(mActivity.getCacheDir(), "tmp.apk");
        Common.isCancelled(false, mActivity);
        sCommonUtils.saveString("exploringStatus", null, mActivity);
        Intent building = new Intent(mActivity, BuildingActivity.class);
        building.putExtra(BuildingActivity.PACKAGE_NAME_INTENT, getPackageNameOriginal());
        mActivity.startActivity(building);
        Common.setStatus(mActivity.getString(R.string.preparing_apk, mRoot.getName()), mActivity);
        sCommonUtils.saveString("packageName", null, mActivity);

        mBuildDir = new File(mRoot, ".aeeBuild");
        mBackUpPath = new File(mRoot, ".aeeBackup");
    }

    private String getPackageNameOriginal() {
        try {
            JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(mRoot, "/.aeeBackup/appData")));
            return jsonObject.getString("package_name");
        } catch (JSONException ignored) {
        }
        return null;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (mBuildDir.exists()) {
            sFileUtils.delete(mBuildDir);
        }
        sFileUtils.mkdir(mBuildDir);

        if (mTMPZip.exists()) {
            sFileUtils.delete(mTMPZip);
        }

        Common.setStatus(mActivity.getString(R.string.preparing_source), mActivity);

        APKData.prepareSource(mBuildDir, mRoot, mBackUpPath, mActivity);
        if (Common.getError() > 0) {
            return;
        }
        APKEditorUtils.zip(mBuildDir, mTMPZip);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Common.setStatus(mActivity.getString(R.string.zip_aligning), mActivity);
            try {
                RandomAccessFile apkUnaligned = new RandomAccessFile(mTMPZip, "r");
                FileOutputStream apkAligned = new FileOutputStream(new File(mActivity.getCacheDir(), "tmp_zipAligned.apk"));
                ZipAlign.alignZip(apkUnaligned, apkAligned);
                mTMPZip = new File(mActivity.getCacheDir(), "tmp_zipAligned.apk");
                sFileUtils.delete(new File(mActivity.getCacheDir(), "tmp.apk"));
            } catch (IOException ignored) {
            }
        }

        String packageName = getPackageNameOriginal();

        File mParent;
        if (sPackageUtils.isPackageInstalled(packageName, mActivity) && APKData.isAppBundle(sPackageUtils.getSourceDir(packageName, mActivity))) {
            String sourceDirPath = sPackageUtils.getSourceDir(packageName, mActivity);
            mParent = new File(APKData.getExportAPKsPath(mActivity), Objects.requireNonNull(packageName).replace(".apk", "") + "_aee-signed");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            sFileUtils.mkdir(mParent);

            APKItems apkItems = new APKItems(new File(sourceDirPath).getParentFile(), APKData.getBaseAPK(Objects.requireNonNull(new File(sourceDirPath).getParentFile()), mActivity));
            for (String mSplits : APKData.splitApks(sourceDirPath)) {
                if (!new File(mSplits).equals(apkItems.getBaseAPK())) {
                    Common.setStatus(mActivity.getString(R.string.signing, new File(mSplits).getName()), mActivity);
                    APKData.signApks(new File(mSplits), new File(mParent, new File(mSplits).getName()), mActivity);
                }
            }
            Common.setStatus(mActivity.getString(R.string.signing, apkItems.getBaseAPK().getName()), mActivity);

            sCommonUtils.saveString("packageName", sAPKUtils.getPackageName(apkItems.getBaseAPKPath(), mActivity), mActivity);

            APKData.signApks(mTMPZip, new File(mParent, apkItems.getBaseAPK().getName()), mActivity);
        } else {
            mParent = new File(APKData.getExportAPKsPath(mActivity), Objects.requireNonNull(packageName).replace(".apk", "") + "_aee-signed.apk");
            if (mParent.exists()) {
                sFileUtils.delete(mParent);
            }
            Common.setStatus(mActivity.getString(R.string.signing, mParent.getName()), mActivity);

            APKData.signApks(mTMPZip, mParent, mActivity);
            sCommonUtils.saveString("packageName", sAPKUtils.getPackageName(mParent.getAbsolutePath(), mActivity), mActivity);
        }
        if (Common.isCancelled(mActivity)) {
            sFileUtils.delete(mParent);
        }
    }

    @Override
    public void onPostExecute() {
        sFileUtils.delete(mTMPZip);
        sFileUtils.delete(mBuildDir);
        Common.setFinishStatus(mActivity);
        APKExplorer.setSuccessIntent(true, mActivity);
    }

}