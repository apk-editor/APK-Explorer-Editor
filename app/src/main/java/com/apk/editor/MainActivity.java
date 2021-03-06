package com.apk.editor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.apk.editor.fragments.APKsFragment;
import com.apk.editor.fragments.AboutFragment;
import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.fragments.ProjectsFragment;
import com.apk.editor.utils.APKEditorUtils;
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
        Fragment selectedFragment = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_apps:
                selectedFragment = new ApplicationsFragment();
                break;
            case R.id.nav_projects:
                selectedFragment = new ProjectsFragment();
                break;
            case R.id.nav_apks:
                selectedFragment = new APKsFragment();
                break;
            case R.id.nav_about:
                selectedFragment = new AboutFragment();
                break;
        }

        assert selectedFragment != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).commit();

        return true;
    };

}