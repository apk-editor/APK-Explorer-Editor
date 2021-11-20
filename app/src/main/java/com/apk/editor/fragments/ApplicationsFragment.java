package com.apk.editor.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
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
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private AppCompatImageButton mSortButton;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private LinearLayoutCompat mProgress;
    private MaterialTextView mAppTitle;
    private RecyclerView mRecyclerView;
    private ApplicationsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        mAppTitle = mRootView.findViewById(R.id.app_title);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        mSortButton = mRootView.findViewById(R.id.sort_button);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAppTitle.setText(getString(R.string.apps_installed));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            sUtils.saveString("appTypes", "all", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sUtils.saveString("appTypes", "system", requireActivity());
                            loadApps(requireActivity());
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sUtils.saveString("appTypes", "user", requireActivity());
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
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mAppTitle.setVisibility(View.VISIBLE);
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                mAppTitle.setVisibility(View.GONE);
                AppData.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mSortButton.setOnClickListener(v -> sortMenu(requireActivity()));

        mSearchWord.addTextChangedListener(new TextWatcher() {
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

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Common.getSearchWord() != null) {
                    mSearchWord.setText(null);
                    Common.setSearchWord(null);
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    mAppTitle.setVisibility(View.VISIBLE);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    sUtils.snackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.press_back)).show();
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = sUtils.getString("appTypes", "all", activity);
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
                mProgress.setVisibility(View.VISIBLE);
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
                mProgress.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void sortMenu(Activity activity) {
        PopupMenu popupMenu = new PopupMenu(activity, mSortButton);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by));

        sort.add(0, 1, Menu.NONE, getString(R.string.sort_by_name)).setCheckable(true)
                .setChecked(sUtils.getBoolean("sort_name", false, activity));
        sort.add(0, 2, Menu.NONE, getString(R.string.sort_by_id)).setCheckable(true)
                .setChecked(sUtils.getBoolean("sort_id", true, activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sort.add(0, 3, Menu.NONE, getString(R.string.sort_by_installed)).setCheckable(true)
                    .setChecked(sUtils.getBoolean("sort_installed", false, activity));
            sort.add(0, 4, Menu.NONE, getString(R.string.sort_by_updated)).setCheckable(true)
                    .setChecked(sUtils.getBoolean("sort_updated", false, activity));
            sort.add(0, 5, Menu.NONE, getString(R.string.sort_by_size)).setCheckable(true)
                    .setChecked(sUtils.getBoolean("sort_size", false, activity));
        }
        menu.add(Menu.NONE, 6, Menu.NONE, getString(sUtils.getBoolean("sort_size", false, activity) ?
                R.string.sort_size : R.string.sort_order)).setCheckable(true).setChecked(sUtils.getBoolean(
                        "az_order", true, activity));
        sort.setGroupCheckable(0, true, true);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (!sUtils.getBoolean("sort_name", false, activity)) {
                        sUtils.saveBoolean("sort_name", true, activity);
                        sUtils.saveBoolean("sort_id", false, activity);
                        sUtils.saveBoolean("sort_installed", false, activity);
                        sUtils.saveBoolean("sort_updated", false, activity);
                        sUtils.saveBoolean("sort_size", false, activity);
                        loadApps(activity);
                    }
                    break;
                case 2:
                    if (!sUtils.getBoolean("sort_id", true, activity)) {
                        sUtils.saveBoolean("sort_name", false, activity);
                        sUtils.saveBoolean("sort_id", true, activity);
                        sUtils.saveBoolean("sort_installed", false, activity);
                        sUtils.saveBoolean("sort_updated", false, activity);
                        sUtils.saveBoolean("sort_size", false, activity);
                        loadApps(activity);
                    }
                    break;
                case 3:
                    if (!sUtils.getBoolean("sort_installed", false, activity)) {
                        sUtils.saveBoolean("sort_name", false, activity);
                        sUtils.saveBoolean("sort_id", false, activity);
                        sUtils.saveBoolean("sort_installed", true, activity);
                        sUtils.saveBoolean("sort_updated", false, activity);
                        sUtils.saveBoolean("sort_size", false, activity);
                        loadApps(activity);
                    }
                    break;
                case 4:
                    if (!sUtils.getBoolean("sort_updated", false, activity)) {
                        sUtils.saveBoolean("sort_name", false, activity);
                        sUtils.saveBoolean("sort_id", false, activity);
                        sUtils.saveBoolean("sort_installed", false, activity);
                        sUtils.saveBoolean("sort_updated", true, activity);
                        sUtils.saveBoolean("sort_size", false, activity);
                        loadApps(activity);
                    }
                    break;
                case 5:
                    if (!sUtils.getBoolean("sort_size", false, activity)) {
                        sUtils.saveBoolean("sort_name", false, activity);
                        sUtils.saveBoolean("sort_id", false, activity);
                        sUtils.saveBoolean("sort_installed", false, activity);
                        sUtils.saveBoolean("sort_updated", false, activity);
                        sUtils.saveBoolean("sort_size", true, activity);
                        loadApps(activity);
                    }
                    break;
                case 6:
                    sUtils.saveBoolean("az_order", !sUtils.getBoolean("az_order", true, activity), activity);
                    loadApps(activity);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Common.getSearchWord() != null) {
            mSearchWord.setText(null);
            Common.setSearchWord(null);
        }
    }
    
}