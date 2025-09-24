package com.apk.editor.activities;

import static com.apk.editor.utils.APKExplorer.handleAPKs;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.InvalidFileDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
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
public class APKInstallerActivity extends AppCompatActivity {    private AppCompatImageView mAppIcon;
    private APKParser mAPKParser;
    private File mFile = null;
    private LinearLayoutCompat mMainLayout, mIconsLayout;
    private MaterialButton mExploreIcon;
    private MaterialButton mCancel, mInstall;
    private MaterialTextView mAppName, mPackageID;
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
        mCancel = findViewById(R.id.cancel);
        mTabLayout = findViewById(R.id.tab_Layout);
        mViewPager = findViewById(R.id.view_pager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("apkFileUri") && bundle.getString("apkFileUri") != null) {
            manageInstallation(Uri.parse(bundle.getString("apkFileUri")), null, this).execute();
        } else if (bundle != null && bundle.containsKey("apkFilePath") && bundle.getString("apkFilePath") != null) {
            manageInstallation(null, bundle.getString("apkFilePath"), this).execute();
        } else if (getIntent().getData() != null) {
            manageInstallation(getIntent().getData(), null, this).execute();
        }
    }

    private sExecutor manageInstallation(Uri uri, String filePath, Activity activity) {
        return new sExecutor() {
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
                        loadAPKDetails(activity);
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

    private void loadAPKDetails(Activity activity) {
        sPagerAdapter adapter = new sPagerAdapter(getSupportFragmentManager());
        try {
            if (sPackageUtils.isPackageInstalled(mAPKParser.getPackageName(), activity)) {
                mAppName.setText(sPackageUtils.getAppName(mAPKParser.getPackageName(), activity));
                mPackageID.setText(mAPKParser.getPackageName());
                mAppIcon.setImageDrawable(sPackageUtils.getAppIcon(mAPKParser.getPackageName(), activity));
                mPackageID.setVisibility(View.VISIBLE);
            } else {
                mAppName.setText(mFile.getName().replace(".apk", ""));
                mPackageID.setText(mAPKParser.getPackageName());
                mAppIcon.setImageDrawable(mAPKParser.getAppIcon());
                mPackageID.setVisibility(View.VISIBLE);
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

        mCancel.setOnClickListener(v -> APKExplorer.setCancelIntent(this));
        mInstall.setOnClickListener(v -> {
            List<String> appList = new ArrayList<>();
            appList.add(mFile.getAbsolutePath());
            handleAPKs(true, appList, activity);
        });

        mExploreIcon.setOnClickListener(v -> ExploreOptionsMenu.getMenu(mPackageID.getText().toString().trim(), mFile, null, true, activity));
    }

}