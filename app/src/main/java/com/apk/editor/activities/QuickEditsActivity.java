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
import com.apk.editor.utils.APKPicker;
import com.apk.editor.utils.SerializableItems.APKPickerItems;
import com.apk.editor.utils.SerializableItems.QuickEditsItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.XMLEditor;
import com.apk.editor.utils.dialogs.BundleInstallDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 11, 2025
 */
public class QuickEditsActivity extends AppCompatActivity {

    private static List<QuickEditsItems> mData;
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
                private final Activity mActivity = QuickEditsActivity.this;
                private boolean mAppNameChanged = false, mSuccess = false;
                private File mOutFile;
                private ProgressDialog mProgressDialog;
                private final List<APKPickerItems> mAPKs = new ArrayList<>();

                @Override
                public void onPreExecute() {
                    mProgressDialog = new ProgressDialog(mActivity);
                    mProgressDialog.setTitle(getString(R.string.quick_edits_progress_message));
                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                    mProgressDialog.setIndeterminate(true);
                    if (!isFinishing() && !isDestroyed()) {
                        mProgressDialog.show();
                    }
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

                private boolean isResFileExists(ZipFile zipFile) throws ZipException {
                    return zipFile.getFileHeader("resources.arsc") != null;
                }

                private void updateSplits(String apkPath, boolean isSplit) {
                    try {
                        File tmpFile = File.createTempFile("tmpApp",".apk", getExternalCacheDir());
                        if (tmpFile.exists()) sFileUtils.delete(tmpFile);

                        ZipFile inZip = new ZipFile(apkPath);
                        ZipFile outZip = new ZipFile(tmpFile);

                        List<ResEntry> resEntries = null;
                        if (isResFileExists(inZip)) {
                            InputStream resStream = inZip.getInputStream(inZip.getFileHeader("resources.arsc"));
                            resEntries = new ResourceTableParser(resStream).parse();
                        }

                        mProgressDialog.setMax(inZip.getFileHeaders().size());

                        for (FileHeader header : inZip.getFileHeaders()) {
                            InputStream source;
                            String fileName = header.getFileName();

                            if (fileName.equals("AndroidManifest.xml")) {
                                List<XMLEntry> xmlEntries = (resEntries != null && !resEntries.isEmpty())
                                        ? new aXMLDecoder(inZip.getInputStream(header), resEntries).decode()
                                        : new aXMLDecoder(inZip.getInputStream(header)).decode();
                                update(xmlEntries);

                                byte[] encodedData = (resEntries != null && !resEntries.isEmpty())
                                        ? new aXMLEncoder().encodeString(mActivity, XMLEditor.xmlEntriesToXML(xmlEntries, resEntries))
                                        : new aXMLEncoder().encodeString(mActivity, XMLEditor.xmlEntriesToXML(xmlEntries, null));

                                source = new ByteArrayInputStream(encodedData);
                                outZip.addStream(source, new ZipParameters() {{
                                    setFileNameInZip("AndroidManifest.xml");
                                }});
                                source.close();
                            } else if (fileName.equals("resources.arsc") || fileName.startsWith("lib/")) {
                                source = inZip.getInputStream(header);
                                ZipParameters params = new ZipParameters();
                                params.setFileNameInZip(fileName);
                                params.setCompressionMethod(CompressionMethod.STORE);
                                params.setEntrySize(header.getUncompressedSize());
                                outZip.addStream(source, params);
                                source.close();
                            } else {
                                source = inZip.getInputStream(header);
                                outZip.addStream(source, new ZipParameters() {{
                                    setFileNameInZip(fileName);
                                }});
                                source.close();
                            }

                            mProgressDialog.updateProgress(1);
                        }

                        outZip.close();

                        if (isSplit) {
                            APKData.signApks(tmpFile, new File(mOutFile, new File(apkPath).getName()), mActivity);
                        } else {
                            APKData.signApks(tmpFile, mOutFile, mActivity);
                        }

                        sFileUtils.delete(tmpFile);

                    } catch (IOException | XmlPullParserException ignored) {
                    }
                }

                @Override
                public void doInBackground() {
                    if (sPackageUtils.isPackageInstalled(mPackageName, mActivity) && APKData.isAppBundle(sPackageUtils
                            .getSourceDir(mPackageName, mActivity))) {
                        mOutFile = new File(APKData.getExportPath(mActivity), (!Objects.equals(mPackageName,
                                mData.get(1).getValue()) ? mData.get(1).getValue() : mPackageName));
                        sFileUtils.mkdir(mOutFile);

                        for (String splitAPKPath : APKData.splitApks(sPackageUtils.getSourceDir(mPackageName, mActivity))) {
                            updateSplits(splitAPKPath, true);
                        }
                    } else {
                        mOutFile = new File(APKData.getExportPath(mActivity), (!Objects.equals(mPackageName,
                                mData.get(1).getValue()) ? mData.get(1).getValue() : mPackageName) + "_aee-signed.apk");
                        updateSplits(mAPKPath, false);
                    }

                    if (mOutFile.exists()) {
                        mSuccess = mOutFile.isFile() || mOutFile.isDirectory() && APKData.findPackageName(APKData.splitApks(mOutFile), mActivity) != null;
                    } else {
                        sFileUtils.delete(mOutFile);
                    }

                    if (mOutFile.isDirectory()) {
                        mProgressDialog.setMax(Objects.requireNonNull(mOutFile.listFiles()).length);
                        for (File files : Objects.requireNonNull(mOutFile.listFiles())) {
                            if (files.isFile() && files.getName().endsWith("apk")) {
                                mAPKs.add(new APKPickerItems(files, APKPicker.isSelectedAPK(files, mActivity)));
                            }
                            mProgressDialog.updateProgress(1);
                        }
                    }
                }

                @Override
                public void onPostExecute() {
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException ignored) {}
                    if (mSuccess) {
                        new MaterialAlertDialogBuilder(mActivity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setCancelable(false)
                                .setMessage(getString(R.string.quick_edits_toast_success, mOutFile.getAbsolutePath()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> finish())
                                .setPositiveButton(R.string.install, (dialog, id) -> {
                                    if (mOutFile.isDirectory()) {
                                        new BundleInstallDialog(mAPKs, true, mActivity);
                                    } else {
                                        SplitAPKInstaller.installAPK(true, mOutFile, mActivity);
                                    }
                                }).show();
                    } else {
                        sCommonUtils.toast(getString(R.string.quick_edits_toast_failed), mActivity).show();
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
            private List<XMLEntry> mXMLData;
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
                    if (getResourceMap() != null) {
                        return new aXMLDecoder(getAPKFile().getInputStream(fileHeader), getResourceMap()).decode();
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