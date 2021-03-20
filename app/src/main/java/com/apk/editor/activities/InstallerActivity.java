package com.apk.editor.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class InstallerActivity extends AppCompatActivity {

    private AppCompatImageButton mIcon;
    private MaterialCardView mCancel;
    private MaterialTextView mHeading, mStatus, mTitle;
    private ProgressBar mProgress;
    public static final String HEADING_INTENT = "heading", PATH_INTENT = "path";
    public String mPackageName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);

        mIcon = findViewById(R.id.icon);
        mProgress = findViewById(R.id.progress);
        mCancel = findViewById(R.id.cancel);
        mHeading = findViewById(R.id.heading);
        mTitle = findViewById(R.id.title);
        mStatus = findViewById(R.id.status);

        String path = getIntent().getStringExtra(PATH_INTENT);
        mPackageName = APKData.getAppID(path, this).toString();

        mHeading.setText(getIntent().getStringExtra(HEADING_INTENT));
        mTitle.setText(APKData.getAppName(path, this));
        mIcon.setImageDrawable(APKData.getAppIcon(path, this));

        mCancel.setOnClickListener(v -> {
            finish();
        });

        refreshStatus(this);
    }

    public void refreshStatus(Activity activity) {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(() -> {
                            String installationStatus = APKEditorUtils.getString("installationStatus", "waiting", activity);
                            if (installationStatus.equals("waiting")) {
                                try {
                                    mStatus.setText(getString(R.string.installing, APKData.getAppName(getIntent().getStringExtra(PATH_INTENT), activity)));
                                } catch (NullPointerException ignored) {}
                            } else {
                                mStatus.setText(installationStatus);
                                mProgress.setVisibility(View.GONE);
                                mCancel.setVisibility(View.VISIBLE);
                                if (installationStatus.equals(getString(R.string.installation_status_success))) {
                                    try {
                                        mTitle.setText(AppData.getAppName(mPackageName, activity));
                                        mIcon.setImageDrawable(AppData.getAppIcon(mPackageName, activity));
                                    } catch (NullPointerException ignored) {}
                                }
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (APKEditorUtils.getString("installationStatus", "waiting", this).equals("waiting")) {
            return;
        }
        super.onBackPressed();
    }

}