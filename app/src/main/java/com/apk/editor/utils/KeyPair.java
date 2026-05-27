package com.apk.editor.utils;

import android.content.Context;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

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

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on May 27, 2026
 */
public class KeyPair {

    private final Context context;
    private static final String CERT_START_STRING = "-----BEGIN CERTIFICATE-----";
    private static final String CERT_END_STRING = "-----END CERTIFICATE-----";

    public KeyPair(Context context) {
        this.context = context;

        // Ensure private key from old versions removed
        if (getPK8PrivateKey(context).exists() && !getSigningCredentials(context).exists()) {
            reset(context);
        }
    }

    public static File getPK8PrivateKey(Context context) {
        return new File(context.getFilesDir(), "signing/APKEditor.pk8");
    }

    public static File getSigningCredentials(Context context) {
        return new File(context.getFilesDir(), "signing/APKEditor_crd");
    }

    public static X509Certificate encodeCertificate(InputStream inputStream) throws CertificateException {
        return (X509Certificate) CertificateFactory
                .getInstance("X509")
                .generateCertificate(inputStream);
    }

    public static boolean isCustomSigning(Context context) {
        return sFileUtils.exist(getSigningCredentials(context)) && sFileUtils.exist(getPK8PrivateKey(context));
    }

    public PrivateKey getPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        byte[] keyBytes;
        File keyFile = getPK8PrivateKey(context);
        if (sFileUtils.exist(keyFile)) {
            FileInputStream fis = new FileInputStream(keyFile);
            DataInputStream dis = new DataInputStream(fis);
            keyBytes = new byte[(int) keyFile.length()];
            dis.readFully(keyBytes);
            dis.close();
        } else {
            String keyString =
                    "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDWCykoh8vbwIhD9s" +
                            "i8zOflqa+BvfX1Z5ito0mAuX0PhnDaI5lSDz1EHkk7sml0I+P0LnQxfIRs" +
                            "RFP6cktzfy+C0Kr/NrFsn8D5CjwXnFEGU5T6i3s0f3wuPY07Cl22jqIi+0" +
                            "QS6iZ7dUP6mLHMHHVH9TNnm3Tw1Jb2R/9oORlubwV6yzEkBeLMJLZymPpu" +
                            "iB8TX0dvT1+47hHHRLSSQqGu4lfJ7YI8zwLnXuyRw9V94l16Pu2BokE9KU" +
                            "tv4mrPiyY/C+/870KdVn7B7Yo0qEsW+rzd/ePMvWF9MJynxSqWlg20Dt0D" +
                            "OuOSBgMM+sXCZ5SYGVMqkLl2WLZZRXUedqPVAgMBAAECggEBAL1+O/3p2w" +
                            "y9suWYgbbEITkttHdEWY/dG0n+GYCgtpscBqTYh1AitbasqWD0Xm+3poPy" +
                            "7GMzPXksNLywmyOxIbrKSdiP7xufgxP3f6PXe9QCAw6clYKwqpu1Wmc+ki" +
                            "CgveICWQ31xgPemUQuEpoFR79g0RWUhz7+tndq3i7C/kTI3ahbm1bq0gU2" +
                            "xKr6J31DGfJu2YViXhDxl0/7/w95LxSILWnRNFJo70Sxo4igA6TOTQBo06" +
                            "9yyB9J0uwkW+P1OY5CPs2yywtRrphhdtXirstXXyn/thU3OItz8tYEtWHu" +
                            "gr5v1y0D3yEODrc3P1bWOPu5dn2Io4Fx90TmkazT3IECgYEA+0m2nY4md4" +
                            "ydmzVvzyAU/QURvRS9yBLpkIurTbt2shPPByuwl8iosfthyLBNrhCfwdSD" +
                            "ZEh9YbTgaG4Rx9a0QSA5RQLcpF4SVN1fD5dQ2gp3Ig9R58HXWLIX0ERcYk" +
                            "2VqRTrVQELJXz/M65T+tKdjQlYpE210R2JH9Dqhx0AGb0CgYEA2g6ouWn4" +
                            "TM2nAfUs5l8m6P1WrtDV8yBzaRl8kTwH3GnkBkDzc24SpcoCpUqd5giHPM" +
                            "ECjjd5YP82nDU63ccC2imgAqPeF1iJW3XBXV95jeVt8DAC8PRxdFFDVxn5" +
                            "NR2Myd7JoyqtfdpfmoXS53dPQ2WnoRVM/JlP0riVAGGON/kCgYEA6JxxuS" +
                            "MvJJc8BcLPf1JZW2Zn5znd++jV4IIJzujrlSiVCjQ9QiPzVN44xEe/gJPO" +
                            "7uRDxH794YZH/SN2viBXt7mWifV+PYD/QyOwrYQKyevKH/NChGCBcY9aT+" +
                            "YYBr9+/Idq2MMgiFFPA44qGxL/2OB/94gf+DV5C8SedPg5cZkCgYAFXej/" +
                            "L0GCOmmK3cruHJdrkpioktFBO6I7ivoK5QxYe262TLDxPVtOI0uvX8fFGp" +
                            "6heyqh73GYVo+0hobdYIGMdwvOJNRZhL9UtfA9aRUnzebHy5a28X09XKao" +
                            "pSYCDakias5RqsI8X7yMpBCNy9zyjrTyfQC5EtjpGcvpB32lmQKBgQDLjm" +
                            "n3zEOVvUl3+taJj4+yXvwJNRuiT5PlfLcs3QpziDIzqHpYJCv1YQmK3VMY" +
                            "YN/jbVyIJa4ICK/YaKorI/+VYW4QotPJH4ALjaRfVLOCRjo/R4vQ+qr2fT" +
                            "I2OXWErZmZVwJeCuwMn0gkV30jZnUXBwhe/1/cSeqMxvYk3TATUQ==";
            keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public X509Certificate getCertificate() throws JSONException, CertificateException {
        String certificateString;
        if (getSigningCredentials(context).exists()) {
            JSONObject jsonObject = new JSONObject(sFileUtils.read(getSigningCredentials(context)));
            certificateString = jsonObject.getString("x509Certificate");
        } else {
            certificateString = CERT_START_STRING + "\n" +
                    "MIIDgzCCAmugAwIBAgIEYNn5AjANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJE\n" +
                    "RTEPMA0GA1UECBMGQmVybGluMQ8wDQYDVQQHEwZCZXJsaW4xDDAKBgNVBAoTA0FF\n" +
                    "RTEMMAoGA1UECxMDQUVFMSQwIgYDVQQDDBtBUEsgRXhwbG9yZXIgJiBFZGl0b3Ig\n" +
                    "KEFFRSkwIBcNMjYwNTI3MjAzMjA0WhgPMjA1MzEwMTIyMDMyMDRaMHExCzAJBgNV\n" +
                    "BAYTAkRFMQ8wDQYDVQQIEwZCZXJsaW4xDzANBgNVBAcTBkJlcmxpbjEMMAoGA1UE\n" +
                    "ChMDQUVFMQwwCgYDVQQLEwNBRUUxJDAiBgNVBAMMG0FQSyBFeHBsb3JlciAmIEVk\n" +
                    "aXRvciAoQUVFKTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANYLKSiH\n" +
                    "y9vAiEP2yLzM5+Wpr4G99fVnmK2jSYC5fQ+GcNojmVIPPUQeSTuyaXQj4/QudDF8\n" +
                    "hGxEU/pyS3N/L4LQqv82sWyfwPkKPBecUQZTlPqLezR/fC49jTsKXbaOoiL7RBLq\n" +
                    "Jnt1Q/qYscwcdUf1M2ebdPDUlvZH/2g5GW5vBXrLMSQF4swktnKY+m6IHxNfR29P\n" +
                    "X7juEcdEtJJCoa7iV8ntgjzPAude7JHD1X3iXXo+7YGiQT0pS2/ias+LJj8L7/zv\n" +
                    "Qp1WfsHtijSoSxb6vN3948y9YX0wnKfFKpaWDbQO3QM645IGAwz6xcJnlJgZUyqQ\n" +
                    "uXZYtllFdR52o9UCAwEAAaMhMB8wHQYDVR0OBBYEFMcPPQrCFCOYdUoCTtDP3ldQ\n" +
                    "oi/MMA0GCSqGSIb3DQEBCwUAA4IBAQCgZOouK/fCd96NuseFVMexC3Cn1RWS3oPc\n" +
                    "ABKg62nCbUs3t+Nq+80Kmoqw33WF+eIDzVI3wm4FuhadzDoyC5trfX9OMEIHtNs/\n" +
                    "cydZQ2EN3+ePtSvN3Rfh64zS6Wp+bQP9btVxKcKug+POXeiJ65LSVGjL1XAXRf7j\n" +
                    "Op5kd63Ex6HEY7+jhHlCLzenX6qpOsU4dtyxprDWPh1mP/9+DPRp1Vq9ocX5AoZl\n" +
                    "/+Aerzn9cqsGk0XZ/zNNcWp+tQJKlaJRlsyCROUNcl+rYn05OZ4HW7up/s6GdGPY\n" +
                    "fLaiSO5dcKIZkzoAJ7NrySt9JqfPHqz2hisDHZCQHMB7IQyaEIRt" + "\n"
                    + CERT_END_STRING;
        }
        return encodeCertificate(new ByteArrayInputStream(certificateString.getBytes()));
    }

    public static String getCredentials(Context context) {
        return sFileUtils.read(getSigningCredentials(context));
    }

    public static void reset(Context context) {
        if (sFileUtils.exist(getPK8PrivateKey(context))) {
            sFileUtils.delete(getPK8PrivateKey(context));
        }
        if (sFileUtils.exist(getSigningCredentials(context))) {
            sFileUtils.delete(getSigningCredentials(context));
        }
    }

}