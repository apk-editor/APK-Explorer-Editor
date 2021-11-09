package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.apk.editor.R;
import com.apk.editor.utils.recyclerViewItems.APKItems;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 * Ref: https://gitlab.com/guardianproject/checkey/-/blob/master/app/src/main/java/info/guardianproject/checkey/Utils.java
 */
public class ExternalAPKData {

    private static boolean mFMInstall = false;
    private static File mAPKFile = null;
    private static List<String> mPermissions = null;
    private static String mCertificate = null, mManifest = null, mMinSDKVersion = null, mSDKVersion = null,
            mSize = null, mVersion = null;

    public static APKItems getAPKData(String apk, Context context) {
        try (ApkFile apkFile = new ApkFile(new File(apk))) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            APKItems mAPKData = new APKItems(apkMeta.getLabel(), apkMeta.getPackageName(),
                    apkMeta.getVersionName(), readXMLFromAPK(apk, "AndroidManifest.xml"),
                    apkMeta.getCompileSdkVersion(), apkMeta.getMinSdkVersion(),
                    APKData.getAppIcon(apk, context), apkMeta.getVersionCode(),
                    apkMeta.getUsesPermissions());
            apkFile.close();
            return mAPKData;
        } catch (IOException ignored) {
        }
        return null;
    }

    public static boolean isFMInstall() {
        return mFMInstall;
    }

    public static File getAPKFile() {
        return mAPKFile;
    }

    public static List<String> getData() {
        List<String> mData = new ArrayList<>();
        try {
            if (mVersion != null) {
                mData.add(mVersion);
            }
            if (mSDKVersion != null) {
                mData.add(mSDKVersion);
            }
            if (mMinSDKVersion != null) {
                mData.add(mMinSDKVersion);
            }
            if (mSize != null) {
                mData.add(mSize);
            }
        } catch (Exception ignored) {
        }
        return mData;
    }

    public static List<String> getPermissions() {
        return mPermissions;
    }

    public static String getCertificate() {
        return mCertificate;
    }

    public static String getExtension(Uri uri, Context context) {
        if (APKEditorUtils.isDocumentsUI(uri)) {
            @SuppressLint("Recycle")
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
               return   MimeTypeMap.getFileExtensionFromUrl(Environment.getExternalStorageDirectory().toString() + "/Download/" + cursor.getString(nameIndex));
            }
        } else {
            return MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
        }
        return null;
    }

    public static String getManifest() {
        return mManifest;
    }

    public static String readXMLFromAPK(String apk, String path) {
        try (ApkFile apkFile = new ApkFile(new File(apk))) {
            String xmlData = apkFile.transBinaryXml(path);
            apkFile.close();
            return xmlData;
        } catch (IOException ignored) {
        }
        return null;
    }

    private static String sdkToAndroidVersion(String sdkVersion, Context context) {
        int sdk = Integer.parseInt(sdkVersion);
        switch (sdk) {
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

    public static void isFMInstall(boolean b) {
        mFMInstall = b;
    }

    public static void setAPKFile(File file) {
        mAPKFile = file;
    }

    public static void setCertificate(String certificate) {
        mCertificate = certificate;
    }

    public static void setManifest(String manifest) {
        mManifest = manifest;
    }

    public static void setMinSDKVersion(String minSDKVersion, Context context) {
        mMinSDKVersion = context.getString(R.string.sdk_minimum, sdkToAndroidVersion(minSDKVersion, context));
    }

    public static void setPermissions(List<String> permissions) {
        mPermissions = permissions;
    }

    public static void setSDKVersion(String sdkVersion, Context context) {
        mSDKVersion = context.getString(R.string.sdk_compile, sdkToAndroidVersion(sdkVersion, context));
    }

    public static void setSize(String size) {
        mSize = size;
    }

    public static void setVersionInfo(String version) {
        mVersion = version;
    }

}