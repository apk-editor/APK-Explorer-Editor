package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

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
        setContentView(R.layout.permission_layout);

        if (APKExplorer.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(Build.VERSION.SDK_INT >= 30 ? getString(R.string.file_permission_request_message, getString(R.string.app_name)) : getString(R.string.permission_denied_message));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mPermissionGrant.setOnClickListener(v -> APKExplorer.requestPermission(this));
            return;
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
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> finish())
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
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
                }
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.split_apk_installer)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && Build.VERSION.SDK_INT < 30 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}