package com.apk.editor.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKEditorUtils {

    public static int getThemeAccentColor(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static boolean isFullVersion(Context context) {
        return context.getPackageName().equals("com.apk.editor");
    }

    public interface OnDialogEditTextListener {
        void onClick(String text);
    }

    public static MaterialAlertDialogBuilder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                                            final OnDialogEditTextListener onDialogEditTextListener,
                                                            Context context) {
        return dialogEditText(text, negativeListener, onDialogEditTextListener, -1, context);
    }

    private static MaterialAlertDialogBuilder dialogEditText(String text, final DialogInterface.OnClickListener negativeListener,
                                                            final OnDialogEditTextListener onDialogEditTextListener, int inputType,
                                                            Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(75, 75, 75, 75);

        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setGravity(Gravity.CENTER);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (text != null) {
            editText.append(text);
        }
        editText.setSingleLine(true);
        if (inputType >= 0) {
            editText.setInputType(inputType);
        }

        layout.addView(editText);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context).setView(layout);
        if (negativeListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), negativeListener);
        }
        if (onDialogEditTextListener != null) {
            dialog.setPositiveButton(context.getString(R.string.ok), (dialog1, which)
                    -> onDialogEditTextListener.onClick(Objects.requireNonNull(editText.getText()).toString()))
                    .setOnDismissListener(dialog1 -> {
                        if (negativeListener != null) {
                            negativeListener.onClick(dialog1, 0);
                        }
                    });
        }
        return dialog;
    }

    public static CharSequence fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void unzip(String zip, String path) {
        try {
            new ZipFile(zip).extractAll(path);
        } catch (ZipException ignored) {
        }
    }

    static void zip(File path, File zip) {
        try {
            for (File mFile : Objects.requireNonNull(path.listFiles())) {
                if (mFile.isDirectory()) {
                    new ZipFile(zip).addFolder(mFile);
                } else {
                    new ZipFile(zip).addFile(mFile);
                }
            }
        } catch (ZipException ignored) {}
    }

    public static boolean isDocumentsUI(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static String getPath(String path) {
        if (path.startsWith("/document/raw:")) {
            path = path.replace("/document/raw:", "");
        } else if (path.startsWith("/document/primary:")) {
            path = (Environment.getExternalStorageDirectory() + ("/") + path.replace("/document/primary:", ""));
        } else if (path.startsWith("/document/")) {
            path = path.replace("/document/", "/storage/").replace(":", "/");
        }
        if (path.startsWith("/storage_root/storage/emulated/0")) {
            path = path.replace("/storage_root/storage/emulated/0", "/storage/emulated/0");
        } else if (path.startsWith("/storage_root")) {
            path = path.replace("storage_root", "storage/emulated/0");
        }
        if (path.startsWith("/external")) {
            path = path.replace("external", "storage/emulated/0");
        } if (path.startsWith("/root/")) {
            path = path.replace("/root", "");
        }
        if (path.contains("file%3A%2F%2F%2F")) {
            path = path.replace("file%3A%2F%2F%2F", "").replace("%2F", "/");
        }
        if (path.contains("%2520")) {
            path = path.replace("%2520", " ");
        }
        return path;
    }

}