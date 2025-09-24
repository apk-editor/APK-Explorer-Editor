package com.apk.editor.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ResourceTableParser;
import com.apk.axml.aXMLDecoder;
import com.apk.axml.aXMLEncoder;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.QuickEditsAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.SerializableItems.QuickEditsItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.XMLEditor;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

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
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 11, 2025
 */
public class QuickEditsActivity extends AppCompatActivity {

    private static List<QuickEditsItems> mData;
    private static List<XMLEntry> mXMLData;
    private List<ResEntry> mResourceMap;
    private static String mAPKPath, mAppName, mMinSDK, mPackageName, mVersionName, mVersionCode;
    private static Uri mUri;
    private RecyclerView mRecyclerView;
    private QuickEditsAdapter mAdapter;
    public static final String APK_PATH_INTENT = "apk_path", PACKAGE_NAME_INTENT = "package_name", URI_INTENT = "apk_uri";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickedits);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mBuild = findViewById(R.id.build);
        mRecyclerView = findViewById(R.id.recycler_view);

        mAPKPath = getIntent().getStringExtra(APK_PATH_INTENT);
        mUri = getIntent().getParcelableExtra(URI_INTENT);
        mPackageName = getIntent().getStringExtra(PACKAGE_NAME_INTENT);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBack.setOnClickListener(v -> finish());

        loadUI().execute();

        mBuild.setOnClickListener(v -> {
            if (!mAdapter.isQuickEdited()) {
                return;
            }
            new sExecutor() {
                private boolean mAppNameChanged = false, mSuccess = false;
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

                private void update(List<XMLEntry> xmlItems) {
                    for (XMLEntry items : xmlItems) {
                        if (!mAppNameChanged && !Objects.equals(mAppName, mData.get(0).getValue()) && items.getTag().trim().equals("android:label")) {
                            items.setValue(mData.get(0).getValue());
                            mAppNameChanged = true;
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && items.getTag().trim().equals("package")) {
                            items.setValue(mData.get(1).getValue());
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && items.getTag().trim().startsWith("android:name") && items.getValue().trim().contains("_PERMISSION") && items.getValue().trim().contains(mPackageName)) {
                            items.setValue(items.getValue().replace(mPackageName, mData.get(1).getValue()));
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && items.getTag().trim().startsWith("android:name") && items.getValue().trim().contains("_PERMISSION") && !items.getValue().trim().contains(mPackageName)) {
                            items.setValue(items.getValue().replace("_PERMISSION\"", "_PERMISSION_aee\""));
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && items.getTag().trim().trim().startsWith("android:authorities") && items.getValue().trim().contains(mPackageName)) {
                            items.setValue(items.getValue().replace(mPackageName, mData.get(1).getValue()));
                        } else if (!Objects.equals(mPackageName, mData.get(1).getValue()) && items.getTag().trim().trim().startsWith("android:authorities") && !items.getValue().trim().contains(mPackageName)) {
                            items.setValue(items.getValue().replace("android:authorities=\"", "android:authorities=\"aee_"));
                        } else if (!Objects.equals(mVersionName, mData.get(2).getValue()) && items.getTag().trim().contains("android:versionName")) {
                            items.setValue(mData.get(2).getValue());
                        } else if (!Objects.equals(mVersionCode, mData.get(3).getValue()) && items.getTag().trim().contains("android:versionCode")) {
                            items.setValue(mData.get(3).getValue());
                        } else if (!Objects.equals(mMinSDK, mData.get(4).getValue()) && items.getTag().trim().contains("android:minSdkVersion")) {
                            items.setValue(mData.get(4).getValue());
                        }
                    }
                }

                @Override
                public void doInBackground() {
                    update(mXMLData);

                    mOutFile = new File(APKData.getExportPath(QuickEditsActivity.this), (!Objects.equals(mPackageName,
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
                                byte[] encodedData = new aXMLEncoder().encodeString(QuickEditsActivity.this, XMLEditor.xmlEntriesToXML(mXMLData, mResourceMap));
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

    private sExecutor loadUI() {
        return new sExecutor() {
            private final Activity mActivity = QuickEditsActivity.this;
            private ProgressDialog mProgressDialog;
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(mActivity);
                mProgressDialog.setTitle(getString(R.string.loading));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                if (mAPKPath == null) {
                    if (mUri != null) {
                        String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(mActivity, mUri)).getName();
                        File apkFile = new File(mActivity.getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
                        sFileUtils.copy(mUri, apkFile, mActivity);
                        if (apkFile.exists()) {
                            mAPKPath = apkFile.getAbsolutePath();
                        }
                    } else if (mPackageName != null) {
                        mAPKPath = sPackageUtils.getSourceDir(mPackageName, mActivity);
                    }
                }
            }

            private List<ResEntry> getResourceMap() {
                try {
                    if (getResourceStream() != null) {
                        ResourceTableParser parser = new ResourceTableParser(getResourceStream());
                        return parser.parse();
                    } else {
                        return null;
                    }
                } catch (IOException ignored) {
                    return null;
                }
            }

            @Override
            public void doInBackground() {
                mResourceMap = getResourceMap();
                mXMLData = getManifestDecoded();
                mPackageName = getPackageNameCurrent();
                mAppName = getAppNameCurrent();
                mMinSDK = getCurrentSDKMin();
                mVersionCode = getVersionCodeCurrent();
                mVersionName = getVersionNameCurrent();
                mData = getData();

                mAdapter = new QuickEditsAdapter(mData);
            }

            private ZipFile getAPKFile() {
                return new ZipFile(mAPKPath);
            }

            private InputStream getResourceStream() {
                try {
                    FileHeader fileHeader = getAPKFile().getFileHeader("resources.arsc");
                    return getAPKFile().getInputStream(fileHeader);
                } catch (IOException e) {
                    return null;
                }
            }

            private List<XMLEntry> getManifestDecoded() {
                try {
                    FileHeader fileHeader = getAPKFile().getFileHeader("AndroidManifest.xml");
                    if (mResourceMap != null) {
                        return new aXMLDecoder(getAPKFile().getInputStream(fileHeader), mResourceMap).decode();
                    } else {
                        return new aXMLDecoder(getAPKFile().getInputStream(fileHeader)).decode();
                    }
                } catch (IOException | XmlPullParserException e) {
                    return null;
                }
            }

            private String getAppNameCurrent() {
                for (XMLEntry items : mXMLData) {
                    if (items.getTag().trim().equals("android:label")) {
                        return items.getValue();
                    }
                }
                return null;
            }

            private String getCurrentSDKMin() {
                for (XMLEntry items : mXMLData) {
                    if (items.getTag().trim().equals("android:minSdkVersion")) {
                        return items.getValue();
                    }
                }
                return null;
            }

            private String getPackageNameCurrent() {
                for (XMLEntry items : mXMLData) {
                    if (items.getTag().trim().equals("package")) {
                        return items.getValue();
                    }
                }
                return null;
            }

            private String getVersionNameCurrent() {
                for (XMLEntry items : mXMLData) {
                    if (items.getTag().trim().equals("android:versionName")) {
                        return items.getValue();
                    }
                }
                return null;
            }

            private String getVersionCodeCurrent() {
                for (XMLEntry items : mXMLData) {
                    if (items.getTag().trim().equals("android:versionCode")) {
                        return items.getValue();
                    }
                }
                return null;
            }

            private List<QuickEditsItems> getData() {
                mData = new ArrayList<>();
                mData.add(new QuickEditsItems(mActivity.getString(R.string.quick_edits_app_name), mAppName));
                mData.add(new QuickEditsItems(mActivity.getString(R.string.quick_edits_package_name), mPackageName));
                mData.add(new QuickEditsItems(mActivity.getString(R.string.quick_edits_version_name), mVersionName));
                mData.add(new QuickEditsItems(mActivity.getString(R.string.quick_edits_version_code), mVersionCode));
                mData.add(new QuickEditsItems(mActivity.getString(R.string.quick_edits_sdk_min), mMinSDK));
                return mData;
            }

            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                mRecyclerView.setAdapter(mAdapter);
            }
        };
    }

}