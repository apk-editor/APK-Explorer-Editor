package com.apk.editor.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager.widget.ViewPager;

import com.apk.editor.R;
import com.apk.editor.fragments.APKDetailsFragment;
import com.apk.editor.fragments.CertificateFragment;
import com.apk.editor.fragments.ManifestFragment;
import com.apk.editor.fragments.PermissionsFragment;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.ExternalAPKData;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.recyclerViewItems.APKItems;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.Utils.sAPKCertificateUtils;
import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends AppCompatActivity {

    private AppCompatImageView mAppIcon;
    private Drawable mIcon = null;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialCardView mCancel, mInstall;
    private MaterialTextView mAppName, mPackageID;
    private String mName = null, mExtension = null, mPackageName = null;
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

        if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), this).execute();
        }
    }

    private sExecutor manageInstallation(Uri uri, Activity activity) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.loading));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                sUtils.delete(getExternalFilesDir("APK"));
                mExtension = ExternalAPKData.getExtension(uri, activity);
                mFile = new File(getExternalFilesDir("APK"), "tmp." + mExtension);
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                sUtils.copy(uri, mFile, activity);
                try {
                    APKItems mAPKData = ExternalAPKData.getAPKData(mFile.getAbsolutePath(), activity);
                    if (mAPKData != null) {
                        if (mAPKData.getAppName() != null) {
                            mName = mAPKData.getAppName();
                        }
                        if (mAPKData.getPackageName() != null) {
                            mPackageName = mAPKData.getPackageName();
                        }
                        if (mAPKData.getIcon() != null) {
                            mIcon = mAPKData.getIcon();
                        }
                        if (mAPKData.getPermissions() != null) {
                            ExternalAPKData.setPermissions(mAPKData.getPermissions());
                        }
                        if (mAPKData.getManifest() != null) {
                            ExternalAPKData.setManifest(mAPKData.getManifest());
                        }
                        if (new sAPKCertificateUtils(mFile,null, activity).getCertificateDetails() != null) {
                            ExternalAPKData.setCertificate(new sAPKCertificateUtils(mFile,null, activity).getCertificateDetails());
                        }
                        if (mAPKData.getVersionName() != null) {
                            ExternalAPKData.setVersionInfo(getString(R.string.version, mAPKData.getVersionName() + " (" + mAPKData.getVersionCode() + ")"));
                        }
                        if (mAPKData.getSDKVersion() != null) {
                            ExternalAPKData.setSDKVersion(mAPKData.getSDKVersion(), activity);
                        }
                        if (mAPKData.getMinSDKVersion() != null) {
                            ExternalAPKData.setMinSDKVersion(mAPKData.getMinSDKVersion(), activity);
                        }
                        ExternalAPKData.setSize(getString(R.string.size, sAPKUtils.getAPKSize(mFile.getAbsolutePath())) + " (" + mFile.length() + " bytes)");
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (mFile.exists()) {
                    if (mName != null || mPackageName != null || mIcon != null) {
                        ExternalAPKData.isFMInstall(true);
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
                                .setMessage(getString(R.string.wrong_extension, ".apks/.apkm/.xapk" + mExtension))
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
        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        try {
            if (mName != null) {
                mAppName.setText(mName);
                mAppName.setVisibility(View.VISIBLE);
            }
            if (mPackageName != null) {
                mPackageID.setText(mPackageName);
                mPackageID.setVisibility(View.VISIBLE);
            }
            if (mIcon != null) {
                mAppIcon.setImageDrawable(mIcon);
            }

            adapter.AddFragment(new APKDetailsFragment(), getString(R.string.details));
            if (ExternalAPKData.getPermissions() != null) {
                adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
            }
            if (ExternalAPKData.getManifest() != null) {
                adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));
            }
            if (ExternalAPKData.getCertificate() != null) {
                adapter.AddFragment(new CertificateFragment(), getString(R.string.certificate));
            }
        } catch (Exception ignored) {}

        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mMainLayout.setVisibility(View.VISIBLE);
        mIconsLayout.setVisibility(View.VISIBLE);

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