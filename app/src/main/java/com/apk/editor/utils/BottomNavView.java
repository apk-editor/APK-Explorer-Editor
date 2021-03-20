package com.apk.editor.utils;

import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import com.apk.editor.R;
import com.apk.editor.fragments.APKsFragment;
import com.apk.editor.fragments.AboutFragment;
import com.apk.editor.fragments.ApplicationsFragment;
import com.apk.editor.fragments.ProjectsFragment;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 10, 2021
 */
public class BottomNavView {

    public static Fragment getNavMenu (MenuItem menuItem) {
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
        return selectedFragment;
    }

}