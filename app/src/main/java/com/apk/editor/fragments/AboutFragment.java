package com.apk.editor.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
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
import com.apk.editor.adapters.AboutAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sSerializableItems;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_about, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), sCommonUtils.getOrientation(requireActivity()) == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2));
        AboutAdapter mRecycleViewAdapter = new AboutAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AppSettings.navigateToFragment(requireActivity(), R.id.nav_apks);
            }
        });

        return mRootView;
    }

    private List<sSerializableItems> getData() {
        List<sSerializableItems> mData = new ArrayList<>();
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.mipmap.ic_launcher, requireActivity()), getString(R.string.app_name), getString(R.string.version, BuildConfig.VERSION_NAME),null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_github, requireActivity()), getString(R.string.source_code), getString(R.string.source_code_summary),"https://github.com/apk-editor/APK-Explorer-Editor"));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_support, requireActivity()), getString(R.string.support_group), getString(R.string.support_group_summary), "https://t.me/apkexplorer"));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_issue, requireActivity()), getString(R.string.report_issue), getString(R.string.report_issue_summary), "https://github.com/apk-editor/APK-Explorer-Editor/issues/new"));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_share, requireActivity()), getString(R.string.invite_friends), getString(R.string.invite_friends_Summary), null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_book, requireActivity()),getString(R.string.documentation), getString(R.string.documentation_summary), "https://apk-editor.github.io/general/"));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_translate, requireActivity()), getString(R.string.translations), getString(R.string.translations_summary),null));
        mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_credits, requireActivity()), getString(R.string.credits), getString(R.string.credits_summary), null));
        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_donate, requireActivity()), getString(R.string.donations), getString(R.string.donations_summary),"https://www.paypal.me/menacherry/"));
        } else {
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_privacy, requireActivity()), getString(R.string.privacy_policy), getString(R.string.privacy_policy_summary), "https://github.com/apk-editor/APK-Explorer-Editor/blob/master/privacy-policy.md"));
            mData.add(new sSerializableItems(sCommonUtils.getDrawable(R.drawable.ic_rate, requireActivity()),getString(R.string.rate_us), getString(R.string.rate_us_Summary), "https://play.google.com/store/apps/details?id=com.apk.explorer"));
        }
        return mData;
    }
    
}