package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.Common;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 13, 2021
 */
public class APKTasksActivity extends AppCompatActivity {

    private AppCompatImageView mIcon;
    private ProgressBar mProgress;
    private MaterialCardView mCancel, mDetails;
    private MaterialTextView mError, mOutputPath, mSuccess, mTaskSummary;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apktasks);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
        mCancel = findViewById(R.id.cancel);
        mDetails = findViewById(R.id.details);
        mError = findViewById(R.id.error);
        mOutputPath = findViewById(R.id.output_path);
        mTaskSummary = findViewById(R.id.task_summary);
        mSuccess = findViewById(R.id.success);

        mError.setTextColor(Color.RED);
        mSuccess.setTextColor(Color.GREEN);

        if (Common.isBuilding()) {
            mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_build));
        } else {
            mIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_explore));
            mIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorBlue));
        }

        mOutputPath.setText(getString(R.string.resigned_apks_path, APKData.getExportAPKsPath(this)));

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

        mCancel.setOnClickListener(v -> onBackPressed());

        refreshStatus(this);
    }

    public void refreshStatus(Activity activity) {
        new Thread() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            if (!Common.isFinished()) {
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
                                if (Common.isBuilding() || Common.getError() > 0 || Common.getSuccess() > 0) {
                                    mCancel.setVisibility(View.VISIBLE);
                                    mOutputPath.setVisibility(View.VISIBLE);
                                    if (Common.getError() > 0) {
                                        mIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_clear));
                                        mIcon.setColorFilter(Color.RED);
                                        mDetails.setVisibility(View.VISIBLE);
                                    } else {
                                        mIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_check));
                                        mIcon.setColorFilter(Color.GREEN);
                                    }
                                    mTaskSummary.setVisibility(View.GONE);
                                    return;
                                }
                                finish();
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
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
        if (Common.getErrorList().size() > 0) {
            Common.getErrorList().clear();
        }
    }

    @Override
    public void onBackPressed() {
        if (Common.isFinished()) {
            if (Common.getError() > 0) {
                Common.setError(0);
            }
            if (Common.getSuccess() > 0) {
                Common.setSuccess(0);
            }
            if (Common.isBuilding()) {
                Common.isBuilding(false);
            }
            finish();
        }
    }

}