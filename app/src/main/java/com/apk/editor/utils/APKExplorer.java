package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import com.apk.axml.aXMLDecoder;
import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.activities.APKTasksActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

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
            if (!sUtils.getBoolean("az_order", true, activity)) {
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
            if (!sUtils.getBoolean("az_order", true, activity)) {
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
        if (sUtils.read(new File(path)) == null) return null;
        try {
            return new JSONObject(sUtils.read(new File(path)));
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static void setIcon(AppCompatImageButton icon, Drawable drawable, Context context) {
        icon.setImageDrawable(drawable);
        icon.setColorFilter(sUtils.isDarkTheme(context) ? ContextCompat.getColor(context, R.color.colorWhite) :
                ContextCompat.getColor(context, R.color.colorBlack));
    }

    public static int getSpanCount(Activity activity) {
        return sUtils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
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

    public static List<String> getTextViewData(String path, String searchWord, Context context) {
        List<String> mData = new ArrayList<>();
        String text = null;
        if (isBinaryXML(path)) {
            try (FileInputStream inputStream = new FileInputStream(path)) {
                text = new aXMLDecoder().decode(inputStream).trim();
            } catch (Exception e) {
                sUtils.toast(context.getString(R.string.xml_decode_failed, new File(path).getName()), context).show();
            }
        } else if (ExternalAPKData.isFMInstall()) {
            text = path;
        } else {
            text = sUtils.read(new File(path));
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
            OutputStream imageOutStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, new File(dest).getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                imageOutStream = context.getContentResolver().openOutputStream(uri);
            } else {
                File image = new File(dest);
                imageOutStream = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
            imageOutStream.close();
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

    private static void installAPKs(Activity activity) {
        if (APKData.findPackageName(activity) != null) {
            if (Common.getAPKList().size() > 1) {
                SplitAPKInstaller.installSplitAPKs(Common.getAPKList(), null, activity);
            } else {
                SplitAPKInstaller.installAPK(new File(Common.getAPKList().get(0)), activity);
            }
            if (Build.VERSION.SDK_INT < 29) {
                activity.finish();
            }
        } else {
            sUtils.snackBar(activity.findViewById(android.R.id.content), activity.getString(R.string.installation_status_bad_apks)).show();
        }
    }

    public static void handleAPKs(Activity activity) {
        if (APKEditorUtils.isFullVersion(activity)) {
            if (sUtils.getString("installerAction", null, activity) == null) {
                new sSingleItemDialog(0, null, new String[] {
                        activity.getString(R.string.install),
                        activity.getString(R.string.install_resign),
                        activity.getString(R.string.resign_only)
                }, activity) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        sUtils.saveBoolean("firstSigning", true, activity);
                        if (itemPosition == 0) {
                            installAPKs(activity);
                        } else if (itemPosition == 1) {
                            if (!sUtils.getBoolean("firstSigning", false, activity)) {
                                AppData.getSigningOptionsMenu(null, activity).show();
                            } else {
                                APKData.reSignAPKs(null, true, activity);
                            }
                        } else {
                            if (!sUtils.getBoolean("firstSigning", false, activity)) {
                                AppData.getSigningOptionsMenu(null, activity).show();
                            } else {
                                APKData.reSignAPKs(null, false, activity);
                            }
                        }
                    }
                }.show();
            } else if (sUtils.getString("installerAction", null, activity).equals(activity.getString(R.string.install))) {
                installAPKs(activity);
            } else {
                if (!sUtils.getBoolean("firstSigning", false, activity)) {
                    AppData.getSigningOptionsMenu(null, activity).show();
                } else {
                    APKData.reSignAPKs(null,true, activity);
                }
            }
        } else {
            installAPKs(activity);
        }
    }

    public static void exploreAPK(String packageName, Context context) {
        new sExecutor() {
            private File mBackUpPath, mExplorePath;

            @Override
            public void onPreExecute() {
                Common.isBuilding(false);
                Common.isCancelled(false);
                Common.setFinishStatus(false);
                Common.setAppID(packageName);
                mExplorePath = new File(context.getCacheDir().getPath(), packageName);
                mBackUpPath = new File(mExplorePath, ".aeeBackup");
                Common.setPath(mExplorePath.getAbsolutePath());
                if (!mExplorePath.exists()) {
                    Common.setFinishStatus(false);
                    Common.setStatus(null);
                    Intent apkTasks = new Intent(context, APKTasksActivity.class);
                    context.startActivity(apkTasks);
                    Common.setStatus(context.getString(R.string.exploring, sPackageUtils.getAppName(packageName, context)));
                } else if (!sUtils.exist(new File(mBackUpPath, "appData"))) {
                    sUtils.delete(mExplorePath);
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void doInBackground() {
                if (!mExplorePath.exists()) {
                    sUtils.mkdir(mExplorePath);
                    sUtils.mkdir(mBackUpPath);
                    // Store basic information about the app
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        Bitmap.createScaledBitmap(drawableToBitmap(sPackageUtils.getAppIcon(packageName, context)), 150, 150, true).compress(Bitmap
                                .CompressFormat.PNG,100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        JSONObject mJSONObject = new JSONObject();
                        mJSONObject.put("app_icon", Base64.encodeToString(byteArray, Base64.DEFAULT));
                        mJSONObject.put("app_name", sPackageUtils.getAppName(packageName, context));
                        mJSONObject.put("package_name", packageName);
                        mJSONObject.put("smali_edited", false);
                        sUtils.create(mJSONObject.toString(), new File(mBackUpPath, "appData"));
                    } catch (JSONException ignored) {
                    }
                    APKEditorUtils.unzip(sPackageUtils.getSourceDir(packageName, context), mExplorePath.getAbsolutePath());
                    // Decompile dex file(s)
                    for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                        if (files.getName().startsWith("classes") && files.getName().endsWith(".dex") && !Common.isCancelled()) {
                            sUtils.mkdir(mBackUpPath);
                            sUtils.copy(files, new File(mBackUpPath, files.getName()));
                            sUtils.delete(files);
                            File mDexExtractPath = new File(mExplorePath, files.getName());
                            sUtils.mkdir(mDexExtractPath);
                            Common.setStatus(context.getString(R.string.decompiling, files.getName()));
                            new DexToSmali(false, new File(sPackageUtils.getSourceDir(packageName, context)), mDexExtractPath, 0, files.getName()).execute();
                        }
                    }
                }
                if (Common.isCancelled()) {
                    sUtils.delete(mExplorePath);
                    Common.isCancelled(false);
                    Common.setFinishStatus(true);
                }
            }

            @Override
            public void onPostExecute() {
                if (!Common.isFinished()) {
                    Common.setFinishStatus(true);
                    Intent explorer = new Intent(context, APKExploreActivity.class);
                    context.startActivity(explorer);
                }
            }
        }.execute();
    }

}