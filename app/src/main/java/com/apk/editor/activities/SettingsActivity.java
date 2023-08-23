package com.apk.editor.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.SettingsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.dialogs.ClearAppSettingsDialog;
import com.apk.editor.utils.tasks.TransferApps;

import java.io.File;
import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class SettingsActivity extends AppCompatActivity {

    private final ArrayList<sSerializableItems> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        SettingsAdapter mRecycleViewAdapter = new SettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mData.add(new sSerializableItems(null, getString(R.string.user_interface), null, null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_theme, this), getString(R.string.app_theme), sThemeUtils.getAppTheme(this), null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_translate, this), getString(R.string.language), AppSettings.getLanguage(this), null));
        mData.add(new sSerializableItems(null, getString(R.string.settings_general), null, null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_projects, this), getString(R.string.project_exist_action), AppSettings.getProjectExistAction(this), null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_export, this), getString(R.string.export_path_apks), AppSettings.getExportAPKsPath(this), null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_export, this), getString(R.string.export_path_resources), AppSettings.getExportPath(this), null));
        if (APKEditorUtils.isFullVersion(this)) {
            mData.add(new sSerializableItems(null, getString(R.string.signing_title), null, null));
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_android, this), getString(R.string.export_options), AppSettings.getAPKs(this), null));
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_installer, this), getString(R.string.installer_action), AppSettings.getInstallerAction(this), null));
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_key, this), getString(R.string.sign_apk_with), AppSettings.getAPKSign(this), null));
        }
        mData.add(new sSerializableItems(null, getString(R.string.settings_misc), null, null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_delete, this), getString(R.string.clear_cache), getString(R.string.clear_cache_summary), null));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getTextTwo() != null) {
                if (position == 1) {
                    sThemeUtils.setAppTheme(this);
                } else if (position == 2) {
                    AppSettings.setLanguage(this);
                } else if (position == 4) {
                    new sSingleChoiceDialog(R.drawable.ic_projects, getString(R.string.project_exist_action),
                            AppSettings.getProjectExitingMenu(this), AppSettings.getProjectExitingMenuPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("projectAction", getString(R.string.save), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_projects, SettingsActivity.this), getString(R.string.project_exist_action), AppSettings.getProjectExistAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("projectAction", getString(R.string.delete), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_projects, SettingsActivity.this), getString(R.string.project_exist_action), AppSettings.getProjectExistAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else {
                                sCommonUtils.saveString("projectAction", null, SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_projects, SettingsActivity.this), getString(R.string.project_exist_action), AppSettings.getProjectExistAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }.show();
                } else if (position == 5) {
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, this)) {
                        sPermissionUtils.requestPermission(
                                new String[] {
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                }, this);
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            new sSingleChoiceDialog(R.drawable.ic_export, getString(R.string.export_path_apks),
                                    AppSettings.getAPKExportPathMenu(this), AppSettings.getExportAPKsPathPosition(this), this) {

                                @Override
                                public void onItemSelected(int itemPosition) {
                                    if (itemPosition == 0) {
                                        sCommonUtils.saveString("exportAPKsPath", "externalFiles", SettingsActivity.this);
                                        mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                                R.drawable.ic_export, SettingsActivity.this), getString(R.string.export_path_apks), AppSettings
                                                .getExportAPKsPath(SettingsActivity.this), null));
                                        mRecycleViewAdapter.notifyItemChanged(position);
                                        new TransferApps(SettingsActivity.this).execute();
                                    } else if (itemPosition == 1) {
                                        sCommonUtils.saveString("exportAPKsPath", "internalStorage", SettingsActivity.this);
                                        mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                                R.drawable.ic_export, SettingsActivity.this), getString(R.string.export_path_apks), AppSettings
                                                .getExportAPKsPath(SettingsActivity.this), null));
                                        mRecycleViewAdapter.notifyItemChanged(position);
                                        new TransferApps(SettingsActivity.this).execute();
                                    }
                                }
                            }.show();
                        }
                    }
                } else if (position == 6) {
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, this)) {
                        sPermissionUtils.requestPermission(
                                new String[] {
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                }, this);
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            new sSingleChoiceDialog(R.drawable.ic_export, getString(R.string.export_path_resources),
                                    AppSettings.getExportPathMenu(this), AppSettings.getExportPathPosition(this), this) {

                                @Override
                                public void onItemSelected(int itemPosition) {
                                    if (itemPosition == 0) {
                                        sCommonUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString(), SettingsActivity.this);
                                        mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                                R.drawable.ic_export, SettingsActivity.this), getString(R.string.export_path_resources), AppSettings
                                                .getExportPath(SettingsActivity.this), null));
                                        mRecycleViewAdapter.notifyItemChanged(position);
                                    } else if (itemPosition == 1) {
                                        sCommonUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString() + "/AEE", SettingsActivity.this);
                                        mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                                R.drawable.ic_export, SettingsActivity.this), getString(R.string.export_path_resources), AppSettings
                                                .getExportPath(SettingsActivity.this), null));
                                        mRecycleViewAdapter.notifyItemChanged(position);
                                    } else {
                                        sCommonUtils.saveString("exportPath", null, SettingsActivity.this);
                                        mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                                R.drawable.ic_export, SettingsActivity.this), getString(R.string.export_path_resources), AppSettings
                                                .getExportPath(SettingsActivity.this), null));
                                        mRecycleViewAdapter.notifyItemChanged(position);
                                    }
                                }
                            }.show();
                        }
                    }
                } else if (APKEditorUtils.isFullVersion(this) && position == 8) {
                    new sSingleChoiceDialog(R.drawable.ic_android, getString(R.string.export_options),
                            AppSettings.getExportingAPKMenu(this), AppSettings.getExportingAPKsPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("exportAPKs", getString(R.string.export_storage), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_android, SettingsActivity.this), getString(R.string.export_options), AppSettings
                                        .getAPKs(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("exportAPKs", getString(R.string.export_resign), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_android, SettingsActivity.this), getString(R.string.export_options), AppSettings
                                        .getAPKs(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else {
                                sCommonUtils.saveString("exportAPKs", null, SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_android, SettingsActivity.this), getString(R.string.export_options), AppSettings
                                        .getAPKs(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }.show();
                } else if (APKEditorUtils.isFullVersion(this) && position == 9) {
                    new sSingleChoiceDialog(R.drawable.ic_installer, getString(R.string.installer_action),
                            AppSettings.getInstallerMenu(this), AppSettings.getInstallerMenuPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("installerAction", getString(R.string.install), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_installer, SettingsActivity.this), getString(R.string.installer_action), AppSettings
                                        .getInstallerAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("installerAction", getString(R.string.install_resign), SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_installer, SettingsActivity.this), getString(R.string.installer_action), AppSettings
                                        .getInstallerAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            } else {
                                sCommonUtils.saveString("installerAction", null, SettingsActivity.this);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_installer, SettingsActivity.this), getString(R.string.installer_action), AppSettings
                                        .getInstallerAction(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }.show();
                } else if (APKEditorUtils.isFullVersion(this) && position == 10) {
                    new sSingleChoiceDialog(R.drawable.ic_key, getString(R.string.sign_apk_with),
                            new String[] {
                                    getString(R.string.sign_apk_default),
                                    getString(R.string.sign_apk_custom)
                            }, AppSettings.getAPKSignPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                if (AppSettings.isCustomKey(SettingsActivity.this)) {
                                    sCommonUtils.saveString("PrivateKey", null, SettingsActivity.this);
                                    sFileUtils.delete(new File(getFilesDir(), "signing/APKEditor.pk8"));
                                    sCommonUtils.saveString("X509Certificate", null, SettingsActivity.this);
                                    sFileUtils.delete(new File(getFilesDir(), "signing/APKEditorCert"));
                                    mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                            R.drawable.ic_key, SettingsActivity.this), getString(R.string.sign_apk_with), AppSettings
                                            .getAPKSign(SettingsActivity.this), null));
                                    mRecycleViewAdapter.notifyItemChanged(position);
                                }
                            } else {
                                Intent signing = new Intent(SettingsActivity.this, APKSignActivity.class);
                                startActivity(signing);
                                mData.set(position, new sSerializableItems(sCommonUtils.getDrawable(
                                        R.drawable.ic_key, SettingsActivity.this), getString(R.string.sign_apk_with), AppSettings
                                        .getAPKSign(SettingsActivity.this), null));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                        }
                    }.show();
                } else {
                    new ClearAppSettingsDialog(this).show();
                }
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

}