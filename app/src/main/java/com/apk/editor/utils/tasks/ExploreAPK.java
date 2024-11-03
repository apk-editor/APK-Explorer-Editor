package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.activities.APKTasksActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.DexToSmali;
import com.apk.editor.utils.ExternalAPKData;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExploreAPK extends sExecutor {

    private final Context mContext;
    private File mBackUpPath, mExplorePath;
    private File mAPKFile;
    private final int mOptions;
    private final String mPackageName;
    private final Uri mUri;

    public ExploreAPK(String packageName, File apkFile, Uri uri, int options, Context context) {
        mPackageName = packageName;
        mAPKFile = apkFile;
        mUri = uri;
        mOptions = options;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onPreExecute() {
        Common.isBuilding(false);
        Common.isCancelled(false);
        Common.setFinishStatus(false);
        if (mUri != null) {
            String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mContext, mUri)).getName();
            mAPKFile = new File(mContext.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
            sFileUtils.copy(mUri, mAPKFile, mContext);
        }
        Common.setAppID(mPackageName != null ? mPackageName : mAPKFile.getName());
        mExplorePath = new File(mContext.getCacheDir().getPath(), mPackageName != null ? mPackageName : mAPKFile.getName());
        mBackUpPath = new File(mExplorePath, ".aeeBackup");
        Common.setPath(mExplorePath.getAbsolutePath());
        if (!mExplorePath.exists()) {
            Common.setFinishStatus(false);
            Common.setStatus(null);
            Intent apkTasks = new Intent(mContext, APKTasksActivity.class);
            mContext.startActivity(apkTasks);
            Common.setStatus(mContext.getString(R.string.exploring, mPackageName != null ? sPackageUtils.getAppName(mPackageName, mContext) : mAPKFile.getName()));
        } else if (!sFileUtils.exist(new File(mBackUpPath, "appData"))) {
            sFileUtils.delete(mExplorePath);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (!mExplorePath.exists()) {
            sFileUtils.mkdir(mExplorePath);
            sFileUtils.mkdir(mBackUpPath);

            ExternalAPKData.generateAppDetails(mPackageName, mAPKFile, mBackUpPath, mContext);

            APKEditorUtils.unzip(mPackageName != null ? sPackageUtils.getSourceDir(mPackageName, mContext) : mAPKFile.getAbsolutePath(), mExplorePath.getAbsolutePath());
            // Decompile dex file(s)
            if (sCommonUtils.getString("decompileSetting", null, mContext) != null && sCommonUtils.getString("decompileSetting",
                    null, mContext).equals(mContext.getString(R.string.explore_options_full)) || mOptions == 1) {
                for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                    if (files.getName().startsWith("classes") && files.getName().endsWith(".dex") && !Common.isCancelled()) {
                        sFileUtils.mkdir(mBackUpPath);
                        sFileUtils.copy(files, new File(mBackUpPath, files.getName()));
                        sFileUtils.delete(files);
                        File mDexExtractPath = new File(mExplorePath, files.getName());
                        sFileUtils.mkdir(mDexExtractPath);
                        Common.setStatus(mContext.getString(R.string.decompiling, files.getName()));
                        new DexToSmali(false, mPackageName != null ? new File(sPackageUtils.getSourceDir(mPackageName, mContext))
                                : mAPKFile, mDexExtractPath, 0, files.getName()).execute();
                    }
                }
            }
        }
        if (Common.isCancelled()) {
            sFileUtils.delete(mExplorePath);
            Common.isCancelled(false);
            Common.setFinishStatus(true);
        }
    }

    @Override
    public void onPostExecute() {
        if (!Common.isFinished()) {
            Common.setFinishStatus(true);
            Intent explorer = new Intent(mContext, APKExploreActivity.class);
            mContext.startActivity(explorer);
        }
    }

}