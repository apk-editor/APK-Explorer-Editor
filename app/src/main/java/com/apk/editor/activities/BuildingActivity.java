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

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Feb. 21, 2025
 */
public class BuildingActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;
    public static final String PACKAGE_NAME_INTENT = "packageName";

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

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
        mIcon.setScaleX(-1);

        String mPackageNameOriginal = getIntent().getStringExtra(PACKAGE_NAME_INTENT);

        mOutputPath.setText(getString(R.string.resigned_apks_path, APKData.getExportAPKsPath(this)));

        mInstall.setOnClickListener(v -> {
            if (sPackageUtils.isPackageInstalled(Common.getPackageName(this), this) && APKData.isAppBundle(sPackageUtils
                    .getSourceDir(Common.getPackageName(this), this))) {
                SplitAPKInstaller.installSplitAPKs(new File(APKData.getExportAPKsPath(this), mPackageNameOriginal + "_aee-signed").getAbsolutePath(), this);
            } else {
                SplitAPKInstaller.installAPK(new File(APKData.getExportAPKsPath(this), mPackageNameOriginal + "_aee-signed.apk"), this);
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
            if (!Common.isFinished(this)) {
                Common.isCancelled(true, this);
                mTaskSummary.setText(getString(R.string.cancelling));
                mTaskSummary.setTextColor(Color.RED);
                mCancel.setVisibility(View.GONE);
                return;
            }
            finish();
        });

        mRunnable = () -> {
            if (Common.isFinished(this)) {
                if (Common.isCancelled(this)) {
                    finish();
                }
                mProgress.setVisibility(View.GONE);
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
                    if (sPackageUtils.isPackageInstalled(Common.getPackageName(this), this)) {
                        mInstall.setText(getString(R.string.update));
                    }
                    mInstall.setVisibility(View.VISIBLE);
                }
                mTaskSummary.setVisibility(View.GONE);
                mIcon.setScaleX(1);
            } else {
                try {
                    if (Common.getStatus(this) != null && !Common.isFinished(this)) {
                        mTaskSummary.setVisibility(View.VISIBLE);
                        mTaskSummary.setText(Common.getStatus(this));
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
            }
            mHandler.postDelayed(mRunnable, 500);
        };
        mHandler.postDelayed(mRunnable, 500);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Common.isFinished(BuildingActivity.this)) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    finish();
                }
            }
        });
    }

}