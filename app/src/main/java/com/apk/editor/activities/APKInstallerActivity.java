package com.apk.editor.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.fragments.APKDetailsFragment;
import com.apk.editor.fragments.CertificateFragment;
import com.apk.editor.fragments.ManifestFragment;
import com.apk.editor.fragments.PermissionsFragment;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.dialogs.InvalidFileDialog;
import com.apk.editor.utils.dialogs.SelectBundleDialog;
import com.apk.editor.utils.tasks.ExploreAPK;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mExploreIcon;
    private AppCompatImageView mAppIcon;
    private APKParser mAPKParser;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialCardView mCancel, mInstall;
    private MaterialTextView mAppName, mInstallText, mPackageID;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkdetails);

        mExploreIcon = findViewById(R.id.explore);
        mAppIcon = findViewById(R.id.app_image);
        mAppName = findViewById(R.id.app_title);
        mPackageID = findViewById(R.id.package_id);
        mMainLayout = findViewById(R.id.main_layout);
        mIconsLayout = findViewById(R.id.icons_layout);
        mInstall = findViewById(R.id.install);
        mInstallText = findViewById(R.id.install_text);
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("apkFileUri")) {
            if (bundle.getString("apkFileUri") != null) {
                manageInstallation(Uri.parse(bundle.getString("apkFileUri")), this).execute();
            }
        } else if (getIntent().getData() != null) {
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
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setTitle(R.string.app_name);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                sFileUtils.delete(getExternalFilesDir("APK"));
                String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uri)).getName();
                mFile = new File(getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                sFileUtils.copy(uri, mFile, activity);
                try {
                    mAPKParser = new APKParser();
                    mAPKParser.parse(mFile.getAbsolutePath(), activity);
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
                    if (mAPKParser.isParsed()) {
                        loadAPKDetails(activity);
                        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
                            mInstallText.setText(getString(R.string.update));
                        }
                    } else if (mFile.getName().endsWith("apkm") || mFile.getName().endsWith("apks") || mFile.getName().endsWith("xapk")) {
                        new SelectBundleDialog(mFile.getAbsolutePath(), true, activity).show();
                    } else {
                        new InvalidFileDialog(true, activity).show();
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
            if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
                mAppName.setText(sPackageUtils.getAppName(mAPKParser.getPackageName(), activity));
                mPackageID.setText(mAPKParser.getPackageName());
                mAppIcon.setImageDrawable(sPackageUtils.getAppIcon(mAPKParser.getPackageName(), activity));
                mPackageID.setVisibility(View.VISIBLE);
            } else {
                mAppName.setText(mAPKParser.getPackageName());
                mAppIcon.setImageDrawable(mAPKParser.getAppIcon());
            }

            adapter.AddFragment(new APKDetailsFragment(), getString(R.string.details));
            if (mAPKParser.getPermissions() != null) {
                adapter.AddFragment(new PermissionsFragment(), getString(R.string.permissions));
            }
            if (mAPKParser.getManifest() != null) {
                adapter.AddFragment(new ManifestFragment(), getString(R.string.manifest));
            }
            if (mAPKParser.getCertificate() != null) {
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
            APKExplorer.handleAPKs(true, activity);
        });

        mExploreIcon.setOnClickListener(v -> {
            new ExploreAPK(null, mFile, activity).execute();
            activity.finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}