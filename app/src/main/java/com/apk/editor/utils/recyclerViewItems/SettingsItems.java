package com.apk.editor.utils.recyclerViewItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 31, 2021
 */
public class SettingsItems implements Serializable {

    private final String mDescription, mTitle;
    private final Drawable mIcon;

    public SettingsItems(String title, String description, Drawable icon) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Drawable getIcon() {
        return mIcon;
    }

}