package com.apk.editor.utils;

import android.view.View;

import com.apk.editor.utils.recyclerViewItems.PackageItems;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on June 12, 2021
 */
public class Common {

    private static boolean mBuilding = false, mFinish = false, mPrivateKey = false, mRSATemplate = false;
    private static List<PackageItems> mPackageData = null;
    private static final List<String> mAPKList = new ArrayList<>(), mErrorList = new ArrayList<>();
    private static int mError = 0, mSuccess = 0;
    private static MaterialCardView mSelect;
    private static String mAppID, mFilePath = null, mFileToReplace = null, mPackageName = null,
            mPath = null, mSearchText, mSearchWord, mStatus = null;

    public static boolean isBuilding() {
        return mBuilding;
    }

    public static boolean isFinished() {
        return mFinish;
    }

    public static boolean isTextMatched(String searchText, String searchWord) {
        for (int a = 0; a < searchText.length() - searchWord.length() + 1; a++) {
            if (searchWord.equalsIgnoreCase(searchText.substring(a, a + searchWord.length()))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPrivateKey() {
        return mPrivateKey;
    }

    public static boolean hasRASATemplate() {
        return mRSATemplate;
    }

    public static int getError() {
        return mError;
    }

    public static int getSuccess() {
        return mSuccess;
    }

    public static List<PackageItems> getPackageData() {
        return mPackageData;
    }

    public static List<String> getAPKList() {
        return mAPKList;
    }

    public static List<String> getErrorList() {
        return mErrorList;
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

    public static String getStatus() {
        return mStatus;
    }

    public static void initializeView(View view, int id) {
        mSelect = view.findViewById(id);
    }

    public static void isBuilding(boolean b) {
        mBuilding = b;
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

    public static void setError(int i) {
        mError = i;
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

    public static void setPackageData(List<PackageItems> data) {
        mPackageData = data;
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

    public static void setStatus(String status) {
        mStatus = status;
    }

    public static void setSuccess(int i) {
        mSuccess = i;
    }

}