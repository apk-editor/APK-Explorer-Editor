package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.apk.axml.APKParser;
import com.apk.axml.aXMLDecoder;
import com.apk.editor.R;
import com.apk.editor.utils.dialogs.SigningOptionsDialog;
import com.apk.editor.utils.tasks.ExploreAPK;
import com.apk.editor.utils.tasks.ResignAPKs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorer {

    public static List<String> getData(File[] files, boolean supported, Activity activity) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            // Add directories
            for (File mFile : files) {
                if (mFile.isDirectory() && !mFile.getName().matches(".aeeBackup|.aeeBuild")) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mDir, String.CASE_INSENSITIVE_ORDER);
            if (!sCommonUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mDir);
            }
            mData.addAll(mDir);
            // Add files
            for (File mFile :files) {
                if (supported) {
                    if (mFile.isFile()) {
                        mFiles.add(mFile.getAbsolutePath());
                    }
                } else if (mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mFiles.add(mFile.getAbsolutePath());

                }
            }
            Collections.sort(mFiles, String.CASE_INSENSITIVE_ORDER);
            if (!sCommonUtils.getBoolean("az_order", true, activity)) {
                Collections.reverse(mFiles);
            }
            mData.addAll(mFiles);
        } catch (NullPointerException ignored) {
            activity.finish();
        }
        return mData;
    }

    public static boolean isTextFile(String path) {
        return path.endsWith(".txt") || path.endsWith(".xml") || path.endsWith(".json") || path.endsWith(".properties")
                || path.endsWith(".version") || path.endsWith(".sh") || path.endsWith(".MF") || path.endsWith(".SF")
                || path.endsWith(".html") || path.endsWith(".ini") || path.endsWith(".smali");
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static boolean isBinaryXML(String path) {
        return path.endsWith(".xml") && (new File(path).getName().equals("AndroidManifest.xml") || path.contains(Common.getAppID() + "/res/"));
    }

    public static boolean isSmaliEdited(String path) {
        if (getAppData(path) == null) return false;
        try {
            return Objects.requireNonNull(getAppData(path)).getBoolean("smali_edited");
        } catch (JSONException ignored) {
        }
        return false;
    }

    public static Bitmap getAppIcon(String path) {
        if (getAppData(path) == null) return null;
        try {
            return stringToBitmap(Objects.requireNonNull(getAppData(path)).getString("app_icon"));
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static Bitmap stringToBitmap(String string) {
        try {
            byte[] imageAsBytes = Base64.decode(string.getBytes(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean isSupportedFile(String path) {
        return path.endsWith(".apk") || path.endsWith(".apks") || path.endsWith(".apkm") || path.endsWith(".xapk");
    }

    public static JSONObject getAppData(String path) {
        if (sFileUtils.read(new File(path)) == null) return null;
        try {
            return new JSONObject(sFileUtils.read(new File(path)));
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static void setIcon(AppCompatImageButton icon, Drawable drawable, Context context) {
        icon.setImageDrawable(drawable);
        icon.setColorFilter(sThemeUtils.isDarkTheme(context) ? ContextCompat.getColor(context, R.color.colorWhite) :
                ContextCompat.getColor(context, R.color.colorBlack));
    }

    public static int getSpanCount(Activity activity) {
        return sCommonUtils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    public static String getAppName(String path) {
        if (getAppData(path) == null) return null;
        try {
            return Objects.requireNonNull(getAppData(path)).getString("app_name");
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static String getPackageName(String path) {
        if (getAppData(path) == null) return null;
        try {
            return Objects.requireNonNull(getAppData(path)).getString("package_name");
        } catch (JSONException ignored) {
        }
        return null;
    }

    @SuppressLint("StringFormatInvalid")
    public static List<String> getTextViewData(String path, String searchWord, boolean parsedManifest, Context context) {
        List<String> mData = new ArrayList<>();
        String text = null;
        if (isBinaryXML(path)) {
            try (FileInputStream inputStream = new FileInputStream(path)) {
                text = new aXMLDecoder().decode(inputStream).trim();
            } catch (Exception e) {
                sCommonUtils.toast(context.getString(R.string.xml_decode_failed, new File(path).getName()), context).show();
            }
        } else if (path.endsWith(".RSA")) {
            text = APKParser.getCertificateDetails(path);
        } else if (parsedManifest) {
            text = path;
        } else {
            text = sFileUtils.read(new File(path));
        }
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                if (searchWord == null) {
                    mData.add(line);
                } else if (Common.isTextMatched(line, searchWord)) {
                    mData.add(line);
                }
            }
        }
        return mData;
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static void saveImage(Bitmap bitmap, String dest, Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, new File(dest).getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                OutputStream imageOutStream = context.getContentResolver().openOutputStream(Objects.requireNonNull(uri));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, Objects.requireNonNull(imageOutStream));
                imageOutStream.close();
            } else {
                File image = new File(dest);
                FileOutputStream imageOutStream = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
                imageOutStream.close();
            }
        } catch(Exception ignored) {
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static String getFormattedFileSize(File file) {
        long sizeInByte = file.length();
        if (sizeInByte > 1024) {
            long sizeInKB = sizeInByte / 1024;
            long decimal = (sizeInKB - 1024) / 1024;
            if (sizeInKB > 1024) {
                return sizeInKB / 1024 + "." + decimal + " MB";
            } else {
                return sizeInKB + " KB";
            }
        } else {
            return sizeInByte + " B";
        }
    }

    private static void installAPKs(boolean exit, Activity activity) {
        SplitAPKInstaller.installSplitAPKs(exit, Common.getAPKList(), null, activity);
    }

    public static void handleAPKs(boolean exit, Activity activity) {
        if (APKEditorUtils.isFullVersion(activity)) {
            if (sCommonUtils.getString("installerAction", null, activity) == null) {
                new sSingleItemDialog(0, null, new String[] {
                        activity.getString(R.string.install),
                        activity.getString(R.string.install_resign),
                        activity.getString(R.string.resign_only)
                }, activity) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        sCommonUtils.saveBoolean("firstSigning", true, activity);
                        if (itemPosition == 0) {
                            installAPKs(exit, activity);
                        } else if (itemPosition == 1) {
                            if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
                                new SigningOptionsDialog(null, exit, activity).show();
                            } else {
                                new ResignAPKs(null, true, exit, activity).execute();
                            }
                        } else {
                            if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
                                new SigningOptionsDialog(null, exit, activity).show();
                            } else {
                                new ResignAPKs(null, false, exit, activity).execute();
                            }
                        }
                    }
                }.show();
            } else if (sCommonUtils.getString("installerAction", null, activity).equals(activity.getString(R.string.install))) {
                installAPKs(exit, activity);
            } else {
                if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
                    new SigningOptionsDialog(null, exit, activity).show();
                } else {
                    new ResignAPKs(null,true, exit, activity).execute();
                }
            }
        } else {
            installAPKs(exit, activity);
        }
    }

    public static void exploreApps(String packageName, File apkFile, Uri uri, boolean exit, Activity activity) {
        if (sFileUtils.exist(new File(activity.getCacheDir().getPath(), packageName != null ? packageName : apkFile.getName()))) {
            new ExploreAPK(packageName, apkFile, uri, -1, activity).execute();
        } else if (sCommonUtils.getString("decompileSetting", null, activity) == null) {
            new sSingleItemDialog(0, null, new String[] {
                    activity.getString(R.string.explore_options_simple),
                    activity.getString(R.string.explore_options_full)
            }, activity) {

                @Override
                public void onItemSelected(int itemPosition) {
                    new ExploreAPK(packageName, apkFile, uri, itemPosition, activity).execute();
                    if (exit) {
                        activity.finish();
                    }
                }
            }.show();
        } else if (sCommonUtils.getString("decompileSetting", null, activity).equals(activity.getString(R.string.explore_options_full))) {
            new ExploreAPK(packageName, apkFile, uri, 1, activity).execute();
        } else {
            new ExploreAPK(packageName, apkFile, uri, 0, activity).execute();
        }
    }

}