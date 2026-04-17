package com.apk.editor.utils.SerializableItems;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on April 17, 2025
 */
public class SettingsItems implements Serializable {

    private final Drawable icon;
    private final String title;
    private String description;

    public SettingsItems(Drawable icon, String title, String description) {
        this.icon = icon;
        this.title = title;
        this.description = description;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}