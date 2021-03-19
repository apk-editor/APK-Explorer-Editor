package com.apk.editor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 19, 2021
 */
public class APKSignActivity extends AppCompatActivity {

    private AppCompatImageButton mClearKey, mClearRSA;
    private MaterialTextView mKeySummary, mRSASummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apksign);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mClearKey = findViewById(R.id.clear_key);
        mClearRSA = findViewById(R.id.clear_rsa);
        MaterialTextView mInfo = findViewById(R.id.info);
        mKeySummary = findViewById(R.id.key_summary);
        mRSASummary = findViewById(R.id.rsa_summary);
        FrameLayout mKey = findViewById(R.id.private_key);
        FrameLayout mRSA = findViewById(R.id.rsa);

        mInfo.setOnClickListener(v -> {
            Intent documentation = new Intent(this, DocumentationActivity.class);
            startActivity(documentation);
        });

        setStatus();

        mKey.setOnClickListener(v -> {
            APKExplorer.mPrivateKey = true;
            Intent filePicker = new Intent(this, FilePickerActivity.class);
            startActivity(filePicker);
        });

        mRSA.setOnClickListener(v -> {
            APKExplorer.mRSATemplate = true;
            Intent filePicker = new Intent(this, FilePickerActivity.class);
            startActivity(filePicker);
        });

        mBack.setOnClickListener(v -> finish());
    }

    private void setStatus() {
        if (APKEditorUtils.getString("PrivateKey", null, this) != null) {
            mKeySummary.setText(APKEditorUtils.getString("PrivateKey", null, this));
            mClearKey.setColorFilter(Color.RED);
            mClearKey.setVisibility(View.VISIBLE);
            mClearKey.setOnClickListener(v -> {
                APKEditorUtils.saveString("PrivateKey", null, this);
                new File(getFilesDir(), "signing/APKEditor.pk8").delete();
                mKeySummary.setText(getString(R.string.private_key_summary));
                mClearKey.setVisibility(View.GONE);
            });
        } else {
            mClearKey.setVisibility(View.GONE);
        }

        if (APKEditorUtils.getString("RSATemplate", null, this) != null) {
            mRSASummary.setText(APKEditorUtils.getString("RSATemplate", null, this));
            mClearRSA.setColorFilter(Color.RED);
            mClearRSA.setVisibility(View.VISIBLE);
            mClearRSA.setOnClickListener(v -> {
                APKEditorUtils.saveString("RSATemplate", null, this);
                new File(getFilesDir(), "signing/APKEditor").delete();
                mRSASummary.setText(getString(R.string.rsa_template_summary));
                mClearRSA.setVisibility(View.GONE);
            });
        } else {
            mClearRSA.setVisibility(View.GONE);
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

        if (APKExplorer.mPrivateKey) APKExplorer.mPrivateKey = false;
        if (APKExplorer.mRSATemplate) APKExplorer.mRSATemplate = false;
    }

}