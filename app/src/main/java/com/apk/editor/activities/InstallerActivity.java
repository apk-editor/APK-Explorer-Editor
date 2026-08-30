package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Serializables.PackageItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerActivity extends BaseActivity {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;
    private static boolean mUpdating = false;
    private static Drawable mAppIcon = null;
    public static final String APK_LIST_INTENT = "apk_list", PATH_INTENT = "path";
    private static String mAppName = null, mPackageName = null;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer, R.id.layout_root);

        AppCompatImageButton mIcon = findViewById(R.id.icon);
        ProgressBar mProgress = findViewById(R.id.progress);
        MaterialButton mOpen = findViewById(R.id.open);
        MaterialButton mCancel = findViewById(R.id.cancel);
        MaterialTextView mTitle = findViewById(R.id.title);
        MaterialTextView mStatus = findViewById(R.id.status);

        new sExecutor() {
            private List<String> mAPKList = null;
            public String mAPKPath;
            @Override
            public void onPreExecute() {

            }@Override
            public void doInBackground() {
                mAPKList = getIntent().getStringArrayListExtra(APK_LIST_INTENT);
                mAPKPath = getIntent().getStringExtra(PATH_INTENT);

                loadPackageDetails();

                mUpdating = sPackageUtils.isPackageInstalled(mPackageName, InstallerActivity.this);
            }

            private void loadPackageDetails() {
                PackageManager pm = getPackageManager();

                if (mAPKList != null) {
                    for (String apkPath : mAPKList) {
                        if (sFileUtils.exist(apkPath)) {
                            PackageInfo pi = pm.getPackageArchiveInfo(apkPath, 0);
                            if (pi != null) {
                                ApplicationInfo ai = pi.applicationInfo;
                                Objects.requireNonNull(ai).sourceDir = apkPath;
                                ai.publicSourceDir = apkPath;

                                mAppName = pm.getApplicationLabel(ai).toString();
                                mAppIcon = pm.getApplicationIcon(ai);
                                mPackageName = ai.packageName;
                                return;
                            }
                        }
                    }
                } else if (mAPKPath != null) {
                    PackageInfo pi = pm.getPackageArchiveInfo(mAPKPath, 0);
                    if (pi != null) {
                        ApplicationInfo ai = pi.applicationInfo;
                        Objects.requireNonNull(ai).sourceDir = mAPKPath;
                        ai.publicSourceDir = mAPKPath;

                        mAppName = pm.getApplicationLabel(ai).toString();
                        mAppIcon = pm.getApplicationIcon(ai);
                        mPackageName = ai.packageName;
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (mAppName != null) {
                    mTitle.setText(mAppName);
                }
                if (mAppIcon != null) {
                    mIcon.setImageDrawable(mAppIcon);
                }
            }
        }.execute();

        mUpdating = sPackageUtils.isPackageInstalled(mPackageName, this);

        mOpen.setOnClickListener(v -> {
            startActivity(getPackageManager().getLaunchIntentForPackage(mPackageName));
            finish();
        });

        mCancel.setOnClickListener(v -> exit());

        mRunnable = () -> {
            String installationStatus = sCommonUtils.getString("installationStatus", "waiting", this);
            if (installationStatus.equals("waiting")) {
                try {
                    mStatus.setText(getString(R.string.installing, mAppName));
                } catch (NullPointerException ignored) {}
            } else {
                mStatus.setText(installationStatus);
                mProgress.setVisibility(View.GONE);
                mCancel.setVisibility(View.VISIBLE);
                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                    try {
                        mTitle.setText(mAppName);
                        mIcon.setImageDrawable(mAppIcon);
                        if (getPackageManager().getLaunchIntentForPackage(mPackageName) != null) {
                            mOpen.setVisibility(View.VISIBLE);
                        }
                    } catch (NullPointerException ignored) {}
                }
            }
            mHandler.postDelayed(mRunnable, 500);
        };
        mHandler.postDelayed(mRunnable, 500);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exit();
            }
        });
    }

    private void exit() {
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (!mUpdating && sCommonUtils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            AppData.getData(sCommonUtils.getInt("showAppType", 2, this), this).add(new PackageItems(mPackageName, this));
        }
        if (sFileUtils.exist(new File(getCacheDir(),"splits"))) {
            sFileUtils.delete(new File(getCacheDir(),"splits"));
        }
        finish();
    }

}