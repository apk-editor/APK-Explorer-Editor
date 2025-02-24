package com.apk.editor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.R;
import com.apk.editor.utils.Common;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Feb 21, 2025
 */
public class ExploringActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exploring);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ProgressBar mProgress = findViewById(R.id.progress);
        MaterialButton mCancel = findViewById(R.id.cancel);
        MaterialTextView mTaskSummary = findViewById(R.id.task_summary);

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
                finish();
            } else {
                if (Common.getStatus(this) != null && !Common.isFinished(this)) {
                    mTaskSummary.setVisibility(View.VISIBLE);
                    mTaskSummary.setText(Common.getStatus(this));
                }
            }
            mHandler.postDelayed(mRunnable, 500);
        };
        mHandler.postDelayed(mRunnable, 500);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               if (Common.isFinished(ExploringActivity.this)) {
                   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                   finish();
               }
            }
        });
    }

}