package com.apk.editor.utils;

import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.apk.editor.utils.recyclerViewItems.PackageItems;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on June 12, 2021
 */
public class Common {

    private static AppCompatEditText mSearchWordApks, mSearchWordApps, mSearchWordProjects;
    private static boolean mBuilding = false, mBusy = false, mCancel = false, mFinish = false,
            mPrivateKey = false, mReloading = false, mRSATemplate = false;
    private static List<PackageItems> mPackageData = null;
    private static final List<String> mAPKList = new ArrayList<>(), mErrorList = new ArrayList<>();
    private static int mError = 0, mSuccess = 0;
    private static MaterialCardView mSelect;
    private static MaterialTextView mApksTitle, mAppsTitle, mProjectsTitle;
    private static String mAppID, mFilePath = null, mFileToReplace = null, mPackageName = null,
            mPath = null, mSearchWord, mStatus = null;

    public static AppCompatEditText getAPKsSearchWord() {
        return mSearchWordApks;
    }

    public static AppCompatEditText getAppsSearchWord() {
        return mSearchWordApps;
    }

    public static AppCompatEditText getProjectsSearchWord() {
        return mSearchWordProjects;
    }

    public static boolean isBuilding() {
        return mBuilding;
    }

    public static boolean isBusy() {
        return mBusy;
    }

    public static boolean isCancelled() {
        return mCancel;
    }

    public static boolean isFinished() {
        return mFinish;
    }

    public static boolean isReloading() {
        return mReloading;
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

    public static MaterialTextView getAPKsTitle() {
        return mApksTitle;
    }

    public static MaterialTextView getAppsTitle() {
        return mAppsTitle;
    }

    public static MaterialTextView getProjectsTitle() {
        return mProjectsTitle;
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

    public static String getSearchWord() {
        return mSearchWord;
    }

    public static String getStatus() {
        return mStatus;
    }

    public static void initializeAPKsSearchWord(View view, int id) {
        mSearchWordApks = view.findViewById(id);
    }

    public static void initializeAPKsTitle(View view, int id) {
        mApksTitle = view.findViewById(id);
    }

    public static void initializeAppsSearchWord(View view, int id) {
        mSearchWordApps = view.findViewById(id);
    }

    public static void initializeAppsTitle(View view, int id) {
        mAppsTitle = view.findViewById(id);
    }

    public static void initializeProjectsSearchWord(View view, int id) {
        mSearchWordProjects = view.findViewById(id);
    }

    public static void initializeProjectsTitle(View view, int id) {
        mProjectsTitle = view.findViewById(id);
    }

    public static void initializeView(View view, int id) {
        mSelect = view.findViewById(id);
    }

    public static void isBuilding(boolean b) {
        mBuilding = b;
    }

    public static void isCancelled(boolean b) {
        mCancel = b;
    }

    public static void isReloading(boolean b) {
        mReloading = b;
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

    public static void setProgress(boolean b, View view) {
        mBusy = b;
        view.setVisibility(b ? View.VISIBLE : View.GONE);
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