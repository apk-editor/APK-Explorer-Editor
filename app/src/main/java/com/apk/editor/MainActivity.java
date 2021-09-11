package com.apk.editor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.activities.SettingsActivity;
import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.BottomNavView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set App Language
        APKEditorUtils.setLanguage(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        AppCompatImageButton mSettings = findViewById(R.id.settings_menu);

        mBottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ApplicationsFragment()).commit();
        }

        mSettings.setOnClickListener(v -> {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener
            = menuItem -> {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                BottomNavView.getNavMenu(menuItem)).commit();
        return true;
    };

}