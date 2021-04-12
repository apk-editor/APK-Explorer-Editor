package com.apk.editor.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.google.android.material.card.MaterialCardView;

import net.dongliu.apk.parser.ApkFile;

import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorer {

    public static boolean mFinish = false, mPrivateKey = false, mRSATemplate = false;
    public static List<String> mAPKList = new ArrayList<>();
    public static MaterialCardView mSelect;
    public static String mAppID, mPath = null, mFilePath = null, mFileToReplace = null;

    public static List<String> getData(File[] files, boolean supported, Activity activity) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            mData.clear();
            // Add directories
            for (File mFile : files) {
                if (mFile.isDirectory() && !mFile.getName().equals(".aeeBackup")) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mDir);
            if (!APKEditorUtils.getBoolean("az_order", true, activity)) {
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
            Collections.sort(mFiles);
            if (!APKEditorUtils.getBoolean("az_order", true, activity)) {
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

    private static boolean isSupportedFile(String path) {
        return path.endsWith(".apk") || path.endsWith(".apks") || path.endsWith(".apkm") || path.endsWith(".xapk");
    }

    public static void setIcon(AppCompatImageButton icon, Drawable drawable, Context context) {
        icon.setImageDrawable(drawable);
        icon.setColorFilter(APKEditorUtils.isDarkTheme(context) ? context.getResources().getColor(R.color.colorWhite) :
                context.getResources().getColor(R.color.colorBlack));
    }

    public static int getSpanCount(Activity activity) {
        return APKEditorUtils.getOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
    }

    public static String readXMLFromAPK(String apk, String path) {
        try (ApkFile apkFile = new ApkFile(new File(apk))) {
            String xmlData = apkFile.transBinaryXml(path);
            apkFile.close();
            return xmlData;
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Uri getIconFromPath(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    public static void saveImage(Bitmap bitmap, String path) {
        File file = new File(path);
        try {
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException ignored) {}
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

    public static void exploreAPK(String packageName, Context context) {
        new AsyncTask<Void, Void, Void>() {
            private File mExplorePath;
            private File mBackUpPath;
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.exploring, AppData.getAppName(packageName, context)));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                APKExplorer.mAppID = packageName;
                mExplorePath = new File(context.getCacheDir().getPath(), packageName);
                mBackUpPath = new File(mExplorePath, ".aeeBackup");
                APKExplorer.mPath = mExplorePath.getAbsolutePath();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                if (!mExplorePath.exists()) {
                    mExplorePath.mkdirs();
                    mBackUpPath.mkdirs();
                    APKEditorUtils.unzip(AppData.getSourceDir(packageName, context), mExplorePath.getAbsolutePath());
                    // Decompile dex file(s)
                    for (File files : Objects.requireNonNull(mExplorePath.listFiles())) {
                        if (files.getName().startsWith("classes") && files.getName().endsWith(".dex")) {
                            APKEditorUtils.copy(files.getAbsolutePath(), new File(mBackUpPath, files.getName()).getAbsolutePath());
                            APKEditorUtils.delete(files.getAbsolutePath());
                            File mDexExtractPath = new File(mExplorePath, files.getName());
                            mDexExtractPath.mkdirs();
                            mProgressDialog.setMessage(context.getString(R.string.decompiling, files.getName()));
                            dexToSmali(new File(AppData.getSourceDir(APKExplorer.mAppID, context)), mDexExtractPath, files.getName(), false, 0);
                        }
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                Intent explorer = new Intent(context, APKExploreActivity.class);
                context.startActivity(explorer);
            }
        }.execute();
    }

    /*
     * The following code is based on the original work of @iBotPeaches for https://github.com/iBotPeaches/Apktool
     */
    public static void dexToSmali(File apkFile, File outDir, String dexName, boolean debugInfo, int api) {
        try {
            final BaksmaliOptions options = new BaksmaliOptions();

            // options
            options.deodex = false;
            options.implicitReferences = false;
            options.parameterRegisters = true;
            options.localsDirective = true;
            options.sequentialLabels = true;
            options.debugInfo = debugInfo;
            options.codeOffsets = false;
            options.accessorComments = false;
            options.registerInfo = 0;
            options.inlineResolver = null;

            // set jobs automatically
            int jobs = Runtime.getRuntime().availableProcessors();
            if (jobs > 6) {
                jobs = 6;
            }

            // create the container
            MultiDexContainer<? extends DexBackedDexFile> container = DexFileFactory.loadDexContainer(apkFile, Opcodes.forApi(api));
            MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry;
            DexBackedDexFile dexFile;

            // If we have 1 item, ignore the passed file. Pull the DexFile we need.
            if (container.getDexEntryNames().size() == 1) {
                dexEntry = container.getEntry(container.getDexEntryNames().get(0));
            } else {
                dexEntry = container.getEntry(dexName);
            }

            // Double check the passed param exists
            if (dexEntry == null) {
                dexEntry = container.getEntry(container.getDexEntryNames().get(0));
            }

            assert dexEntry != null;
            dexFile = dexEntry.getDexFile();

            if (dexFile.supportsOptimizedOpcodes()) {
                throw new Exception("Warning: You are disassembling an odex file without deodexing it.");
            }

            if (dexFile instanceof DexBackedOdexFile) {
                options.inlineResolver = InlineMethodResolver.createInlineMethodResolver(((DexBackedOdexFile)dexFile).getOdexVersion());
            }

            Baksmali.disassembleDexFile(dexFile, outDir, jobs, options);
        } catch (Exception ignored) {
        }
    }

}