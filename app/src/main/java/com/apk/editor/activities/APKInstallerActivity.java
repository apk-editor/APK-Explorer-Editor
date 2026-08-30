package com.apk.editor.activities;

import static com.apk.editor.utils.APKExplorer.handleAPKs;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager2.widget.ViewPager2;

import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.fragments.APKDetailsFragment;
import com.apk.editor.fragments.CertificateFragment;
import com.apk.editor.fragments.ManifestFragment;
import com.apk.editor.fragments.PermissionsFragment;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.InvalidFileDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 27, 2021
 */
public class APKInstallerActivity extends BaseActivity {

    private AppCompatImageView mAppIcon;
    private APKParser mAPKParser;
    private File mFile = null;
    private ConstraintLayout mMainLayout;
    private MaterialButton mExploreIcon;
    private MaterialButton mCancel, mInstall;
    private MaterialTextView mAppName, mPackageID;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkdetails, R.id.layout_root);

        mExploreIcon = findViewById(R.id.explore);
        mAppIcon = findViewById(R.id.app_image);
        mAppName = findViewById(R.id.app_title);
        mPackageID = findViewById(R.id.package_id);
        mMainLayout = findViewById(R.id.layout_root);
        mInstall = findViewById(R.id.install);
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("apkFileUri") && bundle.getString("apkFileUri") != null) {
            manageInstallation(Uri.parse(bundle.getString("apkFileUri")), null).execute();
        } else if (bundle != null && bundle.containsKey("apkFilePath") && bundle.getString("apkFilePath") != null) {
            manageInstallation(null, bundle.getString("apkFilePath")).execute();
        } else if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), null).execute();
        }
    }

    private sExecutor manageInstallation(Uri uri, String filePath) {
        return new sExecutor() {
            private final Activity activity = APKInstallerActivity.this;
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setTitle(activity.getString(R.string.loading));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();

                sFileUtils.delete(Objects.requireNonNull(getExternalFilesDir("APK")));
                if (filePath == null) {
                    String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uri)).getName();
                    mFile = new File(getExternalFilesDir("APK"), Objects.requireNonNull(fileName));
                }
            }

            @Override
            public void doInBackground() {
                if (filePath != null) {
                    mFile = new File(filePath);
                } else {
                    sFileUtils.copy(uri, mFile, activity);
                }
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
                        loadAPKDetails();
                        if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
                            mInstall.setText(getString(R.string.update));
                        }
                    } else if (mFile.getName().endsWith("apkm") || mFile.getName().endsWith("apks") || mFile.getName().endsWith("xapk")) {
                        SplitAPKInstaller.handleAppBundle(mFile.getAbsolutePath(), activity);
                    } else {
                        new InvalidFileDialog(true, activity);
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

    private void loadAPKDetails() {
        sPagerAdapter adapter = new sPagerAdapter(this);
        try {
            if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), this)) {
                mAppName.setText(sPackageUtils.getAppName(mAPKParser.getPackageName(), this));
                mAppIcon.setImageDrawable(sPackageUtils.getAppIcon(mAPKParser.getPackageName(), this));
            } else {
                mAppName.setText(mAPKParser.getAppName());
                mAppIcon.setImageDrawable(mAPKParser.getAppIcon());
            }
            mPackageID.setText(mAPKParser.getPackageName());
            mPackageID.setVisibility(View.VISIBLE);

            adapter.addFragment(new APKDetailsFragment(), getString(R.string.details));
            if (mAPKParser.getPermissions() != null) {
                adapter.addFragment(new PermissionsFragment(), getString(R.string.permissions));
            }
            if (mAPKParser.getManifest() != null) {
                adapter.addFragment(new ManifestFragment(), getString(R.string.manifest));
            }
            if (mAPKParser.getCertificate() != null) {
                adapter.addFragment(new CertificateFragment(), getString(R.string.certificate));
            }
        } catch (Exception ignored) {}

        mViewPager.setAdapter(adapter);
        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        mMainLayout.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(v -> APKExplorer.setCancelIntent(this));
        mInstall.setOnClickListener(v -> {
            List<String> appList = new ArrayList<>();
            appList.add(mFile.getAbsolutePath());
            handleAPKs(true, appList, this);
        });

        mExploreIcon.setOnClickListener(v -> ExploreOptionsMenu.getMenu(mPackageID.getText().toString().trim(), mFile, null, true, this));
    }

}