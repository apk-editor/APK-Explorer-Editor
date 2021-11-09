package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 * Ref: https://gitlab.com/guardianproject/checkey/-/blob/master/app/src/main/java/info/guardianproject/checkey/Utils.java
 */
public class APKCertificate {

    private static String getCertificateFingerprint(X509Certificate cert, String hashAlgorithm) {
        String hash;
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            byte[] rawCert = cert.getEncoded();
            hash = toHexString(md.digest(rawCert));
            md.reset();
        } catch (CertificateEncodingException e) {
            hash = "CertificateEncodingException";
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            hash = "NoSuchAlgorithm";
            e.printStackTrace();
        }
        return hash;
    }

    private static X509Certificate[] getX509Certificates(String path, Context context) {
        X509Certificate[] certs = null;
        CertificateFactory certificateFactory;
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo pkgInfo = AppData.getPackageManager(context).getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES);
            certificateFactory = CertificateFactory.getInstance("X509");
            certs = new X509Certificate[pkgInfo.signatures.length];
            for (int i = 0; i < certs.length; i++) {
                byte[] cert = pkgInfo.signatures[i].toByteArray();
                InputStream inStream = new ByteArrayInputStream(cert);
                certs[i] = (X509Certificate) certificateFactory.generateCertificate(inStream);
            }
        } catch (CertificateException ignored) {
        }
        return certs;
    }

    private static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static String getCertificateDetails(String path, Context context) {
        StringBuilder sb = new StringBuilder();
        X509Certificate[] certs = getX509Certificates(path, context);
        if (certs == null || certs.length < 1) {
            return null;
        }
        X509Certificate cert = certs[0];

        PublicKey publickey = cert.getPublicKey();
        try {
            sb.append("Subject: ").append(cert.getSubjectDN().getName()).append("\n\n");
            sb.append("Issuer: ").append(cert.getIssuerDN().getName()).append("\n\n");
            sb.append("Issued Date: ").append(cert.getNotBefore().toString()).append("\n");
            sb.append("Expiry Date: ").append(cert.getNotAfter().toString()).append("\n");
            sb.append("Algorithm: ").append(cert.getSigAlgName()).append(", Type: ").append(publickey.getFormat()).append(", Version: ").append(cert.getVersion()).append("\n");
            sb.append("Serial Number: ").append(cert.getSerialNumber().toString(16)).append("\n");
            sb.append("\nChecksums\n").append("MD5: ").append(getCertificateFingerprint(cert, "MD5").toLowerCase(Locale.ENGLISH)).append("\n");
            sb.append("SHA1: ").append(getCertificateFingerprint(cert, "SHA1").toLowerCase(Locale.ENGLISH)).append("\n");
            sb.append("SHA-256: ").append(getCertificateFingerprint(cert, "SHA-256").toLowerCase(Locale.ENGLISH)).append("\n");
            sb.append("\nPublic Key\n").append(publickey.toString().split("=")[1].split(",")[0]);
        } catch (Exception ignored) {}
        return sb.toString();
    }

}