package com.apk.editor.utils.Serializables;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on April 17, 2025
 */
public class SettingsItems implements Serializable {

    private final int id, iconRes;
    private final String title;
    private String description;

    public SettingsItems(String title) {
        this.iconRes = Integer.MIN_VALUE;
        this.title = title;
        this.description = null;
        this.id = 0;
    }

    public SettingsItems(int iconRes, String title, String description, int id) {
        this.iconRes = iconRes;
        this.title = title;
        this.description = description;
        this.id = id;
    }

    public int getIconRes() {
        return this.iconRes;
    }

    public int getId() {
        return this.id;
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