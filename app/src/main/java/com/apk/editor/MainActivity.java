package com.apk.editor;

import android.content.Intent;
import android.os.Bundle;

import com.apk.editor.activities.BaseActivity;
import com.apk.editor.activities.SettingsActivity;
import com.apk.editor.activities.StartActivity;
import com.apk.editor.fragments.APKsFragment;
import com.apk.editor.fragments.AboutFragment;
import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.fragments.ProjectsFragment;
import com.apk.editor.utils.AppSettings;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.crashreporter.Utils.CrashReporter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;
import navView.NavView;
import navView.serializables.NavViewEntry;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main, R.id.layout_root);
        
        // Initialize App Theme & Language
        sThemeUtils.initializeAppTheme(this);
        AppSettings.initializeAppLanguage(this);

        new CrashReporter("E-Mail: apkeditor@protonmail.com", this).initialize();

        NavView mNavView = findViewById(R.id.nav_view);

        if (!sCommonUtils.getBoolean("welcome_message", false, this)) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        List<NavViewEntry> data = new CopyOnWriteArrayList<>();
        data.add(new NavViewEntry(ApplicationsFragment::new, R.drawable.ic_apps, getString(R.string.apps)));
        data.add(new NavViewEntry(ProjectsFragment::new, R.drawable.ic_projects, getString(R.string.projects)));
        data.add(new NavViewEntry(APKsFragment::new, R.drawable.ic_android_app, getString(R.string.apks)));
        data.add(new NavViewEntry(AboutFragment::new, R.drawable.ic_about, getString(R.string.about)));

        mNavView.setNavigationItems(data);

        mNavView.setOnItemSelectedListener((fragment, position) -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        );

        mNavView.setOnMenuButtonClicked(R.drawable.ic_settings, () -> {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ApplicationsFragment()).commit();
        }
    }

}