package com.apk.editor.utils.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.activities.APKTasksActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.DexToSmali;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 28, 2023
 */
public class ExploreAPK extends sExecutor {

    private final Context mContext;
    private File mBackUpPath, mExplorePath;
    private final String mPackageName;

    public ExploreAPK(String packageName, Context context) {
        mPackageName = packageName;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        Common.isBuilding(false);
        Common.isCancelled(false);
        Common.setFinishStatus(false);
        Common.setAppID(mPackageName);
        mExplorePath = new File(mContext.getCacheDir().getPath(), mPackageName);
        mBackUpPath = new File(mExplorePath, ".aeeBackup");
        Common.setPath(mExplorePath.getAbsolutePath());
        if (!mExplorePath.exists()) {
            Common.setFinishStatus(false);
            Common.setStatus(null);
            Intent apkTasks = new Intent(mContext, APKTasksActivity.class);
            mContext.startActivity(apkTasks);
            Common.setStatus(mContext.getString(R.string.exploring, sPackageUtils.getAppName(mPackageName, mContext)));
        } else if (!sUtils.exist(new File(mBackUpPath, "appData"))) {
            sUtils.delete(mExplorePath);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void doInBackground() {
        if (!mExplorePath.exists()) {
            sUtils.mkdir(mExplorePath);
            sUtils.mkdir(mBackUpPath);
            // Store basic information about the app
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap.createScaledBitmap(APKExplorer.drawableToBitmap(sPackageUtils.getAppIcon(mPackageName, mContext)),
                        150, 150, true).compress(Bitmap.CompressFormat.PNG,100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                JSONObject mJSONObject = new JSONObject();
                mJSONObject.put("app_icon", Base64.encodeToString(byteArray, Base64.DEFAULT));
                mJSONObject.put("app_name", sPackageUtils.getAppName(mPackageName, mContext));
                mJSONObject.put("package_name", mPackageName);
                mJSONObject.put("smali_edited", false);
                sUtils.create(mJSONObject.toString(), new File(mBackUpPath, "appData"));
            } catch (JSONException ignored) {
            }
            APKEditorUtils.unzip(sPackageUtils.getSourceDir(mPackageName, mContext), mExplorePath.getAbsolutePath());
            // Decompile dex file(s)
            for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                if (files.getName().startsWith("classes") && files.getName().endsWith(".dex") && !Common.isCancelled()) {
                    sUtils.mkdir(mBackUpPath);
                    sUtils.copy(files, new File(mBackUpPath, files.getName()));
                    sUtils.delete(files);
                    File mDexExtractPath = new File(mExplorePath, files.getName());
                    sUtils.mkdir(mDexExtractPath);
                    Common.setStatus(mContext.getString(R.string.decompiling, files.getName()));
                    new DexToSmali(false, new File(sPackageUtils.getSourceDir(mPackageName, mContext)),
                            mDexExtractPath, 0, files.getName()).execute();
                }
            }
        }
        if (Common.isCancelled()) {
            sUtils.delete(mExplorePath);
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