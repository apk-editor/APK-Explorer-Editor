package com.apk.editor.utils;

import android.view.View;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on June 12, 2021
 */
public class Common {

    private static boolean mFinish = false, mPrivateKey = false, mRSATemplate = false;
    private static final List<String> mAPKList = new ArrayList<>();
    private static MaterialCardView mSelect;
    private static String mAppID, mFilePath = null, mFileToReplace = null, mPackageName = null, mPath = null, mSearchText, mSearchWord;

    public static boolean isFinished() {
        return mFinish;
    }

    public static boolean hasPrivateKey() {
        return mPrivateKey;
    }

    public static boolean hasRASATemplate() {
        return mRSATemplate;
    }

    public static List<String> getAPKList() {
        return mAPKList;
    }

    public static MaterialCardView getSelectCard() {
        return mSelect;
    }

    public static String getAppID() {
        return mAppID;
    }

    public static String getFilePath() {
        return mFilePath;
    }

    public static String getFileToReplace() {
        return mFileToReplace;
    }

    public static String getPackageName() {
        return mPackageName;
    }

    public static String getPath() {
        return mPath;
    }

    public static String getSearchText() {
        return mSearchText;
    }

    public static String getSearchWord() {
        return mSearchWord;
    }

    public static void initializeView(View view, int id) {
        mSelect = view.findViewById(id);
    }

    public static void setFinishStatus(boolean b) {
        mFinish = b;
    }

    public static void setPrivateKeyStatus(boolean b) {
        mPrivateKey = b;
    }

    public static void setRSATemplateStatus(boolean b) {
        mRSATemplate = b;
    }

    public static void setAppID(String appID) {
        mAppID = appID;
    }

    public static void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public static void setFileToReplace(String fileToReplace) {
        mFileToReplace = fileToReplace;
    }

    public static void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public static void setPath(String path) {
        mPath = path;
    }

    public static void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    public static void setSearchWord(String searchWord) {
        mSearchWord = searchWord;
    }

}