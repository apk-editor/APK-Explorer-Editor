package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class SignatureCheck {

    public static boolean isPackageInstalled(String packageID, Context context) {
        try {
            context.getPackageManager().getApplicationInfo(packageID, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isSignatureMatched(String packageName, File apkFile, Context context) {
        return Arrays.equals(getApkSignature(apkFile, context), getPackageSignature(packageName, context));
    }

    @SuppressLint("PackageManagerGetSignatures")
    private static byte[] getPackageSignature(String packageName, Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return signatureToBytes(pkgInfo.signatures);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return null;
    }

    private static byte[] getApkSignature(File apkFile, Context context) {
        final String pkgPath = apkFile.getAbsolutePath();
        if (apkFile.exists()) {
            PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(pkgPath, PackageManager.GET_SIGNATURES);
            if (pkgInfo == null) {
                throw new IllegalArgumentException("Could not find PackageInfo for package at " + pkgPath);
            }
            return signatureToBytes(pkgInfo.signatures);
        }
        return null;
    }

    private static byte[] signatureToBytes(Signature[] signatures) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Signature sig : signatures) {
            try {
                outputStream.write(sig.toByteArray());
            } catch (IOException ignored) {}
        }
        return outputStream.toByteArray();
    }

    public static void showSignatureErrorDialog(Drawable icon, String name, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(icon)
                .setTitle(R.string.signature_error)
                .setMessage(context.getString(R.string.signature_error_summary, name))
                .setPositiveButton(R.string.cancel, (dialog, id) -> {
                }).show();
    }

}