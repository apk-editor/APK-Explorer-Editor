package com.apk.editor.utils.SerializableItems;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct. 31, 2024
 */
public class XMLItems implements Serializable {

    private final boolean mIsBoolean;
    private boolean mRemove;
    private String mID, mValue;

    public XMLItems(String id, String value, boolean isBoolean, boolean remove) {
        this.mID = id;
        this.mValue = value;
        this.mIsBoolean = isBoolean;
        this.mRemove = remove;
    }

    public boolean isBoolean() {
        return mIsBoolean;
    }

    public boolean isRemoved() {
        return mRemove;
    }

    public String getID() {
        return mID;
    }

    public String getValue() {
        return mValue;
    }

    public void setToRemove(boolean remove) {
        mRemove = remove;
    }

    public void setID(@NonNull String id) {
        mID = id;
    }

    public void setValue(@Nullable String value) {
        mValue = value;
    }

}