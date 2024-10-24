package com.apk.editor.utils;

import android.app.Activity;

import com.apk.editor.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 23, 2024
 */
public class CommonViews {

    public static MaterialCardView getCardView(Activity activity, int id) {
        return activity.findViewById(id);
    }

    public static void navigateToFragment(Activity activity, int position) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(position);
    }

}