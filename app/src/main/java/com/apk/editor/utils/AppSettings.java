package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.adapters.RecycleViewSettingsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class AppSettings {

    private static final ArrayList <RecycleViewSettingsItem> mData = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    public static ArrayList<RecycleViewSettingsItem> getData(Context context) {
        mData.clear();
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.user_interface), null, null));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.app_theme), getAppTheme(context), context.getResources().getDrawable(R.drawable.ic_theme)));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.language), getLanguage(context), context.getResources().getDrawable(R.drawable.ic_translate)));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.settings_general), null, null));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.project_exist_action), getProjectExistAction(context), context.getResources().getDrawable(R.drawable.ic_projects)));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.export_path_apks), getExportAPKsPath(context), context.getResources().getDrawable(R.drawable.ic_export)));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.export_path_resources), getExportPath(context), context.getResources().getDrawable(R.drawable.ic_export)));
        if (APKEditorUtils.isFullVersion(context)) {
            mData.add(new RecycleViewSettingsItem(context.getString(R.string.text_editing), getEditingOptions(context), context.getResources().getDrawable(R.drawable.ic_edit)));
            mData.add(new RecycleViewSettingsItem(context.getString(R.string.signing_title), null, null));
            mData.add(new RecycleViewSettingsItem(context.getString(R.string.export_options), getAPKs(context), context.getResources().getDrawable(R.drawable.ic_android)));
            mData.add(new RecycleViewSettingsItem(context.getString(R.string.installer_action), getInstallerAction(context), context.getResources().getDrawable(R.drawable.ic_installer)));
            mData.add(new RecycleViewSettingsItem(context.getString(R.string.sign_apk_with), getAPKSign(context), context.getResources().getDrawable(R.drawable.ic_key)));
        }
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.settings_misc), null, null));
        mData.add(new RecycleViewSettingsItem(context.getString(R.string.clear_cache), context.getString(R.string.clear_cache_summary), context.getResources().getDrawable(R.drawable.ic_delete)));
        return mData;
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
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    private static String getExportAPKsPath(Context context) {
        String exportAPKPath = APKEditorUtils.getString("exportAPKsPath", "externalFiles", context);
        if (exportAPKPath.equals("internalStorage")) {
            return context.getString(R.string.export_path_default);
        } else {
            return context.getString(R.string.export_path_files_dir);
        }
    }

    private static String getExportPath(Context context) {
        if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString())) {
            return context.getString(R.string.export_path_download);
        } else if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString())) {
            return context.getString(R.string.sdcard);
        } else {
            return context.getString(R.string.export_path_default);
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

    public static void handleSettingsActions(RecycleViewSettingsAdapter adapter, int position, Activity activity) {
        if (getData(activity).get(position).getDescription() != null) {
            if (position == 1) {
                setAppTheme(activity);
            } else if (position == 2) {
                setLanguage(activity);
            } else if (position == 4) {
                setProjectExistAction(adapter, position,activity);
            } else if (position == 5) {
                if (APKExplorer.isPermissionDenied(activity)) {
                    APKExplorer.launchPermissionDialog(activity);
                } else {
                    setExportAPKsPath(adapter, position, activity);
                }
            } else if (position == 6) {
                if (APKExplorer.isPermissionDenied(activity)) {
                    APKExplorer.launchPermissionDialog(activity);
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
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.app_theme), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("appTheme", "Auto", context);
                    restartApp(context);
                    break;
                case 1:
                    APKEditorUtils.saveString("appTheme", "Dark", context);
                    restartApp(context);
                    break;
                case 2:
                    APKEditorUtils.saveString("appTheme", "Light", context);
                    restartApp(context);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    private static void setLanguage(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.app_language), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (!APKEditorUtils.getLanguage(context).equals(java.util.Locale.getDefault().getLanguage())) {
                        APKEditorUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
                        restartApp(context);
                    }
                    break;
                case 1:
                    if (!APKEditorUtils.getLanguage(context).equals("ar")) {
                        APKEditorUtils.saveString("appLanguage", "ar", context);
                        restartApp(context);
                    }
                    break;
                case 2:
                    if (!APKEditorUtils.getLanguage(context).equals("zh")) {
                        APKEditorUtils.saveString("appLanguage", "zh", context);
                        restartApp(context);
                    }
                    break;
                case 3:
                    if (!APKEditorUtils.getLanguage(context).equals("cs")) {
                        APKEditorUtils.saveString("appLanguage", "cs", context);
                        restartApp(context);
                    }
                    break;
                case 4:
                    if (!APKEditorUtils.getLanguage(context).equals("de")) {
                        APKEditorUtils.saveString("appLanguage", "de", context);
                        restartApp(context);
                    }
                    break;
                case 5:
                    if (!APKEditorUtils.getLanguage(context).equals("en_US")) {
                        APKEditorUtils.saveString("appLanguage", "en_US", context);
                        restartApp(context);
                    }
                    break;
                case 6:
                    if (!APKEditorUtils.getLanguage(context).equals("fr")) {
                        APKEditorUtils.saveString("appLanguage", "fr", context);
                        restartApp(context);
                    }
                    break;
                case 7:
                    if (!APKEditorUtils.getLanguage(context).equals("es")) {
                        APKEditorUtils.saveString("appLanguage", "es", context);
                        restartApp(context);
                    }
                    break;
                case 8:
                    if (!APKEditorUtils.getLanguage(context).equals("ru")) {
                        APKEditorUtils.saveString("appLanguage", "ru", context);
                        restartApp(context);
                    }
                    break;
                case 9:
                    if (!APKEditorUtils.getLanguage(context).equals("tr")) {
                        APKEditorUtils.saveString("appLanguage", "tr", context);
                        restartApp(context);
                    }
                    break;
                case 10:
                    if (!APKEditorUtils.getLanguage(context).equals("vi")) {
                        APKEditorUtils.saveString("appLanguage", "vi", context);
                        restartApp(context);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setExportAPKsPath(RecycleViewSettingsAdapter adapter, int position, Activity activity) {
        new MaterialAlertDialogBuilder(activity).setItems(activity.getResources().getStringArray(
                R.array.export_path_apk), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("exportAPKsPath", "externalFiles", activity);
                    mData.set(position, new RecycleViewSettingsItem(activity.getString(R.string.export_path_apks), getExportAPKsPath(activity), activity.getResources().getDrawable(R.drawable.ic_export)));
                    adapter.notifyItemChanged(position);
                    transferExportedApps(activity);
                    break;
                case 1:
                    APKEditorUtils.saveString("exportAPKsPath", "internalStorage", activity);
                    mData.set(position, new RecycleViewSettingsItem(activity.getString(R.string.export_path_apks), getExportAPKsPath(activity), activity.getResources().getDrawable(R.drawable.ic_export)));
                    adapter.notifyItemChanged(position);
                    transferExportedApps(activity);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setExportPath(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.export_path), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString(), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_path_resources), getExportPath(context), context.getResources().getDrawable(R.drawable.ic_export)));
                    adapter.notifyItemChanged(position);
                    break;
                case 1:
                    APKEditorUtils.saveString("exportPath", null, context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_path_resources), getExportPath(context), context.getResources().getDrawable(R.drawable.ic_export)));
                    adapter.notifyItemChanged(position);
                    break;
                case 2:
                    APKEditorUtils.saveString("exportPath", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_path_resources), getExportPath(context), context.getResources().getDrawable(R.drawable.ic_export)));
                    adapter.notifyItemChanged(position);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setAPKs(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.export_apk), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_storage), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_options), getAPKs(context), context.getResources().getDrawable(R.drawable.ic_android)));
                    adapter.notifyItemChanged(position);
                    break;
                case 1:
                    APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_resign), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_options), getAPKs(context), context.getResources().getDrawable(R.drawable.ic_android)));
                    adapter.notifyItemChanged(position);
                    break;
                case 2:
                    APKEditorUtils.saveString("exportAPKs", null, context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.export_options), getAPKs(context), context.getResources().getDrawable(R.drawable.ic_android)));
                    adapter.notifyItemChanged(position);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setProjectExistAction(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.project_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("projectAction", context.getString(R.string.save), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.project_exist_action), getProjectExistAction(context), context.getResources().getDrawable(R.drawable.ic_projects)));
                    adapter.notifyItemChanged(position);
                    break;
                case 1:
                    APKEditorUtils.saveString("projectAction", context.getString(R.string.delete), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.project_exist_action), getProjectExistAction(context), context.getResources().getDrawable(R.drawable.ic_projects)));
                    adapter.notifyItemChanged(position);
                    break;
                case 2:
                    APKEditorUtils.saveString("projectAction", null, context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.project_exist_action), getProjectExistAction(context), context.getResources().getDrawable(R.drawable.ic_projects)));
                    adapter.notifyItemChanged(position);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setEditingOptions(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.editing_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveBoolean("editText", false, context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.text_editing), getEditingOptions(context), context.getResources().getDrawable(R.drawable.ic_edit)));
                    adapter.notifyItemChanged(position);
                    break;
                case 1:
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.warning)
                            .setMessage(context.getString(R.string.text_editing_summary))
                            .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(context.getString(R.string.enable), (dialog, id) -> {
                                APKEditorUtils.saveBoolean("editText", true, context);
                                mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.text_editing), getEditingOptions(context), context.getResources().getDrawable(R.drawable.ic_edit)));
                                adapter.notifyItemChanged(position);
                            }).show();
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setInstallerAction(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.installer_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("installerAction", context.getString(R.string.install), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.installer_action), getInstallerAction(context), context.getResources().getDrawable(R.drawable.ic_installer)));
                    adapter.notifyItemChanged(position);
                    break;
                case 1:
                    APKEditorUtils.saveString("installerAction", context.getString(R.string.install_resign), context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.installer_action), getInstallerAction(context), context.getResources().getDrawable(R.drawable.ic_installer)));
                    adapter.notifyItemChanged(position);
                    break;
                case 2:
                    APKEditorUtils.saveString("installerAction", null, context);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.installer_action), getInstallerAction(context), context.getResources().getDrawable(R.drawable.ic_installer)));
                    adapter.notifyItemChanged(position);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setAPKSign(RecycleViewSettingsAdapter adapter, int position, Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.signing_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (isCustomKey(context)) {
                        APKEditorUtils.saveString("PrivateKey", null, context);
                        new File(context.getFilesDir(), "signing/APKEditor.pk8").delete();
                        APKEditorUtils.saveString("RSATemplate", null, context);
                        new File(context.getFilesDir(), "signing/APKEditor").delete();
                        mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.sign_apk_with), getAPKSign(context), context.getResources().getDrawable(R.drawable.ic_key)));
                        adapter.notifyItemChanged(position);
                    }
                    break;
                case 1:
                    Intent signing = new Intent(context, APKSignActivity.class);
                    context.startActivity(signing);
                    mData.set(position, new RecycleViewSettingsItem(context.getString(R.string.sign_apk_with), getAPKSign(context), context.getResources().getDrawable(R.drawable.ic_key)));
                    adapter.notifyItemChanged(position);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
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
        new AsyncTask<Void, Void, Void>() {
            private File sourceDir;
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.transfer_exported_apk));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                File destDir;
                if (APKEditorUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
                    sourceDir = context.getExternalFilesDir("");
                    destDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                } else {
                    destDir = context.getExternalFilesDir("");
                    sourceDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                }
                APKEditorUtils.copyDir(sourceDir, destDir);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
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