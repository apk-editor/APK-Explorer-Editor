package com.apk.editor.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends AppCompatActivity {

    private String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!APKEditorUtils.isWritePermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            finish();
        }

        if (getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (APKEditorUtils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mPath = APKEditorUtils.getPath(file);
            }
            if (mPath != null && APKEditorUtils.exist(mPath)) {
                if (mPath.endsWith(".apk")) {
                    SplitAPKInstaller.installAPK(new File(mPath), this);
                    finish();
                } else if (mPath.endsWith(".apkm") || mPath.endsWith(".apks") || mPath.endsWith(".xapk")) {
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(getString(R.string.bundle_install_question, new File(mPath).getName()))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                finish();
                            })
                            .setPositiveButton(R.string.install, (dialogInterface, i) -> {
                                SplitAPKInstaller.handleAppBundle(mPath, this);
                                finish();
                            }).show();
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                finish();
                            }).show();
                }
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.split_apk_installer)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            finish();
                        }).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}