package com.apk.editor.utils.SerializableItems;

import java.io.Serializable;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March. 22, 2025
 */
public class ResItems implements Serializable {

    private final String mName, mPublicID, mType, mValue;

    public ResItems(String publicID, String type, String name, String value) {
        this.mPublicID = publicID;
        this.mType = type;
        this.mName = name;
        this.mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getPublicID() {
        return mPublicID;
    }

    public String getType() {
        return mType;
    }

    public String getValue() {
        return mValue;
    }

}