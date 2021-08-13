package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.Common;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 13, 2021
 */
public class APKTasksActivity extends AppCompatActivity {

    private AppCompatImageView mIcon;
    private ProgressBar mProgress;
    private MaterialCardView mCancel;
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
        mError = findViewById(R.id.error);
        mOutputPath = findViewById(R.id.output_path);
        mTaskSummary = findViewById(R.id.task_summary);
        mSuccess = findViewById(R.id.success);

        mError.setTextColor(Color.RED);
        mSuccess.setTextColor(Color.GREEN);

        if (Common.isBuilding()) {
            mIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_build));
        } else {
            mIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_explore));
            mIcon.setColorFilter(getResources().getColor(R.color.colorBlue));
        }

        mOutputPath.setText(getString(R.string.resigned_apks_path, APKData.getExportAPKsPath(this)));

        mCancel.setOnClickListener(v -> onBackPressed());

        refreshStatus();
    }

    public void refreshStatus() {
        new Thread() {
            @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
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
                                        mSuccess.setText(getString(R.string.success) + ": " + Common.getSuccess());                                    return;
                                    }
                                } catch (NullPointerException ignored) {
                                }
                            } else {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                Common.setStatus(null);
                                mProgress.setVisibility(View.GONE);
                                if (Common.isBuilding() || Common.getError() > 0 || Common.getSuccess() > 0) {
                                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                                    mIcon.setColorFilter(Color.GREEN);
                                    mCancel.setVisibility(View.VISIBLE);
                                    mOutputPath.setVisibility(View.VISIBLE);
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