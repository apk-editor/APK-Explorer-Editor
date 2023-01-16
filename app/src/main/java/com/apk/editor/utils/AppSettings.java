package com.apk.editor.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.adapters.SettingsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.Utils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Utils.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.Utils.sThemeUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class AppSettings {

    public static ArrayList<sSerializableItems> getData(Context context) {
        ArrayList <sSerializableItems> mData = new ArrayList<>();
        mData.add(new sSerializableItems(null, context.getString(R.string.user_interface), null, null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_theme, context), context.getString(R.string.app_theme), sThemeUtils.getAppTheme(context), null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_translate, context), context.getString(R.string.language), getLanguage(context), null));
        mData.add(new sSerializableItems(null, context.getString(R.string.settings_general), null, null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_projects, context), context.getString(R.string.project_exist_action), getProjectExistAction(context), null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_apks), getExportAPKsPath(context), null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_resources), getExportPath(context), null));
        if (APKEditorUtils.isFullVersion(context)) {
            mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_edit, context), context.getString(R.string.text_editing), getEditingOptions(context), null));
            mData.add(new sSerializableItems(null, context.getString(R.string.signing_title), null, null));
            mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_android, context), context.getString(R.string.export_options), getAPKs(context), null));
            mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_installer, context), context.getString(R.string.installer_action), getInstallerAction(context), null));
            mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_key, context), context.getString(R.string.sign_apk_with), getAPKSign(context), null));
        }
        mData.add(new sSerializableItems(null, context.getString(R.string.settings_misc), null, null));
        mData.add(new sSerializableItems(sUtils.getDrawable(R.drawable.ic_delete, context), context.getString(R.string.clear_cache), context.getString(R.string.clear_cache_summary), null));
        return mData;
    }

    private static int getAPKSignPosition(Context context) {
        if (isCustomKey(context)) {
            return 1;
        } else {
            return 0;
        }
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
        if (sUtils.getBoolean("editText", false, context)) {
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

    public static List<sSerializableItems> getCredits(Context context) {
        List<sSerializableItems> mData = new ArrayList<>();
        mData.add(new sSerializableItems(null, "Willi Ye", "Kernel Adiutor", "https://github.com/Grarak/KernelAdiutor"));
        mData.add(new sSerializableItems(null, "Hsiafan", "APK parser", "https://github.com/hsiafan/apk-parser"));
        mData.add(new sSerializableItems(null, "Srikanth Reddy Lingala", "Zip4j", "https://github.com/srikanth-lingala/zip4j"));
        mData.add(new sSerializableItems(null, "Aefyr", "SAI", "https://github.com/Aefyr/SAI"));
        if (APKEditorUtils.isFullVersion(context)) {
            mData.add(new sSerializableItems(null, "Google", "apksig", "https://android.googlesource.com/platform/tools/apksig"));
        }
        mData.add(new sSerializableItems(null, "Connor Tumbleson", "Apktool", "https://github.com/iBotPeaches/Apktool/"));
        mData.add(new sSerializableItems(null, "Ben Gruver", "smali/baksmali", "https://github.com/JesusFreke/smali/"));

        mData.add(new sSerializableItems(null, "sunilpaulmathew", "Package Manager", "https://github.com/SmartPack/PackageManager"));
        mData.add(new sSerializableItems(null, "Gospel Gilbert", "App Icon", "https://t.me/gilgreat0295"));
        mData.add(new sSerializableItems(null, "Mohammed Qubati", "Arabic Translation", "https://t.me/Alqubati_MrK"));
        mData.add(new sSerializableItems(null, "wushidi", "Chinese (Simplified) Translation", "https://t.me/wushidi"));
        mData.add(new sSerializableItems(null, "fossdd", "German Translation", "https://chaos.social/@fossdd"));
        mData.add(new sSerializableItems(null, "bruh", "Vietnamese Translation", null));
        mData.add(new sSerializableItems(null, "Bruno", "French Translation", null));
        mData.add(new sSerializableItems(null, "Miloš Koliáš", "Czech Translation", null));
        mData.add(new sSerializableItems(null, "Mehmet Un", "Turkish Translation", null));
        mData.add(new sSerializableItems(null, "Jander Mander", "Arabic Translation", null));
        mData.add(new sSerializableItems(null, "Diego", "Spanish Translation", "https://github.com/sguinetti"));
        mData.add(new sSerializableItems(null, "tommynok", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Alexander Steiner", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Hoa Gia Đại Thiếu", "Vietnamese Translation", null));
        mData.add(new sSerializableItems(null, "mezysinc", "Portuguese (Brazilian) Translation", "https://github.com/mezysinc"));
        mData.add(new sSerializableItems(null, "Andreaugustoqueiroz999", "Portuguese (Portugal) Translation", null));
        mData.add(new sSerializableItems(null, "Dodi Studio", "Indonesian Translation", "null"));
        mData.add(new sSerializableItems(null, "Cooky", "Polish Translation", null));
        mData.add(new sSerializableItems(null, "Erős Pista", "Hungarian Translation", null));
        return mData;
    }

    private static String getLanguage(Context context) {
        switch (sUtils.getLanguage(context)) {
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
            case "hu":
                return context.getString(R.string.language_hu);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    private static String getExportAPKsPath(Context context) {
        String exportAPKPath = sUtils.getString("exportAPKsPath", "externalFiles", context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && exportAPKPath.equals("internalStorage")) {
            return context.getString(R.string.export_path_default);
        } else {
            return context.getString(R.string.export_path_files_dir);
        }
    }

    private static String getExportPath(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (sUtils.getString("exportPath", null, context) != null && sUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString())) {
                return context.getString(R.string.sdcard);
            } else if (sUtils.getString("exportPath", null, context) != null && sUtils.getString("exportPath", null, context).equals(Environment.getExternalStorageDirectory().toString() + "/AEE")) {
                return context.getString(R.string.export_path_default);
            } else {
                return context.getString(R.string.export_path_download);
            }
        } else {
            return context.getString(R.string.export_path_download);
        }
    }

    private static String getAPKs(Context context) {
        if (sUtils.getString("exportAPKs", null, context) != null) {
            return sUtils.getString("exportAPKs", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    private static String getProjectExistAction(Context context) {
        if (sUtils.getString("projectAction", null, context) != null) {
            return sUtils.getString("projectAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    private static String getEditingOptions(Context context) {
        if (sUtils.getBoolean("editText", false, context)) {
            return context.getString(R.string.enable);
        } else {
            return context.getString(R.string.disable);
        }
    }

    private static String getInstallerAction(Context context) {
        if (sUtils.getString("installerAction", null, context) != null) {
            return sUtils.getString("installerAction", null, context);
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
                context.getString(R.string.language_in),
                context.getString(R.string.language_hu)
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
        if (getData(activity).get(position).getTextTwo() != null) {
            if (position == 1) {
                sThemeUtils.setAppTheme(activity);
            } else if (position == 2) {
                setLanguage(activity);
            } else if (position == 4) {
                setProjectExistAction(adapter, position,activity);
            } else if (position == 5) {
                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
                    sPermissionUtils.requestPermission(
                            new String[] {
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, activity);
                } else {
                    setExportAPKsPath(adapter, position, activity);
                }
            } else if (position == 6) {
                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, activity)) {
                    sPermissionUtils.requestPermission(
                            new String[] {
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },activity);
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

    private static void setLanguage(Context context) {
        new sSingleChoiceDialog(R.drawable.ic_translate, context.getString(R.string.language),
                getAppLanguageMenu(context), getAppLanguagePosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                switch (itemPosition) {
                    case  0:
                        sUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
                        restartApp(context);
                        break;
                    case 1:
                        sUtils.saveString("appLanguage", "ar", context);
                        restartApp(context);
                        break;
                    case 2:
                        sUtils.saveString("appLanguage", "zh", context);
                        restartApp(context);
                        break;
                    case 3:
                        sUtils.saveString("appLanguage", "cs", context);
                        restartApp(context);
                        break;
                    case 4:
                        sUtils.saveString("appLanguage", "de", context);
                        restartApp(context);
                        break;
                    case 5:
                        sUtils.saveString("appLanguage", "en_US", context);
                        restartApp(context);
                        break;
                    case 6:
                        sUtils.saveString("appLanguage", "fr", context);
                        restartApp(context);
                        break;
                    case 7:
                        sUtils.saveString("appLanguage", "es", context);
                        restartApp(context);
                        break;
                    case 8:
                        sUtils.saveString("appLanguage", "ru", context);
                        restartApp(context);
                        break;
                    case 9:
                        sUtils.saveString("appLanguage", "tr", context);
                        restartApp(context);
                        break;
                    case 10:
                        sUtils.saveString("appLanguage", "vi", context);
                        restartApp(context);
                        break;
                    case 11:
                        sUtils.saveString("appLanguage", "pl", context);
                        restartApp(context);
                        break;
                    case 12:
                        sUtils.saveString("appLanguage", "in", context);
                        restartApp(context);
                        break;
                    case 13:
                        sUtils.saveString("appLanguage", "hu", context);
                        restartApp(context);
                        break;
                    }
            }
        }.show();
    }

    private static void setExportAPKsPath(SettingsAdapter adapter, int position, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            new sSingleChoiceDialog(R.drawable.ic_export, context.getString(R.string.export_path_apks),
                    getAPKExportPathMenu(context), getExportAPKsPathPosition(context), context) {

                @Override
                public void onItemSelected(int itemPosition) {
                    if (itemPosition == 0) {
                        sUtils.saveString("exportAPKsPath", "externalFiles", context);
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_apks), getExportAPKsPath(context), null));
                        adapter.notifyItemChanged(position);
                        transferExportedApps(context);
                    } else if (itemPosition == 1) {
                        sUtils.saveString("exportAPKsPath", "internalStorage", context);
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_apks), getExportAPKsPath(context), null));
                        adapter.notifyItemChanged(position);
                        transferExportedApps(context);
                    }
                }
            }.show();
        }
    }

    private static void setExportPath(SettingsAdapter adapter, int position, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            new sSingleChoiceDialog(R.drawable.ic_export, context.getString(R.string.export_path_resources),
                    getExportPathMenu(context), getExportPathPosition(context), context) {

                @Override
                public void onItemSelected(int itemPosition) {
                    if (itemPosition == 0) {
                        sUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString(), context);
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_resources), getExportPath(context), null));
                        adapter.notifyItemChanged(position);
                    } else if (itemPosition == 1) {
                        sUtils.saveString("exportPath", Environment.getExternalStorageDirectory().toString() + "/AEE", context);
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_resources), getExportPath(context), null));
                        adapter.notifyItemChanged(position);
                    } else {
                        sUtils.saveString("exportPath", null, context);
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_export, context), context.getString(R.string.export_path_resources), getExportPath(context), null));
                        adapter.notifyItemChanged(position);
                    }
                }
            }.show();
        }
    }

    private static void setAPKs(SettingsAdapter adapter, int position, Context context) {
        new sSingleChoiceDialog(R.drawable.ic_android, context.getString(R.string.export_options),
                getExportingAPKMenu(context), getExportingAPKsPosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    sUtils.saveString("exportAPKs", context.getString(R.string.export_storage), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_android, context), context.getString(R.string.export_options), getAPKs(context), null));
                    adapter.notifyItemChanged(position);
                } else if (itemPosition == 1) {
                    sUtils.saveString("exportAPKs", context.getString(R.string.export_resign), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_android, context), context.getString(R.string.export_options), getAPKs(context), null));
                    adapter.notifyItemChanged(position);
                } else {
                    sUtils.saveString("exportAPKs", null, context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_android, context), context.getString(R.string.export_options), getAPKs(context), null));
                    adapter.notifyItemChanged(position);
                }
            }
        }.show();
    }

    private static void setProjectExistAction(SettingsAdapter adapter, int position, Context context) {
        new sSingleChoiceDialog(R.drawable.ic_projects, context.getString(R.string.project_exist_action),
                getProjectExitingMenu(context), getProjectExitingMenuPosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    sUtils.saveString("projectAction", context.getString(R.string.save), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_projects, context), context.getString(R.string.project_exist_action), getProjectExistAction(context), null));
                    adapter.notifyItemChanged(position);
                } else if (itemPosition == 1) {
                    sUtils.saveString("projectAction", context.getString(R.string.delete), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_projects, context), context.getString(R.string.project_exist_action), getProjectExistAction(context), null));
                    adapter.notifyItemChanged(position);
                } else {
                    sUtils.saveString("projectAction", null, context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_projects, context), context.getString(R.string.project_exist_action), getProjectExistAction(context), null));
                    adapter.notifyItemChanged(position);
                }
            }
        }.show();
    }

    private static void setEditingOptions(SettingsAdapter adapter, int position, Context context) {
        new sSingleChoiceDialog(R.drawable.ic_edit, context.getString(R.string.text_editing),
                getEditingOptionsMenu(context), getEditingOptionsPosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    sUtils.saveBoolean("editText", false, context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_edit, context), context.getString(R.string.text_editing), getEditingOptions(context), null));
                    adapter.notifyItemChanged(position);
                } else {
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.warning)
                            .setMessage(context.getString(R.string.text_editing_summary))
                            .setNegativeButton(context.getString(R.string.cancel), (d, id) -> {
                            })
                            .setPositiveButton(context.getString(R.string.enable), (d, id) -> {
                                sUtils.saveBoolean("editText", true, context);
                                getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_edit, context), context.getString(R.string.text_editing), getEditingOptions(context), null));
                                adapter.notifyItemChanged(position);
                            }).show();
                }
            }
        }.show();
    }

    private static void setInstallerAction(SettingsAdapter adapter, int position, Context context) {
        new sSingleChoiceDialog(R.drawable.ic_installer, context.getString(R.string.installer_action),
                getInstallerMenu(context), getInstallerMenuPosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    sUtils.saveString("installerAction", context.getString(R.string.install), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_installer, context), context.getString(R.string.installer_action), getInstallerAction(context), null));
                    adapter.notifyItemChanged(position);
                } else if (itemPosition == 1) {
                    sUtils.saveString("installerAction", context.getString(R.string.install_resign), context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_installer, context), context.getString(R.string.installer_action), getInstallerAction(context), null));
                    adapter.notifyItemChanged(position);
                } else {
                    sUtils.saveString("installerAction", null, context);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_installer, context), context.getString(R.string.installer_action), getInstallerAction(context), null));
                    adapter.notifyItemChanged(position);
                }
            }
        }.show();
    }

    private static void setAPKSign(SettingsAdapter adapter, int position, Context context) {
        new sSingleChoiceDialog(R.drawable.ic_key, context.getString(R.string.sign_apk_with),
                new String[] {
                        context.getString(R.string.sign_apk_default),
                        context.getString(R.string.sign_apk_custom)
                }, getAPKSignPosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    if (isCustomKey(context)) {
                        sUtils.saveString("PrivateKey", null, context);
                        sUtils.delete(new File(context.getFilesDir(), "signing/APKEditor.pk8"));
                        sUtils.saveString("X509Certificate", null, context);
                        sUtils.delete(new File(context.getFilesDir(), "signing/APKEditorCert"));
                        getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_key, context), context.getString(R.string.sign_apk_with), getAPKSign(context), null));
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    Intent signing = new Intent(context, APKSignActivity.class);
                    context.startActivity(signing);
                    getData(context).set(position, new sSerializableItems(sUtils.getDrawable(R.drawable.ic_key, context), context.getString(R.string.sign_apk_with), getAPKSign(context), null));
                    adapter.notifyItemChanged(position);
                }
            }
        }.show();
    }

    private static void deleteAppSettings(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.warning)
                .setMessage(activity.getString(R.string.clear_cache_message))
                .setNegativeButton(activity.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(activity.getString(R.string.delete), (dialog, id) -> {
                    sUtils.delete(activity.getCacheDir());
                    sUtils.delete(activity.getFilesDir());
                    if (APKEditorUtils.isFullVersion(activity) && isCustomKey(activity)) {
                        sUtils.saveString("PrivateKey", null, activity);
                        sUtils.delete(new File(activity.getFilesDir(), "signing/APKEditor.pk8"));
                        sUtils.saveString("X509Certificate", null, activity);
                        sUtils.delete(new File(activity.getFilesDir(), "signing/APKEditorCert"));
                    }
                    activity.finish();
                }).show();
    }

    private static void transferExportedApps(Context context) {
        new sExecutor() {
            private File sourceDir;
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.transfer_exported_apk));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }

            @Override
            public void doInBackground() {
                File destDir;
                if (sUtils.getString("exportAPKsPath", "externalFiles", context).equals("internalStorage")) {
                    sourceDir = context.getExternalFilesDir("");
                    destDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                } else {
                    destDir = context.getExternalFilesDir("");
                    sourceDir = new File(Environment.getExternalStorageDirectory(), "/AEE/exportedAPKs");
                }
                sUtils.copyDir(sourceDir, destDir);
            }

            @Override
            public void onPostExecute() {
                sUtils.delete(sourceDir);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }.execute();
    }

    private static boolean isCustomKey(Context context) {
        return sUtils.getString("PrivateKey", null, context) != null &&
                sUtils.getString("X509Certificate", null, context) != null;
    }

    public static boolean isTextEditingEnabled(Context context) {
        return sUtils.getBoolean("editText", false, context);
    }

    private static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}