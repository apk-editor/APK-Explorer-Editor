package com.apk.editor.activities;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.aXMLEncoder;
import com.apk.editor.R;
import com.apk.editor.adapters.QuickEditsAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.SerializableItems.QuickEditsItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 11, 2025
 */
public class QuickEditsActivity extends AppCompatActivity {

    public static final String APK_PATH_INTENT = "apk_path", APP_NAME_INTENT = "app_name",
            MANIFEST_INTENT = "manifest", MIN_SDK_INTENT = "min_Sdk", PACKAGE_NAME_INTENT = "package_name",
            VERSION_NAME_INTENT = "version_name", VERSION_CODE_INTENT = "version_code";
    private static String mAPKPath, mAppName, mManifestDecoded, mMinSDK, mPackageName, mVersionName, mVersionCode;
    private static List<QuickEditsItems> mData;
    private QuickEditsAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickedits);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mBuild = findViewById(R.id.build);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppName = getIntent().getStringExtra(APP_NAME_INTENT);
        mAPKPath = getIntent().getStringExtra(APK_PATH_INTENT);
        mPackageName = getIntent().getStringExtra(PACKAGE_NAME_INTENT);
        mVersionCode = getIntent().getStringExtra(VERSION_CODE_INTENT);
        mVersionName = getIntent().getStringExtra(VERSION_NAME_INTENT);
        mMinSDK = getIntent().getStringExtra(MIN_SDK_INTENT);
        mManifestDecoded = Objects.requireNonNull(getIntent().getStringExtra(MANIFEST_INTENT));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new QuickEditsAdapter(getData());
        mRecyclerView.setAdapter(mAdapter);

        mBack.setOnClickListener(v -> finish());

        mBuild.setOnClickListener(v -> {
            if (!mAdapter.isQuickEdited()) {
                return;
            }
            new sExecutor() {
                private boolean mSuccess = false;
                private File mOutFile;
                private ProgressDialog mProgressDialog;
                @Override
                public void onPreExecute() {
                    mProgressDialog = new ProgressDialog(QuickEditsActivity.this);
                    mProgressDialog.setTitle(getString(R.string.quick_edits_progress_message));
                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.show();
                }

                @Override
                public void doInBackground() {
                    mOutFile = new File(APKData.getExportAPKsPath(QuickEditsActivity.this), (!Objects.equals(mPackageName,
                            mData.get(1).getValue()) ? mData.get(1).getValue() : mPackageName) + "_aee-signed.apk");
                    try {
                        File tmpFile = File.createTempFile("tmpApp",".apk", getExternalCacheDir());
                        FileInputStream fis = new FileInputStream(mAPKPath);
                        FileOutputStream fos = new FileOutputStream(tmpFile);
                        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fis));
                        ZipOutputStream zipOutputStream = new ZipOutputStream(fos);

                        for (ZipEntry in; (in = zipInputStream.getNextEntry()) != null;) {
                            ZipEntry out;
                            InputStream source;
                            if (in.getName().equals("AndroidManifest.xml")) {
                                out = new ZipEntry(in);
                                byte[] encodedData = new aXMLEncoder().encodeString(QuickEditsActivity.this, getStringBuilder().toString().trim());
                                out.setSize(encodedData.length);
                                source = new ByteArrayInputStream(encodedData);
                            } else {
                                out = in;
                                source = zipInputStream;
                            }
                            zipOutputStream.putNextEntry(out);
                            sFileUtils.copyStream(source, zipOutputStream);
                        }
                        zipInputStream.close();
                        zipOutputStream.close();

                        APKData.signApks(tmpFile, mOutFile, QuickEditsActivity.this);

                        sFileUtils.delete(tmpFile);

                        if (mOutFile.exists() && sAPKUtils.getPackageName(mOutFile.getAbsolutePath(), QuickEditsActivity.this) != null) {
                            mSuccess = true;
                        } else {
                            sFileUtils.delete(mOutFile);
                        }
                    } catch (IOException | XmlPullParserException ignored) {
                    }
                }

                @NonNull
                private StringBuilder getStringBuilder() {
                    StringBuilder sb = new StringBuilder();
                    for (String line : mManifestDecoded.split("\n")) {
                        if (!Objects.equals(mAppName, mData.get(0).getValue()) && line.contains("android:label=\"" + mAppName)) {
                            sb.append(line.replace(mAppName, mData.get(0).getValue())).append("\n");
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && line.contains("package=\"" + mPackageName)) {
                            sb.append(line.replace(mPackageName, mData.get(1).getValue())).append("\n");
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && line.contains("android:name=\"") && line.contains("_PERMISSION\"") && line.contains(mPackageName)) {
                            sb.append(line.replace(mPackageName, mData.get(1).getValue())).append("\n");
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && line.contains("android:name=\"") && line.contains("_PERMISSION\"") && !line.contains(mPackageName)) {
                            sb.append(line.replace("_PERMISSION\"", "_PERMISSION_aee\"")).append("\n");
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && line.trim().startsWith("android:authorities") && line.contains(mPackageName)) {
                            sb.append(line.replace(mPackageName, mData.get(1).getValue())).append("\n");
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && line.trim().startsWith("android:authorities") && !line.contains(mPackageName)) {
                            sb.append(line.replace("android:authorities=\"", "android:authorities=\"aee_")).append("\n");
                        } else if (!Objects.equals(mVersionName, mData.get(2).getValue()) && line.contains("android:versionName=\"" + mVersionName)) {
                            sb.append(line.replace(mVersionName, mData.get(2).getValue())).append("\n");
                        } else if (!Objects.equals(mVersionCode, mData.get(3).getValue()) && line.contains("android:versionCode=\"" + mVersionCode)) {
                            sb.append(line.replace(mVersionCode, mData.get(3).getValue())).append("\n");
                        } else if (!Objects.equals(mMinSDK, mData.get(4).getValue()) && line.contains("android:minSdkVersion=\"" + mMinSDK)) {
                            sb.append(line.replace(mMinSDK, mData.get(4).getValue())).append("\n");
                        } else {
                            sb.append(line).append("\n");
                        }
                    }
                    return sb;
                }

                @Override
                public void onPostExecute() {
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException ignored) {}
                    if (mSuccess) {
                        new MaterialAlertDialogBuilder(QuickEditsActivity.this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setCancelable(false)
                                .setMessage(getString(R.string.quick_edits_toast_success, mOutFile.getAbsolutePath()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> finish())
                                .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installAPK(true, mOutFile, QuickEditsActivity.this)
                                ).show();
                    } else {
                        sCommonUtils.toast(getString(R.string.quick_edits_toast_failed), QuickEditsActivity.this).show();
                        finish();
                    }
                }
            }.execute();
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               finish();
            }
        });
    }

    private List<QuickEditsItems> getData() {
        mData = new ArrayList<>();
        mData.add(new QuickEditsItems(getString(R.string.quick_edits_app_name), mAppName));
        mData.add(new QuickEditsItems(getString(R.string.quick_edits_package_name), mPackageName));
        mData.add(new QuickEditsItems(getString(R.string.quick_edits_version_name), mVersionName));
        mData.add(new QuickEditsItems(getString(R.string.quick_edits_version_code), mVersionCode));
        mData.add(new QuickEditsItems(getString(R.string.quick_edits_sdk_min), mMinSDK));
        return mData;
    }

}