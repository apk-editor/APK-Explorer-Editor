package com.apk.editor.activities;

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
import com.apk.editor.utils.dialogs.ClearAppSettingsDialog;
import com.apk.editor.utils.menu.ExploreOptionsMenu;

import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import com.apk.editor.utils.SerializableItems.SettingsItems;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class SettingsActivity extends BaseActivity {

    private final ArrayList<SettingsItems> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings, R.id.layout_root);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SettingsAdapter mRecycleViewAdapter = new SettingsAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

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

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (position == 1) {
                new sSingleChoiceDialog(R.drawable.ic_theme, getString(R.string.app_theme),
                        AppSettings.getAppThemeMenu(this), AppSettings.getAppThemePosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == AppSettings.getAppThemePosition(v.getContext())) {
                            return;
                        }
                        switch (itemPosition) {
                            case 2:
                                sCommonUtils.saveInt("appTheme", 2, v.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                break;
                            case 1:
                                sCommonUtils.saveInt("appTheme", 1, v.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                break;
                            default:
                                sCommonUtils.saveInt("appTheme", 0, v.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                break;
                        }
                        mData.get(position).setDescription(sThemeUtils.getAppTheme(v.getContext()));
                        mRecycleViewAdapter.notifyItemChanged(position);
                    }
                }.show();
            } else if (position == 2) {
                AppSettings.setLanguage(this);
            } else if (position == 3) {
                new sSingleChoiceDialog(R.drawable.ic_explore, getString(R.string.explore_options),
                        ExploreOptionsMenu.getOption(this), AppSettings.getExploreOptionsMenuPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == 0) {
                            sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_simple), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_explore, getString(R.string.explore_options), AppSettings.getExploreOptions(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else if (itemPosition == 1) {
                            sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_full), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_explore, getString(R.string.explore_options), AppSettings.getExploreOptions(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else if (APKEditorUtils.isFullVersion(v.getContext()) && itemPosition == 2) {
                            sCommonUtils.saveString("decompileSetting", getString(R.string.explore_options_quick), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_explore, getString(R.string.explore_options), AppSettings.getExploreOptions(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else {
                            sCommonUtils.saveString("decompileSetting", null, v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_explore, getString(R.string.explore_options), AppSettings.getExploreOptions(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        }
                    }
                }.show();
            } else if (position == 4) {
                new sSingleChoiceDialog(R.drawable.ic_projects, getString(R.string.project_exist_action),
                        AppSettings.getProjectExitingMenu(this), AppSettings.getProjectExitingMenuPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == 0) {
                            sCommonUtils.saveString("projectAction", getString(R.string.save), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_projects, getString(R.string.project_exist_action), AppSettings.getProjectExistAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else if (itemPosition == 1) {
                            sCommonUtils.saveString("projectAction", getString(R.string.delete), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_projects, getString(R.string.project_exist_action), AppSettings.getProjectExistAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else {
                            sCommonUtils.saveString("projectAction", null, v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_projects, getString(R.string.project_exist_action), AppSettings.getProjectExistAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        }
                    }
                }.show();
            } else if (position == 5) {
                new sSingleChoiceDialog(R.drawable.ic_android_app, getString(R.string.export_options),
                        AppSettings.getExportingAPKMenu(this), AppSettings.getExportingAPKsPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == 0) {
                            sCommonUtils.saveString("exportAPKs", getString(R.string.export_storage), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_android_app, getString(R.string.export_options), AppSettings
                                    .getAPKs(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else if (itemPosition == 1) {
                            sCommonUtils.saveString("exportAPKs", getString(R.string.export_resign), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_android_app, getString(R.string.export_options), AppSettings
                                    .getAPKs(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else {
                            sCommonUtils.saveString("exportAPKs", null, v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_android_app, getString(R.string.export_options), AppSettings
                                    .getAPKs(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        }
                    }
                }.show();
            } else if (position == 6) {
                new sSingleChoiceDialog(R.drawable.ic_installer, getString(R.string.installer_action),
                        AppSettings.getInstallerMenu(this), AppSettings.getInstallerMenuPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == 0) {
                            sCommonUtils.saveString("installerAction", getString(R.string.install), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_installer, getString(R.string.installer_action), AppSettings
                                    .getInstallerAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else if (itemPosition == 1) {
                            sCommonUtils.saveString("installerAction", getString(R.string.install_resign), v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_installer, getString(R.string.installer_action), AppSettings
                                    .getInstallerAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        } else {
                            sCommonUtils.saveString("installerAction", null, v.getContext());
                            mData.set(position, new SettingsItems(R.drawable.ic_installer, getString(R.string.installer_action), AppSettings
                                    .getInstallerAction(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        }
                    }
                }.show();
            } else if (position == 7) {
                new sSingleChoiceDialog(R.drawable.ic_key, getString(R.string.sign_apk_with),
                        new String[] {
                                getString(R.string.sign_apk_default),
                                getString(R.string.sign_apk_custom)
                        }, AppSettings.getAPKSignPosition(this), this) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == 0) {
                            if (AppSettings.isCustomKey(v.getContext())) {
                                KeyPair.reset(v.getContext());
                                mData.set(position, new SettingsItems(R.drawable.ic_key, getString(R.string.sign_apk_with), AppSettings
                                        .getAPKSign(v.getContext()), position));
                                mRecycleViewAdapter.notifyItemChanged(position);
                            }
                        } else {
                            Intent signing = new Intent(v.getContext(), APKSignActivity.class);
                            startActivity(signing);
                            mData.set(position, new SettingsItems(R.drawable.ic_key, getString(R.string.sign_apk_with), AppSettings
                                    .getAPKSign(v.getContext()), position));
                            mRecycleViewAdapter.notifyItemChanged(position);
                        }
                    }
                }.show();
            } else if (position == 8) {
                new ClearAppSettingsDialog(this);
            }
        });

        mBack.setOnClickListener(v -> finish());
    }

}