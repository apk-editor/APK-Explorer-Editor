package com.apk.editor.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct 10, 2024
 */
public class PK8File extends File {

    public PK8File(File keyFile) {
        super(keyFile.toURI());
    }

    public PrivateKey getPrivateKey() {
        try {
            FileInputStream fis = new FileInputStream(getAbsoluteFile());
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) getAbsoluteFile().length()];
            dis.readFully(keyBytes);
            dis.close();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException ignored) {
            return null;
        }
    }

}
