package com.apk.editor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.BottomNavView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize app theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ApplicationsFragment()).commit();
        }

        if (!APKEditorUtils.getBoolean("welcome_message", false, this)) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.warning_message))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                        finish();
                    })
                    .setPositiveButton(getString(R.string.accept), (dialog, id) -> {
                        APKEditorUtils.saveBoolean("welcome_message", true, this);
                    }).show();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = menuItem -> {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                BottomNavView.getNavMenu(menuItem)).commit();
        return true;
    };

}