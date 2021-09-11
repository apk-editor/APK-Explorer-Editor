package com.apk.editor.utils.recyclerViewItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AboutItems implements Serializable {

    private final String mDescription, mTitle, mURL;
    private final Drawable mIcon;

    public AboutItems(String title, String description, Drawable icon, String url) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
        this.mURL = url;
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

    public String getURL() {
        return mURL;
    }

}