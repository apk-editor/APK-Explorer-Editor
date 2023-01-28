package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.adapters.TextViewAdapter;
import com.apk.editor.utils.APKExplorer;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class ManifestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.recyclerview_layout, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        APKParser mAPKParser = new APKParser();

        if (mAPKParser.getManifest() != null) {
            try {
                mRecyclerView.setAdapter(new TextViewAdapter(APKExplorer.getTextViewData(mAPKParser.getManifest(), null, requireActivity()), null));
            } catch (Exception ignored) {
            }
        }

        return mRootView;
    }
    
}