package com.apk.editor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class StartActivity extends AppCompatActivity {

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize app theme
        sThemeUtils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        MaterialButton mStart = findViewById(R.id.start);
        MaterialTextView mText = findViewById(R.id.text);
        mProgress = findViewById(R.id.progress_bar);

        if (!sCommonUtils.getBoolean("welcome_message", false, this)) {
            mText.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        } else {
            acquireData(this);
        }

        mStart.setOnClickListener(v -> {
            mText.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.GONE);
            acquireData(this);
        });
    }

    private void acquireData(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                if (!sCommonUtils.getBoolean("welcome_message", false, activity)) {
                    sCommonUtils.saveBoolean("welcome_message", true, activity);
                }
            }

            @Override
            public void doInBackground() {
                Common.setPackageData(AppData.getRawData(mProgress, activity));
            }

            @Override
            public void onPostExecute() {
                Intent mainActivity = new Intent(activity, MainActivity.class);
                activity.startActivity(mainActivity);
                activity.finish();
            }
        }.execute();
    }

}