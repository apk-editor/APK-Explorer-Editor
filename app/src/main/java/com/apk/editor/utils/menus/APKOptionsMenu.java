package com.apk.editor.utils.menus;

import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.tasks.SaveAPKtoDownloads;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on February 03, 2023
 */
public class APKOptionsMenu extends PopupMenu {

    public APKOptionsMenu(File apkFile, View view) {
        super(view.getContext(), view);
        Menu menu = getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.share);
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_to_downloads);
        setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    APKData.shareFile(apkFile, "application/java-archive", view.getContext());
                    break;
                case 1:
                    new SaveAPKtoDownloads(apkFile, view.getContext()).execute();
                    break;
            }
            return false;
        });
        show();
    }

}