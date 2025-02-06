package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 13, 2021
 */
public class APKTasksActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static boolean mBuilding = false;
    public static final String BUILDING_INTENT = "building", PACKAGE_NAME_INTENT = "packageName";

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apktasks);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AppCompatImageView mIcon = findViewById(R.id.icon);
        ProgressBar mProgress = findViewById(R.id.progress);
        MaterialButton mInstall = findViewById(R.id.install);
        MaterialButton mCancel = findViewById(R.id.cancel);
        MaterialButton mDetails = findViewById(R.id.details);
        MaterialTextView mError = findViewById(R.id.error);
        MaterialTextView mOutputPath = findViewById(R.id.output_path);
        MaterialTextView mTaskSummary = findViewById(R.id.task_summary);
        MaterialTextView mSuccess = findViewById(R.id.success);

        mError.setTextColor(Color.RED);
        mSuccess.setTextColor(Color.GREEN);

        String mPackageName = getIntent().getStringExtra(PACKAGE_NAME_INTENT);
        mBuilding = getIntent().getBooleanExtra(BUILDING_INTENT, false);

        if (mBuilding) {
            mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_build));
        } else {
            mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_explore));
        }

        mOutputPath.setText(getString(R.string.resigned_apks_path, APKData.getExportAPKsPath(this)));

        if (sPackageUtils.isPackageInstalled(mPackageName, this)) {
            mInstall.setText(getString(R.string.update));
        }

        mInstall.setOnClickListener(v -> {
            if (sPackageUtils.isPackageInstalled(mPackageName, this) && APKData.isAppBundle(sPackageUtils
                    .getSourceDir(mPackageName, this))) {
                SplitAPKInstaller.installSplitAPKs(false, null, new File(APKData.getExportAPKsPath(this),
                        Objects.requireNonNull(mPackageName).replace(".apk", "") + "_aee-signed/base.apk")
                        .getAbsolutePath(), this);
            } else {
                SplitAPKInstaller.installAPK(false, new File(APKData.getExportAPKsPath(this),
                        Objects.requireNonNull(mPackageName).replace(".apk", "")
                                + "_aee-signed.apk"), this);
            }
            finish();
        });

        mDetails.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (String strings : Common.getErrorList()) {
                sb.append(strings).append("\n");
            }
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.failed_smali_message, sb.toString()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.cancel, (dialog, id) -> finish()).show();
        });

        mCancel.setOnClickListener(v -> {
            if (!Common.isFinished()) {
                Common.isCancelled(true);
                mTaskSummary.setText(getString(R.string.cancelling));
                mTaskSummary.setTextColor(Color.RED);
                mCancel.setVisibility(View.GONE);
                return;
            }
            exit();
        });

        mRunnable = () -> {
            if (Common.isCancelled()) {
                mTaskSummary.setText(getString(R.string.cancelling));
                if (mBuilding && Common.isFinished()) {
                    finish();
                }
            } else if (!Common.isFinished()) {
                try {
                    if (Common.getStatus() != null) {
                        mTaskSummary.setVisibility(View.VISIBLE);
                        mTaskSummary.setText(Common.getStatus());
                    }
                    if (Common.getError() > 0 || Common.getSuccess() > 0) {
                        mError.setVisibility(View.VISIBLE);
                        mSuccess.setVisibility(View.VISIBLE);
                        mError.setText(getString(R.string.failed) + ": " + Common.getError());
                        mSuccess.setText(getString(R.string.success) + ": " + Common.getSuccess());
                        if (Common.getError() > 0) {
                            mOutputPath.setText(getString(R.string.resigned_apks_error));
                        }
                    }
                } catch (NullPointerException ignored) {
                }
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Common.setStatus(null);
                mProgress.setVisibility(View.GONE);
                if (mBuilding || Common.getError() > 0 || Common.getSuccess() > 0) {
                    mCancel.setVisibility(View.VISIBLE);
                    mOutputPath.setVisibility(View.VISIBLE);
                    if (Common.getError() > 0) {
                        mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_clear));
                        mIcon.setColorFilter(Color.RED);
                        mDetails.setVisibility(View.VISIBLE);
                        mInstall.setVisibility(View.GONE);
                    } else {
                        mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                        mIcon.setColorFilter(Color.GREEN);
                        mInstall.setVisibility(View.VISIBLE);
                    }
                    mTaskSummary.setVisibility(View.GONE);
                    return;
                }
                finish();
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

    @Override
    public void onStart() {
        super.onStart();

        if (Common.getError() > 0) {
            Common.setError(0);
        }
        if (Common.getSuccess() > 0) {
            Common.setSuccess(0);
        }
        if (!Common.getErrorList().isEmpty()) {
            Common.getErrorList().clear();
        }
    }

    public void exit() {
        if (Common.isFinished()) {
            if (Common.getError() > 0) {
                Common.setError(0);
            }
            if (Common.getSuccess() > 0) {
                Common.setSuccess(0);
            }
            if (mBuilding) {
                mBuilding = false;
            }
            finish();
        }
    }

}