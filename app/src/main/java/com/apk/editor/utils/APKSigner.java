package com.apk.editor.utils;

import android.content.Context;
import android.os.Build;

import com.android.apksig.ApkSigner;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 16, 2023
 */
public class APKSigner {

    private final Context mContext;

    public APKSigner(Context context) {
        mContext = context;
    }

    private static PrivateKey getPrivateKey(File keyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        FileInputStream fis = new FileInputStream(keyFile);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) keyFile.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf =
                KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static X509Certificate getCertificate(File certificateFile) throws CertificateException {
        String certificateString;
        if (certificateFile.exists()) {
            certificateString = sFileUtils.read(certificateFile);
        } else {
            certificateString = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICyTCCAbGgAwIBAgIEfNgbbDANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwpB\n" +
                    "UEsgRWRpdG9yMB4XDTIxMDIyNzEyNDM0NFoXDTQ2MDIyMTEyNDM0NFowFTETMBEG\n" +
                    "A1UEAxMKQVBLIEVkaXRvcjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
                    "ALzPirwEi4iJdk2YG6UT0LY9mp01En7MvIfomupQ+++3eKT6HEIBzcrVrWTUbmu9\n" +
                    "fLii3CWkGmw0jx6Sdjvv2LpZKk8Vw2pB0rI5ewXNjXcjwpEtQQDPu0YaG8wcRgiA\n" +
                    "lwVm5L1qxxiS/Gpp7jsJDQsjlxt+U329X5E4ODbg6GDNEe/zss9W2+AQID7FbPVN\n" +
                    "UWM3NzMIMzl8i7XVIQujzYiL7B3FE4oXMaqhQcEG7yo4LH30cEL5k98uFpKc0nWA\n" +
                    "b0rMB4nNZn9dexNT6CUcnEXHVd1bDDUe3l6UZrl+2PO/gcZFMbK+2LJASkt92jGf\n" +
                    "cH7YSBYLoB6PAem650tm2ZECAwEAAaMhMB8wHQYDVR0OBBYEFK4U40Qda8jX9A2f\n" +
                    "iuBn6vV2QevRMA0GCSqGSIb3DQEBCwUAA4IBAQBjnGi5A0PjA2gpcdHTNZYEBHG2\n" +
                    "e+9IVCq+LaZbmo5flby1XjGQ/FipzMjmtKFmYHtXgCVt2edgI5Urhj7nLFYa8Yjy\n" +
                    "zrN0sWjJagCJM/CjyRo8B0A+xNEq7pmiFQfsP2DFGvAkz89gavPtlaDKQHUkedLK\n" +
                    "QGI9YAK6mgStb6Olw4DGhCUE3IH7TBH08HvubizzgtHyxs9pGt6/QumWYnJnfEGd\n" +
                    "0Sk2k337vc0OH+rZPYChcqis48OZ+IrQodP2N749M6yOEXHcV2NixmciJ1vrWdu3\n" +
                    "aJDI5t2p3qX1HYneBQ8yc9rvWIf4xFjT5AXzHt8cszdSNBrrJrewrvmJr1kZ\n" +
                    "-----END CERTIFICATE-----";
        }
        InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes());
        return (X509Certificate) CertificateFactory
                .getInstance("X509")
                .generateCertificate(inputStream);
    }

    public void sign(File apkFile, File output) throws Exception {
        ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder("CERT", getPrivateKey(
                new File(mContext.getFilesDir(), "signing/APKEditor.pk8")), Collections.singletonList(getCertificate(
                        new File(mContext.getFilesDir(), "signing/APKEditorCert")))).build();
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