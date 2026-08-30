package com.apk.editor.fragments;

import android.view.Menu;
import android.widget.PopupMenu;

import com.apk.editor.R;

import in.sunilpaulmathew.sCommon.Adapters.sPagerAdapter;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class ExportedAppsFragment extends ParentFragment {

    @Override
    public int setTabPosition() {
        return sCommonUtils.getInt("showApkType", 0, requireActivity());
    }

    @Override
    public int setTitle() {
        return R.string.apps_exported;
    }

    @Override
    public void triggerSort() {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), mMenuButton);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setIcon(R.drawable.ic_sort_az).setCheckable(true)
                .setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
        //popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                mViewModel.triggerSort();
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public sPagerAdapter setAdapter() {
        sPagerAdapter adapter = new sPagerAdapter(requireActivity());
        adapter.addFragment(APKsFragment.newInstance(0), getString(R.string.apks));
        adapter.addFragment(APKsFragment.newInstance(1), getString(R.string.bundles));
        return adapter;
    }

}