package com.apk.editor.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AsyncTasks;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends AppCompatActivity {

    private File mFile = null;
    private String mExtension = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_layout);

        if (APKExplorer.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_message));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mPermissionGrant.setOnClickListener(v -> APKExplorer.requestPermission(this));
            return;
        }

        if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), this).execute();
        }
    }

    private AsyncTasks manageInstallation(Uri uri, Activity activity) {
        return new AsyncTasks() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.preparing_installation));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                APKEditorUtils.delete(getExternalFilesDir("APK").getAbsolutePath());
                mExtension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
                mFile = new File(getExternalFilesDir("APK"), "tmp." + mExtension);
            }

            @Override
            public void doInBackground() {
                try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    int read;
                    byte[] bytes = new byte[8192];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                } catch (IOException ignored) {}
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (mFile.exists()) {
                    if (mExtension.equals("apk")) {
                        SplitAPKInstaller.installAPK(mFile, activity);
                        finish();
                    } else if (mExtension.equals("apkm") || mExtension.equals("apks") || mExtension.equals("xapk")) {
                        new MaterialAlertDialogBuilder(activity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.split_apk_installer)
                                .setMessage(getString(R.string.install_bundle_question))
                                .setCancelable(false)
                                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> finish())
                                .setPositiveButton(R.string.install, (dialogInterface, i) -> {
                                    SplitAPKInstaller.handleAppBundle(mFile.getAbsolutePath(), activity);
                                    finish();
                                }).show();
                    } else {
                        new MaterialAlertDialogBuilder(activity)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.split_apk_installer)
                                .setMessage(getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                                .setCancelable(false)
                                .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
                    }
                } else {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(getString(R.string.file_path_error))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
                }
            }
        };
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