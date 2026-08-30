package com.apk.editor.fragments;

import com.apk.editor.R;
import com.apk.editor.utils.dialogs.SortOptionsDialog;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class InstalledAppsFragment extends ParentFragment {

    @Override
    public int setTabPosition() {
        return sCommonUtils.getInt("showAppType", 2, requireActivity());
    }

    @Override
    public int setTitle() {
        return R.string.apps_installed;
    }

    @Override
    public void triggerSort() {
        new SortOptionsDialog(requireActivity()) {
            @Override
            public void onItemClicked() {
                mViewModel.triggerSort();
            }
        };
    }

    @Override
    public sPagerAdapter setAdapter() {
        sPagerAdapter adapter = new sPagerAdapter(requireActivity());
        adapter.addFragment(ApplicationsFragment.newInstance(0), getString(R.string.all));
        adapter.addFragment(ApplicationsFragment.newInstance(1), getString(R.string.system));
        adapter.addFragment(ApplicationsFragment.newInstance(2), getString(R.string.user));
        return adapter;
    }

}