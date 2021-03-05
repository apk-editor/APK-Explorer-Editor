package com.apk.editor.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExploreFragment;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new APKExploreFragment()).commit();
    }

}