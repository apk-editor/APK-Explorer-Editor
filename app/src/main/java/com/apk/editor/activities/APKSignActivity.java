package com.apk.editor.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.android.apksig.internal.util.ByteStreams;
import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.interfaces.KeyStoreAliasChoiceDialog;
import com.apk.editor.interfaces.KeyStoreVerifierInterface;
import com.apk.editor.utils.APKSigner;
import com.apk.editor.utils.PK8File;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 19, 2021
 */
public class APKSignActivity extends AppCompatActivity {

    private AppCompatImageButton mClearKey;
    private MaterialTextView mKeySummary, mText;
    private JSONObject mJSONObject = null;
    private String mKeySummaryText = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apksign);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mClearKey = findViewById(R.id.clear_key);
        mKeySummary = findViewById(R.id.key_summary);
        mText = findViewById(R.id.text);
        FrameLayout mKey = findViewById(R.id.private_key);

        mKeySummaryText = getString(R.string.private_key_summary) + "\n" + "(PKCS #8 or PKCS #12)";

        setStatus();

        mKey.setOnClickListener(v -> {
            Intent keyPicker = new Intent(Intent.ACTION_GET_CONTENT);
            keyPicker.setType("*/*");
            keyPicker.addCategory(Intent.CATEGORY_OPENABLE);
            keyPickerResultLauncher.launch(keyPicker);
        });

        mClearKey.setOnClickListener(v -> {
            sFileUtils.delete(APKSigner.getPK8PrivateKey(this));
            sFileUtils.delete(APKSigner.getSigningCredentials(this));
            mKeySummary.setText(mKeySummaryText);
            mClearKey.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mText.setText(null);
        });

        mBack.setOnClickListener(v -> finish());
    }

    private void setStatus() {
        if (APKSigner.getSigningCredentials(this).exists()) {
            try {
                mJSONObject = new JSONObject(sFileUtils.read(APKSigner.getSigningCredentials(this)));
                mKeySummary.setText(mJSONObject.getString("privateKey"));
                mClearKey.setVisibility(View.VISIBLE);

                mText.setText(mJSONObject.getString("certificate"));
                mText.setVisibility(View.VISIBLE);
            } catch (JSONException ignored) {}
        } else {
            mKeySummary.setText(mKeySummaryText);
            mClearKey.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mText.setText(null);
        }
    }

    ActivityResultLauncher<Intent> certificatePickerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (uriFile != null) {
                        try {
                            X509Certificate x509Certificate = APKSigner.encodeCertificate(getContentResolver().openInputStream(uriFile));
                            if (x509Certificate != null) {
                                PrivateKey privateKey = new PK8File(APKSigner.getPK8PrivateKey(this)).getPrivateKey();
                                if (!isKeysMatches(privateKey, x509Certificate)) {
                                    new MaterialAlertDialogBuilder(this)
                                            .setMessage(R.string.keypair_mismatch_message)
                                            .setNegativeButton(getString(R.string.cancel), (dialog, id) -> sFileUtils.delete(APKSigner.getPK8PrivateKey(this)))
                                            .setPositiveButton(getString(R.string.choose_new), (dialog, id) -> chooseCertificate()
                                            ).show();
                                    return;
                                }
                                mJSONObject = new JSONObject();
                                String decodedPrivateKey = Base64.encodeToString(privateKey.getEncoded(), 0);
                                mJSONObject.put("privateKey", decodedPrivateKey);
                                String decodedCertificate = sFileUtils.read(uriFile, this);
                                mJSONObject.put("x509Certificate", decodedCertificate);
                                String summaryText = APKParser.getCertificateDetails(x509Certificate);
                                mJSONObject.put("certificate", summaryText);
                                sFileUtils.create(mJSONObject.toString(), APKSigner.getSigningCredentials(this));
                                mText.setVisibility(View.VISIBLE);
                                mText.setText(summaryText);
                            } else {
                                sCommonUtils.toast(getString(R.string.x509_certificate_invalid), this).show();
                            }
                        } catch (FileNotFoundException | JSONException ignored) {
                        }

                        setStatus();
                    } else {
                        sCommonUtils.toast(getString(R.string.file_path_error), this).show();
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> keyPickerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (uriFile != null) {
                        // Check if the selected file is a PK8 private key
                        PrivateKey privateKey = getPrivateKeyFromUri(uriFile);
                        if (privateKey != null) {
                            privateKeyToFile(privateKey, APKSigner.getPK8PrivateKey(this));
                            new MaterialAlertDialogBuilder(this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.app_name)
                                    .setMessage(R.string.x509_certificate_requirement_message)
                                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> sFileUtils.delete(APKSigner.getPK8PrivateKey(this)))
                                    .setPositiveButton(getString(R.string.select), (dialog, id) -> chooseCertificate()
                                    ).show();
                            mKeySummary.setText(Base64.encodeToString(privateKey.getEncoded(), 0));
                            mClearKey.setVisibility(View.VISIBLE);
                        } else {
                            // Check if the selected file is a PK12 Keystore
                            new KeyStoreVerifierInterface(getString(R.string.keystore_password_hint), this) {
                                @Override
                                public void positiveButtonLister(Editable s) {
                                    mJSONObject = new JSONObject();
                                    KeyStore keyStore = loadKeyStore(uriFile, s.toString().trim());
                                    if (keyStore ==  null) {
                                        sCommonUtils.toast(getString(R.string.keystore_loading_failed), APKSignActivity.this).show();
                                    } else {
                                        if (getAliases(keyStore) == null) {
                                            sCommonUtils.toast(getString(R.string.keystore_alias_unavailable), APKSignActivity.this).show();
                                        } else {
                                            new KeyStoreAliasChoiceDialog(getString(R.string.keystore_alias_hint),
                                                    getAliases(keyStore), 0, APKSignActivity.this) {

                                                @Override
                                                public void onItemSelected(int itemPosition) {
                                                    new KeyStoreVerifierInterface(getString(R.string.keystore_alias_password_hint), APKSignActivity.this) {
                                                        @Override
                                                        public void positiveButtonLister(Editable s) {
                                                            if (verify(keyStore, Objects.requireNonNull(getAliases(keyStore))[itemPosition], s.toString().trim())) {
                                                                try {
                                                                    PrivateKey privateKey = (PrivateKey) keyStore.getKey(Objects.requireNonNull(getAliases(keyStore))[itemPosition], s.toString().trim().toCharArray());
                                                                    X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(Objects.requireNonNull(getAliases(keyStore))[itemPosition]);

                                                                    if (privateKeyToFile(privateKey, APKSigner.getPK8PrivateKey(APKSignActivity.this))) {
                                                                        String decodedPrivateKey = Base64.encodeToString(privateKey.getEncoded(), 0);
                                                                        mJSONObject.put("privateKey", decodedPrivateKey);
                                                                    }
                                                                    String decodedCert = "-----BEGIN CERTIFICATE-----\n" + Base64.encodeToString(x509Certificate.getEncoded(), 0) + "-----END CERTIFICATE-----";
                                                                    mJSONObject.put("x509Certificate", decodedCert);
                                                                    String summaryText = APKParser.getCertificateDetails(x509Certificate);
                                                                    mJSONObject.put("certificate", summaryText);

                                                                    sFileUtils.create(mJSONObject.toString(), APKSigner.getSigningCredentials(APKSignActivity.this));

                                                                    mClearKey.setVisibility(View.VISIBLE);
                                                                    setStatus();
                                                                } catch (KeyStoreException |
                                                                         NoSuchAlgorithmException |
                                                                         JSONException |
                                                                         CertificateEncodingException |
                                                                         UnrecoverableKeyException ignored) {
                                                                }
                                                            } else {
                                                                sCommonUtils.toast(getString(R.string.password_invalid), APKSignActivity.this).show();
                                                            }
                                                        }
                                                    }.show();
                                                }
                                            }.show();
                                        }
                                    }
                                }
                            }.show();
                        }
                    } else {
                        sCommonUtils.toast(getString(R.string.file_path_error), this).show();
                    }
                }
            }
    );

    private KeyStore loadKeyStore(Uri uri, String ksPassword) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(is, ksPassword.toCharArray());
            return keystore;
        } catch (IOException | KeyStoreException | CertificateException |
                 NoSuchAlgorithmException ignored) {
            return null;
        }
    }

    private static boolean isKeysMatches(PrivateKey privateKey, X509Certificate certificate) {
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
        return rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus())
                && BigInteger.valueOf(2).modPow(rsaPublicKey.getPublicExponent()
                        .multiply(rsaPrivateKey.getPrivateExponent()).subtract(BigInteger.ONE),
                rsaPublicKey.getModulus()).equals(BigInteger.ONE);
    }

    private static boolean privateKeyToFile(PrivateKey privateKey, File destFile) {
        // Make sure signing environment exists
        if (!sFileUtils.exist(Objects.requireNonNull(destFile.getParentFile()))) {
            sFileUtils.mkdir(destFile.getParentFile());
        }
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                    privateKey.getEncoded());
            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(pkcs8EncodedKeySpec.getEncoded());
            fos.close();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static boolean verify(KeyStore keyStore, String alias, String password) {
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
            X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
            return privateKey != null && x509Certificate != null;
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException ignored) {
            return false;
        }
    }

    private PrivateKey getPrivateKeyFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] keyBytes = ByteStreams.toByteArray(Objects.requireNonNull(inputStream));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException ignored) {
            return null;
        }
    }

    private static String[] getAliases(KeyStore keyStore) {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            return new String[] {aliases.nextElement()};
        } catch (KeyStoreException ignored) {
            return null;
        }
    }

    private void chooseCertificate() {
        Intent keyPicker = new Intent(Intent.ACTION_GET_CONTENT);
        keyPicker.setType("*/*");
        keyPicker.addCategory(Intent.CATEGORY_OPENABLE);
        certificatePickerResultLauncher.launch(keyPicker);
    }

    @Override
    public void onResume() {
        super.onResume();

        setStatus();
    }

}