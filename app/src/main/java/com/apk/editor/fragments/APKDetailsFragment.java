package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.APKDetailsAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.recyclerViewItems.APKItems;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class APKDetailsFragment extends Fragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.recyclerview_layout, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.setAdapter(new APKDetailsAdapter(getData()));

        return mRootView;
    }

    private List<String> getData() {
        List<String> mData = new ArrayList<>();
        APKItems apkData = APKExplorer.getAPKData(Common.getAPKFile().getAbsolutePath(), requireActivity());
        if (apkData != null) {
            try {
                if (apkData.getVersionName() != null) {
                    mData.add(getString(R.string.version, apkData.getVersionName() + " (" + apkData.getVersionCode() + ")"));
                }
                if (apkData.getSDKVersion() != null) {
                    mData.add(getString(R.string.sdk_compile, apkData.getSDKVersion()));
                }
                if (apkData.getMinSDKVersion() != null) {
                    mData.add(getString(R.string.sdk_minimum, apkData.getMinSDKVersion()));
                }
                mData.add(getString(R.string.size, AppData.getAPKSize(Common.getAPKFile().length())));
            } catch (Exception ignored) {
            }
        }
        return mData;
    }
    
}