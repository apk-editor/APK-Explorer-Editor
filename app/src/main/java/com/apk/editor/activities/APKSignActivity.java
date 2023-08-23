package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.Common;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 19, 2021
 */
public class APKSignActivity extends AppCompatActivity {

    private AppCompatImageButton mClearKey, mClearCert;
    private MaterialTextView mKeySummary, mCertSummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apksign);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mClearKey = findViewById(R.id.clear_key);
        mClearCert = findViewById(R.id.clear_cert);
        MaterialTextView mInfo = findViewById(R.id.info);
        mKeySummary = findViewById(R.id.key_summary);
        mCertSummary = findViewById(R.id.cert_summary);
        FrameLayout mKey = findViewById(R.id.private_key);
        FrameLayout mCert = findViewById(R.id.cert);

        mInfo.setOnClickListener(v -> {
            Intent documentation = new Intent(this, DocumentationActivity.class);
            startActivity(documentation);
        });

        setStatus();

        mKey.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 29) {
                Intent keyPicker = new Intent(Intent.ACTION_GET_CONTENT);
                keyPicker.setType("*/*");
                keyPickerResultLauncher.launch(keyPicker);
            } else {
                Common.setPrivateKeyStatus(true);
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                startActivity(filePicker);
            }
        });

        mCert.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 29) {
                Intent certPicker = new Intent(Intent.ACTION_GET_CONTENT);
                certPicker.setType("*/*");
                certPickerResultLauncher.launch(certPicker);
            } else {
                Common.setRSATemplateStatus(true);
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                startActivity(filePicker);
            }
        });

        mBack.setOnClickListener(v -> finish());
    }

    private void setStatus() {
        if (sCommonUtils.getString("PrivateKey", null, this) != null) {
            mKeySummary.setText(sCommonUtils.getString("PrivateKey", null, this));
            mClearKey.setColorFilter(Color.RED);
            mClearKey.setVisibility(View.VISIBLE);
            mClearKey.setOnClickListener(v -> {
                sCommonUtils.saveString("PrivateKey", null, this);
                sFileUtils.delete(new File(getFilesDir(), "signing/APKEditor.pk8"));
                mKeySummary.setText(getString(R.string.private_key_summary));
                mClearKey.setVisibility(View.GONE);
            });
        } else {
            mClearKey.setVisibility(View.GONE);
        }

        if (sCommonUtils.getString("X509Certificate", null, this) != null) {
            mCertSummary.setText(sCommonUtils.getString("X509Certificate", null, this));
            mClearCert.setColorFilter(Color.RED);
            mClearCert.setVisibility(View.VISIBLE);
            mClearCert.setOnClickListener(v -> {
                sCommonUtils.saveString("X509Certificate", null, this);
                sFileUtils.delete(new File(getFilesDir(), "signing/APKEditorCert"));
                mCertSummary.setText(getString(R.string.x509_certificate_summary));
                mClearCert.setVisibility(View.GONE);
            });
        } else {
            mClearCert.setVisibility(View.GONE);
        }
    }

    @SuppressLint("StringFormatInvalid")
    ActivityResultLauncher<Intent> certPickerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (uriFile != null) {
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.signing_select_question, getString(R.string.x509_certificate)))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.select, (dialog, id) -> {
                                    sCommonUtils.saveString("X509Certificate", new File(getFilesDir(), "signing/APKEditorCert").getAbsolutePath(), this);
                                    sFileUtils.copy(uriFile, new File(getFilesDir(), "signing/APKEditorCert"), this);
                                    setStatus();
                                }).show();
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
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.signing_select_question, getString(R.string.private_key)))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.select, (dialog, id) -> {
                                    sCommonUtils.saveString("PrivateKey", new File(getFilesDir(), "signing/APKEditor.pk8").getAbsolutePath(), this);
                                    sFileUtils.copy(uriFile, new File(getFilesDir(), "signing/APKEditor.pk8"), this);
                                    setStatus();
                                }).show();
                    }
                }
            }
    );

    @Override
    public void onResume() {
        super.onResume();

        setStatus();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Common.hasPrivateKey()) Common.setPrivateKeyStatus(false);
        if (Common.hasRASATemplate()) Common.setRSATemplateStatus(false);
    }

}