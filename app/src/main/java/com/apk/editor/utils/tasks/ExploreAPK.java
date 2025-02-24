package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import androidx.documentfile.provider.DocumentFile;

import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.activities.ExploringActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.DexToSmali;
import com.apk.editor.utils.ExternalAPKData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    private File mAPKFile, mAPKDetailsFile;
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
        if (mUri != null) {
            String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mContext, mUri)).getName();
            mAPKFile = new File(mContext.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
            sFileUtils.copy(mUri, mAPKFile, mContext);
        }
        mExplorePath = new File(mContext.getCacheDir().getPath(), mAPKFile != null ? mPackageName + "_" +
                mAPKFile.getName().replace(".apk", "") : mPackageName);

        mBackUpPath = new File(mExplorePath, ".aeeBackup");
        mAPKDetailsFile = new File(mBackUpPath, "appData");
        if (mExplorePath.exists()) {
            if (!sFileUtils.exist(mAPKDetailsFile)) {
                sFileUtils.delete(mExplorePath);
            }
        } else {
            Common.isCancelled(false, mContext);
            sCommonUtils.saveString("exploringStatus", null, mContext);
            Intent explore = new Intent(mContext, ExploringActivity.class);
            mContext.startActivity(explore);
            sCommonUtils.saveString("exploringStatus", mContext.getString(R.string.exploring, mAPKFile != null ? mAPKFile.getName() : sPackageUtils.getAppName(mPackageName, mContext)), mContext);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (!mExplorePath.exists()) {
            sFileUtils.mkdir(mExplorePath);
            sFileUtils.mkdir(mBackUpPath);

            generateAppDetails();

            APKEditorUtils.unzip(mAPKFile != null ? mAPKFile.getAbsolutePath() : sPackageUtils.getSourceDir(mPackageName, mContext), mExplorePath.getAbsolutePath());
            // Decompile dex file(s)
            if (sCommonUtils.getString("decompileSetting", null, mContext) != null && sCommonUtils.getString("decompileSetting",
                    null, mContext).equals(mContext.getString(R.string.explore_options_full)) || mOptions == 1) {
                for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                    if (files.getName().startsWith("classes") && files.getName().endsWith(".dex") && !Common.isCancelled(mContext)) {
                        sFileUtils.mkdir(mBackUpPath);
                        sFileUtils.copy(files, new File(mBackUpPath, files.getName()));
                        sFileUtils.delete(files);
                        File mDexExtractPath = new File(mExplorePath, files.getName());
                        sFileUtils.mkdir(mDexExtractPath);
                        sCommonUtils.saveString("exploringStatus", mContext.getString(R.string.decompiling, files.getName()), mContext);
                        new DexToSmali(false, mAPKFile != null ? mAPKFile : new File(sPackageUtils.getSourceDir(mPackageName, mContext)), mDexExtractPath, 0, files.getName()).execute();
                    }
                }
            }
        }
        if (Common.isCancelled(mContext)) {
            sFileUtils.delete(mExplorePath);
            Common.isCancelled(false, mContext);
            Common.setFinishStatus(mContext);
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void generateAppDetails() {
        JSONObject mJSONObject = new JSONObject();
        APKParser mAPKParser = new APKParser();
        mAPKParser.parse(mAPKFile != null ? mAPKFile.getAbsolutePath() : sPackageUtils.getSourceDir(mPackageName, mContext), mContext);

        // Store basic information about the app
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap.createScaledBitmap(APKExplorer.drawableToBitmap(mAPKParser.getAppIcon()), 150, 150,
                    true).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            mJSONObject.put("app_icon", Base64.encodeToString(byteArray, Base64.DEFAULT));
            mJSONObject.put("app_name", mAPKFile != null ? mAPKFile.getName().replace(".apk", "") : sPackageUtils.getAppName(mPackageName, mContext));
            mJSONObject.put("package_name", mPackageName);
            mJSONObject.put("version_info", mContext.getString(R.string.version, mAPKParser.getVersionName() + " (" + mAPKParser.getVersionCode() + ")"));
            if (mAPKParser.getMinSDKVersion() != null) {
                try {
                    mJSONObject.put("sdk_minimum", mContext.getString(R.string.sdk_minimum, ExternalAPKData.sdkToAndroidVersion(mAPKParser.getMinSDKVersion(), mContext)));
                } catch (NumberFormatException ignored) {
                    mJSONObject.put("sdk_minimum", mContext.getString(R.string.sdk_minimum, mAPKParser.getMinSDKVersion()));
                }
            }
            if (mAPKParser.getCompiledSDKVersion() != null) {
                try {
                    mJSONObject.put("sdk_compiled", mContext.getString(R.string.sdk_compile, ExternalAPKData.sdkToAndroidVersion(mAPKParser.getCompiledSDKVersion(), mContext)));
                } catch (NumberFormatException ignored) {
                    mJSONObject.put("sdk_minimum", mContext.getString(R.string.sdk_compile, mAPKParser.getCompiledSDKVersion()));
                }
            }
            mJSONObject.put("certificate_info", mAPKParser.getCertificate().trim());
            mJSONObject.put("smali_edited", false);
            sFileUtils.create(mJSONObject.toString(), mAPKDetailsFile);
        } catch (JSONException ignored) {
        }
    }

    @Override
    public void onPostExecute() {
        if (mExplorePath.exists()) {
            Common.setFinishStatus(mContext);
            Intent explorer = new Intent(mContext, APKExploreActivity.class);
            explorer.putExtra(APKExploreActivity.BACKUP_PATH_INTENT, mAPKDetailsFile.getAbsolutePath());
            mContext.startActivity(explorer);
        }
    }

}