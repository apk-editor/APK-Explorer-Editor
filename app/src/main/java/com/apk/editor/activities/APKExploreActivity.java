package com.apk.editor.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        AppCompatImageView mApplicationIcon = findViewById(R.id.app_image);
        MaterialTextView mApplicationName = findViewById(R.id.app_title);
        MaterialTextView mPackageName = findViewById(R.id.package_id);

        mApplicationIcon.setImageBitmap(APKExplorer.getAppIcon(Common.getPath() + "/.aeeBackup/appData"));
        mApplicationName.setText(APKExplorer.getAppName(Common.getPath() + "/.aeeBackup/appData"));
        mPackageName.setText(APKExplorer.getPackageName(Common.getPath() + "/.aeeBackup/appData"));
        mPackageName.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new APKExplorerFragment()).commit();
    }

}