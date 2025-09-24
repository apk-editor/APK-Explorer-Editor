package com.apk.editor.utils.menus;

import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.apk.editor.R;
import com.apk.editor.utils.tasks.ShareBundle;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on February 03, 2023
 */
public class BundleOptionsMenu extends PopupMenu {

    public BundleOptionsMenu(String bundlePath, View view) {
        super(view.getContext(), view);
        Menu menu = getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.share);
        setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                new ShareBundle(bundlePath, view.getContext()).execute();
            }
            return false;
        });
        show();
    }

}