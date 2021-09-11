package com.apk.editor.utils.apkSigner;

import android.util.Base64;

import java.io.InputStream;
import java.security.MessageDigest;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 * Based on the original work of Aefyr for https://github.com/Aefyr/PseudoApkSigner
 */
public class APKSignerUtils {

    static byte[] getFileHash(InputStream fileInputStream, String hashingAlgorithm) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(hashingAlgorithm);

        byte[] buffer = new byte[1024 * 1024];

        int read;
        while ((read = fileInputStream.read(buffer)) > 0)
            messageDigest.update(buffer, 0, read);

        fileInputStream.close();

        return messageDigest.digest();
    }

    static byte[] hash(byte[] bytes, String hashingAlgorithm) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(hashingAlgorithm);
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, 0);
    }

}