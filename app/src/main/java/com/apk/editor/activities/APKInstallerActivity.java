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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager.widget.ViewPager;

import com.apk.editor.R;
import com.apk.editor.adapters.PagerAdapter;
import com.apk.editor.fragments.APKDetailsFragment;
import com.apk.editor.fragments.CertificateFragment;
import com.apk.editor.fragments.ManifestFragment;
import com.apk.editor.fragments.PermissionsFragment;
import com.apk.editor.utils.APKCertificate;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AsyncTasks;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.recyclerViewItems.APKItems;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends AppCompatActivity {

    private AppCompatImageView mAppIcon;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialCardView mCancel, mInstall;
    private MaterialTextView mAppName, mPackageID;
    private String mExtension = null;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkdetails);

        mAppIcon = findViewById(R.id.app_image);
        mAppName = findViewById(R.id.app_title);
        mPackageID = findViewById(R.id.package_id);
        mMainLayout = findViewById(R.id.main_layout);
        mIconsLayout = findViewById(R.id.icons_layout);
        mInstall = findViewById(R.id.install);
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        if (APKExplorer.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
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
                        Common.setAPKFile(mFile);
                        Common.isFMInstall(true);
                        loadAPKDetails(activity);
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

    private void loadAPKDetails(Activity activity) {
        APKItems apkData = APKExplorer.getAPKData(mFile.getAbsolutePath(), activity);
        if (apkData != null) {
            mMainLayout.setVisibility(View.VISIBLE);
            mIconsLayout.setVisibility(View.VISIBLE);

            PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
            try {
                if (apkData.getAppName() != null) {
                    mAppName.setText(apkData.getAppName());
                    mAppName.setVisibility(View.VISIBLE);
                }
                if (apkData.getPackageName() != null) {
                    mPackageID.setText(apkData.getPackageName());
                    mPackageID.setVisibility(View.VISIBLE);
                }
                if (apkData.getIcon() != null) {
                    mAppIcon.setImageDrawable(apkData.getIcon());
                }

                adapter.AddFragment(new APKDetailsFragment(), getString(R.string.details));
                if (apkData.getPermissions() != null) {
                    adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
                }
                if (apkData.getManifest() != null) {
                    adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));
                }
                if (APKCertificate.getCertificateDetails(mFile.getAbsolutePath(), activity) != null) {
                    adapter.AddFragment(new CertificateFragment(), getString(R.string.certificate));
                }
            } catch (Exception ignored) {}

            mViewPager.setAdapter(adapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }

        mCancel.setOnClickListener(v -> finish());
        mInstall.setOnClickListener(v -> {
            Common.getAPKList().add(mFile.getAbsolutePath());
            APKExplorer.handleAPKs(activity);
        });
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