package com.apk.editor.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ApplicationsAdapter;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.SerializableItems.PackageItems;
import com.apk.editor.utils.dialogs.ExportOptionsDialog;
import com.apk.editor.utils.dialogs.SortOptionsDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends Fragment {

    private ApplicationsAdapter mRecycleViewAdapter;
    private boolean mExit = false, mSelectAll = false;
    private final Handler mHandler = new Handler();
    private final List<String> mPackageNames = new CopyOnWriteArrayList<>();
    private ContentLoadingProgressBar mProgress;
    private MaterialButton mBatchButton;
    private RecyclerView mRecyclerView;
    private String mSearchText = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_applications, container, false);

        MaterialAutoCompleteTextView mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatchButton = mRootView.findViewById(R.id.batch_options);
        mProgress = mRootView.findViewById(R.id.progress);
        MaterialButton mSearchButton = mRootView.findViewById(R.id.search_button);
        MaterialButton mMenuButton = mRootView.findViewById(R.id.menu_button);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition())).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            sCommonUtils.saveString("appTypes", "all", requireActivity());
                            mSelectAll = false;
                            loadApps(mSearchText);
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sCommonUtils.saveString("appTypes", "system", requireActivity());
                            mSelectAll = false;
                            loadApps(mSearchText);
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sCommonUtils.saveString("appTypes", "user", requireActivity());
                            mSelectAll = false;
                            loadApps(mSearchText);
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

        mBatchButton.setOnClickListener(v -> new ExportOptionsDialog(mPackageNames, mSelectAll, requireActivity()) {
            @Override
            public void selectAllLister(boolean checked) {
                if (checked) {
                    mSelectAll = false;
                    mPackageNames.clear();
                } else {
                    mSelectAll = true;
                }
                loadApps(mSearchText);
            }
        });

        mSearchButton.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                if (mSearchText != null) {
                    mSearchText = null;
                    mSearchWord.setText(null);
                }
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                AppData.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mMenuButton.setOnClickListener(v -> new SortOptionsDialog(requireActivity()) {
            @Override
            public void onItemClicked() {
                loadApps(mSearchText);
            }
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadApps(s.toString().trim().toLowerCase());
            }
        });

        loadApps(mSearchText);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (mBatchButton.getVisibility() == View.VISIBLE) {
                    mSelectAll = false;
                    mPackageNames.clear();
                    loadApps(mSearchText);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    sCommonUtils.toast(getString(R.string.press_back), requireActivity()).show();
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition() {
        String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void loadApps(String searchWord) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                List<PackageItems> mData = AppData.getData(searchWord, requireActivity());
                mRecycleViewAdapter = new ApplicationsAdapter(mData, mPackageNames, mBatchButton, searchWord, requireActivity());
                if (mSelectAll) {
                    mPackageNames.clear();
                    for (PackageItems items : mData) {
                        mPackageNames.add(items.getPackageName());
                    }
                }
            }

            @Override
            public void onPostExecute() {
                mSearchText = searchWord;
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
            }
        }.execute();
    }
    
}