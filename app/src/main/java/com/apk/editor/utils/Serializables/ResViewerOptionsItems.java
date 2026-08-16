package com.apk.editor.utils.Serializables;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class ResViewerOptionsItems implements Serializable {

    private final int id;
    private final String title;

    public ResViewerOptionsItems(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

}