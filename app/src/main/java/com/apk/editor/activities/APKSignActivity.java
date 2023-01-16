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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.Common;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

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
                Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
                installer.setType("*/*");
                installer.addCategory(Intent.CATEGORY_OPENABLE);
                installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                startActivityForResult(installer, 0);
            } else {
                Common.setPrivateKeyStatus(true);
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                startActivity(filePicker);
            }
        });

        mCert.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 29) {
                Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
                installer.setType("*/*");
                installer.addCategory(Intent.CATEGORY_OPENABLE);
                installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                startActivityForResult(installer, 1);
            } else {
                Common.setRSATemplateStatus(true);
                Intent filePicker = new Intent(this, FilePickerActivity.class);
                startActivity(filePicker);
            }
        });

        mBack.setOnClickListener(v -> finish());
    }

    private void setStatus() {
        if (sUtils.getString("PrivateKey", null, this) != null) {
            mKeySummary.setText(sUtils.getString("PrivateKey", null, this));
            mClearKey.setColorFilter(Color.RED);
            mClearKey.setVisibility(View.VISIBLE);
            mClearKey.setOnClickListener(v -> {
                sUtils.saveString("PrivateKey", null, this);
                new File(getFilesDir(), "signing/APKEditor.pk8").delete();
                mKeySummary.setText(getString(R.string.private_key_summary));
                mClearKey.setVisibility(View.GONE);
            });
        } else {
            mClearKey.setVisibility(View.GONE);
        }

        if (sUtils.getString("X509Certificate", null, this) != null) {
            mCertSummary.setText(sUtils.getString("X509Certificate", null, this));
            mClearCert.setColorFilter(Color.RED);
            mClearCert.setVisibility(View.VISIBLE);
            mClearCert.setOnClickListener(v -> {
                sUtils.saveString("X509Certificate", null, this);
                new File(getFilesDir(), "signing/APKEditorCert").delete();
                mCertSummary.setText(getString(R.string.x509_certificate_summary));
                mClearCert.setVisibility(View.GONE);
            });
        } else {
            mClearCert.setVisibility(View.GONE);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.signing_select_question, requestCode == 0 ? getString(R.string.private_key) : getString(R.string.x509_certificate)))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.select, (dialog, id) -> {
                            if (requestCode == 0) {
                                sUtils.saveString("PrivateKey", new File(getFilesDir(), "signing/APKEditor.pk8").getAbsolutePath(), this);
                                sUtils.copy(uri, new File(getFilesDir(), "signing/APKEditor.pk8"), this);
                            } else if (requestCode == 1) {
                                sUtils.saveString("X509Certificate", new File(getFilesDir(), "signing/APKEditorCert").getAbsolutePath(), this);
                                sUtils.copy(uri, new File(getFilesDir(), "signing/APKEditorCert"), this);
                            }
                            setStatus();
                        }).show();
            }
        }
    }

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