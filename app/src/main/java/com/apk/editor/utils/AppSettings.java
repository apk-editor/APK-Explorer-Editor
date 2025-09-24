package com.apk.editor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;
import in.sunilpaulmathew.sCommon.Dialog.sSingleChoiceDialog;

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
        String country = getCountry(context);
        switch (getLanguage(context)) {
            case "pt":
                return country.equalsIgnoreCase("BR") ? 22 : 21;
            case "el":
                return 20;
            case "th":
                return 19;
            case "lt":
                return 18;
            case "hi":
                return 17;
            case "uk":
                return 16;
            case "hu":
                return 15;
            case "in":
                return 14;
            case "pl":
                return 13;
            case "vi":
                return 12;
            case "tr":
                return 11;
            case "ru":
                return 10;
            case "es":
                return 9;
            case "fr":
                return 8;
            case "en":
                return country.equalsIgnoreCase("US") ? 7 : 0;
            case "de":
                return 6;
            case "cs":
                return 5;
            case "zh":
                return 4;
            case "ar":
                return country.equalsIgnoreCase("SA") ? 3 : country.equalsIgnoreCase("AE") ? 2 : 1;
            default:
                return 0;
        }
    }

    public static int getProjectExitingMenuPosition(Context context) {
        for (int i = 0; i < getProjectExitingMenu(context).length; i++) {
            if (getProjectExistAction(context).equals(getProjectExitingMenu(context)[i])) {
                return i;
            }
        }
        return 2;
    }

    public static int getExploreOptionsMenuPosition(Context context) {
        for (int i = 0; i < ExploreOptionsMenu.getOption(context).length; i++) {
            if (getExploreOptions(context).equals(ExploreOptionsMenu.getOption(context)[i])) {
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
        mData.add(new sSerializableItems(null, "Bruno Berteau", "French Translation", null));
        mData.add(new sSerializableItems(null, "2BiaW", "French Translation", null));
        mData.add(new sSerializableItems(null, "Miloš Koliáš", "Czech Translation", null));
        mData.add(new sSerializableItems(null, "Mehmet Un", "Turkish Translation", null));
        mData.add(new sSerializableItems(null, "Kyoya", "Turkish Translation", null));
        mData.add(new sSerializableItems(null, "Batuhanakkurt000", "Turkish Translation", null));
        mData.add(new sSerializableItems(null, "Jander Mander", "Arabic Translation", null));
        mData.add(new sSerializableItems(null, "Diego", "Spanish Translation", "https://github.com/sguinetti"));
        mData.add(new sSerializableItems(null, "tommynok", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Alexander Steiner", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "hinteor", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Roman", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Artem", "Russian Translation", null));
        mData.add(new sSerializableItems(null, "Hoa Gia Đại Thiếu", "Vietnamese Translation", null));
        mData.add(new sSerializableItems(null, "mezysinc", "Portuguese (Brazilian) Translation", "https://github.com/mezysinc"));
        mData.add(new sSerializableItems(null, "Andreaugustoqueiroz999", "Portuguese (Portugal) Translation", null));
        mData.add(new sSerializableItems(null, "Dodi Studio", "Indonesian Translation", "null"));
        mData.add(new sSerializableItems(null, "Cooky", "Polish Translation", null));
        mData.add(new sSerializableItems(null, "Adrian", "Polish Translation", null));
        mData.add(new sSerializableItems(null, "Erős Pista", "Hungarian Translation", null));
        mData.add(new sSerializableItems(null, "Andrii Chvalov", "Ukrainian Translation", "null"));
        mData.add(new sSerializableItems(null, "Veydzher", "Ukrainian Translation", null));
        mData.add(new sSerializableItems(null, "يمني", "Arabic (UAE) Translation", null));
        mData.add(new sSerializableItems(null, "Thibault Pitoiset", "French (Belgium) Translation", "null"));
        mData.add(new sSerializableItems(null, "Mohd Ayan", "Hindi Translation", null));
        mData.add(new sSerializableItems(null, "tas", "Lithuanian Translation", null));
        mData.add(new sSerializableItems(null, "vuvov11", "Thai Translation", null));
        mData.add(new sSerializableItems(null, "Me", "Arabic (SA) Translation", null));
        mData.add(new sSerializableItems(null, "asdfqw", "Chinese (zh-Hans) Translation", null));
        mData.add(new sSerializableItems(null, "Fsotm. mai", "Chinese (zh-Hans) Translation", null));
        mData.add(new sSerializableItems(null, "sardonicdozen", "Chinese (zh-Hans) Translation", null));
        mData.add(new sSerializableItems(null, "始", "Chinese (zh-Hans) Translation", null));
        mData.add(new sSerializableItems(null, "VisionR1", "Greek Translation", "https://github.com/VisionR1"));
        return mData;
    }

    private static Locale getLocale(Context context) {
        if (getCountry(context) != null) {
            return new Locale(getLanguage(context), getCountry(context));
        } else {
            return new Locale(getLanguage(context));
        }
    }

    private static String getCountry(Context context) {
        return sCommonUtils.getString("country", java.util.Locale.getDefault().getLanguage(), context);
    }

    private static String getLanguage(Context context) {
        return sCommonUtils.getString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
    }

    public static String getLanguageDescription(Context context) {
        String country = getCountry(context);
        switch (getLanguage(context)) {
            case "en":
                return country.equalsIgnoreCase("US") ? context.getString(R.string.language_en, "US")
                        : context.getString(R.string.app_theme_auto);
            case "ar":
                return country.equalsIgnoreCase("SA") ? context.getString(R.string.language_ar, "SA")
                        : country.equalsIgnoreCase("AE") ? context.getString(R.string.language_ar, "AE")
                        : context.getString(R.string.language_ar, "AR");
            case "fr":
                return context.getString(R.string.language_fr, "FR");
            case "de":
                return context.getString(R.string.language_de);
            case "vi":
                return context.getString(R.string.language_vi);
            case "zh":
                return context.getString(R.string.language_zh, "Hans");
            case "cs":
                return context.getString(R.string.language_cs);
            case "tr":
                return context.getString(R.string.language_tr);
            case "es":
                return context.getString(R.string.language_es, "ES");
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
            case "hi":
                return context.getString(R.string.language_hi);
            case "lt":
                return context.getString(R.string.language_lt);
            case "th":
                return context.getString(R.string.language_th);
            case "el":
                return context.getString(R.string.language_el);
            case "pt":
                return context.getString(R.string.language_pt, country.equalsIgnoreCase("BR") ? "BR" : "PT");
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    public static String getAPKs(Context context) {
        if (sCommonUtils.getString("exportAPKs", null, context) != null) {
            return sCommonUtils.getString("exportAPKs", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getExploreOptions(Context context) {
        if (sCommonUtils.getString("decompileSetting", null, context) != null) {
            return sCommonUtils.getString("decompileSetting", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getProjectExistAction(Context context) {
        if (sCommonUtils.getString("projectAction", null, context) != null) {
            return sCommonUtils.getString("projectAction", null, context);
        } else {
            return context.getString(R.string.prompt);
        }
    }

    public static String getInstallerAction(Context context) {
        if (sCommonUtils.getString("installerAction", null, context) != null) {
            return sCommonUtils.getString("installerAction", null, context);
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
                context.getString(R.string.language_ar, "AR"),
                context.getString(R.string.language_ar, "AE"),
                context.getString(R.string.language_ar, "SA"),
                context.getString(R.string.language_zh, "Hans"),
                context.getString(R.string.language_cs),
                context.getString(R.string.language_de),
                context.getString(R.string.language_en, "US"),
                context.getString(R.string.language_fr, "FR"),
                context.getString(R.string.language_es, "ES"),
                context.getString(R.string.language_ru),
                context.getString(R.string.language_tr),
                context.getString(R.string.language_vi),
                context.getString(R.string.language_pl),
                context.getString(R.string.language_in),
                context.getString(R.string.language_hu),
                context.getString(R.string.language_uk),
                context.getString(R.string.language_hi),
                context.getString(R.string.language_lt),
                context.getString(R.string.language_th),
                context.getString(R.string.language_el),
                context.getString(R.string.language_pt, "PT"),
                context.getString(R.string.language_pt, "BR")
        };
    }

    public static String[] getProjectExitingMenu(Context context) {
        return new String[] {
                context.getString(R.string.save),
                context.getString(R.string.delete),
                context.getString(R.string.prompt)
        };
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

    public static void initializeAppLanguage(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(getLocale(context));
        res.updateConfiguration(conf, dm);
    }

    public static void navigateToFragment(Activity activity, int position) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(position);
    }

    public static void setLanguage(Context context) {
        new sSingleChoiceDialog(R.drawable.ic_translate, context.getString(R.string.language),
                getAppLanguageMenu(context), getAppLanguagePosition(context), context) {

            @Override
            public void onItemSelected(int itemPosition) {
                switch (itemPosition) {
                    case  0:
                        if (Objects.equals(getLanguage(context), Locale.getDefault().getLanguage()) && Objects.equals(getCountry(context), Locale.getDefault().getCountry())) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
                        sCommonUtils.saveString("country", java.util.Locale.getDefault().getCountry(), context);
                        break;
                    case 1:
                        if (Objects.equals(getLanguage(context), "ar") && Objects.equals(getCountry(context), "AR")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ar", context);
                        sCommonUtils.saveString("country", "AR", context);
                        break;

                    case 2:
                        if (Objects.equals(getLanguage(context), "ar") && Objects.equals(getCountry(context), "AE")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ar", context);
                        sCommonUtils.saveString("country", "AE", context);
                        break;
                    case 3:
                        if (Objects.equals(getLanguage(context), "ar") && Objects.equals(getCountry(context), "SA")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ar", context);
                        sCommonUtils.saveString("country", "SA", context);
                        break;
                    case 4:
                        if (Objects.equals(getLanguage(context), "zh") && Objects.equals(getCountry(context), "CN")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "zh", context);
                        sCommonUtils.saveString("country", "CN", context);
                        break;
                    case 5:
                        if (Objects.equals(getLanguage(context), "cs") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "cs", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 6:
                        if (Objects.equals(getLanguage(context), "de") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "de", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 7:
                        if (Objects.equals(getLanguage(context), "en") && Objects.equals(getCountry(context), "US")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "en", context);
                        sCommonUtils.saveString("country", "US", context);
                        break;
                    case 8:
                        if (Objects.equals(getLanguage(context), "fr") && Objects.equals(getCountry(context), "FR")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "fr", context);
                        sCommonUtils.saveString("country", "FR", context);
                        break;
                    case 9:
                        if (Objects.equals(getLanguage(context), "es") && Objects.equals(getCountry(context), "ES")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "es", context);
                        sCommonUtils.saveString("country", "ES", context);
                        break;
                    case 10:
                        if (Objects.equals(getLanguage(context), "ru") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "ru", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 11:
                        if (Objects.equals(getLanguage(context), "tr") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "tr", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 12:
                        if (Objects.equals(getLanguage(context), "vi") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "vi", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 13:
                        if (Objects.equals(getLanguage(context), "pl") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pl", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 14:
                        if (Objects.equals(getLanguage(context), "in") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "in", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 15:
                        if (Objects.equals(getLanguage(context), "hu") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "hu", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 16:
                        if (Objects.equals(getLanguage(context), "uk") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "uk", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 17:
                        if (Objects.equals(getLanguage(context), "hi") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "hi", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 18:
                        if (Objects.equals(getLanguage(context), "lt") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "lt", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 19:
                        if (Objects.equals(getLanguage(context), "th") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "th", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 20:
                        if (Objects.equals(getLanguage(context), "el") && Objects.equals(getCountry(context), null)) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "el", context);
                        sCommonUtils.saveString("country", null, context);
                        break;
                    case 21:
                        if (Objects.equals(getLanguage(context), "pt") && Objects.equals(getCountry(context), "PT")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pt", context);
                        sCommonUtils.saveString("country", "PT", context);
                        break;
                    case 22:
                        if (Objects.equals(getLanguage(context), "pt") && Objects.equals(getCountry(context), "BR")) {
                            return;
                        }
                        sCommonUtils.saveString("appLanguage", "pt", context);
                        sCommonUtils.saveString("country", "BR", context);
                        break;
                }
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        }.show();
    }

    public static boolean isCustomKey(Context context) {
        return APKSigner.getSigningCredentials(context).exists() && APKSigner.getPK8PrivateKey(context).exists();
    }

}