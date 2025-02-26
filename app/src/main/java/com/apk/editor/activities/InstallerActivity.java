package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.PackageItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static boolean mUpdating = false;
    public static final String HEADING_INTENT = "heading", PATH_INTENT = "path";
    private static String mPackageName = null;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        AppCompatImageButton mIcon = findViewById(R.id.icon);
        ProgressBar mProgress = findViewById(R.id.progress);
        MaterialButton mOpen = findViewById(R.id.open);
        MaterialButton mCancel = findViewById(R.id.cancel);
        MaterialTextView mHeading = findViewById(R.id.heading);
        MaterialTextView mTitle = findViewById(R.id.title);
        MaterialTextView mStatus = findViewById(R.id.status);

        String path = getIntent().getStringExtra(PATH_INTENT);
        if (path != null) {
            try {
                mPackageName = Objects.requireNonNull(sAPKUtils.getPackageName(path, this));
                mTitle.setText(sAPKUtils.getAPKName(path, this));
                mIcon.setImageDrawable(sAPKUtils.getAPKIcon(path, this));
            } catch (NullPointerException ignored) {}
        } else {
            mPackageName = APKData.findPackageName(this);
            mTitle.setText(getName());
            mIcon.setImageDrawable(getIcon());
        }

        mUpdating = sPackageUtils.isPackageInstalled(mPackageName, this);

        mHeading.setText(getIntent().getStringExtra(HEADING_INTENT));

        mOpen.setOnClickListener(v -> {
            startActivity(getPackageManager().getLaunchIntentForPackage(mPackageName));
            finish();
        });

        mCancel.setOnClickListener(v -> exit());

        mRunnable = () -> {
            String installationStatus = sCommonUtils.getString("installationStatus", "waiting", this);
            if (installationStatus.equals("waiting")) {
                try {
                    if (getIntent().getStringExtra(PATH_INTENT) != null) {
                        mStatus.setText(getString(R.string.installing, sAPKUtils.getAPKName(getIntent().getStringExtra(PATH_INTENT), this)));
                    } else {
                        mStatus.setText(getString(R.string.installing, getName()));
                    }
                } catch (NullPointerException ignored) {}
            } else {
                mStatus.setText(installationStatus);
                mProgress.setVisibility(View.GONE);
                mCancel.setVisibility(View.VISIBLE);
                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                    try {
                        mTitle.setText(sPackageUtils.getAppName(mPackageName, this));
                        mIcon.setImageDrawable(sPackageUtils.getAppIcon(mPackageName, this));
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

    private CharSequence getName() {
        CharSequence name = null;
        for (String mAPKs : Common.getAPKList()) {
            if (sAPKUtils.getAPKName(mAPKs, this) != null) {
                name = sAPKUtils.getAPKName(mAPKs, this);
            }
        }
        return name;
    }

    private Drawable getIcon() {
        Drawable icon = null;
        for (String mAPKs : Common.getAPKList()) {
            if (sAPKUtils.getAPKIcon(mAPKs, this) != null) {
                icon = sAPKUtils.getAPKIcon(mAPKs, this);
            }
        }
        return icon;
    }

    private void exit() {
        if (sCommonUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        if (!mUpdating && sCommonUtils.getString("installationStatus", "waiting", this).equals(getString(R.string.installation_status_success))) {
            AppData.getData(this).add(new PackageItems(mPackageName, this));
        }
        if (sFileUtils.exist(new File(getCacheDir(),"splits"))) {
            sFileUtils.delete(new File(getCacheDir(),"splits"));
        }
        finish();
    }

}