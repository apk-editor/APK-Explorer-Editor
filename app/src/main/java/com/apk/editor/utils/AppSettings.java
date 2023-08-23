package com.apk.editor.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import com.apk.editor.MainActivity;
import com.apk.editor.R;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Utils.sSingleChoiceDialog;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class AppSettings {

    public static int getAPKSignPosition(Context context) {
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

    public static int getProjectExitingMenuPosition(Context context) {
        for (int i = 0; i < getProjectExitingMenu(context).length; i++) {
            if (getProjectExistAction(context).equals(getProjectExitingMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    public static int getExportAPKsPathPosition(Context context) {
        if (getExportAPKsPath(context).equals(context.getString(R.string.export_path_default))) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getExportPathPosition(Context context) {
        for (int i = 0; i < getExportPathMenu(context).length; i++) {
            if (getExportPath(context).equals(getExportPathMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    public static int getExportingAPKsPosition(Context context) {
        for (int i = 0; i < getExportingAPKMenu(context).length; i++) {
            if (getAPKs(context).equals(getExportingAPKMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    public static int getInstallerMenuPosition(Context context) {
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
        mData.add(new sSerializableItems(null, "Andrii Chvalov", "Ukrainian Translation", "null"));
        mData.add(new sSerializableItems(null, "Veydzher", "Ukrainian Translation", null));
        mData.add(new sSerializableItems(null, "يمني", "Arabic (UAE) Translation", null));
        return mData;
    }

    public static String getLanguage(Context context) {
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
            case "uk":
                return context.getString(R.string.language_uk);
            case "lt":
                return context.getString(R.string.language_lt);
            case "hi":
                return context.getString(R.string.language_hi);
            case "th":
                return context.getString(R.string.language_th);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    public static String getExportAPKsPath(Context context) {
        String exportAPKPath = sUtils.getString("exportAPKsPath", "externalFiles", context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && exportAPKPath.equals("internalStorage")) {
            return context.getString(R.string.export_path_default);
        } else {
            return context.getString(R.string.export_path_files_dir);
        }
    }

    public static String getExportPath(Context context) {
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

    public static String getAPKs(Context context) {
        if (sUtils.getString("exportAPKs", null, context) != null) {
            return sUtils.getString("exportAPKs", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getProjectExistAction(Context context) {
        if (sUtils.getString("projectAction", null, context) != null) {
            return sUtils.getString("projectAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getInstallerAction(Context context) {
        if (sUtils.getString("installerAction", null, context) != null) {
            return sUtils.getString("installerAction", null, context);
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
                context.getString(R.string.language_hu),
                context.getString(R.string.language_uk)
        };
    }

    public static String[] getProjectExitingMenu(Context context) {
        return new String[] {
                context.getString(R.string.save),
                context.getString(R.string.delete),
                context.getString(R.string.prompt)
        };
    }

    public static String[] getAPKExportPathMenu(Context context) {
        return new String[] {
                context.getString(R.string.export_path_files_dir),
                context.getString(R.string.export_path_default)
        };
    }

    public static String[] getExportPathMenu(Context context) {
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

    public static String[] getExportingAPKMenu(Context context) {
        return new String[] {
                context.getString(R.string.export_storage),
                context.getString(R.string.export_resign),
                context.getString(R.string.prompt)
        };
    }

    public static String[] getInstallerMenu(Context context) {
        return new String[] {
                context.getString(R.string.install),
                context.getString(R.string.install_resign),
                context.getString(R.string.prompt)
        };
    }

    public static void setLanguage(Context context) {
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
                    case 14:
                        sUtils.saveString("appLanguage", "uk", context);
                        restartApp(context);
                        break;
                    case 15:
                        sUtils.saveString("appLanguage", "hi", context);
                        restartApp(context);
                        break;
                    case 16:
                        sUtils.saveString("appLanguage", "lt", context);
                        restartApp(context);
                        break;
                    case 17:
                        sUtils.saveString("appLanguage", "th", context);
                        restartApp(context);
                        break;
                }
            }
        }.show();
    }

    public static boolean isCustomKey(Context context) {
        return sUtils.getString("PrivateKey", null, context) != null &&
                sUtils.getString("X509Certificate", null, context) != null;
    }

    private static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}