package com.apk.editor.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.adapters.SettingsAdapter;
import com.apk.editor.utils.recyclerViewItems.SettingsItems;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class AppSettings {

    private static final ArrayList <SettingsItems> mData = new ArrayList<>();

    public static ArrayList<SettingsItems> getData(Context context) {
        mData.clear();
        mData.add(new SettingsItems(context.getString(R.string.user_interface), null, null));
        mData.add(new SettingsItems(context.getString(R.string.app_theme), getAppTheme(context), ContextCompat.getDrawable(context, R.drawable.ic_theme)));
        mData.add(new SettingsItems(context.getString(R.string.language), getLanguage(context), ContextCompat.getDrawable(context, R.drawable.ic_translate)));
        mData.add(new SettingsItems(context.getString(R.string.settings_general), null, null));
        mData.add(new SettingsItems(context.getString(R.string.project_exist_action), getProjectExistAction(context), ContextCompat.getDrawable(context, R.drawable.ic_projects)));
        mData.add(new SettingsItems(context.getString(R.string.export_path_apks), getExportAPKsPath(context), ContextCompat.getDrawable(context, R.drawable.ic_export)));
        mData.add(new SettingsItems(context.getString(R.string.export_path_resources), getExportPath(context), ContextCompat.getDrawable(context, R.drawable.ic_export)));
        if (APKEditorUtils.isFullVersion(context)) {
            mData.add(new SettingsItems(context.getString(R.string.text_editing), getEditingOptions(context), ContextCompat.getDrawable(context, R.drawable.ic_edit)));
            mData.add(new SettingsItems(context.getString(R.string.signing_title), null, null));
            mData.add(new SettingsItems(context.getString(R.string.export_options), getAPKs(context), ContextCompat.getDrawable(context, R.drawable.ic_android)));
            mData.add(new SettingsItems(context.getString(R.string.installer_action), getInstallerAction(context), ContextCompat.getDrawable(context, R.drawable.ic_installer)));
            mData.add(new SettingsItems(context.getString(R.string.sign_apk_with), getAPKSign(context), ContextCompat.getDrawable(context, R.drawable.ic_key)));
        }
        mData.add(new SettingsItems(context.getString(R.string.settings_misc), null, null));
        mData.add(new SettingsItems(context.getString(R.string.clear_cache), context.getString(R.string.clear_cache_summary), ContextCompat.getDrawable(context, R.drawable.ic_delete)));
        return mData;
    }

    private static int getAPKSignPosition(Context context) {
        if (isCustomKey(context)) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int getAppThemePosition(Context context) {
        for (int i = 0; i < getAppThemeMenu(context).length; i++) {
            if (getAppTheme(context).equals(getAppThemeMenu(context)[i])) {
                return i;
            }
        }
        return 0;
    }

    private static int getAppLanguagePosition(Context context) {
        for (int i = 0; i < getAppLanguageMenu(context).length; i++) {
            if (getLanguage(context).equals(getAppLanguageMenu(context)[i])) {
                return i;
            }
        }
        return 0;
    }

    private static int getEditingOptionsPosition(Context context) {
        if (APKEditorUtils.getBoolean("editText", false, context)) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int getProjectExitingMenuPosition(Context context) {
        for (int i = 0; i < getProjectExitingMenu(context).length; i++) {
            if (getProjectExistAction(context).equals(getProjectExitingMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    private static int getExportAPKsPathPosition(Context context) {
        if (getExportAPKsPath(context).equals(context.getString(R.string.export_path_default))) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int getExportPathPosition(Context context) {
        for (int i = 0; i < getExportPathMenu(context).length; i++) {
            if (getExportPath(context).equals(getExportPathMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    private static int getExportingAPKsPosition(Context context) {
        for (int i = 0; i < getExportingAPKMenu(context).length; i++) {
            if (getAPKs(context).equals(getExportingAPKMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    private static int getInstallerMenuPosition(Context context) {
        for (int i = 0; i < getInstallerMenu(context).length; i++) {
            if (getInstallerAction(context).equals(getInstallerMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    private static String getAppTheme(Context context) {
        String appTheme = APKEditorUtils.getString("appTheme", "Auto", context);
        if (appTheme.equals("Dark")) {
            return context.getString(R.string.app_theme_dark);
        } else if (appTheme.equals("Light")) {
            return context.getString(R.string.app_theme_light);
        } else {
            return context.getString(R.string.app_theme_auto);
        }
    }

    private static String getLanguage(Context context) {
        switch (APKEditorUtils.getLanguage(context)) {
            case "en_US":
                return context.getString(R.string.language_en);
            case "ar":
                return context.getString(R.string.language_ar);
            case "fr":
                return context.getString(R.string.language_fr);
            case "de":
                return context.getString(R.string.language_de);
            case "vi":
                return context.getString(R.string.language_vi);
            case "zh":
                return context.getString(R.string.language_zh);
            case "cs":
                return context.getString(R.string.language_cs);
            case "tr":
                return context.getString(R.string.language_tr);
            case "es":
                return context.getString(R.string.language_es);
            case "ru":
                return context.getString(R.string.language_ru);
            case "pl":
                return context.getString(R.string.language_pl);
            case "in":
                return context.getString(R.string.language_in);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    private static String getExportAPKsPath(Context context) {
        String exportAPKPath = APKEditorUtils.getString("exportAPKsPath", "externalFiles", context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && exportAPKPath.equals("internalStorage")) {
            return context.getString(R.string.export_path_default);
        } else {
            return context.getString(R.string.export_path_files_dir);
        }
    }

    private static String getExportPath(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString())) {
                return context.getString(R.string.sdcard);
            } else if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString() + "/AEE")) {
                return context.getString(R.string.export_path_default);
            } else {
                return context.getString(R.string.export_path_download);
            }
        } else {
            return context.getString(R.string.export_path_download);
        }
    }

    private static String getAPKs(Context context) {
        if (APKEditorUtils.getString("exportAPKs", null, context) != null) {
            return APKEditorUtils.getString("exportAPKs", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    private static String getProjectExistAction(Context context) {
        if (APKEditorUtils.getString("projectAction", null, context) != null) {
            return APKEditorUtils.getString("projectAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    private static String getEditingOptions(Context context) {
        if (APKEditorUtils.getBoolean("editText", false, context)) {
            return context.getString(R.string.enable);
        } else {
            return context.getString(R.string.disable);
        }
    }

    private static String getInstallerAction(Context context) {
        if (APKEditorUtils.getString("installerAction", null, context) != null) {
            return APKEditorUtils.getString("installerAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    private static String getAPKSign(Context context) {
        if (isCustomKey(context)) {
            return context.getString(R.string.sign_apk_custom);
        } else {
            return context.getString(R.string.sign_apk_default);
        }
    }

    private static String[] getAPKSignMenu(Context context) {
        return new String[] {
                context.getString(R.string.sign_apk_default),
                context.getString(R.string.sign_apk_custom)
        };
    }

    private static String[] getAppThemeMenu(Context context) {
        return new String[] {
                context.getString(R.string.app_theme_auto),
                context.getString(R.string.app_theme_dark),
                context.getString(R.string.app_theme_light)
        };
    }

    private static String[] getAppLanguageMenu(Context context) {
        return new String[] {
                context.getString(R.string.app_theme_auto),
                context.getString(R.string.language_ar),
                context.getString(R.string.language_zh),
                context.getString(R.string.language_cs),
                context.getString(R.string.language_de),
                context.getString(R.string.language_en),
                context.getString(R.string.language_fr),
                context.getString(R.string.language_es),
                context.getString(R.string.language_ru),
                context.getString(R.string.language_tr),
                context.getString(R.string.language_vi),
                context.getString(R.string.language_pl),
                context.getString(R.string.language_in)
        };
    }

    private static String[] getEditingOptionsMenu(Context context) {
        return new String[] {
                context.getString(R.string.disable),
                context.getString(R.string.enable)
        };
    }

    private static String[] getProjectExitingMenu(Context context) {
        return new String[] {
                context.getString(R.string.save),
                context.getString(R.string.delete),
                context.getString(R.string.prompt)
        };
    }

    private static String[] getAPKExportPathMenu(Context context) {
        return new String[] {
                context.getString(R.string.export_path_files_dir),
                context.getString(R.string.export_path_default)
        };
    }

    private static String[] getExportPathMenu(Context context) {
        if (Build.VERSION.SDK_INT < 29) {
            return new String[]{
                    context.getString(R.string.sdcard),
                    context.getString(R.string.export_path_default),
                    context.getString(R.string.export_path_download)
            };
        } else {
            return new String[]{
                    context.getString(R.string.export_path_download)
            };
        }
    }

    private static String[] getExportingAPKMenu(Context context) {
        return new String[] {
                context.getString(R.string.export_storage),
                context.getString(R.string.export_resign),
                context.getString(R.string.prompt)
        };
    }

    private static String[] getInstallerMenu(Context context) {
        return new String[] {
                context.getString(R.string.install),
                context.getString(R.string.install_resign),
                context.getString(R.string.prompt)
        };
    }

    public static void handleSettingsActions(SettingsAdapter adapter, int position, Activity activity) {
        if (getData(activity).get(position).getDescription() != null) {
            if (position == 1) {
                setAppTheme(activity);
            } else if (position == 2) {
                setLanguage(activity);
            } else if (position == 4) {
                setProjectExistAction(adapter, position,activity);
            } else if (position == 5) {
                if (APKExplorer.isPermissionDenied(activity)) {
                    APKExplorer.requestPermission(activity);
                } else {
                    setExportAPKsPath(adapter, position, activity);
                }
            } else if (position == 6) {
                if (APKExplorer.isPermissionDenied(activity)) {
                    APKExplorer.requestPermission(activity);
                } else {
                    setExportPath(adapter, position, activity);
                }
            } else if (APKEditorUtils.isFullVersion(activity) && position == 7) {
                setEditingOptions(adapter, position,activity);
            } else if (APKEditorUtils.isFullVersion(activity) && position == 9) {
                setAPKs(adapter, position,activity);
            } else if (APKEditorUtils.isFullVersion(activity) && position == 10) {
                setInstallerAction(adapter, position,activity);
            } else if (APKEditorUtils.isFullVersion(activity) && position == 11) {
                setAPKSign(adapter, position,activity);
            } else {
                deleteAppSettings(activity);
            }
        }
    }

    private static void setAppTheme(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getAppThemeMenu(context), getAppThemePosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveString("appTheme", "Auto", context);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    } else if (itemPosition == 1) {
                        APKEditorUtils.saveString("appTheme", "Dark", context);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        APKEditorUtils.saveString("appTheme", "Light", context);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setLanguage(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getAppLanguageMenu(context), getAppLanguagePosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
                        restartApp(context);
                    } else if (itemPosition == 1) {
                        APKEditorUtils.saveString("appLanguage", "ar", context);
                        restartApp(context);
                    } else if (itemPosition == 2) {
                        APKEditorUtils.saveString("appLanguage", "zh", context);
                        restartApp(context);
                    } else if (itemPosition == 3) {
                        APKEditorUtils.saveString("appLanguage", "cs", context);
                        restartApp(context);
                    } else if (itemPosition == 4) {
                        APKEditorUtils.saveString("appLanguage", "de", context);
                        restartApp(context);
                    } else if (itemPosition == 5) {
                        APKEditorUtils.saveString("appLanguage", "en_US", context);
                        restartApp(context);
                    } else if (itemPosition == 6) {
                        APKEditorUtils.saveString("appLanguage", "fr", context);
                        restartApp(context);
                    } else if (itemPosition == 7) {
                        APKEditorUtils.saveString("appLanguage", "es", context);
                        restartApp(context);
                    } else if (itemPosition == 8) {
                        APKEditorUtils.saveString("appLanguage", "ru", context);
                        restartApp(context);
                    } else if (itemPosition == 9) {
                        APKEditorUtils.saveString("appLanguage", "tr", context);
                        restartApp(context);
                    } else if (itemPosition == 10) {
                        APKEditorUtils.saveString("appLanguage", "vi", context);
                        restartApp(context);
                    } else if (itemPosition == 11) {
                        APKEditorUtils.saveString("appLanguage", "pl", context);
                        restartApp(context);
                    } else if (itemPosition == 12) {
                        APKEditorUtils.saveString("appLanguage", "in", context);
                        restartApp(context);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setExportAPKsPath(SettingsAdapter adapter, int position, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            new MaterialAlertDialogBuilder(activity)
                    .setSingleChoiceItems(getAPKExportPathMenu(activity), getExportAPKsPathPosition(activity), (dialog, itemPosition) -> {
                        if (itemPosition == 0) {
                            APKEditorUtils.saveString("exportAPKsPath", "externalFiles", activity);
                            mData.set(position, new SettingsItems(activity.getString(R.string.export_path_apks), getExportAPKsPath(activity), ContextCompat.getDrawable(activity, R.drawable.ic_export)));
                            adapter.notifyItemChanged(position);
                            transferExportedApps(activity);
                        } else if (itemPosition == 1) {
                            APKEditorUtils.saveString("exportAPKsPath", "internalStorage", activity);
                            mData.set(position, new SettingsItems(activity.getString(R.string.export_path_apks), getExportAPKsPath(activity), ContextCompat.getDrawable(activity, R.drawable.ic_export)));
                            adapter.notifyItemChanged(position);
                            transferExportedApps(activity);
                        }
                        dialog.dismiss();
                    }).show();
        }
    }

    private static void setExportPath(SettingsAdapter adapter, int position, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            new MaterialAlertDialogBuilder(context)
                    .setSingleChoiceItems(getExportPathMenu(context), getExportPathPosition(context), (dialog, itemPosition) -> {
                        if (itemPosition == 0) {
                            APKEditorUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString(), context);
                            mData.set(position, new SettingsItems(context.getString(R.string.export_path_resources), getExportPath(context), ContextCompat.getDrawable(context, R.drawable.ic_export)));
                            adapter.notifyItemChanged(position);
                        } else if (itemPosition == 1) {
                            APKEditorUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString() + "/AEE", context);
                            mData.set(position, new SettingsItems(context.getString(R.string.export_path_resources), getExportPath(context), ContextCompat.getDrawable(context, R.drawable.ic_export)));
                            adapter.notifyItemChanged(position);
                        } else {
                            APKEditorUtils.saveString("exportPath", null, context);
                            mData.set(position, new SettingsItems(context.getString(R.string.export_path_resources), getExportPath(context), ContextCompat.getDrawable(context, R.drawable.ic_export)));
                            adapter.notifyItemChanged(position);
                        }
                        dialog.dismiss();
                    }).show();
        }
    }

    private static void setAPKs(SettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getExportingAPKMenu(context), getExportingAPKsPosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_storage), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.export_options), getAPKs(context), ContextCompat.getDrawable(context, R.drawable.ic_android)));
                        adapter.notifyItemChanged(position);
                    } else if (itemPosition == 1) {
                        APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_resign), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.export_options), getAPKs(context), ContextCompat.getDrawable(context, R.drawable.ic_android)));
                        adapter.notifyItemChanged(position);
                    } else {
                        APKEditorUtils.saveString("exportAPKs", null, context);
                        mData.set(position, new SettingsItems(context.getString(R.string.export_options), getAPKs(context), ContextCompat.getDrawable(context, R.drawable.ic_android)));
                        adapter.notifyItemChanged(position);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setProjectExistAction(SettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getProjectExitingMenu(context), getProjectExitingMenuPosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveString("projectAction", context.getString(R.string.save), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.project_exist_action), getProjectExistAction(context), ContextCompat.getDrawable(context, R.drawable.ic_projects)));
                        adapter.notifyItemChanged(position);
                    } else if (itemPosition == 1) {
                        APKEditorUtils.saveString("projectAction", context.getString(R.string.delete), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.project_exist_action), getProjectExistAction(context), ContextCompat.getDrawable(context, R.drawable.ic_projects)));
                        adapter.notifyItemChanged(position);
                    } else {
                        APKEditorUtils.saveString("projectAction", null, context);
                        mData.set(position, new SettingsItems(context.getString(R.string.project_exist_action), getProjectExistAction(context), ContextCompat.getDrawable(context, R.drawable.ic_projects)));
                        adapter.notifyItemChanged(position);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setEditingOptions(SettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getEditingOptionsMenu(context), getEditingOptionsPosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveBoolean("editText", false, context);
                        mData.set(position, new SettingsItems(context.getString(R.string.text_editing), getEditingOptions(context), ContextCompat.getDrawable(context, R.drawable.ic_edit)));
                        adapter.notifyItemChanged(position);
                    } else {
                        new MaterialAlertDialogBuilder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.warning)
                                .setMessage(context.getString(R.string.text_editing_summary))
                                .setNegativeButton(context.getString(R.string.cancel), (d, id) -> {
                                })
                                .setPositiveButton(context.getString(R.string.enable), (d, id) -> {
                                    APKEditorUtils.saveBoolean("editText", true, context);
                                    mData.set(position, new SettingsItems(context.getString(R.string.text_editing), getEditingOptions(context), ContextCompat.getDrawable(context, R.drawable.ic_edit)));
                                    adapter.notifyItemChanged(position);
                                }).show();
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setInstallerAction(SettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getInstallerMenu(context), getInstallerMenuPosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        APKEditorUtils.saveString("installerAction", context.getString(R.string.install), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.installer_action), getInstallerAction(context), ContextCompat.getDrawable(context, R.drawable.ic_installer)));
                        adapter.notifyItemChanged(position);
                    } else if (itemPosition == 1) {
                        APKEditorUtils.saveString("installerAction", context.getString(R.string.install_resign), context);
                        mData.set(position, new SettingsItems(context.getString(R.string.installer_action), getInstallerAction(context), ContextCompat.getDrawable(context, R.drawable.ic_installer)));
                        adapter.notifyItemChanged(position);
                    } else {
                        APKEditorUtils.saveString("installerAction", null, context);
                        mData.set(position, new SettingsItems(context.getString(R.string.installer_action), getInstallerAction(context), ContextCompat.getDrawable(context, R.drawable.ic_installer)));
                        adapter.notifyItemChanged(position);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void setAPKSign(SettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setSingleChoiceItems(getAPKSignMenu(context), getAPKSignPosition(context), (dialog, itemPosition) -> {
                    if (itemPosition == 0) {
                        if (isCustomKey(context)) {
                            APKEditorUtils.saveString("PrivateKey", null, context);
                            new File(context.getFilesDir(), "signing/APKEditor.pk8").delete();
                            APKEditorUtils.saveString("RSATemplate", null, context);
                            new File(context.getFilesDir(), "signing/APKEditor").delete();
                            mData.set(position, new SettingsItems(context.getString(R.string.sign_apk_with), getAPKSign(context), ContextCompat.getDrawable(context, R.drawable.ic_key)));
                            adapter.notifyItemChanged(position);
                        }
                    } else {
                        Intent signing = new Intent(context, APKSignActivity.class);
                        context.startActivity(signing);
                        mData.set(position, new SettingsItems(context.getString(R.string.sign_apk_with), getAPKSign(context), ContextCompat.getDrawable(context, R.drawable.ic_key)));
                        adapter.notifyItemChanged(position);
                    }
                    dialog.dismiss();
                }).show();
    }

    private static void deleteAppSettings(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.warning)
                .setMessage(activity.getString(R.string.clear_cache_message))
                .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(activity.getString(R.string.delete), (dialog, id) -> {
                    APKEditorUtils.delete(activity.getCacheDir().getAbsolutePath());
                    APKEditorUtils.delete(activity.getFilesDir().getAbsolutePath());
                    if (APKEditorUtils.isFullVersion(activity) && isCustomKey(activity)) {
                        APKEditorUtils.saveString("PrivateKey", null, activity);
                        APKEditorUtils.saveString("RSATemplate", null, activity);
                    }
                    activity.finish();
                }).show();
    }

    private static void transferExportedApps(Context context) {
        new AsyncTasks() {
            private File sourceDir;
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.transfer_exported_apk));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            public void doInBackground() {
                File destDir;
                if (APKEditorUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
                    sourceDir = context.getExternalFilesDir("");
                    destDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                } else {
                    destDir = context.getExternalFilesDir("");
                    sourceDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                }
                APKEditorUtils.copyDir(sourceDir, destDir);
            }

            @Override
            public void onPostExecute() {
                APKEditorUtils.delete(sourceDir.getAbsolutePath());
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    private static boolean isCustomKey(Context context) {
        return APKEditorUtils.getString("PrivateKey", null, context) != null &&
                APKEditorUtils.getString("RSATemplate", null, context) != null;
    }

    public static boolean isTextEditingEnabled(Context context) {
        return APKEditorUtils.getBoolean("editText", false, context);
    }

    private static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}