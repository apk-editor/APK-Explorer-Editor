package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewAboutAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.RecycleViewItem;

import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AboutFragment extends Fragment {

    private boolean mExit;
    private Handler mHandler = new Handler();
    private ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_about, container, false);

        mData.add(new RecycleViewItem(getString(R.string.app_name), getString(R.string.version, BuildConfig.VERSION_NAME), getResources().getDrawable(R.mipmap.ic_launcher), null));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(R.drawable.ic_github), "https://github.com/apk-editor/APK-Explorer-Editor"));
        mData.add(new RecycleViewItem(getString(R.string.support_group), getString(R.string.support_group_summary), getResources().getDrawable(R.drawable.ic_support), "https://t.me/apkexplorer"));
        mData.add(new RecycleViewItem(getString(R.string.invite_friends), getString(R.string.invite_friends_Summary), getResources().getDrawable(R.drawable.ic_share), null));
        mData.add(new RecycleViewItem(getString(R.string.donations), getString(R.string.donations_summary), getResources().getDrawable(R.drawable.ic_donate), "https://www.paypal.me/menacherry/"));

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKEditorUtils.getOrientation(requireActivity()) == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2));
        RecycleViewAboutAdapter mRecycleViewAdapter = new RecycleViewAboutAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    APKEditorUtils.snackbar(requireActivity().findViewById(android.R.id.content), getString(R.string.press_back));
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }
    
}