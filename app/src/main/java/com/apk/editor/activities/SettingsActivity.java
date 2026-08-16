package com.apk.editor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.SettingsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.KeyPair;
import com.apk.editor.utils.menu.ExploreOptionsMenu;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import com.apk.editor.utils.Serializables.SettingsItems;
import com.apk.editor.utils.tasks.ClearAppSettings;

import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class SettingsActivity extends BaseActivity {

    private final ArrayList<SettingsItems> mData = new ArrayList<>();
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings, R.id.layout_root);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SettingsAdapter(mData, position -> {
            Activity activity = this;
            int itemID = mData.get(position).getId();
            switch (itemID) {
                case 8:
                    new ClearAppSettings(this).execute();
                    return;
                case 7:
                    new sSingleChoiceDialog(R.drawable.ic_key, getString(R.string.sign_apk_with),
                            new String[] {
                                    getString(R.string.sign_apk_default),
                                    getString(R.string.sign_apk_custom)
                            }, AppSettings.getAPKSignPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                if (AppSettings.isCustomKey(activity)) {
                                    KeyPair.reset(activity);
                                }
                            } else {
                                Intent signing = new Intent(activity, APKSignActivity.class);
                                startActivity(signing);
                            }
                            mData.get(position).setDescription(AppSettings.getAPKSign(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
                case 6:
                    new sSingleChoiceDialog(R.drawable.ic_installer, getString(R.string.installer_action),
                            AppSettings.getInstallerMenu(this), AppSettings.getInstallerMenuPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("installerAction", getString(R.string.install), activity);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("installerAction", getString(R.string.install_resign), activity);
                            } else {
                                sCommonUtils.saveString("installerAction", null, activity);
                            }
                            mData.get(position).setDescription(AppSettings.getInstallerAction(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
                case 5:
                    new sSingleChoiceDialog(R.drawable.ic_android_app, getString(R.string.export_options),
                            AppSettings.getExportingAPKMenu(this), AppSettings.getExportingAPKsPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("exportAPKs", getString(R.string.export_storage), activity);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("exportAPKs", getString(R.string.export_resign), activity);
                            } else {
                                sCommonUtils.saveString("exportAPKs", null, activity);
                            }
                            mData.get(position).setDescription(AppSettings.getAPKs(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
                case 4:
                    new sSingleChoiceDialog(R.drawable.ic_projects, getString(R.string.project_exist_action),
                            AppSettings.getProjectExitingMenu(this), AppSettings.getProjectExitingMenuPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("projectAction", getString(R.string.save), activity);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("projectAction", getString(R.string.delete), activity);
                            } else {
                                sCommonUtils.saveString("projectAction", null, activity);
                            }
                            mData.get(position).setDescription(AppSettings.getProjectExistAction(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
                case 3:
                    new sSingleChoiceDialog(R.drawable.ic_explore, getString(R.string.explore_options),
                            ExploreOptionsMenu.getOption(this), AppSettings.getExploreOptionsMenuPosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == 0) {
                                sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_simple), activity);
                            } else if (itemPosition == 1) {
                                sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_full), activity);
                            } else if (APKEditorUtils.isFullVersion(activity) && itemPosition == 2) {
                                sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_quick), activity);
                            } else {
                                sCommonUtils.saveString("decompileSetting", null, activity);
                            }
                            mData.get(position).setDescription(AppSettings.getExploreOptions(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
                case 2:
                    AppSettings.setLanguage(this);
                    break;
                default:
                    new sSingleChoiceDialog(R.drawable.ic_theme, getString(R.string.app_theme),
                            AppSettings.getAppThemeMenu(this), AppSettings.getAppThemePosition(this), this) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            if (itemPosition == AppSettings.getAppThemePosition(activity)) {
                                return;
                            }
                            switch (itemPosition) {
                                case 2:
                                    sCommonUtils.saveInt("appTheme", 2, activity);
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    break;
                                case 1:
                                    sCommonUtils.saveInt("appTheme", 1, activity);
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                    break;
                                default:
                                    sCommonUtils.saveInt("appTheme", 0, activity);
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                    break;
                            }
                            mData.get(position).setDescription(sThemeUtils.getAppTheme(activity));
                            mAdapter.notifyItemChanged(position);
                        }
                    }.show();
                    break;
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mData.add(new SettingsItems(getString(R.string.user_interface)));
        mData.add(new SettingsItems(R.drawable.ic_theme, getString(R.string.app_theme), sThemeUtils.getAppTheme(this), 1));
        mData.add(new SettingsItems(R.drawable.ic_translate, getString(R.string.language), AppSettings.getLanguageDescription(this), 2));
        mData.add(new SettingsItems(getString(R.string.settings_general)));
        mData.add(new SettingsItems(R.drawable.ic_explore, getString(R.string.explore_options), AppSettings.getExploreOptions(this), 3));
        mData.add(new SettingsItems(R.drawable.ic_projects, getString(R.string.project_exist_action), AppSettings.getProjectExistAction(this), 4));
        if (APKEditorUtils.isFullVersion(this)) {
            mData.add(new SettingsItems(getString(R.string.signing_title)));
            mData.add(new SettingsItems(R.drawable.ic_android_app, getString(R.string.export_options), AppSettings.getAPKs(this), 5));
            mData.add(new SettingsItems(R.drawable.ic_installer, getString(R.string.installer_action), AppSettings.getInstallerAction(this), 6));
            mData.add(new SettingsItems(R.drawable.ic_key, getString(R.string.sign_apk_with), AppSettings.getAPKSign(this), 7));
        }
        mData.add(new SettingsItems(getString(R.string.settings_misc)));
        mData.add(new SettingsItems(R.drawable.ic_delete, getString(R.string.clear_cache), getString(R.string.clear_cache_summary), 8));

        mBack.setOnClickListener(v -> finish());
    }

}