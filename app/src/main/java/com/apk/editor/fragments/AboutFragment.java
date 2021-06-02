package com.apk.editor.fragments;

import android.annotation.SuppressLint;
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
    private final Handler mHandler = new Handler();
    private final ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_about, container, false);

        mData.add(new RecycleViewItem(getString(R.string.app_name), getString(R.string.version, BuildConfig.VERSION_NAME), getResources().getDrawable(R.mipmap.ic_launcher), null));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(R.drawable.ic_github), "https://github.com/apk-editor/APK-Explorer-Editor"));
        mData.add(new RecycleViewItem(getString(R.string.support_group), getString(R.string.support_group_summary), getResources().getDrawable(R.drawable.ic_support), "https://t.me/apkexplorer"));
        mData.add(new RecycleViewItem(getString(R.string.report_issue), getString(R.string.report_issue_summary), getResources().getDrawable(R.drawable.ic_issue), "https://github.com/apk-editor/APK-Explorer-Editor/issues/new"));
        mData.add(new RecycleViewItem(getString(R.string.invite_friends), getString(R.string.invite_friends_Summary), getResources().getDrawable(R.drawable.ic_share), null));
        mData.add(new RecycleViewItem(getString(R.string.documentation), getString(R.string.documentation_summary), getResources().getDrawable(R.drawable.ic_book), null));
        mData.add(new RecycleViewItem(getString(R.string.credits), getString(R.string.credits_summary), getResources().getDrawable(R.drawable.ic_credits), null));
        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mData.add(new RecycleViewItem(getString(R.string.donations), getString(R.string.donations_summary), getResources().getDrawable(R.drawable.ic_donate), "https://www.paypal.me/menacherry/"));
        } else {
            mData.add(new RecycleViewItem(getString(R.string.privacy_policy), getString(R.string.privacy_policy_summary), getResources().getDrawable(R.drawable.ic_privacy), "https://github.com/apk-editor/APK-Explorer-Editor/blob/master/privacy-policy.md"));
            mData.add(new RecycleViewItem(getString(R.string.rate_us), getString(R.string.rate_us_Summary), getResources().getDrawable(R.drawable.ic_rate), "https://play.google.com/store/apps/details?id=com.apk.explorer"));
        }

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