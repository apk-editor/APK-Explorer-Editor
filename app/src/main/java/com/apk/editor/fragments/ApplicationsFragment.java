package com.apk.editor.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ApplicationsAdapter;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends Fragment {

    private AppCompatImageButton mSortButton;
    private LinearLayoutCompat mProgress;
    private RecyclerView mRecyclerView;
    private ApplicationsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        Common.initializeAppsTitle(mRootView, R.id.app_title);
        Common.initializeAppsSearchWord(mRootView, R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        mSortButton = mRootView.findViewById(R.id.sort_button);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Common.getAppsTitle().setText(getString(R.string.apps_installed));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            sCommonUtils.saveString("appTypes", "all", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sCommonUtils.saveString("appTypes", "system", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sCommonUtils.saveString("appTypes", "user", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearchButton.setOnClickListener(v -> {
            if (Common.getAppsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getAppsSearchWord().setVisibility(View.GONE);
                Common.getAppsTitle().setVisibility(View.VISIBLE);
                if (Common.getAppsSearchWord() != null) {
                    Common.getAppsSearchWord().setText(null);
                }
                AppData.toggleKeyboard(0, Common.getAppsSearchWord(), requireActivity());
            } else {
                Common.getAppsSearchWord().setVisibility(View.VISIBLE);
                Common.getAppsSearchWord().requestFocus();
                Common.getAppsTitle().setVisibility(View.GONE);
                AppData.toggleKeyboard(1, Common.getAppsSearchWord(), requireActivity());
            }
        });

        mSortButton.setOnClickListener(v -> sortMenu(requireActivity()));

        Common.getAppsSearchWord().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.setSearchWord(s.toString().toLowerCase());
                loadApps(requireActivity());
            }
        });

        loadApps(requireActivity());

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = sCommonUtils.getString("appTypes", "all", activity);
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void loadApps(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                Common.setProgress(true, mProgress);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new ApplicationsAdapter(AppData.getData(activity));
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                Common.setProgress(false, mProgress);
            }
        }.execute();
    }

    private void sortMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSortButton);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));

        sort.add(0, 1, Menu.NONE, getString(R.string.sort_by_name)).setCheckable(true)
                .setChecked(sCommonUtils.getInt("sort_apps", 1, activity) == 0);
        sort.add(0, 2, Menu.NONE, getString(R.string.sort_by_id)).setCheckable(true)
                .setChecked(sCommonUtils.getInt("sort_apps", 1, activity) == 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sort.add(0, 3, Menu.NONE, getString(R.string.sort_by_installed)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, activity) == 2);
            sort.add(0, 4, Menu.NONE, getString(R.string.sort_by_updated)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, activity) == 3);
            sort.add(0, 5, Menu.NONE, getString(R.string.sort_by_size)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, activity) == 4);
        }
        menu.add(Menu.NONE, 6, Menu.NONE, getString(sCommonUtils.getInt("sort_apps", 1, activity) == 4 ?
                R.string.sort_size : (sCommonUtils.getInt("sort_apps", 1, activity) == 2 || sCommonUtils
                        .getInt("sort_apps", 0, activity) == 3) ? R.string.sort_time : R.string.sort_order))
                .setCheckable(true).setChecked(sCommonUtils.getBoolean("az_order", true, activity));
        sort.setGroupCheckable(0, true, true);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (sCommonUtils.getInt("sort_apps", 1, activity) != 0) {
                        sCommonUtils.saveInt("sort_apps", 0, activity);
                        loadApps(activity);
                    }
                    break;
                case 2:
                    if (sCommonUtils.getInt("sort_apps", 1, activity) != 1) {
                        sCommonUtils.saveInt("sort_apps", 1, activity);
                        loadApps(activity);
                    }
                    break;
                case 3:
                    if (sCommonUtils.getInt("sort_apps", 1, activity) != 2) {
                        sCommonUtils.saveInt("sort_apps", 2, activity);
                        loadApps(activity);
                    }
                    break;
                case 4:
                    if (sCommonUtils.getInt("sort_apps", 1, activity) != 3) {
                        sCommonUtils.saveInt("sort_apps", 3, activity);
                        loadApps(activity);
                    }
                    break;
                case 5:
                    if (sCommonUtils.getInt("sort_apps", 1, activity) != 4) {
                        sCommonUtils.saveInt("sort_apps", 4, activity);
                        loadApps(activity);
                    }
                    break;
                case 6:
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, activity), activity);
                    loadApps(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }
    
}