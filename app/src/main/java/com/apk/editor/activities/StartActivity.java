package com.apk.editor.activities;

import android.content.Intent;
import android.os.Bundle;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.google.android.material.button.MaterialButton;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start, R.id.layout_root);

        MaterialButton mStart = findViewById(R.id.start);

        mStart.setOnClickListener(v -> {
            sCommonUtils.saveBoolean("welcome_message", true, this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

}