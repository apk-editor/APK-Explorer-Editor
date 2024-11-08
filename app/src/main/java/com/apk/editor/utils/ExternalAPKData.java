package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.apk.axml.APKParser;
import com.apk.editor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 * Ref: https://gitlab.com/guardianproject/checkey/-/blob/master/app/src/main/java/info/guardianproject/checkey/Utils.java
 */
public class ExternalAPKData {

    @SuppressLint("StringFormatInvalid")
    public static List<String> getData(Context context) {
        List<String> mData = new ArrayList<>();
        APKParser mAPKParser = new APKParser();
        try {
            if (mAPKParser.getVersionName() != null) {
                mData.add(context.getString(R.string.version, mAPKParser.getVersionName() + " (" + mAPKParser.getVersionCode() + ")"));
            }
            if (mAPKParser.getCompiledSDKVersion() != null) {
                mData.add(context.getString(R.string.sdk_compile, sdkToAndroidVersion(mAPKParser.getCompiledSDKVersion(), context)));
            }
            if (mAPKParser.getMinSDKVersion() != null) {
                mData.add(context.getString(R.string.sdk_minimum, sdkToAndroidVersion(mAPKParser.getMinSDKVersion(), context)));
            }
            if (mAPKParser.getAPKSize() != Integer.MIN_VALUE) {
                long size = mAPKParser.getAPKSize() / 1024;
                long decimal = (size - 1024) / 1024;
                String apkSize;
                if (size > 1024) {
                    apkSize = size / 1024 + "." + decimal + " MB";
                } else {
                    apkSize = size  + " KB";
                }
                mData.add(context.getString(R.string.size, apkSize) + " (" + mAPKParser.getAPKSize() + " bytes)");
            }
        } catch (Exception ignored) {
        }
        return mData;
    }

    @SuppressLint("StringFormatInvalid")
    private static String sdkToAndroidVersion(String sdkVersion, Context context) {
        int sdk = Integer.parseInt(sdkVersion);
        switch (sdk) {
            case 35:
                return context.getString(R.string.android_version, "15 (VANILLA_ICE_CREAM, " + sdkVersion + ")");
            case 34:
                return context.getString(R.string.android_version, "14 (UPSIDE_DOWN_CAKE, " + sdkVersion + ")");
            case 33:
                return context.getString(R.string.android_version, "13 (TIRAMISU, " + sdkVersion + ")");
            case 32:
                return context.getString(R.string.android_version, "12.1 (S_V2, " + sdkVersion + ")");
            case 31:
                return context.getString(R.string.android_version, "12 (S, " + sdkVersion + ")");
            case 30:
                return context.getString(R.string.android_version, "11 (R, " + sdkVersion + ")");
            case 29:
                return context.getString(R.string.android_version, "10 (Q, " + sdkVersion + ")");
            case 28:
                return context.getString(R.string.android_version, "9 (P, " + sdkVersion + ")");
            case 27:
                return context.getString(R.string.android_version, "8 (O_MR1, " + sdkVersion + ")");
            case 26:
                return context.getString(R.string.android_version, "8.0 (0, " + sdkVersion + ")");
            case 25:
                return context.getString(R.string.android_version, "7.1.1 (N_MRI, " + sdkVersion + ")");
            case 24:
                return context.getString(R.string.android_version, "7.0 (N, " + sdkVersion + ")");
            case 23:
                return context.getString(R.string.android_version, "6.0 (M, " + sdkVersion + ")");
            case 22:
                return context.getString(R.string.android_version, "5.1 (LOLLIPOP_MR1, " + sdkVersion + ")");
            case 21:
                return context.getString(R.string.android_version, "5.0 (LOLLIPOP, " + sdkVersion + ")");
            case 20:
                return context.getString(R.string.android_version, "4.4 (KITKAT_WATCH, " + sdkVersion + ")");
            case 19:
                return context.getString(R.string.android_version, "4.4 (KITKAT, " + sdkVersion + ")");
            case 18:
                return context.getString(R.string.android_version, "4.3 (JELLY_BEAN_MR2, " + sdkVersion + ")");
            case 17:
                return context.getString(R.string.android_version, "4.2 (JELLY_BEAN_MR1, " + sdkVersion + ")");
            case 16:
                return context.getString(R.string.android_version, "4.1 (JELLY_BEAN, " + sdkVersion + ")");
            case 15:
                return context.getString(R.string.android_version, "4.0.3 (ICE_CREAM_SANDWICH_MR1, " + sdkVersion + ")");
            case 14:
                return context.getString(R.string.android_version, "4.0 (ICE_CREAM_SANDWICH, " + sdkVersion + ")");
            case 13:
                return context.getString(R.string.android_version, "3.2 (HONEYCOMB_MR2, " + sdkVersion + ")");
            case 12:
                return context.getString(R.string.android_version, "3.1 (HONEYCOMB_MR1, " + sdkVersion + ")");
            case 11:
                return context.getString(R.string.android_version, "3.0 (HONEYCOMB, " + sdkVersion + ")");
            case 10:
                return context.getString(R.string.android_version, "2.3.3 (GINGERBREAD_MR1, " + sdkVersion + ")");
            case 9:
                return context.getString(R.string.android_version, "2.3 (GINGERBREAD, " + sdkVersion + ")");
            case 8:
                return context.getString(R.string.android_version, "2.2 (FROYO, " + sdkVersion + ")");
            case 7:
                return context.getString(R.string.android_version, "2.1 (ECLAIR_MR1, " + sdkVersion + ")");
            case 6:
                return context.getString(R.string.android_version, "2.0.1 (ECLAIR_0_1, " + sdkVersion + ")");
            case 5:
                return context.getString(R.string.android_version, "2.0 (ECLAIR, " + sdkVersion + ")");
            case 4:
                return context.getString(R.string.android_version, "1.6 (DONUT, " + sdkVersion + ")");
            case 3:
                return context.getString(R.string.android_version, "1.5 (CUPCAKE, " + sdkVersion + ")");
            case 2:
                return context.getString(R.string.android_version, "1.1 (BASE_1_1, " + sdkVersion + ")");
            case 1:
                return context.getString(R.string.android_version, "1.0 (BASE, " + sdkVersion + ")");
            default:
                return sdkVersion;
        }
    }

    public static void generateAppDetails(String packageName, File apkFile, File backupPath, Context context) {
        JSONObject mJSONObject = new JSONObject();
        APKParser mAPKParser = new APKParser();
        mAPKParser.parse(packageName != null ? sPackageUtils.getSourceDir(packageName, context) : apkFile.getAbsolutePath(), context);

        // Store basic information about the app
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap.createScaledBitmap(APKExplorer.drawableToBitmap(mAPKParser.getAppIcon()), 150, 150,
                    true).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            mJSONObject.put("app_icon", Base64.encodeToString(byteArray, Base64.DEFAULT));
            mJSONObject.put("app_name", packageName != null ? sPackageUtils.getAppName(packageName, context) : apkFile.getName());
            mJSONObject.put("package_name", packageName);
            mJSONObject.put("version_info", context.getString(R.string.version, mAPKParser.getVersionName() + " (" + mAPKParser.getVersionCode() + ")"));
            if (mAPKParser.getMinSDKVersion() != null) {
                try {
                    mJSONObject.put("sdk_minimum", context.getString(R.string.sdk_minimum, ExternalAPKData.sdkToAndroidVersion(mAPKParser.getMinSDKVersion(), context)));
                } catch (NumberFormatException ignored) {
                    mJSONObject.put("sdk_minimum", context.getString(R.string.sdk_minimum, mAPKParser.getMinSDKVersion()));
                }
            }
            if (mAPKParser.getCompiledSDKVersion() != null) {
                try {
                    mJSONObject.put("sdk_compiled", context.getString(R.string.sdk_compile, ExternalAPKData.sdkToAndroidVersion(mAPKParser.getCompiledSDKVersion(), context)));
                } catch (NumberFormatException ignored) {
                    mJSONObject.put("sdk_minimum", context.getString(R.string.sdk_compile, mAPKParser.getCompiledSDKVersion()));
                }
            }
            mJSONObject.put("certificate_info", mAPKParser.getCertificate().trim());
            mJSONObject.put("smali_edited", false);
            sFileUtils.create(mJSONObject.toString(), new File(backupPath, "appData"));
        } catch (JSONException ignored) {
        }
    }

}