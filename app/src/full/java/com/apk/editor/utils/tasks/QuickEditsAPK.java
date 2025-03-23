package com.apk.editor.utils.tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.apk.axml.aXMLDecoder;
import com.apk.editor.R;
import com.apk.editor.activities.QuickEditsActivity;
import com.apk.editor.utils.dialogs.ProgressDialog;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on MArch 14, 2025
 */
public class QuickEditsAPK extends sExecutor {

    private final Context mContext;
    private File mAPKFile;
    private ProgressDialog mProgressDialog;
    private String mPackageName, mAppName = null, mManifestDecoded = null, mMinSDK = null, mVersionName = null, mVersionCode = null;
    private final Uri mUri;

    public QuickEditsAPK(String packageName, File apkFile, Uri uri, Context context) {
        mPackageName = packageName;
        mAPKFile = apkFile;
        mUri = uri;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.loading));
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        if (mAPKFile == null) {
            if (mUri != null) {
                String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mContext, mUri)).getName();
                mAPKFile = new File(mContext.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
                sFileUtils.copy(mUri, mAPKFile, mContext);
            } else if (mPackageName != null) {
                mAPKFile = new File(sPackageUtils.getSourceDir(mPackageName, mContext));
            }
        }
    }

    @Override
    public void doInBackground() {
        mManifestDecoded = getManifestDecoded();
        mPackageName = getPackageNameCurrent();
        mAppName = getAppNameCurrent();
        mMinSDK = getCurrentSDKMin();
        mVersionCode = getVersionCodeCurrent();
        mVersionName = getVersionNameCurrent();
    }

    private String getManifestDecoded() {
        try (ZipFile zipFile = new ZipFile(mAPKFile)) {
            FileHeader fileHeader = zipFile.getFileHeader("AndroidManifest.xml");
            return new aXMLDecoder(zipFile.getInputStream(fileHeader)).decode().trim();
        } catch (IOException | XmlPullParserException ignored) {
            return null;
        }
    }

    private String getAppNameCurrent() {
        for (String line : Objects.requireNonNull(mManifestDecoded).split(">\\n" + " {4}")) {
            if (line.trim().startsWith("<application")) {
                for (String lines : line.split("\n")) {
                    if (lines.contains("android:label=")) {
                        return lines.trim().replace("android:label=", "").replace("\"", "");
                    }
                }
            }
        }
        return null;
    }

    private String getCurrentSDKMin() {
        for (String line : Objects.requireNonNull(mManifestDecoded).split("\n")) {
            if (line.trim().contains("android:minSdkVersion=")) {
                return line.trim().replace("android:minSdkVersion=", "").replace("\"", "");
            }
        }
        return null;
    }

    private String getPackageNameCurrent() {
        for (String line : Objects.requireNonNull(mManifestDecoded).split("\n")) {
            if (line.trim().contains("package=")) {
                return line.trim().replace("package=", "").replace("\"", "");
            }
        }
        return null;
    }

    private String getVersionNameCurrent() {
        for (String line : Objects.requireNonNull(mManifestDecoded).split("\n")) {
            if (line.trim().contains("android:versionName=")) {
                return line.trim().replace("android:versionName=", "").replace("\"", "");
            }
        }
        return null;
    }

    private String getVersionCodeCurrent() {
        for (String line : Objects.requireNonNull(mManifestDecoded).split("\n")) {
            if (line.trim().contains("android:versionCode=")) {
                return line.trim().replace("android:versionCode=", "").replace("\"", "");
            }
        }
        return null;
    }

    @Override
    public void onPostExecute() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException ignored) {
        }
        Intent quickEdits = new Intent(mContext, QuickEditsActivity.class);
        quickEdits.putExtra(QuickEditsActivity.APP_NAME_INTENT, mAppName);
        quickEdits.putExtra(QuickEditsActivity.APK_PATH_INTENT, mAPKFile.getAbsolutePath());
        quickEdits.putExtra(QuickEditsActivity.PACKAGE_NAME_INTENT, mPackageName);
        quickEdits.putExtra(QuickEditsActivity.VERSION_NAME_INTENT, mVersionName);
        quickEdits.putExtra(QuickEditsActivity.VERSION_CODE_INTENT, mVersionCode);
        quickEdits.putExtra(QuickEditsActivity.MIN_SDK_INTENT, mMinSDK);
        quickEdits.putExtra(QuickEditsActivity.MANIFEST_INTENT, mManifestDecoded);
        mContext.startActivity(quickEdits);
    }

}