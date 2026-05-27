package com.apk.editor.utils;

import android.content.Context;
import android.os.Build;

import com.android.apksig.ApkSigner;

import java.io.File;
import java.util.Collections;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 16, 2023
 */
public class APKSigner {

    public APKSigner() {
    }

    @SuppressWarnings("deprecation")
    public void sign(File apkFile, File output, Context context) throws Exception {
        KeyPair keyStore = new KeyPair(context);
        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder(
                        "CERT",
                        keyStore.getPrivateKey(),
                        Collections.singletonList(keyStore.getCertificate())
                ).build();
        ApkSigner.Builder builder = new ApkSigner.Builder(Collections.singletonList(signerConfig));
        builder.setInputApk(apkFile);
        builder.setOutputApk(output);
        builder.setCreatedBy("APK Editor");
        builder.setV1SigningEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setV2SigningEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setV3SigningEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setV4SigningEnabled(true);
        }
        builder.setMinSdkVersion(-1);
        ApkSigner signer = builder.build();
        signer.sign();
    }

}