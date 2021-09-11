package com.apk.editor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.MainActivity;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AsyncTasks;
import com.apk.editor.utils.Common;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class StartActivity extends AppCompatActivity {

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        MaterialCardView mStartCard = findViewById(R.id.start_card);
        MaterialTextView mText = findViewById(R.id.text);
        mProgress = findViewById(R.id.progress_bar);

        if (!APKEditorUtils.getBoolean("welcome_message", false, this)) {
            mText.setVisibility(View.VISIBLE);
            mStartCard.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        } else {
            acquireData(this);
        }

        mStartCard.setOnClickListener(v -> {
            mText.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            mStartCard.setVisibility(View.GONE);
            acquireData(this);
        });
    }

    private void acquireData(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                APKEditorUtils.saveBoolean("welcome_message", true, activity);
            }

            @Override
            public void doInBackground() {
                Common.setPackageData(AppData.getRawData(activity));
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