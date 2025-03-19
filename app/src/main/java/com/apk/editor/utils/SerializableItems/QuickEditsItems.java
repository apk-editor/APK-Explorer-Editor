package com.apk.editor.utils.SerializableItems;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March. 11, 2025
 */
public class QuickEditsItems implements Serializable {

    private boolean mEdited = false;
    private final String mName;
    private String mValue;

    public QuickEditsItems(String name, String value) {
        this.mName = name;
        this.mValue = value;
    }

    public boolean isEdited() {
        return mEdited;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
        mEdited = true;
    }

}