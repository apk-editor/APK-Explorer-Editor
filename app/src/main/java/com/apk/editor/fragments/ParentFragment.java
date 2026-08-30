package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.apk.editor.R;
import com.apk.editor.viewModels.FragmentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public abstract class ParentFragment extends Fragment {

    protected FragmentViewModel mViewModel;
    protected MaterialButton mMenuButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_parent, container, false);

        MaterialButton mSearchButton = mRootView.findViewById(R.id.search_button);
        mMenuButton = mRootView.findViewById(R.id.menu_button);
        MaterialTextView mTitle = mRootView.findViewById(R.id.title);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        ViewPager2 mViewPager = mRootView.findViewById(R.id.view_pager);

        mViewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);

        mTitle.setText(setTitle());

        sPagerAdapter mAdapter = setAdapter();
        mViewPager.setAdapter(mAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mAdapter.getPageTitle(position))
        ).attach();

        mViewPager.setCurrentItem(setTabPosition(), false);

        mSearchButton.setOnClickListener(v -> mViewModel.triggerSearch(true));

        mMenuButton.setOnClickListener(v -> triggerSort());

        return mRootView;
    }

    public abstract sPagerAdapter setAdapter();

    public abstract int setTabPosition();

    public abstract int setTitle();

    public abstract void triggerSort();

    @Override
    public void onResume() {
        super.onResume();

        mViewModel.triggerSearch(false);
    }

}