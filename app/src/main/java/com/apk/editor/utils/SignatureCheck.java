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
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

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

    public static boolean isSignatureMatched(String packageName, Context context) {
        String mAPKEditorKey = "[48, -126, 2, -55, 48, -126, 1, -79, -96, 3, 2, 1, 2, 2, 4, 124, -40, 27, 108, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 3, 19, 10, 65, 80, 75, 32, 69, 100, 105, 116, 111, 114, 48, 30, 23, 13, 50, 49, 48, 50, 50, 55, 49, 50, 52, 51, 52, 52, 90, 23, 13, 52, 54, 48, 50, 50, 49, 49, 50, 52, 51, 52, 52, 90, 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 3, 19, 10, 65, 80, 75, 32, 69, 100, 105, 116, 111, 114, 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -68, -49, -118, -68, 4, -117, -120, -119, 118, 77, -104, 27, -91, 19, -48, -74, 61, -102, -99, 53, 18, 126, -52, -68, -121, -24, -102, -22, 80, -5, -17, -73, 120, -92, -6, 28, 66, 1, -51, -54, -43, -83, 100, -44, 110, 107, -67, 124, -72, -94, -36, 37, -92, 26, 108, 52, -113, 30, -110, 118, 59, -17, -40, -70, 89, 42, 79, 21, -61, 106, 65, -46, -78, 57, 123, 5, -51, -115, 119, 35, -62, -111, 45, 65, 0, -49, -69, 70, 26, 27, -52, 28, 70, 8, -128, -105, 5, 102, -28, -67, 106, -57, 24, -110, -4, 106, 105, -18, 59, 9, 13, 11, 35, -105, 27, 126, 83, 125, -67, 95, -111, 56, 56, 54, -32, -24, 96, -51, 17, -17, -13, -78, -49, 86, -37, -32, 16, 32, 62, -59, 108, -11, 77, 81, 99, 55, 55, 51, 8, 51, 57, 124, -117, -75, -43, 33, 11, -93, -51, -120, -117, -20, 29, -59, 19, -118, 23, 49, -86, -95, 65, -63, 6, -17, 42, 56, 44, 125, -12, 112, 66, -7, -109, -33, 46, 22, -110, -100, -46, 117, -128, 111, 74, -52, 7, -119, -51, 102, 127, 93, 123, 19, 83, -24, 37, 28, -100, 69, -57, 85, -35, 91, 12, 53, 30, -34, 94, -108, 102, -71, 126, -40, -13, -65, -127, -58, 69, 49, -78, -66, -40, -78, 64, 74, 75, 125, -38, 49, -97, 112, 126, -40, 72, 22, 11, -96, 30, -113, 1, -23, -70, -25, 75, 102, -39, -111, 2, 3, 1, 0, 1, -93, 33, 48, 31, 48, 29, 6, 3, 85, 29, 14, 4, 22, 4, 20, -82, 20, -29, 68, 29, 107, -56, -41, -12, 13, -97, -118, -32, 103, -22, -11, 118, 65, -21, -47, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 3, -126, 1, 1, 0, 99, -100, 104, -71, 3, 67, -29, 3, 104, 41, 113, -47, -45, 53, -106, 4, 4, 113, -74, 123, -17, 72, 84, 42, -66, 45, -90, 91, -102, -114, 95, -107, -68, -75, 94, 49, -112, -4, 88, -87, -52, -56, -26, -76, -95, 102, 96, 123, 87, -128, 37, 109, -39, -25, 96, 35, -107, 43, -122, 62, -25, 44, 86, 26, -15, -120, -14, -50, -77, 116, -79, 104, -55, 106, 0, -119, 51, -16, -93, -55, 26, 60, 7, 64, 62, -60, -47, 42, -18, -103, -94, 21, 7, -20, 63, 96, -59, 26, -16, 36, -49, -49, 96, 106, -13, -19, -107, -96, -54, 64, 117, 36, 121, -46, -54, 64, 98, 61, 96, 2, -70, -102, 4, -83, 111, -93, -91, -61, -128, -58, -124, 37, 4, -36, -127, -5, 76, 17, -12, -16, 123, -18, 110, 44, -13, -126, -47, -14, -58, -49, 105, 26, -34, -65, 66, -23, -106, 98, 114, 103, 124, 65, -99, -47, 41, 54, -109, 125, -5, -67, -51, 14, 31, -22, -39, 61, -128, -95, 114, -88, -84, -29, -61, -103, -8, -118, -48, -95, -45, -10, 55, -66, 61, 51, -84, -114, 17, 113, -36, 87, 99, 98, -58, 103, 34, 39, 91, -21, 89, -37, -73, 104, -112, -56, -26, -35, -87, -34, -91, -11, 29, -119, -34, 5, 15, 50, 115, -38, -17, 88, -121, -8, -60, 88, -45, -28, 5, -13, 30, -33, 28, -77, 55, 82, 52, 26, -21, 38, -73, -80, -82, -7, -119, -81, 89, 25]";
        String mAppKey = Arrays.toString(Objects.requireNonNull(getSignature(packageName, context)));
        return mAPKEditorKey.equals(mAppKey);
    }

    @SuppressLint("PackageManagerGetSignatures")
    private static byte[] getSignature(String packageName, Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return signatureToBytes(pkgInfo.signatures);
        } catch (PackageManager.NameNotFoundException ignored) {}
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