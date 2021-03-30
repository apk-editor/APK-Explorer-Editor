package com.apk.editor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class AppSettings {

    public static String getAppTheme(Context context) {
        String appTheme = APKEditorUtils.getString("appTheme", "Auto", context);
        if (appTheme.equals("Dark")) {
            return context.getString(R.string.app_theme_dark);
        } else if (appTheme.equals("Light")) {
            return context.getString(R.string.app_theme_light);
        } else {
            return context.getString(R.string.app_theme_auto);
        }
    }

    public static String getLanguage(Context context) {
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
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    public static String getExportPath(Context context) {
        if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString())) {
            return context.getString(R.string.export_path_download);
        } else if (APKEditorUtils.getString("exportPath", null, context) != null && APKEditorUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString())) {
            return context.getString(R.string.sdcard);
        } else {
            return context.getString(R.string.export_path_default);
        }
    }

    public static String getAPKs(Context context) {
        if (APKEditorUtils.getString("exportAPKs", null, context) != null) {
            return APKEditorUtils.getString("exportAPKs", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getProjectExistAction(Context context) {
        if (APKEditorUtils.getString("projectAction", null, context) != null) {
            return APKEditorUtils.getString("projectAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getEditingOptions(Context context) {
        if (APKEditorUtils.getBoolean("editText", false, context)) {
            return context.getString(R.string.enable);
        } else {
            return context.getString(R.string.disable);
        }
    }

    public static String getInstallerAction(Context context) {
        if (APKEditorUtils.getString("installerAction", null, context) != null) {
            return APKEditorUtils.getString("installerAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getAPKSign(Context context) {
        if (isCustomKey(context)) {
            return context.getString(R.string.sign_apk_custom);
        } else {
            return context.getString(R.string.sign_apk_default);
        }
    }

    public static void setAppTheme(Context context) {
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

    public static void setLanguage(Context context) {
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
                    if (!APKEditorUtils.getLanguage(context).equals("de")) {
                        APKEditorUtils.saveString("appLanguage", "de", context);
                        restartApp(context);
                    }
                    break;
                case 4:
                    if (!APKEditorUtils.getLanguage(context).equals("en_US")) {
                        APKEditorUtils.saveString("appLanguage", "en_US", context);
                        restartApp(context);
                    }
                    break;
                case 5:
                    if (!APKEditorUtils.getLanguage(context).equals("fr")) {
                        APKEditorUtils.saveString("appLanguage", "fr", context);
                        restartApp(context);
                    }
                    break;
                case 6:
                    if (!APKEditorUtils.getLanguage(context).equals("vi")) {
                        APKEditorUtils.saveString("appLanguage", "vi", context);
                        restartApp(context);
                    }
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setExportPath(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.export_path), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString(), context);
                    break;
                case 1:
                    APKEditorUtils.saveString("exportPath", null, context);
                    break;
                case 2:
                    APKEditorUtils.saveString("exportPath", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), context);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setAPKs(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.export_apk), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_storage), context);
                    break;
                case 1:
                    APKEditorUtils.saveString("exportAPKs", context.getString(R.string.export_resign), context);
                    break;
                case 2:
                    APKEditorUtils.saveString("exportAPKs", null, context);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setProjectExistAction(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.project_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("projectAction", context.getString(R.string.save), context);
                    break;
                case 1:
                    APKEditorUtils.saveString("projectAction", context.getString(R.string.delete), context);
                    break;
                case 2:
                    APKEditorUtils.saveString("projectAction", null, context);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setEditingOptions(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.editing_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveBoolean("editText", false, context);
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
                            }).show();
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setInstallerAction(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.installer_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    APKEditorUtils.saveString("installerAction", context.getString(R.string.install), context);
                    break;
                case 1:
                    APKEditorUtils.saveString("installerAction", context.getString(R.string.install_resign), context);
                    break;
                case 2:
                    APKEditorUtils.saveString("installerAction", null, context);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void setAPKSign(Context context) {
        new MaterialAlertDialogBuilder(context).setItems(context.getResources().getStringArray(
                R.array.signing_options), (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    if (isCustomKey(context)) {
                        APKEditorUtils.saveString("PrivateKey", null, context);
                        new File(context.getFilesDir(), "signing/APKEditor.pk8").delete();
                        APKEditorUtils.saveString("RSATemplate", null, context);
                        new File(context.getFilesDir(), "signing/APKEditor").delete();
                    }
                    break;
                case 1:
                    Intent signing = new Intent(context, APKSignActivity.class);
                    context.startActivity(signing);
                    break;
            }
        }).setOnDismissListener(dialogInterface -> {
        }).show();
    }

    public static void deleteAppSettings(Activity activity) {
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