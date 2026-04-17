package com.apk.editor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.apk.editor.activities.SettingsActivity;
import com.apk.editor.activities.StartActivity;
import com.apk.editor.fragments.APKsFragment;
import com.apk.editor.fragments.AboutFragment;
import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.fragments.ProjectsFragment;
import com.apk.editor.utils.AppSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import in.sunilpaulmathew.crashreporter.Utils.CrashReporter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class MainActivity extends AppCompatActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize App Theme & Language
        sThemeUtils.initializeAppTheme(this);
        AppSettings.initializeAppLanguage(this);

        new CrashReporter("E-Mail: apkeditor@protonmail.com", this).initialize();

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        FrameLayout mFragmentContainer = findViewById(R.id.fragment_container);
        MaterialButton mSettings = findViewById(R.id.settings_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(0, systemBars.top, 0, 0);

            return insets;
        });

        if (!sCommonUtils.getBoolean("welcome_message", false, this)) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Menu menu = mBottomNav.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, null).setIcon(R.drawable.ic_apps).setTitle(R.string.apps);
        menu.add(Menu.NONE, 1, Menu.NONE, null).setIcon(R.drawable.ic_projects).setTitle(R.string.projects);
        menu.add(Menu.NONE, 2, Menu.NONE, null).setIcon(R.drawable.ic_android).setTitle(R.string.apks);
        menu.add(Menu.NONE, 3, Menu.NONE, null).setIcon(R.drawable.ic_about).setTitle(R.string.about);

        mBottomNav.setOnItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case 0:
                            mFragment = new ApplicationsFragment();
                            break;
                        case 1:
                            mFragment = new ProjectsFragment();
                            break;
                        case 2:
                            mFragment = new APKsFragment();
                            break;
                        case 3:
                            mFragment = new AboutFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mFragment).commit();
                    return true;
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ApplicationsFragment()).commit();
        }

        mBottomNav.post(() -> mFragmentContainer.setPadding(0, 0, 0, mBottomNav.getHeight()));

        mSettings.setOnClickListener(v -> {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        });
    }

}