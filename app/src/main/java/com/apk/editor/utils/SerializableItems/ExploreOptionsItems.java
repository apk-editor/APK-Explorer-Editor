package com.apk.editor.utils.SerializableItems;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 26, 2026
 */
public class ExploreOptionsItems implements Serializable {

    private final boolean checkable, checked;
    private final int id, iconRes;
    private final String title;

    public ExploreOptionsItems(int iconRes, String title, boolean checkable, boolean checked, int id) {
        this.iconRes = iconRes;
        this.title = title;
        this.checkable = checkable;
        this.checked = checked;
        this.id = id;
    }

    public ExploreOptionsItems(int iconRes, String title, int id) {
        this.iconRes = iconRes;
        this.title = title;
        this.checkable = false;
        this.checked = false;
        this.id = id;
    }

    public boolean isCheckable() {
        return this.checkable;
    }

    public boolean isChecked() {
        return this.checked;
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

}