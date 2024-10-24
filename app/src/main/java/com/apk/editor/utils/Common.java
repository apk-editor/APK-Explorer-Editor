package com.apk.editor.utils;

import android.view.View;

import com.apk.editor.utils.recyclerViewItems.PackageItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on June 12, 2021
 */
public class Common {

    private static boolean mBuilding = false, mBusy = false, mCancel = false, mFinish = false,
            mReloading = false;
    private static List<File> mFile = null;
    private static List<PackageItems> mPackageData = null;
    private static final List<String> mAPKList = new ArrayList<>(), mErrorList = new ArrayList<>();
    private static int mError = 0, mSuccess = 0;
    private static String mAppID, mFilePath = null, mFileToReplace = null, mPackageName = null,
            mPath = null, mStatus = null;

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

    public static int getError() {
        return mError;
    }

    public static int getSuccess() {
        return mSuccess;
    }

    public static List<File> getFiles() {
        return mFile;
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

    public static String getStatus() {
        return mStatus;
    }

    public static void addToFilesList(File file) {
        if (mFile == null) {
            mFile = new ArrayList<>();
        }
        mFile.add(file);
    }

    public static void clearFilesList() {
        mFile = null;
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

    public static void removeFromFilesList(File file) {
        if (mFile == null || mFile.isEmpty()) return;
        mFile.remove(file);
    }

    public static void setFinishStatus(boolean b) {
        mFinish = b;
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

    public static void setStatus(String status) {
        mStatus = status;
    }

    public static void setSuccess(int i) {
        mSuccess = i;
    }

}