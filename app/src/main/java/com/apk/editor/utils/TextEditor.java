package com.apk.editor.utils;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditor {

    public static String mSearchText;

    public static boolean isBinaryXML(String path) {
        return path.endsWith(".xml") && (new File(path).getName().equals("AndroidManifest.xml") || path.contains(APKExplorer.mAppID + "/res/"));
    }

}